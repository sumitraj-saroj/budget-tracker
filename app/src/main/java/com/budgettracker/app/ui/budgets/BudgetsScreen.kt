package com.budgettracker.app.ui.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.domain.BudgetProgress
import com.budgettracker.app.domain.Insights
import com.budgettracker.app.ui.components.AmountText
import com.budgettracker.app.ui.components.BudgetProgressBar
import com.budgettracker.app.ui.components.EmptyState
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.Periods
import com.budgettracker.app.util.formatMoney
import kotlin.math.roundToInt

private fun budgetPace(progress: BudgetProgress): Float =
    ((System.currentTimeMillis() - progress.window.startMillis).toFloat() /
        (progress.window.endMillis - progress.window.startMillis)).coerceIn(0f, 1f)

@Composable
fun BudgetsScreen(
    onEditBudget: (Long) -> Unit,
    onNewBudget: () -> Unit,
    viewModel: BudgetsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Budgets", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onNewBudget) {
                Icon(Icons.Rounded.Add, contentDescription = "New budget")
            }
        }

        if (state.budgets.isEmpty() && !state.loading) {
            EmptyState(
                icon = Icons.Rounded.AccountBalanceWallet,
                title = "No budgets yet",
                message = "Create a weekly, monthly, or custom budget to keep your spending in check.",
                ctaLabel = "New budget",
                onCta = onNewBudget,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.budgets, key = { it.budget.id }) { progress ->
                    BudgetCardFull(
                        progress = progress,
                        currencyCode = state.baseCurrency,
                        onClick = { onEditBudget(progress.budget.id) },
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun BudgetCardFull(progress: BudgetProgress, currencyCode: String, onClick: () -> Unit) {
    val fraction = if (progress.budget.amountMinor <= 0) 0f else (progress.spentMinor.toFloat() / progress.budget.amountMinor).coerceIn(0f, 1f)
    val over = progress.remainingMinor < 0
    val allowance = Insights.dailyAllowance(progress)

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(progress.budget.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${Periods.periodLabel(progress.budget.periodType, progress.budget.customLengthDays)} · ${Fmt.dateShort(progress.window.startMillis)} – ${Fmt.dateShort(progress.window.endMillis - 86400000L)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AmountText(
                    amountMinor = progress.remainingMinor,
                    currencyCode = currencyCode,
                    color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(12.dp))
            BudgetProgressBar(
                progress = fraction,
                pace = budgetPace(progress),
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (over) {
                    "${formatMoney(progress.spentMinor, Currencies.byCode(currencyCode))} spent — over budget by ${formatMoney(-progress.remainingMinor, Currencies.byCode(currencyCode))}"
                } else {
                    "${formatMoney(progress.spentMinor, Currencies.byCode(currencyCode))} of ${formatMoney(progress.budget.amountMinor, Currencies.byCode(currencyCode))} spent"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                buildString {
                    append("${progress.daysLeft} day${if (progress.daysLeft == 1) "" else "s"} left")
                    if (!over && allowance != null && progress.daysLeft > 0) {
                        append(" · ${formatMoney(allowance, Currencies.byCode(currencyCode))}/day left")
                    }
                    if (over) append(" · budget exceeded")
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (progress.budget.categoryIds.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "${progress.budget.categoryIds.size} categor${if (progress.budget.categoryIds.size == 1) "y" else "ies"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
