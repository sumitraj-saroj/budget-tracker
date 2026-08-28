package com.budgettracker.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.AccountWithBalance
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.TxDetailed
import com.budgettracker.app.data.UserPrefs
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.BudgetEntity
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.data.db.GoalEntity
import com.budgettracker.app.data.db.SubscriptionEntity
import com.budgettracker.app.domain.BudgetProgress
import com.budgettracker.app.domain.Insights
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.Periods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class UpcomingPayment(
    val sub: SubscriptionEntity,
    val account: AccountEntity?,
    val daysUntil: Long,
)

data class HomeUiState(
    val loading: Boolean = true,
    val userName: String = "",
    val baseCurrency: String = "USD",
    val totalBalanceMinor: Long = 0,
    val accountCount: Int = 0,
    val monthIncomeMinor: Long = 0,
    val monthExpenseMinor: Long = 0,
    val budgets: List<BudgetProgress> = emptyList(),
    val upcoming: List<UpcomingPayment> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val recent: List<TxDetailed> = emptyList(),
    val lastBackupAt: Long? = null,
    val txCount: Int = 0,
    val topCategories: List<CategoryEntity> = emptyList(),
    val dailyAllowanceMinor: Long? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val prefsRepository: PrefsRepository,
) : ViewModel() {

    private data class CoreData(
        val txs: List<TxDetailed>,
        val balances: List<AccountWithBalance>,
        val budgets: List<BudgetEntity>,
        val subs: List<SubscriptionEntity>,
        val goals: List<GoalEntity>,
    )

    val uiState: StateFlow<HomeUiState> =
        combine(
            repo.transactionsDetailed(),
            repo.accountBalances(),
            repo.budgets,
            repo.subscriptions,
            repo.goals,
        ) { txs, balances, budgets, subs, goals ->
            CoreData(txs, balances, budgets, subs, goals)
        }
            .combine(prefsRepository.prefs) { core, prefs -> buildState(core, prefs) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun buildState(core: CoreData, prefs: UserPrefs): HomeUiState {
        val month = Periods.currentMonth()
        val today = Fmt.toLocalDate(System.currentTimeMillis())
        val accountById = core.balances.associate { it.account.id to it.account }
        val budgetProgress = core.budgets.map { Insights.budgetProgress(it, core.txs, prefs.baseCurrency) }

        // Most-used expense categories for quick-add tiles.
        val topCategories = core.txs.asSequence()
            .filter { it.tx.type == com.budgettracker.app.data.db.TxType.EXPENSE && it.category != null }
            .groupingBy { it.category!!.id }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .mapNotNull { entry -> core.txs.firstOrNull { it.tx.categoryId == entry.key }?.category }

        // Safe-to-spend per day: leftover budget across all budgets / days left.
        val dailyAllowance = Insights.safeToSpendPerDay(budgetProgress)

        return HomeUiState(
            loading = false,
            userName = prefs.displayName,
            baseCurrency = prefs.baseCurrency,
            totalBalanceMinor = Insights.totalBalanceBase(core.balances, prefs.baseCurrency),
            accountCount = core.balances.count { !it.account.isArchived },
            monthIncomeMinor = Insights.sumBetween(core.txs, com.budgettracker.app.data.db.TxType.INCOME, month, prefs.baseCurrency),
            monthExpenseMinor = Insights.sumBetween(core.txs, com.budgettracker.app.data.db.TxType.EXPENSE, month, prefs.baseCurrency),
            budgets = budgetProgress,
            upcoming = core.subs
                .filter { it.isActive }
                .map { sub ->
                    UpcomingPayment(
                        sub = sub,
                        account = accountById[sub.accountId],
                        daysUntil = ChronoUnit.DAYS.between(today, Fmt.toLocalDate(sub.nextDue)),
                    )
                }
                .filter { it.daysUntil <= 30 }
                .sortedBy { it.sub.nextDue },
            goals = core.goals.sortedWith(compareBy({ it.savedMinor >= it.targetMinor }, { it.id })),
            recent = core.txs.take(8),
            lastBackupAt = prefs.lastBackupAt,
            txCount = core.txs.size,
            topCategories = topCategories,
            dailyAllowanceMinor = dailyAllowance,
        )
    }

    fun markSubscriptionPaid(sub: SubscriptionEntity) {
        viewModelScope.launch { repo.markSubscriptionPaid(sub) }
    }
}
