package com.budgettracker.app.ui.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.data.db.Cycle
import com.budgettracker.app.data.db.SubscriptionEntity
import com.budgettracker.app.ui.components.AmountText
import com.budgettracker.app.ui.components.ConfirmDialog
import com.budgettracker.app.ui.components.EmojiBadge
import com.budgettracker.app.ui.components.EmojiPickerDialog
import com.budgettracker.app.ui.components.EmptyState
import com.budgettracker.app.ui.components.ListPickerDialog
import com.budgettracker.app.ui.components.PickerItem
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.convertMinor
import com.budgettracker.app.util.formatMoney
import java.time.temporal.ChronoUnit

private fun Cycle.label(): String = when (this) {
    Cycle.WEEKLY -> "Weekly"
    Cycle.MONTHLY -> "Monthly"
    Cycle.QUARTERLY -> "Every 3 months"
    Cycle.YEARLY -> "Yearly"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(viewModel: SubscriptionsViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SubscriptionWithAccount?>(null) }
    var deleting by remember { mutableStateOf<SubscriptionEntity?>(null) }
    val base = Currencies.byCode(state.baseCurrency)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Subscriptions") },
                actions = {
                    IconButton(onClick = { creating = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add subscription")
                    }
                },
            )
        },
    ) { padding ->
        if (state.subs.isEmpty() && !state.loading) {
            Column(Modifier.padding(padding)) {
                EmptyState(
                    icon = Icons.Rounded.Subscriptions,
                    title = "No subscriptions yet",
                    message = "Track recurring payments like Netflix, Spotify, or rent — and see their true monthly cost.",
                    ctaLabel = "Add subscription",
                    onCta = { creating = true },
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                "Monthly cost",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                "≈ ${formatMoney(state.monthlyTotalBaseMinor, base)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                "${state.activeCount} active subscription${if (state.activeCount == 1) "" else "s"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
                items(state.subs, key = { it.sub.id }) { pair ->
                    SubCard(
                        pair = pair,
                        baseCurrencyCode = state.baseCurrency,
                        onMarkPaid = { viewModel.markPaid(pair.sub) },
                        onEdit = { editing = pair },
                        onPauseToggle = { viewModel.toggleActive(pair.sub) },
                        onDelete = { deleting = pair.sub },
                    )
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }

    if (creating || editing != null) {
        SubEditDialog(
            existing = editing?.sub,
            state = state,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = { id, name, emoji, amountText, accountId, categoryId, cycle, nextDue, note ->
                viewModel.save(id, name, emoji, amountText, accountId, categoryId, cycle, nextDue, note) {
                    creating = false
                    editing = null
                }
            },
            onDelete = { sub ->
                deleting = sub
                creating = false
                editing = null
            },
        )
    }

    if (deleting != null) {
        ConfirmDialog(
            title = "Delete subscription?",
            message = "\"${deleting!!.name}\" will be removed.",
            onConfirm = {
                viewModel.delete(deleting!!)
                deleting = null
            },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
private fun SubCard(
    pair: SubscriptionWithAccount,
    baseCurrencyCode: String,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
    onPauseToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val sub = pair.sub
    val account = pair.account
    val currency = account?.currency ?: Currencies.byCode(baseCurrencyCode)
    val today = java.time.LocalDate.now()
    val daysUntil = ChronoUnit.DAYS.between(today, Fmt.toLocalDate(sub.nextDue))
    val overdue = daysUntil < 0

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBadge(
                    sub.emoji,
                    if (sub.isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(sub.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        buildString {
                            append(sub.cycle.label())
                            if (account != null) append(" · ${account.emoji} ${account.name}")
                            if (!sub.isActive) append(" · Paused")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    AmountText(amountMinor = sub.amountMinor, currencyCode = currency.code, style = MaterialTheme.typography.titleMedium)
                    if (currency.code != baseCurrencyCode) {
                        Text(
                            "≈ ${formatMoney(convertMinor(sub.amountMinor, currency, Currencies.byCode(baseCurrencyCode)), Currencies.byCode(baseCurrencyCode))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                BoxMenu(
                    sub = sub,
                    onEdit = onEdit,
                    onPauseToggle = onPauseToggle,
                    onDelete = onDelete,
                )
            }
            Spacer(Modifier.size(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (overdue && sub.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = when {
                        !sub.isActive -> "Paused"
                        overdue -> "Overdue — was due ${Fmt.dateShort(sub.nextDue)}"
                        daysUntil == 0L -> "Due today"
                        else -> "Due in $daysUntil day${if (daysUntil == 1L) "" else "s"} (${Fmt.dateShort(sub.nextDue)})"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (overdue && sub.isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalButton(onClick = onMarkPaid, enabled = sub.isActive) { Text("Mark paid") }
            }
        }
    }
}

@Composable
private fun BoxMenu(
    sub: SubscriptionEntity,
    onEdit: () -> Unit,
    onPauseToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.foundation.layout.Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "More options", modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    expanded = false
                    onEdit()
                },
            )
            DropdownMenuItem(
                text = { Text(if (sub.isActive) "Pause" else "Resume") },
                onClick = {
                    expanded = false
                    onPauseToggle()
                },
            )
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubEditDialog(
    existing: SubscriptionEntity?,
    state: SubsUiState,
    onDismiss: () -> Unit,
    onSave: (
        id: Long, name: String, emoji: String, amountText: String, accountId: Long,
        categoryId: Long?, cycle: Cycle, nextDue: Long, note: String,
    ) -> Unit,
    onDelete: (SubscriptionEntity) -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var emoji by remember { mutableStateOf(existing?.emoji ?: "📺") }
    var amountText by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(existing?.accountId ?: state.accounts.firstOrNull()?.id ?: 0L) }
    var categoryId by remember { mutableStateOf(existing?.categoryId) }
    var cycle by remember { mutableStateOf(existing?.cycle ?: Cycle.MONTHLY) }
    var nextDue by remember { mutableStateOf(existing?.nextDue ?: System.currentTimeMillis()) }
    var note by remember { mutableStateOf(existing?.note ?: "") }

    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showCyclePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val account = state.accounts.firstOrNull { it.id == accountId }
    val currency = account?.currency ?: Currencies.byCode(state.baseCurrency)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New subscription" else "Edit subscription") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it.take(30) }, label = { Text("Name") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiBadge(emoji, MaterialTheme.colorScheme.tertiary, size = 40.dp, fontSize = 17)
                    Spacer(Modifier.size(12.dp))
                    TextButton(onClick = { showEmojiPicker = true }) { Text("Icon: $emoji") }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                    label = { Text("Amount (${currency.code})") },
                    prefix = { Text(currency.symbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                if (state.accounts.isNotEmpty()) {
                    OutlinedButton(onClick = { showAccountPicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Account: ${account?.let { "${it.emoji} ${it.name}" } ?: "Choose"}")
                    }
                }
                OutlinedButton(onClick = { showCategoryPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Category: ${state.expenseCategories.firstOrNull { it.id == categoryId }?.let { "${it.emoji} ${it.name}" } ?: "Subscriptions"}")
                }
                OutlinedButton(onClick = { showCyclePicker = true }, modifier = Modifier.fillMaxWidth()) { Text("Cycle: ${cycle.label()}") }
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Next due: ${Fmt.date(nextDue)}")
                }
                OutlinedTextField(value = note, onValueChange = { note = it.take(80) }, label = { Text("Note (optional)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank() && amountText.isNotBlank() && accountId != 0L) onSave(existing?.id ?: 0, name, emoji, amountText, accountId, categoryId, cycle, nextDue, note) },
                enabled = name.isNotBlank() && amountText.isNotBlank() && accountId != 0L,
            ) { Text(if (existing == null) "Create" else "Save") }
        },
        dismissButton = {
            if (existing != null) {
                TextButton(onClick = { confirmDelete = true }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )

    if (showEmojiPicker) {
        EmojiPickerDialog(onDismiss = { showEmojiPicker = false }, onPick = { emoji = it })
    }
    if (showAccountPicker) {
        ListPickerDialog(
            title = "Paying from",
            items = state.accounts.map { PickerItem(key = it.id, label = it.name, sublabel = it.currencyCode, leading = it.emoji, selected = it.id == accountId) },
            onDismiss = { showAccountPicker = false },
            onSelect = {
                accountId = it.key
                showAccountPicker = false
            },
        )
    }
    if (showCategoryPicker) {
        ListPickerDialog(
            title = "Category",
            items = state.expenseCategories.map { PickerItem(key = it.id, label = it.name, leading = it.emoji, selected = it.id == categoryId) },
            onDismiss = { showCategoryPicker = false },
            onSelect = {
                categoryId = it.key
                showCategoryPicker = false
            },
        )
    }
    if (showCyclePicker) {
        ListPickerDialog(
            title = "Billing cycle",
            items = Cycle.entries.map { PickerItem(key = it.ordinal.toLong(), label = it.label(), selected = it == cycle) },
            onDismiss = { showCyclePicker = false },
            onSelect = {
                cycle = Cycle.entries[it.key.toInt()]
                showCyclePicker = false
            },
        )
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = nextDue)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { nextDue = Fmt.utcDayStartToLocalMillis(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }
    if (confirmDelete && existing != null) {
        ConfirmDialog(
            title = "Delete subscription?",
            message = "\"${existing.name}\" will be removed.",
            onConfirm = { onDelete(existing) },
            onDismiss = { confirmDelete = false },
        )
    }
}
