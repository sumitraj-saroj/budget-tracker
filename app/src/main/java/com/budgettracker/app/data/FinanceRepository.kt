package com.budgettracker.app.data

import com.budgettracker.app.data.db.AccountDao
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.AccountType
import com.budgettracker.app.data.db.BudgetDao
import com.budgettracker.app.data.db.BudgetEntity
import com.budgettracker.app.data.db.CategoryDao
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.data.db.CategoryType
import com.budgettracker.app.data.db.Cycle
import com.budgettracker.app.data.db.DebtDao
import com.budgettracker.app.data.db.DebtDirection
import com.budgettracker.app.data.db.DebtEntity
import com.budgettracker.app.data.db.GoalDao
import com.budgettracker.app.data.db.GoalEntity
import com.budgettracker.app.data.db.SubscriptionDao
import com.budgettracker.app.data.db.SubscriptionEntity
import com.budgettracker.app.data.db.TxDao
import com.budgettracker.app.data.db.TxEntity
import com.budgettracker.app.data.db.TxType
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.Periods
import com.budgettracker.app.util.convertMinor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class TxDetailed(
    val tx: TxEntity,
    val category: CategoryEntity?,
    val account: AccountEntity?,
    val toAccount: AccountEntity?,
)

data class AccountWithBalance(
    val account: AccountEntity,
    val balanceMinor: Long,
)

private val DefaultExpenseCategories = listOf(
    "Food & Dining" to "🍔", "Groceries" to "🛒", "Transport" to "🚌",
    "Housing" to "🏠", "Utilities" to "💡", "Shopping" to "🛍️",
    "Entertainment" to "🎬", "Health" to "💊", "Education" to "📚",
    "Travel" to "✈️", "Subscriptions" to "📺", "Personal Care" to "🧴",
    "Gifts & Donations" to "🎁", "Fees & Charges" to "🏦", "Pets" to "🐾",
    "Other" to "❓",
)

private val DefaultIncomeCategories = listOf(
    "Salary" to "💼", "Freelance" to "💻", "Business" to "🏪",
    "Investments" to "📈", "Refunds" to "↩️", "Other Income" to "➕",
)

private val CategoryPalette = listOf(
    0xFFF97316, 0xFF22C55E, 0xFF3B82F6, 0xFF8B5CF6, 0xFFEAB308,
    0xFFEC4899, 0xFF06B6D4, 0xFFEF4444, 0xFF6366F1, 0xFF14B8A6,
    0xFFA855F7, 0xFF10B981, 0xFFF43F5E, 0xFF64748B, 0xFFB45309, 0xFF9CA3AF,
)

@Singleton
class FinanceRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val txDao: TxDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val debtDao: DebtDao,
    private val subscriptionDao: SubscriptionDao,
    private val prefsRepository: PrefsRepository,
) {
    val accounts: Flow<List<AccountEntity>> = accountDao.observeAll()
    val categories: Flow<List<CategoryEntity>> = categoryDao.observeAll()
    val transactions: Flow<List<TxEntity>> = txDao.observeAll()
    val budgets: Flow<List<BudgetEntity>> = budgetDao.observeAll()
    val goals: Flow<List<GoalEntity>> = goalDao.observeAll()
    val debts: Flow<List<DebtEntity>> = debtDao.observeAll()
    val subscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.observeAll()

    val baseCurrency: Flow<String> = prefsRepository.prefs.map { it.baseCurrency }

    fun transactionsDetailed(): Flow<List<TxDetailed>> =
        combine(transactions, accounts, categories) { txs, accounts, categories ->
            val accountById = accounts.associateBy { it.id }
            val categoryById = categories.associateBy { it.id }
            txs.map { tx ->
                TxDetailed(
                    tx = tx,
                    category = tx.categoryId?.let { categoryById[it] },
                    account = accountById[tx.accountId],
                    toAccount = tx.toAccountId?.let { accountById[it] },
                )
            }
        }

    fun accountBalances(): Flow<List<AccountWithBalance>> =
        combine(accounts, transactions) { accounts, txs ->
            val outgoing = txs.groupBy { it.accountId }
            val incoming = txs.filter { it.toAccountId != null }.groupBy { it.toAccountId!! }
            accounts.map { account ->
                var balance = account.startingBalanceMinor
                outgoing[account.id]?.forEach { tx ->
                    when (tx.type) {
                        TxType.EXPENSE -> balance -= tx.amountMinor
                        TxType.INCOME -> balance += tx.amountMinor
                        TxType.TRANSFER -> balance -= tx.amountMinor
                    }
                }
                incoming[account.id]?.forEach { tx ->
                    balance += tx.toAmountMinor ?: tx.amountMinor
                }
                AccountWithBalance(account, balance)
            }
        }

    /** Total balance across all non-archived accounts, converted to the base currency. */
    fun totalBalanceBase(): Flow<Long> =
        combine(accountBalances(), baseCurrency) { balances, base ->
            balances.filter { !it.account.isArchived && it.account.includeInTotals }
                .sumOf { convertMinor(it.balanceMinor, it.account.currency, com.budgettracker.app.util.Currencies.byCode(base)) }
        }

    /** Seeds default categories and a starter account on first launch. */
    suspend fun ensureDefaultData(baseCurrency: String) {
        if (categoryDao.count() == 0) {
            val expense = DefaultExpenseCategories.mapIndexed { i, (name, emoji) ->
                CategoryEntity(name = name, emoji = emoji, colorArgb = CategoryPalette[i % CategoryPalette.size], type = CategoryType.EXPENSE)
            }
            val income = DefaultIncomeCategories.mapIndexed { i, (name, emoji) ->
                CategoryEntity(name = name, emoji = emoji, colorArgb = CategoryPalette[(i + 6) % CategoryPalette.size], type = CategoryType.INCOME)
            }
            categoryDao.insertAll(expense + income)
        }
        if (accountDao.count() == 0) {
            accountDao.insert(
                AccountEntity(
                    name = "Cash",
                    emoji = "💵",
                    colorArgb = 0xFF22C55E,
                    type = AccountType.CASH,
                    currencyCode = baseCurrency,
                ),
            )
        }
    }

    // ---------- Transactions ----------

    suspend fun txById(id: Long): TxEntity? = txDao.byId(id)

    suspend fun saveTx(tx: TxEntity) {
        if (tx.id == 0L) txDao.insert(tx) else txDao.update(tx.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTx(tx: TxEntity) = txDao.delete(tx)

    // ---------- Categories ----------

    suspend fun saveCategory(category: CategoryEntity): Long =
        if (category.id == 0L) categoryDao.insert(category) else {
            categoryDao.update(category); category.id
        }

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun categoryById(id: Long): CategoryEntity? = categoryDao.byId(id)

    // ---------- Accounts ----------

    suspend fun saveAccount(account: AccountEntity): Long =
        if (account.id == 0L) accountDao.insert(account) else {
            accountDao.update(account); account.id
        }

    /** Returns false when the account still has transactions (caller should suggest archiving). */
    suspend fun deleteAccount(account: AccountEntity): Boolean {
        if (txDao.countForAccount(account.id) > 0) return false
        accountDao.delete(account)
        return true
    }

    suspend fun accountById(id: Long): AccountEntity? = accountDao.byId(id)

    // ---------- Budgets ----------

    suspend fun budgetById(id: Long): BudgetEntity? = budgetDao.byId(id)

    suspend fun saveBudget(budget: BudgetEntity) {
        if (budget.id == 0L) budgetDao.insert(budget) else budgetDao.update(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.delete(budget)

    // ---------- Goals ----------

    suspend fun goalById(id: Long): GoalEntity? = goalDao.byId(id)

    suspend fun saveGoal(goal: GoalEntity) {
        if (goal.id == 0L) goalDao.insert(goal) else goalDao.update(goal)
    }

    suspend fun deleteGoal(goal: GoalEntity) = goalDao.delete(goal)

    suspend fun contributeToGoal(goal: GoalEntity, amountMinor: Long) {
        goalDao.update(goal.copy(savedMinor = (goal.savedMinor + amountMinor).coerceAtLeast(0)))
    }

    // ---------- Debts ----------

    suspend fun debtById(id: Long): DebtEntity? = debtDao.byId(id)

    suspend fun saveDebt(debt: DebtEntity) {
        if (debt.id == 0L) debtDao.insert(debt) else debtDao.update(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) = debtDao.delete(debt)

    suspend fun settleDebt(debt: DebtEntity, recordTransaction: Boolean, accountId: Long?, categoryId: Long?) {
        debtDao.update(debt.copy(isSettled = true, settledAt = System.currentTimeMillis()))
        if (!recordTransaction || accountId == null) return
        val account = accountDao.byId(accountId) ?: return
        val (type, note) = when (debt.direction) {
            DebtDirection.LENT -> TxType.INCOME to "Debt repaid by ${debt.personName}"
            DebtDirection.BORROWED -> TxType.EXPENSE to "Repaid debt to ${debt.personName}"
        }
        txDao.insert(
            TxEntity(
                amountMinor = debt.amountMinor,
                type = type,
                accountId = account.id,
                categoryId = categoryId,
                date = System.currentTimeMillis(),
                note = note,
            ),
        )
    }

    // ---------- Subscriptions ----------

    suspend fun saveSubscription(sub: SubscriptionEntity) {
        if (sub.id == 0L) subscriptionDao.insert(sub) else subscriptionDao.update(sub)
    }

    suspend fun deleteSubscription(sub: SubscriptionEntity) = subscriptionDao.delete(sub)

    /** Records an expense for this billing cycle and advances the next due date. */
    suspend fun markSubscriptionPaid(sub: SubscriptionEntity) {
        txDao.insert(
            TxEntity(
                amountMinor = sub.amountMinor,
                type = TxType.EXPENSE,
                accountId = sub.accountId,
                categoryId = sub.categoryId,
                date = Periods.startOfDay(System.currentTimeMillis()),
                note = "${sub.name} subscription",
            ),
        )
        subscriptionDao.update(sub.copy(nextDue = advanceNextDue(sub.nextDue, sub.cycle)))
    }

    fun advanceNextDue(currentDue: Long, cycle: Cycle, now: Long = System.currentTimeMillis()): Long {
        var date = Fmt.toLocalDate(currentDue)
        val today = Fmt.toLocalDate(now)
        do {
            date = when (cycle) {
                Cycle.WEEKLY -> date.plusWeeks(1)
                Cycle.MONTHLY -> date.plusMonths(1)
                Cycle.QUARTERLY -> date.plusMonths(3)
                Cycle.YEARLY -> date.plusYears(1)
            }
        } while (!date.isAfter(today))
        return Fmt.fromLocalDate(date)
    }

    companion object {
        fun monthlyCostMinor(sub: SubscriptionEntity): Long = when (sub.cycle) {
            Cycle.WEEKLY -> sub.amountMinor * 52 / 12
            Cycle.MONTHLY -> sub.amountMinor
            Cycle.QUARTERLY -> sub.amountMinor / 3
            Cycle.YEARLY -> sub.amountMinor / 12
        }
    }
}
