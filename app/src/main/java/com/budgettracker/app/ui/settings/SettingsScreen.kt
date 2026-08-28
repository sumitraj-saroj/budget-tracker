package com.budgettracker.app.ui.settings

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.security.BiometricAvailability
import com.budgettracker.app.security.BiometricLock
import com.budgettracker.app.ui.components.CurrencyPickerDialog
import com.budgettracker.app.ui.theme.AccentPresets
import com.budgettracker.app.ui.theme.ThemeMode
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val snackbarHostState = remember { SnackbarHostState() }

    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showCustomColor by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let { viewModel.exportBackup(context, it) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.importBackup(context, it) }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.setRemindersEnabled(granted)
        if (!granted) viewModel.showMessage("Notification permission denied — reminders stay off")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("Settings") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ---- Profile / Google ----
            SettingsCard(title = "Profile") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        val initial = (prefs.googleName ?: prefs.displayName).trim().take(1).ifBlank { "?" }.uppercase()
                        Text(initial, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            prefs.googleName ?: prefs.displayName.ifBlank { "Local profile" },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            prefs.googleEmail ?: "Not signed in",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (prefs.googleEmail == null) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            (context as? Activity)?.let { viewModel.signInWithGoogle(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !busy,
                    ) {
                        Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.size(8.dp))
                        Text("Continue with Google")
                    }
                    if (!viewModel.googleConfigured) {
                        Text(
                            "Google Sign-In needs a Web client ID — see README.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            (context as? Activity)?.let { viewModel.signOut(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Sign out")
                    }
                }
            }

            // ---- Appearance ----
            SettingsCard(title = "Appearance") {
                Text("Theme", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "System" to ThemeMode.SYSTEM,
                        "Light" to ThemeMode.LIGHT,
                        "Dark" to ThemeMode.DARK,
                    ).forEach { (label, mode) ->
                        androidx.compose.material3.FilterChip(
                            selected = prefs.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Dynamic color", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Use wallpaper colors (Android 12+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = prefs.useDynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) },
                        enabled = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("True black (OLED)", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Pure black backgrounds in dark theme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = prefs.amoledBlack,
                        onCheckedChange = { viewModel.setAmoledBlack(it) },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Liquid glass", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Translucent blur on floating bars and headers (Android 12+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = prefs.liquidGlass,
                        onCheckedChange = { viewModel.setLiquidGlass(it) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                val accentActive = !prefs.useDynamicColor
                Column(Modifier.alpha(if (accentActive) 1f else 0.35f)) {
                    Text("Accent color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    AccentGrid(
                        selected = Color(prefs.accentArgb.toInt()),
                        onSelect = { if (accentActive) viewModel.setAccent(it.toArgb().toLong() and 0xFFFFFFFFL) },
                        onCustom = { if (accentActive) showCustomColor = true },
                    )
                }
                if (!accentActive) {
                    Text(
                        "Dynamic color is on — the wallpaper drives app colors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ---- Preferences ----
            SettingsCard(title = "Preferences") {
                var name by remember(prefs.displayName) { mutableStateOf(prefs.displayName) }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(30)
                        viewModel.setDisplayName(it.take(30))
                    },
                    label = { Text("Display name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCurrencyPicker = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Base currency", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "All totals are converted to this currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        "${prefs.baseCurrency} ${Currencies.byCode(prefs.baseCurrency).symbol}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }

            // ---- Reminders ----
            SettingsCard(title = "Reminders") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Enable reminders", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Budget and due-date alerts, even when the app is closed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = prefs.remindersEnabled,
                        onCheckedChange = { enabled ->
                            when {
                                !enabled -> viewModel.setRemindersEnabled(false)
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ->
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                else -> viewModel.setRemindersEnabled(true)
                            }
                        },
                    )
                }
                if (prefs.remindersEnabled) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Budget alerts", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Notify at 80% and 100% of each budget",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = prefs.budgetAlerts,
                            onCheckedChange = { viewModel.setBudgetAlerts(it) },
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Due-date reminders", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Upcoming subscriptions and debts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = prefs.dueReminders,
                            onCheckedChange = { viewModel.setDueReminders(it) },
                        )
                    }
                    if (prefs.dueReminders) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Remind me before",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1 to "1 day", 3 to "3 days", 7 to "1 week").forEach { (days, label) ->
                                FilterChip(
                                    selected = prefs.dueDaysAhead == days,
                                    onClick = { viewModel.setDueDaysAhead(days) },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }
                }
            }

            // ---- Security ----
            SettingsCard(title = "Security") {
                val availability = remember { BiometricLock.availability(context) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Biometric lock", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            when (availability) {
                                BiometricAvailability.AVAILABLE -> "Lock the app with fingerprint or face"
                                BiometricAvailability.NONE_ENROLLED -> "Add a fingerprint in system settings first"
                                else -> "Not available on this device"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = prefs.biometricLock,
                        enabled = availability == BiometricAvailability.AVAILABLE,
                        onCheckedChange = { enabled ->
                            if (enabled && activity != null) {
                                BiometricLock.authenticate(
                                    activity = activity,
                                    title = "Enable biometric lock",
                                    subtitle = "Confirm to protect your data",
                                    onSuccess = { viewModel.setBiometricLock(true) },
                                    onError = { viewModel.setBiometricLock(false) },
                                )
                            } else {
                                viewModel.setBiometricLock(false)
                            }
                        },
                    )
                }
            }

            // ---- Data ----
            SettingsCard(title = "Data") {
                Text(
                    "Your data is stored locally on this device. Export a backup file regularly and keep it somewhere safe (Drive, email, PC).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
                        exportLauncher.launch("budget-tracker-backup-$stamp.json")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                ) {
                    Icon(Icons.Rounded.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Export backup")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showImportConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                ) { Text("Import backup") }
                prefs.lastBackupAt?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Last backup: ${Fmt.date(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ---- About ----
            SettingsCard(title = "About") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("Budget Tracker", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "v1.0.0 · Offline-first · Made with Jetpack Compose",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selectedCode = prefs.baseCurrency,
            onDismiss = { showCurrencyPicker = false },
            onPick = {
                viewModel.setBaseCurrency(it)
                showCurrencyPicker = false
            },
        )
    }

    if (showCustomColor) {
        CustomColorDialog(
            initial = Color(prefs.accentArgb.toInt()),
            onDismiss = { showCustomColor = false },
            onPick = {
                viewModel.setAccent(it.toArgb().toLong() and 0xFFFFFFFFL)
                showCustomColor = false
            },
        )
    }

    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text("Import backup?") },
            text = { Text("This replaces all current data with the contents of the backup file. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    importLauncher.launch(arrayOf("application/json"))
                }) { Text("Import", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showImportConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}

@Composable
private fun AccentGrid(
    selected: Color,
    onSelect: (Color) -> Unit,
    onCustom: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AccentPresets.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (selected == color) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape,
                            )
                            .clickable { onSelect(color) },
                    )
                }
                // pad last row
                repeat(6 - row.size) { Spacer(Modifier.size(38.dp)) }
            }
        }
        TextButton(onClick = onCustom) { Text("Custom color…") }
    }
}

@Composable
private fun CustomColorDialog(
    initial: Color,
    onDismiss: () -> Unit,
    onPick: (Color) -> Unit,
) {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(
        android.graphics.Color.argb(
            255,
            (initial.red * 255).toInt(),
            (initial.green * 255).toInt(),
            (initial.blue * 255).toInt(),
        ),
        hsv,
    )
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2].coerceIn(0.35f, 1f)) }
    val current = Color.hsv(hue, saturation, value.coerceAtLeast(0.35f))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom accent color") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(current, CircleShape)
                        .clip(CircleShape),
                )
                LabeledSlider(label = "Hue", value = hue, range = 0f..360f, onChange = { hue = it }, display = { "${it.toInt()}°" })
                LabeledSlider(label = "Saturation", value = saturation, range = 0.1f..1f, onChange = { saturation = it })
                LabeledSlider(label = "Brightness", value = value, range = 0.35f..1f, onChange = { value = it })
            }
        },
        confirmButton = { TextButton(onClick = { onPick(current) }) { Text("Use this color") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    display: (Float) -> String = { "${(it * 100).toInt()}" },
) {
    Column {
        Text(
            "$label: ${display(value)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
