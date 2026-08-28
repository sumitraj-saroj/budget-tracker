package com.budgettracker.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.db.TxType
import com.budgettracker.app.domain.CategorySlice
import com.budgettracker.app.domain.Insights
import com.budgettracker.app.ui.components.BarEntry
import com.budgettracker.app.util.DateWindow
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.Periods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class StatsPeriod(val label: String) {
    THIS_MONTH("This month"),
    THREE_MONTHS("3 months"),
    SIX_MONTHS("6 months"),
    YEAR("1 year"),
    ALL("All time"),
}

data class StatsUiState(
    val loading: Boolean = true,
    val baseCurrency: String = "USD",
    val period: StatsPeriod = StatsPeriod.THIS_MONTH,
    val income: Long = 0,
    val expense: Long = 0,
    val txCount: Int = 0,
    val slices: List<CategorySlice> = emptyList(),
    val bars: List<BarEntry> = emptyList(),
    val trend: List<Float> = emptyList(),
    val trendLabels: List<String> = emptyList(),
    val dailyAverage: Long = 0,
    val expenseChangePct: Float? = null,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repo: FinanceRepository,
    prefsRepository: PrefsRepository,
) : ViewModel() {

    private val period = MutableStateFlow(StatsPeriod.THIS_MONTH)

    val uiState: StateFlow<StatsUiState> = combine(
        repo.transactionsDetailed(),
        prefsRepository.prefs,
        period,
    ) { txs, prefs, selected ->
        buildState(txs, prefs.baseCurrency, selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun setPeriod(newPeriod: StatsPeriod) {
        period.value = newPeriod
    }

    private fun buildState(txs: List<com.budgettracker.app.data.TxDetailed>, base: String, selected: StatsPeriod): StatsUiState {
        val now = System.currentTimeMillis()
        val window = when (selected) {
            StatsPeriod.THIS_MONTH -> Periods.currentMonth(now)
            StatsPeriod.THREE_MONTHS -> Periods.lastNDays(90, now)
            StatsPeriod.SIX_MONTHS -> Periods.lastNDays(180, now)
            StatsPeriod.YEAR -> Periods.lastNDays(365, now)
            StatsPeriod.ALL -> DateWindow(0, Long.MAX_VALUE)
        }
        val inWindow = txs.filter { it.tx.date >= window.startMillis && it.tx.date < window.endMillis }
        val income = Insights.sumBetween(txs, TxType.INCOME, window, base)
        val expense = Insights.sumBetween(txs, TxType.EXPENSE, window, base)

        // Previous-period comparison for expenses
        val prevWindow: DateWindow? = when (selected) {
            StatsPeriod.THIS_MONTH -> {
                val ym = YearMonth.now().minusMonths(1)
                DateWindow(Fmt.fromLocalDate(ym.atDay(1)), Fmt.fromLocalDate(ym.plusMonths(1).atDay(1)))
            }
            StatsPeriod.THREE_MONTHS -> window.copy(
                startMillis = window.startMillis - 90L * 86_400_000,
                endMillis = window.endMillis - 90L * 86_400_000,
            )
            StatsPeriod.SIX_MONTHS -> window.copy(
                startMillis = window.startMillis - 180L * 86_400_000,
                endMillis = window.endMillis - 180L * 86_400_000,
            )
            StatsPeriod.YEAR -> window.copy(
                startMillis = window.startMillis - 365L * 86_400_000,
                endMillis = window.endMillis - 365L * 86_400_000,
            )
            StatsPeriod.ALL -> null
        }
        val prevExpense = prevWindow?.let { Insights.sumBetween(txs, TxType.EXPENSE, it, base) }
        val expenseChangePct: Float? = when {
            prevWindow == null || prevExpense == null -> null
            prevExpense <= 0L || expense <= 0L -> null
            else -> ((expense - prevExpense).toFloat() / prevExpense) * 100f
        }

        // Category slices: top 5 + Other
        val breakdown = Insights.categoryBreakdown(txs, window, base)
        val slices = if (breakdown.size > 6) {
            val top = breakdown.take(5)
            val rest = breakdown.drop(5)
            top + CategorySlice(
                category = null,
                amountMinor = rest.sumOf { it.amountMinor },
                color = rest.first().color.copy(alpha = 0.5f),
                label = "Other",
            )
        } else {
            breakdown
        }

        // Bars
        val bars = when (selected) {
            StatsPeriod.THIS_MONTH -> {
                val ym = YearMonth.now()
                val weekCount = (ym.lengthOfMonth() + 6) / 7
                (0 until weekCount).map { week ->
                    val startDay = week * 7 + 1
                    val endDay = minOf(startDay + 6, ym.lengthOfMonth())
                    val w = DateWindow(
                        Fmt.fromLocalDate(ym.atDay(startDay)),
                        Fmt.fromLocalDate(ym.atDay(endDay)) + 86_400_000L,
                    )
                    BarEntry(
                        label = "W${week + 1}",
                        income = Insights.sumBetween(txs, TxType.INCOME, w, base),
                        expense = Insights.sumBetween(txs, TxType.EXPENSE, w, base),
                    )
                }
            }
            StatsPeriod.THREE_MONTHS -> Insights.monthlyTotals(txs, 3, base).map { BarEntry(it.label, it.incomeMinor, it.expenseMinor) }
            StatsPeriod.SIX_MONTHS -> Insights.monthlyTotals(txs, 6, base).map { BarEntry(it.label, it.incomeMinor, it.expenseMinor) }
            else -> Insights.monthlyTotals(txs, 12, base).map { BarEntry(it.label, it.incomeMinor, it.expenseMinor) }
        }

        // Cumulative expense trend (daily for short windows, weekly otherwise)
        val spanDays = ChronoUnit.DAYS.between(
            Fmt.toLocalDate(inWindow.minOfOrNull { it.tx.date } ?: now),
            Fmt.toLocalDate(now),
        ).toInt() + 1
        val useWeekly = spanDays > 92
        val dailyExpense = mutableListOf<Float>()
        val trendSpanDays: Int
        if (!useWeekly) {
            val days = spanDays.coerceAtLeast(1)
            trendSpanDays = days
            val today = Fmt.toLocalDate(now)
            val start = today.minusDays((days - 1).toLong())
            val cumulative = LongArray(days)
            inWindow.filter { it.tx.type == TxType.EXPENSE }.forEach { tx ->
                val index = ChronoUnit.DAYS.between(start, Fmt.toLocalDate(tx.tx.date)).toInt()
                if (index in 0 until days) cumulative[index] += Insights.txAmountInBase(tx, base)
            }
            var running = 0L
            for (i in 0 until days) {
                running += cumulative[i]
                dailyExpense.add(running.toFloat())
            }
        } else {
            val today = Fmt.toLocalDate(now)
            val weeks = 26
            trendSpanDays = weeks * 7
            val cumulative = LongArray(weeks)
            inWindow.filter { it.tx.type == TxType.EXPENSE }.forEach { tx ->
                val index = (ChronoUnit.DAYS.between(Fmt.toLocalDate(tx.tx.date), today) / 7).toInt()
                if (index in 0 until weeks) cumulative[weeks - 1 - index] += Insights.txAmountInBase(tx, base)
            }
            var running = 0L
            for (i in 0 until weeks) {
                running += cumulative[i]
                dailyExpense.add(running.toFloat())
            }
        }
        val maxTrend = (dailyExpense.maxOrNull() ?: 0f).coerceAtLeast(1f)
        val trend = dailyExpense.map { (it / maxTrend).coerceIn(0f, 1f) }
        val trendLabels = if (trend.size >= 3) {
            listOf(
                Fmt.dateShort(now - (trendSpanDays - 1) * 86_400_000L),
                Fmt.dateShort(now - (trendSpanDays / 2) * 86_400_000L),
                Fmt.dateShort(now),
            )
        } else {
            emptyList()
        }

        val daysElapsed = when (selected) {
            StatsPeriod.THIS_MONTH -> LocalDate.now().dayOfMonth
            StatsPeriod.ALL -> spanDays.coerceAtLeast(1)
            else -> window.days.coerceAtLeast(1)
        }

        return StatsUiState(
            loading = false,
            baseCurrency = base,
            period = selected,
            income = income,
            expense = expense,
            txCount = inWindow.size,
            slices = slices,
            bars = bars,
            trend = trend,
            trendLabels = trendLabels,
            dailyAverage = expense / daysElapsed,
            expenseChangePct = expenseChangePct,
        )
    }
}
