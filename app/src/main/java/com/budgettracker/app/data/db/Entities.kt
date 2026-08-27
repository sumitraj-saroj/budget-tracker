package com.budgettracker.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.budgettracker.app.util.BudgetPeriodType
import kotlinx.serialization.Serializable

enum class TxType { EXPENSE, INCOME, TRANSFER }
enum class CategoryType { EXPENSE, INCOME }
enum class AccountType { CASH, BANK, CARD, SAVINGS, OTHER }
enum class DebtDirection { LENT, BORROWED }
enum class Cycle { WEEKLY, MONTHLY, QUARTERLY, YEARLY }

@Serializable
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "💳",
    val colorArgb: Long = 0xFF64748B,
    val type: AccountType = AccountType.BANK,
    val currencyCode: String = "USD",
    val startingBalanceMinor: Long = 0,
    val isArchived: Boolean = false,
    val includeInTotals: Boolean = true,
    val sortOrder: Int = 0,
) {
    val currency get() = com.budgettracker.app.util.Currencies.byCode(currencyCode)
}

@Serializable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "🏷️",
    val colorArgb: Long = 0xFF64748B,
    val type: CategoryType,
    val isCustom: Boolean = false,
)

@Serializable
@Entity(tableName = "transactions")
data class TxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,
    val type: TxType,
    val accountId: Long,
    val toAccountId: Long? = null,
    val toAmountMinor: Long? = null,
    val categoryId: Long? = null,
    val date: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Serializable
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amountMinor: Long,
    val periodType: BudgetPeriodType = BudgetPeriodType.MONTHLY,
    val customStart: Long? = null,
    val customLengthDays: Int? = null,
    val categoryIds: List<Long> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "🎯",
    val colorArgb: Long = 0xFF10B981,
    val targetMinor: Long,
    val savedMinor: Long = 0,
    val targetDate: Long? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val direction: DebtDirection,
    val personName: String,
    val amountMinor: Long,
    val note: String = "",
    val dueDate: Long? = null,
    val isSettled: Boolean = false,
    val settledAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Serializable
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "📺",
    val amountMinor: Long,
    val accountId: Long,
    val categoryId: Long? = null,
    val cycle: Cycle = Cycle.MONTHLY,
    val nextDue: Long,
    val isActive: Boolean = true,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
