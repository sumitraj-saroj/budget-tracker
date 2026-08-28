package com.budgettracker.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.TxDetailed
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.data.db.TxEntity
import com.budgettracker.app.data.db.TxType
import com.budgettracker.app.domain.Insights
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TxSort { NEWEST, OLDEST, LARGEST, SMALLEST }

data class TxFilter(
    val query: String = "",
    val type: TxType? = null,
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
) {
    val isActive: Boolean
        get() = query.isNotBlank() || type != null || accountId != null ||
            categoryId != null || startDate != null || endDate != null
}

data class DayGroup(
    val label: String,
    val dateMillis: Long,
    val incomeBase: Long,
    val expenseBase: Long,
    val items: List<TxDetailed>,
)

data class TxnsUiState(
    val loading: Boolean = true,
    val baseCurrency: String = "USD",
    val groups: List<DayGroup> = emptyList(),
    val filter: TxFilter = TxFilter(),
    val sort: TxSort = TxSort.NEWEST,
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val totalCount: Int = 0,
    val filteredCount: Int = 0,
    val filteredExpenseBase: Long = 0,
    val filteredIncomeBase: Long = 0,
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    prefsRepository: PrefsRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(TxFilter())
    private val sort = MutableStateFlow(TxSort.NEWEST)
    private var lastDeleted: TxEntity? = null

    private data class RawData(
        val txs: List<TxDetailed>,
        val accounts: List<AccountEntity>,
        val categories: List<CategoryEntity>,
        val baseCurrency: String,
    )

    private val rawData = combine(
        repo.transactionsDetailed(),
        repo.accounts,
        repo.categories,
        prefsRepository.prefs.map { it.baseCurrency },
    ) { txs, accounts, categories, base ->
        RawData(txs, accounts, categories, base)
    }

    val uiState: StateFlow<TxnsUiState> = combine(
        rawData,
        filter,
        sort,
    ) { raw, filter, sort ->
        buildState(raw, filter, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TxnsUiState())

    private fun buildState(raw: RawData, filter: TxFilter, sort: TxSort): TxnsUiState {
        val accountById = raw.accounts.associateBy { it.id }
        val categoryById = raw.categories.associateBy { it.id }

        var filtered = raw.txs.asSequence().filter { detailed ->
            val tx = detailed.tx
            val typeOk = filter.type == null || tx.type == filter.type
            val accountOk = filter.accountId == null || tx.accountId == filter.accountId || tx.toAccountId == filter.accountId
            val categoryOk = filter.categoryId == null || tx.categoryId == filter.categoryId
            val dateOk = (filter.startDate == null || tx.date >= filter.startDate) &&
                (filter.endDate == null || tx.date < filter.endDate)
            val queryOk = filter.query.isBlank() ||
                tx.note.contains(filter.query, ignoreCase = true) ||
                detailed.category?.name?.contains(filter.query, ignoreCase = true) == true ||
                detailed.account?.name?.contains(filter.query, ignoreCase = true) == true ||
                detailed.toAccount?.name?.contains(filter.query, ignoreCase = true) == true
            typeOk && accountOk && categoryOk && dateOk && queryOk
        }

        filtered = when (sort) {
            TxSort.NEWEST -> filtered.sortedByDescending { it.tx.date }
            TxSort.OLDEST -> filtered.sortedBy { it.tx.date }
            TxSort.LARGEST -> filtered.sortedByDescending { it.tx.amountMinor }
            TxSort.SMALLEST -> filtered.sortedBy { it.tx.amountMinor }
        }

        val list = filtered.toList()
        val groups = list
            .groupBy { Fmt.toLocalDate(it.tx.date) }
            .toSortedMap(compareByDescending { it })
            .map { (date, items) ->
                DayGroup(
                    label = Fmt.relativeDay(Fmt.fromLocalDate(date)),
                    dateMillis = Fmt.fromLocalDate(date),
                    incomeBase = items.filter { it.tx.type == TxType.INCOME }.sumOf { Insights.txAmountInBase(it, raw.baseCurrency) },
                    expenseBase = items.filter { it.tx.type == TxType.EXPENSE }.sumOf { Insights.txAmountInBase(it, raw.baseCurrency) },
                    items = items,
                )
            }

        return TxnsUiState(
            loading = false,
            baseCurrency = raw.baseCurrency,
            groups = groups,
            filter = filter,
            sort = sort,
            accounts = raw.accounts,
            categories = raw.categories,
            totalCount = raw.txs.size,
            filteredCount = list.size,
            filteredExpenseBase = list.filter { it.tx.type == TxType.EXPENSE }.sumOf { Insights.txAmountInBase(it, raw.baseCurrency) },
            filteredIncomeBase = list.filter { it.tx.type == TxType.INCOME }.sumOf { Insights.txAmountInBase(it, raw.baseCurrency) },
        )
    }

    fun setQuery(query: String) {
        filter.value = filter.value.copy(query = query)
    }

    fun setType(type: TxType?) {
        filter.value = filter.value.copy(type = type)
    }

    fun setAccount(accountId: Long?) {
        filter.value = filter.value.copy(accountId = accountId)
    }

    fun setCategory(categoryId: Long?) {
        filter.value = filter.value.copy(categoryId = categoryId)
    }

    fun setDateRange(start: Long?, end: Long?) {
        filter.value = filter.value.copy(startDate = start, endDate = end)
    }

    fun setSort(newSort: TxSort) {
        sort.value = newSort
    }

    fun clearFilters() {
        filter.value = TxFilter(query = filter.value.query)
    }

    fun deleteTx(tx: TxEntity, onDeleted: () -> Unit) {
        viewModelScope.launch {
            lastDeleted = tx
            repo.deleteTx(tx)
            onDeleted()
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            // Re-insert with a NEW id so the list item gets a fresh swipe state
            // (re-using the old id would restore the row partially dismissed).
            lastDeleted?.let { repo.saveTx(it.copy(id = 0, createdAt = System.currentTimeMillis())) }
            lastDeleted = null
        }
    }

    fun duplicate(tx: TxEntity) {
        viewModelScope.launch {
            repo.saveTx(tx.copy(id = 0, createdAt = System.currentTimeMillis()))
        }
    }
}
