package com.budgettracker.app.ui.debts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.data.db.DebtDirection
import com.budgettracker.app.data.db.DebtEntity
import com.budgettracker.app.ui.components.AmountText
import com.budgettracker.app.ui.components.ConfirmDialog
import com.budgettracker.app.ui.components.EmptyState
import com.budgettracker.app.ui.components.ListPickerDialog
import com.budgettracker.app.ui.components.PickerItem
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.formatMoney
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(viewModel: DebtsViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<DebtEntity?>(null) }
    var settling by remember { mutableStateOf<DebtEntity?>(null) }
    var deleting by remember { mutableStateOf<DebtEntity?>(null) }

    val list = if (tab == 0) state.lent else state.borrowed
    val outstanding = if (tab == 0) state.lentOutstandingMinor else state.borrowedOutstandingMinor
    val currency = Currencies.byCode(state.baseCurrency)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Debts & loans") },
                actions = {
                    IconButton(onClick = { creating = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(Modifier.padding(horizontal = 20.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf("Lent out" to 0, "I owe" to 1)
                    options.forEachIndexed { index, (label, _) ->
                        SegmentedButton(
                            selected = tab == index,
                            onClick = { tab = index },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        ) { Text(label) }
                    }
                }
            }
            Text(
                text = if (tab == 0) {
                    if (outstanding > 0) "You're owed ${formatMoney(outstanding, currency)}" else "Nothing outstanding"
                } else {
                    if (outstanding > 0) "You owe ${formatMoney(outstanding, currency)}" else "Nothing outstanding"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )

            if (list.isEmpty() && !state.loading) {
                EmptyState(
                    icon = Icons.Rounded.Handshake,
                    title = if (tab == 0) "No money lent out" else "No money borrowed",
                    message = if (tab == 0) {
                        "Track money you lend to friends and mark it paid when they pay you back."
                    } else {
                        "Track money you borrow and mark it paid when you return it."
                    },
                    ctaLabel = "Add entry",
                    onCta = { creating = true },
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(list, key = { it.id }) { debt ->
                        DebtCard(
                            debt = debt,
                            baseCurrencyCode = state.baseCurrency,
                            onSettle = { settling = debt },
                            onEdit = { editing = debt },
                            onReopen = { viewModel.reopen(debt) },
                        )
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        DebtEditDialog(
            debt = editing,
            baseCurrencyCode = state.baseCurrency,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = { id, direction, personName, amountText, note, dueDate ->
                viewModel.saveDebt(id, direction, personName, amountText, note, dueDate) {
                    creating = false
                    editing = null
                }
            },
            onDelete = { debt ->
                deleting = debt
                editing = null
                creating = false
            },
        )
    }

    if (settling != null) {
        SettleDialog(
            debt = settling!!,
            state = state,
            onDismiss = { settling = null },
            onSettle = { record, accountId, categoryId ->
                viewModel.settle(settling!!, record, accountId, categoryId)
                settling = null
            },
        )
    }

    if (deleting != null) {
        ConfirmDialog(
            title = "Delete entry?",
            message = "\"${deleting!!.personName}\" will be removed.",
            onConfirm = {
                viewModel.delete(deleting!!)
                deleting = null
            },
            onDismiss = { deleting = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebtCard(
    debt: DebtEntity,
    baseCurrencyCode: String,
    onSettle: () -> Unit,
    onEdit: () -> Unit,
    onReopen: () -> Unit,
) {
    Card(
        onClick = onEdit,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (debt.isSettled) {
                MaterialTheme.colorScheme.surfaceContainerLowest
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(debt.personName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val subtitle = buildList {
                        if (debt.dueDate != null) add("Due ${Fmt.dateShort(debt.dueDate!!)}")
                        if (debt.note.isNotBlank()) add(debt.note)
                    }.joinToString(" · ")
                    if (subtitle.isNotBlank()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                AmountText(
                    amountMinor = debt.amountMinor,
                    currencyCode = baseCurrencyCode,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.size(10.dp))
            if (!debt.isSettled) {
                val overdue = debt.dueDate != null && debt.dueDate!! < System.currentTimeMillis()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (overdue) {
                        val daysLate = ChronoUnit.DAYS.between(Fmt.toLocalDate(debt.dueDate!!), java.time.LocalDate.now())
                        Text(
                            "Overdue by $daysLate day${if (daysLate == 1L) "" else "s"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    FilledTonalButton(onClick = onSettle) { Text("Mark as paid") }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = com.budgettracker.app.ui.components.IncomeGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "Paid${debt.settledAt?.let { " · ${Fmt.dateShort(it)}" } ?: ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onReopen) { Text("Reopen") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebtEditDialog(
    debt: DebtEntity?,
    baseCurrencyCode: String,
    onDismiss: () -> Unit,
    onSave: (id: Long, direction: DebtDirection, personName: String, amountText: String, note: String, dueDate: Long?) -> Unit,
    onDelete: (DebtEntity) -> Unit,
) {
    var direction by remember { mutableStateOf(debt?.direction ?: DebtDirection.LENT) }
    var personName by remember { mutableStateOf(debt?.personName ?: "") }
    var amountText by remember {
        mutableStateOf(
            debt?.amountMinor?.let {
                java.math.BigDecimal(it).movePointLeft(Currencies.byCode(baseCurrencyCode).minorDigits).stripTrailingZeros().toPlainString()
            } ?: "",
        )
    }
    var note by remember { mutableStateOf(debt?.note ?: "") }
    var dueDate by remember { mutableStateOf(debt?.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val currency = Currencies.byCode(baseCurrencyCode)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (debt == null) "New entry" else "Edit entry") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf("I lent" to DebtDirection.LENT, "I borrowed" to DebtDirection.BORROWED)
                    options.forEachIndexed { index, (label, option) ->
                        SegmentedButton(
                            selected = direction == option,
                            onClick = { direction = option },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        ) { Text(label) }
                    }
                }
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it.take(40) },
                    label = { Text(if (direction == DebtDirection.LENT) "Who owes you?" else "Who do you owe?") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                    label = { Text("Amount (${currency.code})") },
                    prefix = { Text(currency.symbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(dueDate?.let { "Due ${Fmt.date(it)}" } ?: "Set due date (optional)")
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(80) },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (personName.isNotBlank() && amountText.isNotBlank()) onSave(debt?.id ?: 0, direction, personName, amountText, note, dueDate) },
                enabled = personName.isNotBlank() && amountText.isNotBlank(),
            ) { Text(if (debt == null) "Create" else "Save") }
        },
        dismissButton = {
            if (debt != null) {
                TextButton(onClick = { confirmDelete = true }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate ?: System.currentTimeMillis())
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis?.let { Fmt.utcDayStartToLocalMillis(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    dueDate = null
                    showDatePicker = false
                }) { Text("Clear") }
            },
        ) { androidx.compose.material3.DatePicker(state = datePickerState) }
    }
    if (confirmDelete && debt != null) {
        ConfirmDialog(
            title = "Delete entry?",
            message = "\"${debt.personName}\" will be removed.",
            onConfirm = { onDelete(debt) },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun SettleDialog(
    debt: DebtEntity,
    state: DebtsUiState,
    onDismiss: () -> Unit,
    onSettle: (record: Boolean, accountId: Long?, categoryId: Long?) -> Unit,
) {
    var record by remember { mutableStateOf(true) }
    var accountId by remember { mutableStateOf<Long?>(state.accounts.firstOrNull()?.id) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    val currency = Currencies.byCode(state.baseCurrency)

    val categories = if (debt.direction == DebtDirection.LENT) state.incomeCategories else state.expenseCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark as paid") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (debt.direction == DebtDirection.LENT) {
                        "${debt.personName} repaid you ${formatMoney(debt.amountMinor, currency)}."
                    } else {
                        "You repaid ${debt.personName} ${formatMoney(debt.amountMinor, currency)}."
                    },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Record as transaction", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (debt.direction == DebtDirection.LENT) "Adds income to an account" else "Adds an expense to an account",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = record, onCheckedChange = { record = it })
                }
                if (record) {
                    if (state.accounts.isNotEmpty()) {
                        OutlinedButton(onClick = { showAccountPicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Account: ${state.accounts.firstOrNull { it.id == accountId }?.let { "${it.emoji} ${it.name}" } ?: "Choose"}")
                        }
                        OutlinedButton(onClick = { showCategoryPicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Category: ${categories.firstOrNull { it.id == categoryId }?.let { "${it.emoji} ${it.name}" } ?: "None"}")
                        }
                    } else {
                        Text("Add an account first to record transactions.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSettle(record && state.accounts.isNotEmpty(), accountId, categoryId) }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showAccountPicker) {
        ListPickerDialog(
            title = "Choose account",
            items = state.accounts.map { PickerItem(key = it.id, label = it.name, sublabel = it.currencyCode, leading = it.emoji) },
            onDismiss = { showAccountPicker = false },
            onSelect = {
                accountId = it.key
                showAccountPicker = false
            },
        )
    }
    if (showCategoryPicker) {
        ListPickerDialog(
            title = "Choose category",
            items = categories.map { PickerItem(key = it.id, label = it.name, leading = it.emoji) },
            onDismiss = { showCategoryPicker = false },
            onSelect = {
                categoryId = it.key
                showCategoryPicker = false
            },
        )
    }
}
