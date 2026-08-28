package com.budgettracker.app.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.data.db.Cycle
import com.budgettracker.app.data.db.SubscriptionEntity
import com.budgettracker.app.notifications.ReminderScheduler
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.convertMinor
import com.budgettracker.app.util.parseAmountMinor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class SubscriptionWithAccount(
    val sub: SubscriptionEntity,
    val account: AccountEntity?,
)

data class SubsUiState(
    val loading: Boolean = true,
    val baseCurrency: String = "USD",
    val subs: List<SubscriptionWithAccount> = emptyList(),
    val monthlyTotalBaseMinor: Long = 0,
    val activeCount: Int = 0,
    val accounts: List<AccountEntity> = emptyList(),
    val expenseCategories: List<CategoryEntity> = emptyList(),
    val dueDaysOfMonth: Set<Int> = emptySet(),
)

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val reminderScheduler: ReminderScheduler,
    prefsRepository: PrefsRepository,
) : ViewModel() {

    val uiState: StateFlow<SubsUiState> = combine(
        repo.subscriptions,
        repo.accounts,
        repo.categories,
        prefsRepository.prefs,
    ) { subs, accounts, categories, prefs ->
        val base = Currencies.byCode(prefs.baseCurrency)
        val accountById = accounts.associateBy { it.id }
        val withAccounts = subs.map { SubscriptionWithAccount(it, accountById[it.accountId]) }
        val monthlyTotal = withAccounts
            .filter { it.sub.isActive }
            .sumOf { pair ->
                convertMinor(
                    FinanceRepository.monthlyCostMinor(pair.sub),
                    pair.account?.currency ?: base,
                    base,
                )
            }
        SubsUiState(
            loading = false,
            baseCurrency = prefs.baseCurrency,
            subs = withAccounts,
            monthlyTotalBaseMinor = monthlyTotal,
            activeCount = subs.count { it.isActive },
            accounts = accounts.filter { !it.isArchived },
            expenseCategories = categories.filter { it.type == com.budgettracker.app.data.db.CategoryType.EXPENSE },
            dueDaysOfMonth = withAccounts
                .filter { it.sub.isActive }
                .map { Fmt.toLocalDate(it.sub.nextDue) }
                .filter { YearMonth.from(it) == YearMonth.now() }
                .map { it.dayOfMonth }
                .toSet(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubsUiState())

    fun save(
        id: Long,
        name: String,
        emoji: String,
        amountText: String,
        accountId: Long,
        categoryId: Long?,
        cycle: Cycle,
        nextDue: Long,
        note: String,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            val sub = uiState.value.subs.firstOrNull { it.sub.id == id }?.sub
            val account = repo.accountById(accountId) ?: return@launch
            val amount = parseAmountMinor(amountText, account.currency) ?: return@launch
            repo.saveSubscription(
                SubscriptionEntity(
                    id = id,
                    name = name.trim(),
                    emoji = emoji,
                    amountMinor = amount,
                    accountId = accountId,
                    categoryId = categoryId ?: sub?.categoryId,
                    cycle = cycle,
                    nextDue = nextDue,
                    isActive = sub?.isActive ?: true,
                    note = note.trim(),
                ),
            )
            onSaved()
        }
    }

    fun markPaid(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repo.markSubscriptionPaid(subscription)
            reminderScheduler.checkNow()
        }
    }

    fun toggleActive(subscription: SubscriptionEntity) {
        viewModelScope.launch { repo.saveSubscription(subscription.copy(isActive = !subscription.isActive)) }
    }

    fun delete(subscription: SubscriptionEntity) {
        viewModelScope.launch { repo.deleteSubscription(subscription) }
    }
}
