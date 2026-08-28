package com.budgettracker.app.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.data.AccountWithBalance
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.AccountType
import com.budgettracker.app.ui.components.AmountText
import com.budgettracker.app.ui.components.ConfirmDialog
import com.budgettracker.app.ui.components.CurrencyPickerDialog
import com.budgettracker.app.ui.components.EmojiBadge
import com.budgettracker.app.ui.components.EmojiPickerDialog
import com.budgettracker.app.ui.components.EmptyState
import com.budgettracker.app.ui.components.ListPickerDialog
import com.budgettracker.app.ui.components.PickerItem
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.convertMinor
import com.budgettracker.app.util.formatMoney

private val AccountColorPalette = listOf(
    0xFF22C55E, 0xFF3B82F6, 0xFF8B5CF6, 0xFFF97316, 0xFFEC4899,
    0xFF06B6D4, 0xFFEAB308, 0xFF14B8A6, 0xFF64748B, 0xFFEF4444,
)

private fun AccountType.label(): String = when (this) {
    AccountType.CASH -> "Cash"
    AccountType.BANK -> "Bank"
    AccountType.CARD -> "Card"
    AccountType.SAVINGS -> "Savings"
    AccountType.OTHER -> "Other"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: AccountsViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<AccountEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<AccountEntity?>(null) }
    var deleteBlockedName by remember { mutableStateOf<String?>(null) }

    val base = Currencies.byCode(state.baseCurrency)
    val active = state.accounts.filter { !it.account.isArchived }
    val archived = state.accounts.filter { it.account.isArchived }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Accounts") },
                navigationIcon = {
                    IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { creating = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add account")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (state.accounts.isEmpty() && !state.loading) {
                EmptyState(
                    icon = Icons.Rounded.Wallet,
                    title = "No accounts yet",
                    message = "Add a cash wallet, bank account, or card to start tracking.",
                    ctaLabel = "Add account",
                    onCta = { creating = true },
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    "Total balance",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    formatMoney(state.totalBaseMinor, base),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                Text(
                                    "in ${base.code} · ${active.size} active account${if (active.size == 1) "" else "s"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }

                    items(active, key = { it.account.id }) { item ->
                        AccountRow(item, baseCurrencyCode = state.baseCurrency, onClick = { editing = item.account })
                    }

                    if (archived.isNotEmpty()) {
                        item {
                            Text(
                                "Archived",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                        items(archived, key = { it.account.id }) { item ->
                            AccountRow(item, baseCurrencyCode = state.baseCurrency, onClick = { editing = item.account })
                        }
                    }
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }

    if (creating || editing != null) {
        AccountEditDialog(
            account = editing,
            baseCurrencyCode = state.baseCurrency,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = { id, name, emoji, color, type, currencyCode, startingBalance, isArchived, includeInTotals ->
                viewModel.saveAccount(
                    id = id,
                    name = name,
                    emoji = emoji,
                    colorArgb = color,
                    type = type,
                    currencyCode = currencyCode,
                    startingBalanceText = startingBalance,
                    isArchived = isArchived,
                    includeInTotals = includeInTotals,
                    onSaved = {
                        creating = false
                        editing = null
                    },
                )
            },
            onDelete = { account ->
                viewModel.deleteAccount(account) { deleted ->
                    if (deleted) {
                        editing = null
                        creating = false
                    } else {
                        deleteBlockedName = account.name
                        editing = null
                    }
                }
            },
        )
    }

    if (deleteBlockedName != null) {
        AlertDialog(
            onDismissRequest = { deleteBlockedName = null },
            title = { Text("Can't delete account") },
            text = { Text("\"$deleteBlockedName\" has transactions. Archive it instead to keep your history.") },
            confirmButton = { TextButton(onClick = { deleteBlockedName = null }) { Text("OK") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountRow(item: AccountWithBalance, baseCurrencyCode: String, onClick: () -> Unit) {
    val account = item.account
    val base = Currencies.byCode(baseCurrencyCode)
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmojiBadge(account.emoji, Color(account.colorArgb.toInt()))
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    account.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    buildString {
                        append(account.type.label())
                        if (account.currencyCode != baseCurrencyCode) append(" · ${account.currencyCode}")
                        if (account.isArchived) append(" · Archived")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    amountMinor = item.balanceMinor,
                    currencyCode = account.currencyCode,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (account.currencyCode != baseCurrencyCode && !account.isArchived && account.includeInTotals) {
                    Text(
                        "≈ ${formatMoney(convertMinor(item.balanceMinor, account.currency, base), base)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountEditDialog(
    account: AccountEntity?,
    baseCurrencyCode: String,
    onDismiss: () -> Unit,
    onSave: (
        id: Long, name: String, emoji: String, colorArgb: Long, type: AccountType,
        currencyCode: String, startingBalanceText: String, isArchived: Boolean, includeInTotals: Boolean,
    ) -> Unit,
    onDelete: (AccountEntity) -> Unit,
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var emoji by remember { mutableStateOf(account?.emoji ?: "💳") }
    var color by remember { mutableStateOf(account?.colorArgb ?: AccountColorPalette.first()) }
    var type by remember { mutableStateOf(account?.type ?: AccountType.BANK) }
    var currencyCode by remember { mutableStateOf(account?.currencyCode ?: baseCurrencyCode) }
    var startingBalance by remember {
        mutableStateOf(
            account?.startingBalanceMinor?.let {
                java.math.BigDecimal(it).movePointLeft(Currencies.byCode(account.currencyCode).minorDigits).stripTrailingZeros().toPlainString()
            } ?: "",
        )
    }
    var isArchived by remember { mutableStateOf(account?.isArchived ?: false) }
    var includeInTotals by remember { mutableStateOf(account?.includeInTotals ?: true) }

    var showEmojiPicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showTypePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "New account" else "Edit account") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(24) },
                    label = { Text("Name") },
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiBadge(emoji, Color(color.toInt()), size = 40.dp, fontSize = 17)
                    Spacer(Modifier.size(12.dp))
                    TextButton(onClick = { showEmojiPicker = true }) { Text("Icon: $emoji") }
                    Spacer(Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountColorPalette.forEach { c ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(c.toInt()), CircleShape)
                                .clickable { color = c },
                        )
                    }
                }
                Button(onClick = { showTypePicker = true }) { Text("Type: ${type.label()}") }
                Button(onClick = { showCurrencyPicker = true }) {
                    Text("Currency: ${currencyCode} ${Currencies.byCode(currencyCode).symbol}")
                }
                OutlinedTextField(
                    value = startingBalance,
                    onValueChange = { startingBalance = it.filter { c -> c.isDigit() || c == '.' || c == '-' }.take(14) },
                    label = { Text("Starting balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Include in total balance", Modifier.weight(1f))
                    Switch(checked = includeInTotals, onCheckedChange = { includeInTotals = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Archived", Modifier.weight(1f))
                    Switch(checked = isArchived, onCheckedChange = { isArchived = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(account?.id ?: 0, name, emoji, color, type, currencyCode, startingBalance, isArchived, includeInTotals)
                    }
                },
                enabled = name.isNotBlank(),
            ) { Text(if (account == null) "Create" else "Save") }
        },
        dismissButton = {
            if (account != null) {
                TextButton(onClick = { confirmDelete = true }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )

    if (showEmojiPicker) {
        EmojiPickerDialog(onDismiss = { showEmojiPicker = false }, onPick = { emoji = it })
    }
    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            title = "Account currency",
            selectedCode = currencyCode,
            onDismiss = { showCurrencyPicker = false },
            onPick = { currencyCode = it },
        )
    }
    if (showTypePicker) {
        ListPickerDialog(
            title = "Account type",
            items = AccountType.entries.map { PickerItem(key = it.ordinal.toLong(), label = it.label()) },
            onDismiss = { showTypePicker = false },
            onSelect = { item ->
                type = AccountType.entries[item.key.toInt()]
                showTypePicker = false
            },
        )
    }
    if (confirmDelete && account != null) {
        ConfirmDialog(
            title = "Delete account?",
            message = "\"${account.name}\" will be removed. This only works when the account has no transactions.",
            onConfirm = { onDelete(account) },
            onDismiss = { confirmDelete = false },
        )
    }
}
