package com.budgettracker.app.ui.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.data.db.DebtDirection
import com.budgettracker.app.data.db.DebtEntity
import com.budgettracker.app.util.parseAmountMinor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebtsUiState(
    val loading: Boolean = true,
    val baseCurrency: String = "USD",
    val lent: List<DebtEntity> = emptyList(),
    val borrowed: List<DebtEntity> = emptyList(),
    val lentOutstandingMinor: Long = 0,
    val borrowedOutstandingMinor: Long = 0,
    val accounts: List<AccountEntity> = emptyList(),
    val incomeCategories: List<CategoryEntity> = emptyList(),
    val expenseCategories: List<CategoryEntity> = emptyList(),
)

@HiltViewModel
class DebtsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    prefsRepository: PrefsRepository,
) : ViewModel() {

    val uiState: StateFlow<DebtsUiState> = combine(
        repo.debts,
        repo.accounts,
        repo.categories,
        prefsRepository.prefs,
    ) { debts, accounts, categories, prefs ->
        DebtsUiState(
            loading = false,
            baseCurrency = prefs.baseCurrency,
            lent = debts.filter { it.direction == DebtDirection.LENT },
            borrowed = debts.filter { it.direction == DebtDirection.BORROWED },
            lentOutstandingMinor = debts.filter { it.direction == DebtDirection.LENT && !it.isSettled }.sumOf { it.amountMinor },
            borrowedOutstandingMinor = debts.filter { it.direction == DebtDirection.BORROWED && !it.isSettled }.sumOf { it.amountMinor },
            accounts = accounts.filter { !it.isArchived },
            incomeCategories = categories.filter { it.type == com.budgettracker.app.data.db.CategoryType.INCOME },
            expenseCategories = categories.filter { it.type == com.budgettracker.app.data.db.CategoryType.EXPENSE },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebtsUiState())

    fun saveDebt(
        id: Long,
        direction: DebtDirection,
        personName: String,
        amountText: String,
        note: String,
        dueDate: Long?,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            val currency = com.budgettracker.app.util.Currencies.byCode(uiState.value.baseCurrency)
            val amount = parseAmountMinor(amountText, currency) ?: return@launch
            val existing = if (id > 0) repo.debtById(id) else null
            repo.saveDebt(
                DebtEntity(
                    id = id,
                    direction = direction,
                    personName = personName.trim(),
                    amountMinor = amount,
                    note = note.trim(),
                    dueDate = dueDate,
                    isSettled = existing?.isSettled ?: false,
                    settledAt = existing?.settledAt,
                ),
            )
            onSaved()
        }
    }

    fun settle(debt: DebtEntity, recordTransaction: Boolean, accountId: Long?, categoryId: Long?) {
        viewModelScope.launch {
            repo.settleDebt(debt, recordTransaction, accountId, categoryId)
        }
    }

    fun reopen(debt: DebtEntity) {
        viewModelScope.launch {
            repo.saveDebt(debt.copy(isSettled = false, settledAt = null))
        }
    }

    fun delete(debt: DebtEntity) {
        viewModelScope.launch { repo.deleteDebt(debt) }
    }
}
