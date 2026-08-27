package com.budgettracker.app.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.data.TxDetailed
import com.budgettracker.app.data.db.TxType
import com.budgettracker.app.ui.components.AmountText
import com.budgettracker.app.ui.components.EmptyState
import com.budgettracker.app.ui.components.IncomeGreen
import com.budgettracker.app.ui.components.SwipeDeleteBox
import com.budgettracker.app.ui.components.TxListItem
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.Periods
import com.budgettracker.app.util.formatMoney
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onTxClick: (Long) -> Unit,
    onAddTx: () -> Unit,
    viewModel: TransactionsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var menuForTx by remember { mutableStateOf<TxDetailed?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Records", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Rounded.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        TxSort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (option) {
                                            TxSort.NEWEST -> "Newest first"
                                            TxSort.OLDEST -> "Oldest first"
                                            TxSort.LARGEST -> "Largest amount"
                                            TxSort.SMALLEST -> "Smallest amount"
                                        },
                                    )
                                },
                                onClick = {
                                    viewModel.setSort(option)
                                    showSortMenu = false
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = { showFilterSheet = true }) {
                    Box {
                        Icon(Icons.Rounded.FilterList, contentDescription = "Filters")
                        if (state.filter.isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .align(Alignment.TopEnd),
                            )
                        }
                    }
                }
            }

            // Search + type chips
            Column(Modifier.padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = state.filter.query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search note, category, account") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.filter.query.isNotBlank()) {
                            IconButton(onClick = { viewModel.setQuery("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val typeOptions: List<Pair<String, TxType?>> = listOf(
                        "All" to null,
                        "Expense" to TxType.EXPENSE,
                        "Income" to TxType.INCOME,
                        "Transfer" to TxType.TRANSFER,
                    )
                    typeOptions.forEach { (label, type) ->
                        FilterChip(
                            selected = state.filter.type == type,
                            onClick = { viewModel.setType(type) },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Totals for current filter
            if (state.filteredCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${state.filteredCount} transaction${if (state.filteredCount == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "↑ ${formatMoney(state.filteredIncomeBase, Currencies.byCode(state.baseCurrency))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IncomeGreen,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        "↓ ${formatMoney(state.filteredExpenseBase, Currencies.byCode(state.baseCurrency))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.totalCount == 0) {
                EmptyState(
                    icon = Icons.Rounded.ReceiptLong,
                    title = "No transactions yet",
                    message = "Add your first income or expense to see it here.",
                    ctaLabel = "Add transaction",
                    onCta = onAddTx,
                )
            } else if (state.groups.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.Search,
                    title = "Nothing found",
                    message = "Try changing your search or filters.",
                    ctaLabel = "Clear filters",
                    onCta = viewModel::clearFilters,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    state.groups.forEach { group ->
                        item(key = "header-${group.dateMillis}") {
                            DayHeader(group, baseCurrencyCode = state.baseCurrency)
                        }
                        items(group.items, key = { "tx-${it.tx.id}" }) { item ->
                            SwipeDeleteBox(
                                item = item,
                                onDelete = { deleted ->
                                    viewModel.deleteTx(deleted.tx) {
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Transaction deleted",
                                                actionLabel = "Undo",
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoDelete()
                                            }
                                        }
                                    }
                                },
                            ) {
                                Box {
                                    TxListItem(
                                        item = item,
                                        showDate = false,
                                        onClick = { onTxClick(item.tx.id) },
                                        onLongClick = { menuForTx = item },
                                    )
                                    DropdownMenu(
                                        expanded = menuForTx?.tx?.id == item.tx.id,
                                        onDismissRequest = { menuForTx = null },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                            onClick = {
                                                menuForTx = null
                                                onTxClick(item.tx.id)
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Duplicate") },
                                            leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) },
                                            onClick = {
                                                menuForTx = null
                                                viewModel.duplicate(item.tx)
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                                            onClick = {
                                                menuForTx = null
                                                scope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = "Transaction deleted",
                                                        actionLabel = "Undo",
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.undoDelete()
                                                    }
                                                }
                                                viewModel.deleteTx(item.tx) {}
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            state = state,
            onDismiss = { showFilterSheet = false },
            onAccount = viewModel::setAccount,
            onCategory = viewModel::setCategory,
            onDateRange = viewModel::setDateRange,
            onReset = viewModel::clearFilters,
        )
    }
}

@Composable
private fun DayHeader(group: DayGroup, baseCurrencyCode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(group.label, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.weight(1f))
        if (group.incomeBase > 0) {
            Text(
                "+${formatMoney(group.incomeBase, Currencies.byCode(baseCurrencyCode))}",
                style = MaterialTheme.typography.bodySmall,
                color = IncomeGreen,
            )
            Spacer(Modifier.size(8.dp))
        }
        if (group.expenseBase > 0) {
            Text(
                "-${formatMoney(group.expenseBase, Currencies.byCode(baseCurrencyCode))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    state: TxnsUiState,
    onDismiss: () -> Unit,
    onAccount: (Long?) -> Unit,
    onCategory: (Long?) -> Unit,
    onDateRange: (Long?, Long?) -> Unit,
    onReset: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Filters", style = MaterialTheme.typography.titleLarge)

            Text("Account", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.filter.accountId == null,
                    onClick = { onAccount(null) },
                    label = { Text("All") },
                )
                state.accounts.forEach { account ->
                    FilterChip(
                        selected = state.filter.accountId == account.id,
                        onClick = { onAccount(account.id) },
                        label = { Text("${account.emoji} ${account.name}") },
                    )
                }
            }

            Text("Category", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.filter.categoryId == null,
                    onClick = { onCategory(null) },
                    label = { Text("All") },
                )
                state.categories.forEach { category ->
                    FilterChip(
                        selected = state.filter.categoryId == category.id,
                        onClick = { onCategory(category.id) },
                        label = { Text("${category.emoji} ${category.name}") },
                    )
                }
            }

            Text("Date range", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val presets = listOf(
                    "All time" to (null to null),
                    "This month" to Pair(Periods.currentMonth().startMillis, Periods.currentMonth().endMillis),
                    "Last 30 days" to Pair(Periods.lastNDays(30).startMillis, Periods.lastNDays(30).endMillis),
                )
                presets.forEach { (label, range) ->
                    FilterChip(
                        selected = state.filter.startDate == range.first && (range.second == null || state.filter.endDate == range.second),
                        onClick = { onDateRange(range.first, range.second) },
                        label = { Text(label) },
                    )
                }
                FilterChip(
                    selected = false,
                    onClick = { pickingStart = true },
                    label = {
                        Text(
                            if (state.filter.startDate != null) {
                                "From ${Fmt.dateShort(state.filter.startDate!!)}"
                            } else {
                                "Custom from…"
                            },
                        )
                    },
                )
                if (state.filter.startDate != null) {
                    FilterChip(
                        selected = false,
                        onClick = { pickingEnd = true },
                        label = {
                            Text(
                                if (state.filter.endDate != null) {
                                    "Until ${Fmt.dateShort(state.filter.endDate!! - 1)}"
                                } else {
                                    "Custom until…"
                                },
                            )
                        },
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Reset") }
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Done") }
            }
        }
    }

    if (pickingStart) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.filter.startDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { pickingStart = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateRange(Fmt.utcDayStartToLocalMillis(it), state.filter.endDate)
                    }
                    pickingStart = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingStart = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }
    if (pickingEnd) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.filter.endDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { pickingEnd = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateRange(state.filter.startDate, Fmt.utcDayStartToLocalMillis(it) + 86_400_000L)
                    }
                    pickingEnd = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingEnd = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }
}
