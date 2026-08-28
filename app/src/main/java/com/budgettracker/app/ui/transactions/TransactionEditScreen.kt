package com.budgettracker.app.ui.transactions

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.data.db.CategoryType
import com.budgettracker.app.data.db.TxType
import com.budgettracker.app.ui.components.ColorPaletteRow
import com.budgettracker.app.ui.components.ConfirmDialog
import com.budgettracker.app.ui.components.DateFieldButton
import com.budgettracker.app.ui.components.EmojiPickerDialog
import com.budgettracker.app.ui.components.EmptyState
import com.budgettracker.app.ui.components.NumberKeypad
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.convertMinor
import com.budgettracker.app.util.formatMoney
import com.budgettracker.app.util.hapticKey
import com.budgettracker.app.util.parseAmountMinor

private val CategoryColorPalette = listOf(
    0xFFF97316, 0xFF22C55E, 0xFF3B82F6, 0xFF8B5CF6, 0xFFEC4899,
    0xFF06B6D4, 0xFFEF4444, 0xFFEAB308, 0xFF14B8A6, 0xFF64748B,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditScreen(
    txId: Long,
    onDone: () -> Unit,
    viewModel: TransactionEditViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val view = androidx.compose.ui.platform.LocalView.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showNewCategory by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                title = { Text(if (state.isNew) "Add transaction" else "Edit transaction") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                        }
                    }
                    TextButton(
                        onClick = {
                            view.hapticKey()
                            viewModel.save(onDone)
                        },
                        enabled = state.loaded,
                    ) { Text("Save") }
                },
            )
        },
    ) { padding ->
        if (!state.loaded || state.accounts.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState(
                    icon = Icons.Rounded.Add,
                    title = if (state.loaded) "No account yet" else "Loading…",
                    message = if (state.loaded) "Create an account first from the More tab." else "Just a moment.",
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Spacer(Modifier.height(4.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(
                            "Expense" to TxType.EXPENSE,
                            "Income" to TxType.INCOME,
                            "Transfer" to TxType.TRANSFER,
                        )
                        options.forEachIndexed { index, (label, type) ->
                            SegmentedButton(
                                selected = state.type == type,
                                onClick = { viewModel.setType(type) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            ) { Text(label) }
                        }
                    }

                    // Amount display (keypad is the input)
                    Column {
                        Text(
                            "Amount · ${state.currency.code}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                state.currency.symbol,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                state.amountText.ifEmpty { "0" },
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (state.amountError) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            BlinkingCaret()
                        }
                        if (state.amountError) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Enter a valid amount greater than zero",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    if (state.type == TxType.TRANSFER) {
                        val toAccount = state.toAccount
                        if (toAccount != null && toAccount.currencyCode != state.currency.code) {
                            val amount = parseAmountMinor(state.amountText, state.currency)
                            if (amount != null) {
                                val converted = convertMinor(amount, state.currency, toAccount.currency)
                                Text(
                                    "Receives ≈ ${formatMoney(converted, toAccount.currency)} in ${toAccount.currency.code}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Text(
                        if (state.type == TxType.TRANSFER) "From account" else "Account",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    AccountChips(
                        accounts = state.accounts,
                        selectedId = state.accountId,
                        onSelect = viewModel::setAccount,
                    )

                    if (state.type == TxType.TRANSFER) {
                        Text("To account", style = MaterialTheme.typography.titleSmall)
                        AccountChips(
                            accounts = state.accounts,
                            selectedId = state.toAccountId,
                            onSelect = viewModel::setToAccount,
                        )
                    }

                    if (state.type != TxType.TRANSFER) {
                        Text("Category", style = MaterialTheme.typography.titleSmall)
                        CategoryChips(
                            state = state,
                            onSelect = viewModel::setCategory,
                            onNewCategory = { showNewCategory = true },
                        )
                    }

                    DateFieldButton(label = "Date", value = state.dateMillis, onClick = { showDatePicker = true })

                    OutlinedTextField(
                        value = state.note,
                        onValueChange = viewModel::setNote,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Note (optional)") },
                        placeholder = { Text("e.g. Lunch with friends") },
                    )

                    Spacer(Modifier.height(8.dp))
                }

                NumberKeypad(
                    onKey = viewModel::onKeypad,
                    showDecimal = state.currency.minorDigits > 0,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp),
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setDate(Fmt.utcDayStartToLocalMillis(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    if (showNewCategory) {
        NewCategoryDialog(
            isIncome = state.type == TxType.INCOME,
            onDismiss = { showNewCategory = false },
            onCreate = { name, emoji, color ->
                viewModel.createCategory(name, emoji, color)
                showNewCategory = false
            },
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete transaction?",
            message = "This cannot be undone.",
            onConfirm = { viewModel.delete(onDone) },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@Composable
private fun BlinkingCaret() {
    val transition = rememberInfiniteTransition(label = "caret")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "caretAlpha",
    )
    Box(
        modifier = Modifier
            .padding(start = 3.dp)
            .width(2.dp)
            .height(32.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)),
    )
}

@Composable
private fun AccountChips(
    accounts: List<com.budgettracker.app.data.db.AccountEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        accounts.take(4).forEach { account ->
            FilterChip(
                selected = selectedId == account.id,
                onClick = { onSelect(account.id) },
                label = { Text("${account.emoji} ${account.name}", maxLines = 1) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    if (accounts.size > 4) {
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            accounts.drop(4).take(4).forEach { account ->
                FilterChip(
                    selected = selectedId == account.id,
                    onClick = { onSelect(account.id) },
                    label = { Text("${account.emoji} ${account.name}", maxLines = 1) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    state: TxEditState,
    onSelect: (Long?) -> Unit,
    onNewCategory: () -> Unit,
) {
    val wantedType = if (state.type == TxType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
    val categories = state.categories.filter { it.type == wantedType }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = state.categoryId == category.id,
                    onClick = { onSelect(category.id) },
                    label = { Text("${category.emoji} ${category.name}") },
                )
            }
        }
        TextButton(onClick = onNewCategory) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("New category")
        }
    }
}

@Composable
private fun NewCategoryDialog(
    isIncome: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, Long) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(if (isIncome) "➕" else "🏷️") }
    var color by remember { mutableStateOf(CategoryColorPalette.first()) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(24) },
                    label = { Text("Name") },
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color(color.toInt()),
                        ),
                        modifier = Modifier.size(44.dp),
                    ) {}
                    Spacer(Modifier.width(12.dp))
                    TextButton(onClick = { showEmojiPicker = true }) { Text("Choose icon: $emoji") }
                }
                ColorPaletteRow(
                    colors = CategoryColorPalette.map { androidx.compose.ui.graphics.Color(it.toInt()) },
                    selected = androidx.compose.ui.graphics.Color(color.toInt()),
                    onPick = { color = it.value.toLong() and 0xFFFFFFFFL },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name, emoji, color) },
                enabled = name.isNotBlank(),
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showEmojiPicker) {
        EmojiPickerDialog(onDismiss = { showEmojiPicker = false }, onPick = { emoji = it })
    }
}
