package com.budgettracker.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.domain.BudgetProgress
import com.budgettracker.app.ui.components.AmountText
import com.budgettracker.app.ui.components.EmojiBadge
import com.budgettracker.app.ui.components.EmptyState
import com.budgettracker.app.ui.components.IncomeGreen
import com.budgettracker.app.ui.components.MiniCard
import com.budgettracker.app.ui.components.SectionHeader
import com.budgettracker.app.ui.components.TxListItem
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.Periods
import com.budgettracker.app.util.formatMoney
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onTxClick: (Long) -> Unit,
    onAddTx: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val base = Currencies.byCode(state.baseCurrency)

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Header(userName = state.userName, onOpenSettings = onOpenSettings)
        }
        item {
            BalanceCard(
                totalMinor = state.totalBalanceMinor,
                currencyCode = state.baseCurrency,
                accountCount = state.accountCount,
                monthIncome = state.monthIncomeMinor,
                monthExpense = state.monthExpenseMinor,
            )
        }

        // Budgets
        item {
            SectionHeader(
                title = "Budgets",
                actionLabel = if (state.budgets.isEmpty()) null else "See all",
                onAction = onOpenBudgets,
            )
        }
        if (state.budgets.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Savings,
                    title = "No budgets yet",
                    message = "Set a weekly or monthly spending limit to stay on track.",
                    ctaLabel = "Create a budget",
                    onCta = onOpenBudgets,
                )
            }
        } else {
            items(state.budgets, key = { "budget-${it.budget.id}" }) { progress ->
                BudgetCard(progress, currencyCode = state.baseCurrency, onClick = onOpenBudgets)
            }
        }

        // Upcoming payments
        if (state.upcoming.isNotEmpty()) {
            item {
                SectionHeader(title = "Upcoming payments", actionLabel = "See all", onAction = onOpenSubscriptions)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.upcoming, key = { "sub-${it.sub.id}" }) { upcoming ->
                        UpcomingCard(upcoming, currencyCode = state.baseCurrency)
                    }
                }
            }
        }

        // Goals
        if (state.goals.isNotEmpty()) {
            item {
                SectionHeader(title = "Savings goals", actionLabel = "See all", onAction = onOpenGoals)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.goals, key = { "goal-${it.id}" }) { goal ->
                        GoalCard(goal, baseCurrencyCode = state.baseCurrency)
                    }
                }
            }
        }

        // Backup reminder
        if (state.txCount > 15 && isBackupStale(state.lastBackupAt)) {
            item { BackupNudgeCard(lastBackupAt = state.lastBackupAt, onOpenSettings = onOpenSettings) }
        }

        // Recent activity
        item {
            SectionHeader(title = "Recent activity", actionLabel = if (state.recent.isEmpty()) null else "See all", onAction = onOpenTransactions)
        }
        if (state.recent.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.Add,
                    title = "No transactions yet",
                    message = "Tap the + button to record your first income or expense.",
                    ctaLabel = "Add transaction",
                    onCta = onAddTx,
                )
            }
        } else {
            items(state.recent, key = { "tx-${it.tx.id}" }) { tx ->
                TxListItem(item = tx, onClick = { onTxClick(tx.tx.id) })
            }
        }
    }
}

private fun isBackupStale(lastBackupAt: Long?): Boolean =
    lastBackupAt == null || System.currentTimeMillis() - lastBackupAt > 14L * 24 * 3600 * 1000

@Composable
private fun Header(userName: String, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${Fmt.greeting()}${if (userName.isNotBlank()) "," else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                userName.ifBlank { "there" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .padding(0.dp),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(onClick = onOpenSettings, contentPadding = PaddingValues(0.dp)) {
                Text(
                    userName.trim().take(1).ifBlank { "👤" }.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(
    totalMinor: Long,
    currencyCode: String,
    accountCount: Int,
    monthIncome: Long,
    monthExpense: Long,
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(scheme.primary, scheme.tertiary),
                    ),
                )
                .padding(20.dp),
        ) {
            Text("Total balance", style = MaterialTheme.typography.labelMedium, color = scheme.onPrimary.copy(alpha = 0.85f))
            Spacer(Modifier.height(4.dp))
            Text(
                formatMoney(totalMinor, Currencies.byCode(currencyCode)),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (accountCount == 1) "1 account" else "$accountCount accounts",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onPrimary.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill(
                    icon = { Icon(Icons.Rounded.ArrowUpward, null, tint = scheme.onPrimary, modifier = Modifier.size(14.dp)) },
                    label = "Income",
                    value = formatMoney(monthIncome, Currencies.byCode(currencyCode)),
                    modifier = Modifier.weight(1f),
                )
                StatPill(
                    icon = { Icon(Icons.Rounded.ArrowDownward, null, tint = scheme.onPrimary, modifier = Modifier.size(14.dp)) },
                    label = "Spent",
                    value = formatMoney(monthExpense, Currencies.byCode(currencyCode)),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                "this month",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onPrimary.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun StatPill(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f), MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        icon()
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BudgetCard(progress: BudgetProgress, currencyCode: String, onClick: () -> Unit) {
    val fraction = if (progress.budget.amountMinor <= 0) 0f else (progress.spentMinor.toFloat() / progress.budget.amountMinor).coerceIn(0f, 1f)
    val over = progress.remainingMinor < 0
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(progress.budget.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        Periods.periodLabel(progress.budget.periodType, progress.budget.customLengthDays) +
                            " · ${progress.daysLeft} day${if (progress.daysLeft == 1) "" else "s"} left",
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
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${formatMoney(progress.spentMinor, Currencies.byCode(currencyCode))} of ${formatMoney(progress.budget.amountMinor, Currencies.byCode(currencyCode))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UpcomingCard(upcoming: UpcomingPayment, currencyCode: String) {
    MiniCard(modifier = Modifier.width(160.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiBadge(upcoming.sub.emoji, MaterialTheme.colorScheme.tertiary, size = 34.dp, fontSize = 15)
            Spacer(Modifier.size(10.dp))
            Text(
                upcoming.sub.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        AmountText(
            amountMinor = upcoming.sub.amountMinor,
            currencyCode = upcoming.account?.currencyCode ?: currencyCode,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = if (upcoming.daysUntil < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = when {
                    upcoming.daysUntil < 0 -> "Overdue ${-upcoming.daysUntil}d"
                    upcoming.daysUntil == 0L -> "Due today"
                    else -> "Due in ${upcoming.daysUntil}d"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (upcoming.daysUntil < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GoalCard(goal: com.budgettracker.app.data.db.GoalEntity, baseCurrencyCode: String) {
    val fraction = if (goal.targetMinor <= 0) 0f else (goal.savedMinor.toFloat() / goal.targetMinor).coerceIn(0f, 1f)
    MiniCard(modifier = Modifier.width(180.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiBadge(goal.emoji, Color(goal.colorArgb.toInt()), size = 34.dp, fontSize = 15)
            Spacer(Modifier.size(10.dp))
            Text(goal.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "${formatMoney(goal.savedMinor, Currencies.byCode(baseCurrencyCode))} / ${formatMoney(goal.targetMinor, Currencies.byCode(baseCurrencyCode))}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = Color(goal.colorArgb.toInt()),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${(fraction * 100).roundToInt()}% saved",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BackupNudgeCard(lastBackupAt: Long?, onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Back up your data", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(
                    text = if (lastBackupAt == null) "Keep your records safe. Export a backup file from Settings."
                    else "Last backup was more than 2 weeks ago.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                )
            }
            TextButton(onClick = onOpenSettings) { Text("Open") }
        }
    }
}
