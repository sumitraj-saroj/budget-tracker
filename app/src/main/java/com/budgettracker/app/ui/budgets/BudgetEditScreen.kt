package com.budgettracker.app.ui.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.ui.components.ConfirmDialog
import com.budgettracker.app.ui.components.DateFieldButton
import com.budgettracker.app.util.BudgetPeriodType
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.Periods

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditScreen(
    budgetId: Long,
    onDone: () -> Unit,
    viewModel: BudgetEditViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val currency = Currencies.byCode(state.baseCurrency)

    val window = Periods.budgetWindow(
        periodType = state.periodType,
        customStart = if (state.periodType == BudgetPeriodType.CUSTOM) state.customStart else null,
        customLengthDays = state.customLengthText.toIntOrNull(),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "New budget" else "Edit budget") },
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
                    TextButton(onClick = { viewModel.save(onDone) }) { Text("Save") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Budget name") },
                placeholder = { Text("e.g. Groceries, Fun money") },
                singleLine = true,
            )

            OutlinedTextField(
                value = state.amountText,
                onValueChange = viewModel::setAmount,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount (${currency.code} ${currency.symbol})") },
                isError = state.amountError,
                supportingText = if (state.amountError) {
                    { Text("Enter a valid amount greater than zero") }
                } else {
                    null
                },
                prefix = { Text(currency.symbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )

            Text("Budget period", style = MaterialTheme.typography.titleSmall)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(
                    "Weekly" to BudgetPeriodType.WEEKLY,
                    "Monthly" to BudgetPeriodType.MONTHLY,
                    "Custom" to BudgetPeriodType.CUSTOM,
                )
                options.forEachIndexed { index, (label, type) ->
                    SegmentedButton(
                        selected = state.periodType == type,
                        onClick = { viewModel.setPeriod(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    ) { Text(label) }
                }
            }

            if (state.periodType == BudgetPeriodType.CUSTOM) {
                DateFieldButton(
                    label = "Period starts on",
                    value = state.customStart,
                    onClick = { showDatePicker = true },
                )
                OutlinedTextField(
                    value = state.customLengthText,
                    onValueChange = viewModel::setCustomLength,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Period length (days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Current period", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(
                        "${Fmt.date(window.startMillis)} – ${Fmt.date(window.endMillis - 86_400_000L)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            CategoryPicker(state = state, viewModel = viewModel)

            Button(
                onClick = { viewModel.save(onDone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text(if (state.isNew) "Create budget" else "Save changes") }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.customStart)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setCustomStart(Fmt.utcDayStartToLocalMillis(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete budget?",
            message = "\"${state.name}\" will be removed. Your transactions stay untouched.",
            onConfirm = { viewModel.delete(onDone) },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPicker(state: BudgetEditState, viewModel: BudgetEditViewModel) {
    Column {
        Row {
            Text("Categories", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = viewModel::clearCategories) { Text("All categories") }
        }
        Text(
            if (state.selectedCategoryIds.isEmpty()) {
                "This budget covers all expense categories."
            } else {
                "${state.selectedCategoryIds.size} selected"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.categories.forEach { category ->
                FilterChip(
                    selected = category.id in state.selectedCategoryIds,
                    onClick = { viewModel.toggleCategory(category.id) },
                    label = { Text("${category.emoji} ${category.name}") },
                )
            }
        }
    }
}
