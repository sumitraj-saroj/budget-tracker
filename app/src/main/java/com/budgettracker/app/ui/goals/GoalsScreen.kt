package com.budgettracker.app.ui.goals

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.data.db.GoalEntity
import com.budgettracker.app.ui.components.AmountText
import com.budgettracker.app.ui.components.ConfirmDialog
import com.budgettracker.app.ui.components.EmojiBadge
import com.budgettracker.app.ui.components.EmojiPickerDialog
import com.budgettracker.app.ui.components.EmptyState
import com.budgettracker.app.ui.components.IncomeGreen
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.formatMoney
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

private val GoalColorPalette = listOf(
    0xFF10B981, 0xFF3B82F6, 0xFF8B5CF6, 0xFFF97316, 0xFFEC4899, 0xFF14B8A6,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<GoalEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var contributing by remember { mutableStateOf<GoalEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Savings goals") },
                actions = {
                    IconButton(onClick = { creating = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "New goal")
                    }
                },
            )
        },
    ) { padding ->
        if (state.goals.isEmpty() && !state.loading) {
            Column(Modifier.padding(padding)) {
                EmptyState(
                    icon = Icons.Rounded.Flag,
                    title = "No goals yet",
                    message = "Saving for a trip, a laptop, or an emergency fund? Set a goal and watch it grow.",
                    ctaLabel = "New goal",
                    onCta = { creating = true },
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        baseCurrencyCode = state.baseCurrency,
                        onContribute = { contributing = goal },
                        onEdit = { editing = goal },
                    )
                }
            }
        }
    }

    if (creating || editing != null) {
        GoalEditDialog(
            goal = editing,
            baseCurrencyCode = state.baseCurrency,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = { id, name, emoji, color, targetText, targetDate, note ->
                viewModel.saveGoal(id, name, emoji, color, targetText, targetDate, note) {
                    creating = false
                    editing = null
                }
            },
            onDelete = { goal ->
                viewModel.delete(goal)
                creating = false
                editing = null
            },
        )
    }

    if (contributing != null) {
        ContributeDialog(
            goal = contributing!!,
            baseCurrencyCode = state.baseCurrency,
            onDismiss = { contributing = null },
            onContribute = { amountText ->
                viewModel.contribute(contributing!!, amountText) { contributing = null }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalCard(
    goal: GoalEntity,
    baseCurrencyCode: String,
    onContribute: () -> Unit,
    onEdit: () -> Unit,
) {
    val fraction = if (goal.targetMinor <= 0) 0f else (goal.savedMinor.toFloat() / goal.targetMinor).coerceIn(0f, 1f)
    val complete = goal.savedMinor >= goal.targetMinor && goal.targetMinor > 0
    val currency = Currencies.byCode(baseCurrencyCode)

    Card(
        onClick = onEdit,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBadge(goal.emoji, Color(goal.colorArgb.toInt()))
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(goal.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (goal.targetDate != null) {
                        val daysLeft = ChronoUnit.DAYS.between(
                            java.time.LocalDate.now(),
                            Fmt.toLocalDate(goal.targetDate!!),
                        )
                        Text(
                            when {
                                complete -> "Goal reached!"
                                daysLeft < 0 -> "Past target date"
                                else -> "${Fmt.dateShort(goal.targetDate!!)} · $daysLeft day${if (daysLeft == 1L) "" else "s"} left"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (daysLeft < 0 && !complete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (complete) {
                    Text("🎉", fontSize = 22.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(goal.colorArgb.toInt()),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatMoney(goal.savedMinor, currency)} of ${formatMoney(goal.targetMinor, currency)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${(fraction * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen,
                )
            }
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = onContribute, modifier = Modifier.fillMaxWidth()) {
                Text(if (complete) "Add more" else "Add money")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditDialog(
    goal: GoalEntity?,
    baseCurrencyCode: String,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, emoji: String, colorArgb: Long, targetText: String, targetDate: Long?, note: String) -> Unit,
    onDelete: (GoalEntity) -> Unit,
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var emoji by remember { mutableStateOf(goal?.emoji ?: "🎯") }
    var color by remember { mutableStateOf(goal?.colorArgb ?: GoalColorPalette.first()) }
    var targetText by remember {
        mutableStateOf(
            goal?.targetMinor?.let {
                java.math.BigDecimal(it).movePointLeft(Currencies.byCode(baseCurrencyCode).minorDigits).stripTrailingZeros().toPlainString()
            } ?: "",
        )
    }
    var targetDate by remember { mutableStateOf(goal?.targetDate) }
    var note by remember { mutableStateOf(goal?.note ?: "") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "New goal" else "Edit goal") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it.take(30) }, label = { Text("Name") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EmojiBadge(emoji, Color(color.toInt()), size = 40.dp, fontSize = 17)
                    Spacer(Modifier.size(12.dp))
                    TextButton(onClick = { showEmojiPicker = true }) { Text("Icon: $emoji") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalColorPalette.forEach { c ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(c.toInt()), CircleShape)
                                .clickable { color = c },
                        )
                    }
                }
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                    label = { Text("Target amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(targetDate?.let { "By ${Fmt.date(it)}" } ?: "Set target date (optional)")
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
                onClick = { if (name.isNotBlank() && targetText.isNotBlank()) onSave(goal?.id ?: 0, name, emoji, color, targetText, targetDate, note) },
                enabled = name.isNotBlank() && targetText.isNotBlank(),
            ) { Text(if (goal == null) "Create" else "Save") }
        },
        dismissButton = {
            if (goal != null) {
                TextButton(onClick = { confirmDelete = true }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )

    if (showEmojiPicker) {
        EmojiPickerDialog(onDismiss = { showEmojiPicker = false }, onPick = { emoji = it })
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = targetDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    targetDate = datePickerState.selectedDateMillis?.let { Fmt.utcDayStartToLocalMillis(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    targetDate = null
                    showDatePicker = false
                }) { Text("Clear") }
            },
        ) { DatePicker(state = datePickerState) }
    }
    if (confirmDelete && goal != null) {
        ConfirmDialog(
            title = "Delete goal?",
            message = "\"${goal.name}\" will be removed.",
            onConfirm = { onDelete(goal) },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun ContributeDialog(
    goal: GoalEntity,
    baseCurrencyCode: String,
    onDismiss: () -> Unit,
    onContribute: (String) -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    val currency = Currencies.byCode(baseCurrencyCode)

    // Quick-amount presets in minor units (e.g. ₹100 / ₹500 / ₹1,000).
    val scale = Math.pow(10.0, currency.minorDigits.toDouble()).toLong()
    val presets = listOf(100L * scale, 500L * scale, 1000L * scale)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to \"${goal.name}\"") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { preset ->
                        FilledTonalButton(
                            onClick = {
                                amountText = java.math.BigDecimal(preset)
                                    .movePointLeft(currency.minorDigits)
                                    .stripTrailingZeros()
                                    .toPlainString()
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                formatMoney(preset, currency, showSymbol = false),
                                maxLines = 1,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                    label = { Text("Amount (${currency.code})") },
                    prefix = { Text(currency.symbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                )
                Text(
                    "Currently saved: ${formatMoney(goal.savedMinor, currency)} of ${formatMoney(goal.targetMinor, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onContribute(amountText) },
                enabled = amountText.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
