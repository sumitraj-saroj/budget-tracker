package com.budgettracker.app.ui.budgets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.db.BudgetEntity
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.domain.BudgetProgress
import com.budgettracker.app.domain.Insights
import com.budgettracker.app.util.BudgetPeriodType
import com.budgettracker.app.util.Periods
import com.budgettracker.app.util.parseAmountMinor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetsUiState(
    val loading: Boolean = true,
    val baseCurrency: String = "USD",
    val budgets: List<BudgetProgress> = emptyList(),
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    prefsRepository: PrefsRepository,
) : ViewModel() {

    val uiState: StateFlow<BudgetsUiState> = combine(
        repo.budgets,
        repo.transactionsDetailed(),
        prefsRepository.prefs,
    ) { budgets, txs, prefs ->
        BudgetsUiState(
            loading = false,
            baseCurrency = prefs.baseCurrency,
            budgets = budgets.map { Insights.budgetProgress(it, txs, prefs.baseCurrency) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsUiState())

    fun delete(budget: BudgetEntity) {
        viewModelScope.launch { repo.deleteBudget(budget) }
    }
}

data class BudgetEditState(
    val isNew: Boolean = true,
    val loaded: Boolean = false,
    val name: String = "",
    val amountText: String = "",
    val periodType: BudgetPeriodType = BudgetPeriodType.MONTHLY,
    val customStart: Long = Periods.startOfDay(System.currentTimeMillis()),
    val customLengthText: String = "14",
    val selectedCategoryIds: Set<Long> = emptySet(),
    val categories: List<CategoryEntity> = emptyList(),
    val baseCurrency: String = "USD",
    val amountError: Boolean = false,
)

@HiltViewModel
class BudgetEditViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val prefsRepository: PrefsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val budgetId: Long = savedStateHandle.get<String>("budgetId")?.toLongOrNull() ?: -1L

    private val _state = MutableStateFlow(BudgetEditState())
    val state: StateFlow<BudgetEditState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.categories.collect { categories ->
                _state.update { it.copy(categories = categories.filter { c -> c.type == com.budgettracker.app.data.db.CategoryType.EXPENSE }) }
            }
        }
        viewModelScope.launch {
            val prefs = prefsRepository.prefs.first()
            _state.update { it.copy(baseCurrency = prefs.baseCurrency) }
            if (budgetId > 0) {
                val budget = repoBudgetById(budgetId)
                if (budget != null) {
                    val digits = com.budgettracker.app.util.Currencies.byCode(prefs.baseCurrency).minorDigits
                    _state.update {
                        it.copy(
                            isNew = false,
                            loaded = true,
                            name = budget.name,
                            amountText = java.math.BigDecimal(budget.amountMinor).movePointLeft(digits).stripTrailingZeros().toPlainString(),
                            periodType = budget.periodType,
                            customStart = budget.customStart ?: it.customStart,
                            customLengthText = (budget.customLengthDays ?: 14).toString(),
                            selectedCategoryIds = budget.categoryIds.toSet(),
                        )
                    }
                } else {
                    _state.update { it.copy(loaded = true) }
                }
            } else {
                _state.update { it.copy(loaded = true) }
            }
        }
        viewModelScope.launch {
            prefsRepository.prefs.collect { prefs ->
                _state.update { it.copy(baseCurrency = prefs.baseCurrency) }
            }
        }
    }

    private suspend fun repoBudgetById(id: Long): BudgetEntity? = repo.budgetById(id)

    fun setName(name: String) {
        _state.update { it.copy(name = name.take(40)) }
    }

    fun setAmount(text: String) {
        _state.update { it.copy(amountText = text.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(12), amountError = false) }
    }

    fun setPeriod(type: BudgetPeriodType) {
        _state.update { it.copy(periodType = type) }
    }

    fun setCustomStart(millis: Long) {
        _state.update { it.copy(customStart = Periods.startOfDay(millis)) }
    }

    fun setCustomLength(text: String) {
        _state.update { it.copy(customLengthText = text.filter { it.isDigit() }.take(4)) }
    }

    fun toggleCategory(id: Long) {
        _state.update { it.copy(selectedCategoryIds = if (id in it.selectedCategoryIds) it.selectedCategoryIds - id else it.selectedCategoryIds + id) }
    }

    fun clearCategories() {
        _state.update { it.copy(selectedCategoryIds = emptySet()) }
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val currency = com.budgettracker.app.util.Currencies.byCode(s.baseCurrency)
        val amount = parseAmountMinor(s.amountText.ifBlank { "0" }, currency)
        if (amount == null || amount <= 0 || s.name.isBlank()) {
            _state.update { it.copy(amountError = amount == null || amount <= 0) }
            return
        }
        val length = s.customLengthText.toIntOrNull()?.coerceIn(1, 3650) ?: 14
        viewModelScope.launch {
            repo.saveBudget(
                BudgetEntity(
                    id = if (budgetId > 0) budgetId else 0,
                    name = s.name.trim(),
                    amountMinor = amount,
                    periodType = s.periodType,
                    customStart = if (s.periodType == BudgetPeriodType.CUSTOM) s.customStart else null,
                    customLengthDays = if (s.periodType == BudgetPeriodType.CUSTOM) length else null,
                    categoryIds = s.selectedCategoryIds.toList(),
                ),
            )
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        if (budgetId <= 0) {
            onDone()
            return
        }
        viewModelScope.launch {
            repoBudgetById(budgetId)?.let { repo.deleteBudget(it) }
            onDone()
        }
    }
}
