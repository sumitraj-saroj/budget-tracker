package com.budgettracker.app.domain

import androidx.compose.ui.graphics.Color
import com.budgettracker.app.data.AccountWithBalance
import com.budgettracker.app.data.TxDetailed
import com.budgettracker.app.data.db.BudgetEntity
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.data.db.TxType
import com.budgettracker.app.util.BudgetPeriodType
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.DateWindow
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.Periods
import com.budgettracker.app.util.convertMinor
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class BudgetProgress(
    val budget: BudgetEntity,
    val window: DateWindow,
    val spentMinor: Long,
    val remainingMinor: Long,
    val daysLeft: Int,
)

data class CategorySlice(
    val category: CategoryEntity?,
    val amountMinor: Long,
    val color: Color,
    val label: String,
)

data class MonthTotal(
    val yearMonth: YearMonth,
    val label: String,
    val incomeMinor: Long,
    val expenseMinor: Long,
)

object Insights {

    fun toBase(amountMinor: Long, currencyCode: String, baseCode: String): Long {
        if (currencyCode == baseCode) return amountMinor
        return convertMinor(amountMinor, Currencies.byCode(currencyCode), Currencies.byCode(baseCode))
    }

    fun txAmountInBase(tx: TxDetailed, baseCode: String): Long {
        val code = tx.account?.currencyCode ?: baseCode
        return toBase(tx.tx.amountMinor, code, baseCode)
    }

    fun sumBetween(
        txs: List<TxDetailed>,
        type: TxType,
        window: DateWindow,
        baseCode: String,
        categoryIds: Set<Long>? = null,
    ): Long = txs
        .asSequence()
        .filter { it.tx.type == type && it.tx.date >= window.startMillis && it.tx.date < window.endMillis }
        .filter { categoryIds == null || (it.tx.categoryId != null && it.tx.categoryId in categoryIds) }
        .sumOf { txAmountInBase(it, baseCode) }

    fun budgetProgress(budget: BudgetEntity, txs: List<TxDetailed>, baseCode: String, now: Long = System.currentTimeMillis()): BudgetProgress {
        val window = Periods.budgetWindow(budget.periodType, budget.customStart, budget.customLengthDays, now)
        val spent = sumBetween(txs, TxType.EXPENSE, window, baseCode, budget.categoryIds.toSet().ifEmpty { null })
        val daysLeft = ChronoUnit.DAYS.between(Fmt.toLocalDate(now), Fmt.toLocalDate(window.endMillis - 1)).toInt()
        return BudgetProgress(
            budget = budget,
            window = window,
            spentMinor = spent,
            remainingMinor = budget.amountMinor - spent,
            daysLeft = daysLeft.coerceAtLeast(0),
        )
    }

    /** Amount left per day for the rest of the budget window. */
    fun dailyAllowance(progress: BudgetProgress): Long? {
        if (progress.daysLeft <= 0) return null
        return (progress.remainingMinor / progress.daysLeft).coerceAtLeast(0)
    }

    /**
     * Safe-to-spend per day: leftover budget across all budgets divided by the
     * largest days-left. Shared by Home and the home-screen widget.
     */
    fun safeToSpendPerDay(progresses: List<BudgetProgress>): Long? {
        if (progresses.isEmpty()) return null
        val remaining = progresses.sumOf { it.remainingMinor.coerceAtLeast(0L) }
        val daysLeft = progresses.maxOf { it.daysLeft }.coerceAtLeast(1)
        return remaining / daysLeft
    }

    fun categoryBreakdown(txs: List<TxDetailed>, window: DateWindow, baseCode: String): List<CategorySlice> {
        val sums = LinkedHashMap<Long, Long>()
        var uncategorized = 0L
        txs.asSequence()
            .filter { it.tx.type == TxType.EXPENSE && it.tx.date >= window.startMillis && it.tx.date < window.endMillis }
            .forEach { tx ->
                val base = txAmountInBase(tx, baseCode)
                val id = tx.tx.categoryId
                if (id == null || tx.category == null) uncategorized += base
                else sums[id] = (sums[id] ?: 0L) + base
            }
        val result = sums.mapNotNull { (id, amount) ->
            txs.firstOrNull { it.tx.categoryId == id }?.category?.let { cat ->
                CategorySlice(cat, amount, Color(cat.colorArgb.toInt()), cat.name)
            }
        }.sortedByDescending { it.amountMinor }
        return if (uncategorized > 0) {
            result + CategorySlice(null, uncategorized, Color(0xFF9CA3AF.toInt()), "Uncategorized")
        } else {
            result
        }
    }

    /** Last [months] months (including the current one), oldest first. */
    fun monthlyTotals(txs: List<TxDetailed>, months: Int, baseCode: String): List<MonthTotal> {
        val current = YearMonth.now()
        val windows = (months - 1 downTo 0).map { current.minusMonths(it.toLong()) }
        return windows.map { ym ->
            val start = Fmt.fromLocalDate(ym.atDay(1))
            val end = Fmt.fromLocalDate(ym.plusMonths(1).atDay(1))
            val income = sumBetween(txs, TxType.INCOME, DateWindow(start, end), baseCode)
            val expense = sumBetween(txs, TxType.EXPENSE, DateWindow(start, end), baseCode)
            MonthTotal(ym, ym.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }, income, expense)
        }
    }

    fun totalBalanceBase(balances: List<AccountWithBalance>, baseCode: String): Long =
        balances
            .filter { !it.account.isArchived && it.account.includeInTotals }
            .sumOf { convertMinor(it.balanceMinor, it.account.currency, Currencies.byCode(baseCode)) }
}
