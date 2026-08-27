package com.budgettracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.formatMoney

val EmojiChoices = listOf(
    "🍔", "🛒", "🚌", "🏠", "💡", "🛍️", "🎬", "💊", "📚", "✈️", "📺", "🧴",
    "🎁", "🏦", "🐾", "❓", "💼", "💻", "🏪", "📈", "↩️", "➕", "🎯", "💰",
    "💵", "💳", "🏦", "🪙", "💸", "🧾", "☕", "🍜", "🍕", "🍺", "🎂", "🎮",
    "⚽", "🚗", "⛽", "🛠️", "📱", "🎧", "👕", "💇", "🩺", "🦷", "👶", "🏝️",
    "🎸", "🎨", "📚", "✂️", "🧹", "🧺", "🚿", "🔌", "🧯", "🔒", "📅", "⏰",
)

@Composable
fun EmojiPickerDialog(
    title: String = "Choose an icon",
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(EmojiChoices) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                onPick(emoji)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(emoji, fontSize = 22.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun ColorPaletteRow(
    colors: List<Color>,
    selected: Color,
    onPick: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(color, CircleShape)
                    .border(
                        width = if (selected == color) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape,
                    )
                    .clickable { onPick(color) },
            )
        }
    }
}

/** Searchable currency picker with a live preview amount. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerDialog(
    title: String = "Base currency",
    selectedCode: String? = null,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val currencies = Currencies.all.filter {
        query.isBlank() ||
            it.code.contains(query, ignoreCase = true) ||
            it.name.contains(query, ignoreCase = true)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(currencies) { currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPick(currency.code)
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("${currency.code} · ${currency.symbol}", style = MaterialTheme.typography.bodyLarge)
                                Text(currency.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = formatMoney(123456L, currency),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (currency.code == selectedCode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/** Simple styled surface used as section container on list screens. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(content = content)
    }
}
