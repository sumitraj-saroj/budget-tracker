package com.budgettracker.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.data.db.GoalEntity
import com.budgettracker.app.domain.BudgetProgress
import com.budgettracker.app.ui.components.AmountText
import com.budgettracker.app.ui.components.EmojiBadge
import com.budgettracker.app.ui.components.IncomeGreen
import com.budgettracker.app.ui.components.TxListItem
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 118.dp),
    ) {
        item { HomeHeader(state.userName, onOpenSettings) }
        item { BalanceHero(state.totalBalanceMinor, state.baseCurrency, state.accountCount) }
        item { SummaryStrip(state.monthIncomeMinor, state.monthExpenseMinor, state.baseCurrency) }

        item {
            HomeSectionLabel(
                title = "Budgets",
                action = if (state.budgets.isEmpty()) null else "See all",
                onAction = onOpenBudgets,
            )
        }
        if (state.budgets.isEmpty()) {
            item { QuietRow("Set a weekly or monthly limit to stay on track.", "Add", onOpenBudgets) }
        } else {
            items(state.budgets, key = { "budget-${it.budget.id}" }) { progress ->
                BudgetCard(progress, state.baseCurrency, onOpenBudgets)
            }
        }

        if (state.upcoming.isNotEmpty()) {
            item { HomeSectionLabel("Upcoming", "See all", onOpenSubscriptions) }
            items(state.upcoming.take(3), key = { "sub-${it.sub.id}" }) { upcoming ->
                UpcomingRow(upcoming, state.baseCurrency)
            }
        }

        if (state.goals.isNotEmpty()) {
            item { HomeSectionLabel("Goals", "See all", onOpenGoals) }
            items(state.goals.take(3), key = { "goal-${it.id}" }) { goal ->
                GoalRow(goal, state.baseCurrency)
            }
        }

        if (state.txCount > 15 && isBackupStale(state.lastBackupAt)) {
            item { BackupRow(onOpenSettings) }
        }

        item {
            HomeSectionLabel(
                title = "Recent activity",
                action = if (state.recent.isEmpty()) null else "See all",
                onAction = onOpenTransactions,
            )
        }
        if (state.recent.isEmpty()) {
            item { QuietRow("Tap + to record your first transaction.", "Add", onAddTx) }
        } else {
            items(state.recent, key = { "tx-${it.tx.id}" }) { tx ->
                TxListItem(item = tx, onClick = { onTxClick(tx.tx.id) })
            }
        }
    }
}

private fun isBackupStale(lastBackupAt: Long?): Boolean =
    lastBackupAt == null || System.currentTimeMillis() - lastBackupAt > 14L * 24 * 3600 * 1000

// ---------- Header ----------

@Composable
private fun HomeHeader(userName: String, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                Fmt.greeting(),
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
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                userName.trim().take(1).ifBlank { "·" }.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ---------- Balance hero (pure typography, no card) ----------

@Composable
private fun BalanceHero(totalMinor: Long, currencyCode: String, accountCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            "TOTAL BALANCE",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.4.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            formatMoney(totalMinor, Currencies.byCode(currencyCode)),
            style = TextStyle(
                fontSize = 40.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            if (accountCount == 1) "1 account · $currencyCode" else "$accountCount accounts · $currencyCode",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------- Flat month summary strip ----------

@Composable
private fun SummaryStrip(incomeMinor: Long, expenseMinor: Long, currencyCode: String) {
    val currency = Currencies.byCode(currencyCode)
    val net = incomeMinor - expenseMinor
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryCell(
                label = "Income",
                value = formatMoney(incomeMinor, currency),
                valueColor = IncomeGreen,
                modifier = Modifier.weight(1f),
            )
            CellDivider()
            SummaryCell(
                label = "Spent",
                value = formatMoney(expenseMinor, currency),
                valueColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            CellDivider()
            SummaryCell(
                label = "Net",
                value = formatMoney(net, currency, signed = true),
                valueColor = if (net >= 0) IncomeGreen else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun CellDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

// ---------- Section label ----------

@Composable
private fun HomeSectionLabel(title: String, action: String?, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 1.4.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        if (action != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

/** Compact inline prompt used instead of heavy empty states. */
@Composable
private fun QuietRow(message: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAction) { Text(action) }
    }
}

// ---------- Budget card ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetCard(progress: BudgetProgress, currencyCode: String, onClick: () -> Unit) {
    val fraction = if (progress.budget.amountMinor <= 0) {
        0f
    } else {
        (progress.spentMinor.toFloat() / progress.budget.amountMinor).coerceIn(0f, 1f)
    }
    val over = progress.remainingMinor < 0
    val currency = Currencies.byCode(currencyCode)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    progress.budget.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    if (over) "Over by ${formatMoney(-progress.remainingMinor, currency)}"
                    else "${formatMoney(progress.remainingMinor, currency)} left",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    "${formatMoney(progress.spentMinor, currency)} of ${formatMoney(progress.budget.amountMinor, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${progress.daysLeft}d left",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ---------- Upcoming payment row ----------

@Composable
private fun UpcomingRow(upcoming: UpcomingPayment, baseCurrencyCode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiBadge(upcoming.sub.emoji, MaterialTheme.colorScheme.tertiary, size = 38.dp, fontSize = 15)
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                upcoming.sub.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when {
                    upcoming.daysUntil < 0 -> "Overdue ${-upcoming.daysUntil}d"
                    upcoming.daysUntil == 0L -> "Due today"
                    else -> "Due in ${upcoming.daysUntil}d"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (upcoming.daysUntil < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        AmountText(
            amountMinor = upcoming.sub.amountMinor,
            currencyCode = upcoming.account?.currencyCode ?: baseCurrencyCode,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

// ---------- Goal row ----------

@Composable
private fun GoalRow(goal: GoalEntity, baseCurrencyCode: String) {
    val fraction = if (goal.targetMinor <= 0) 0f else (goal.savedMinor.toFloat() / goal.targetMinor).coerceIn(0f, 1f)
    val currency = Currencies.byCode(baseCurrencyCode)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiBadge(goal.emoji, Color(goal.colorArgb.toInt()), size = 38.dp, fontSize = 15)
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    goal.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${(fraction * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = IncomeGreen,
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color(goal.colorArgb.toInt()),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${formatMoney(goal.savedMinor, currency)} of ${formatMoney(goal.targetMinor, currency)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------- Backup nudge ----------

@Composable
private fun BackupRow(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.Backup,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Back up your data", style = MaterialTheme.typography.titleSmall)
            Text(
                "Export a backup file to keep your records safe.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onOpenSettings) { Text("Open") }
    }
}
