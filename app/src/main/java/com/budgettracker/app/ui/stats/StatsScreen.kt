package com.budgettracker.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.ui.components.AmountText
import com.budgettracker.app.ui.components.DonutChart
import com.budgettracker.app.ui.components.EmptyState
import com.budgettracker.app.ui.components.GroupedBarChart
import com.budgettracker.app.ui.components.IncomeGreen
import com.budgettracker.app.ui.components.TrendChart
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.formatMoney
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    onOpenCategory: (Long) -> Unit = {},
    viewModel: StatsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currency = Currencies.byCode(state.baseCurrency)

    Column {
        Text(
            "Stats",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatsPeriod.entries.forEach { period ->
                FilterChip(
                    selected = state.period == period,
                    onClick = { viewModel.setPeriod(period) },
                    label = { Text(period.label) },
                )
            }
        }

        if (state.txCount == 0 && !state.loading) {
            EmptyState(
                icon = Icons.Rounded.PieChart,
                title = "No data for this period",
                message = "Add some transactions and your charts will appear here.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Summary
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(Modifier.padding(18.dp)) {
                            SummaryColumn(
                                label = "Income",
                                amountMinor = state.income,
                                currencyCode = state.baseCurrency,
                                color = IncomeGreen,
                                modifier = Modifier.weight(1f),
                            )
                            SummaryColumn(
                                label = "Expenses",
                                amountMinor = state.expense,
                                currencyCode = state.baseCurrency,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                            )
                            SummaryColumn(
                                label = "Net",
                                amountMinor = state.income - state.expense,
                                currencyCode = state.baseCurrency,
                                color = if (state.income - state.expense >= 0) IncomeGreen else MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                                signed = true,
                            )
                        }
                        Text(
                            "Avg ${formatMoney(state.dailyAverage, currency)}/day · ${state.txCount} transaction${if (state.txCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 18.dp),
                        )
                        val pct = state.expenseChangePct
                        if (pct != null) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.padding(start = 18.dp, bottom = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (pct > 0f) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (pct > 0f) MaterialTheme.colorScheme.error else IncomeGreen,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(Modifier.size(4.dp))
                                Text(
                                    "${kotlin.math.abs(pct).roundToInt()}% ${comparisonLabel(state.period)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (pct > 0f) MaterialTheme.colorScheme.error else IncomeGreen,
                                )
                            }
                        } else {
                            Spacer(Modifier.height(14.dp))
                        }
                    }
                }

                // Donut
                if (state.slices.isNotEmpty()) {
                    item {
                        ChartCard(title = "Spending by category") {
                            val total = state.slices.sumOf { it.amountMinor }.coerceAtLeast(1)
                            val fractions = state.slices.map { it.color to it.amountMinor.toFloat() / total }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                DonutChart(
                                    fractions = fractions,
                                    modifier = Modifier.size(190.dp),
                                    strokeWidth = 26.dp,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    onSliceClick = { index ->
                                        state.slices.getOrNull(index)?.category?.let { category ->
                                            onOpenCategory(category.id)
                                        }
                                    },
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            formatMoney(total, currency),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            "spent",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                state.slices.forEach { slice ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            Modifier
                                                .size(11.dp)
                                                .background(slice.color, CircleShape),
                                        )
                                        Spacer(Modifier.size(10.dp))
                                        Text(
                                            slice.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            "${(slice.amountMinor.toFloat() / total * 100).roundToInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(end = 10.dp),
                                        )
                                        AmountText(
                                            amountMinor = slice.amountMinor,
                                            currencyCode = state.baseCurrency,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bars
                item {
                    ChartCard(title = "Income vs expenses") {
                        GroupedBarChart(
                            entries = state.bars,
                            incomeColor = IncomeGreen,
                            expenseColor = MaterialTheme.colorScheme.error,
                            formatValue = { fmtShort(it, currency) },
                        )
                    }
                }

                // Trend
                if (state.trend.size > 1) {
                    item {
                        ChartCard(title = "Cumulative spending") {
                            TrendChart(
                                points = state.trend,
                                xLabels = state.trendLabels,
                                color = MaterialTheme.colorScheme.primary,
                                formatValue = { fraction ->
                                    fmtShort((state.expense * fraction).toLong(), currency)
                                },
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(70.dp)) }
            }
        }
    }
}

/** Abbreviate a MINOR-unit amount for chart axis labels, e.g. 25050 paise -> "₹250.5". */
private fun fmtShort(amountMinor: Long, currency: com.budgettracker.app.util.CurrencyInfo): String {
    val major = amountMinor / Math.pow(10.0, currency.minorDigits.toDouble())
    val abs = kotlin.math.abs(major)
    val sign = if (major < 0) "-" else ""
    return when {
        abs >= 1_000_000 -> String.format("%s%s%.1fM", sign, currency.symbol, abs / 1_000_000.0)
        abs >= 1_000 -> String.format("%s%s%.1fk", sign, currency.symbol, abs / 1_000.0)
        abs >= 100 -> String.format("%s%s%.0f", sign, currency.symbol, abs)
        else -> String.format("%s%s%.1f", sign, currency.symbol, abs)
    }
}

private fun comparisonLabel(period: StatsPeriod): String = when (period) {
    StatsPeriod.THIS_MONTH -> "spending vs last month"
    StatsPeriod.THREE_MONTHS -> "spending vs previous 3 months"
    StatsPeriod.SIX_MONTHS -> "spending vs previous 6 months"
    StatsPeriod.YEAR -> "spending vs last year"
    StatsPeriod.ALL -> ""
}

@Composable
private fun SummaryColumn(
    label: String,
    amountMinor: Long,
    currencyCode: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    signed: Boolean = false,
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AmountText(
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            color = color,
            style = MaterialTheme.typography.titleSmall,
            signed = signed,
        )
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 14.dp))
            content()
        }
    }
}
