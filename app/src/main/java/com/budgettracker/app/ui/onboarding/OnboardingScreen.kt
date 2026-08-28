package com.budgettracker.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.ui.components.ColorPaletteRow
import com.budgettracker.app.ui.components.CurrencyPickerDialog
import com.budgettracker.app.ui.theme.AccentPresets
import com.budgettracker.app.ui.theme.ThemeMode
import com.budgettracker.app.util.Currencies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefsRepository: PrefsRepository,
    private val financeRepository: FinanceRepository,
) : ViewModel() {

    fun complete(name: String, currency: String, themeMode: ThemeMode, accentArgb: Long) {
        viewModelScope.launch {
            prefsRepository.setDisplayName(name.trim())
            prefsRepository.setBaseCurrency(currency)
            prefsRepository.setThemeMode(themeMode)
            prefsRepository.setAccent(accentArgb)
            financeRepository.ensureDefaultData(currency)
            prefsRepository.setOnboardingDone(true)
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel()) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var accent by remember { mutableStateOf(AccentPresets.first()) }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(48.dp))
            Text("💸", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = when (step) {
                    0 -> "Welcome to Budget Tracker"
                    1 -> "Choose your currency"
                    else -> "Make it yours"
                },
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (step) {
                    0 -> "Track spending, budgets, subscriptions, and savings goals — all in one place."
                    1 -> "All totals are shown in this currency. You can add accounts in other currencies later."
                    else -> "Pick a look you love. You can change this anytime in settings."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { it / 3 } + fadeOut())
                    }
                },
                label = "onboarding",
            ) { currentStep ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (currentStep) {
                        0 -> OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your name") },
                            placeholder = { Text("e.g. Sumit") },
                            singleLine = true,
                        )
                        1 -> {
                            OutlinedButton(
                                onClick = { showCurrencyPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    "${Currencies.byCode(currency).code} · ${Currencies.byCode(currency).symbol} — ${Currencies.byCode(currency).name}",
                                )
                            }
                        }
                        2 -> {
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                val options = listOf("System" to ThemeMode.SYSTEM, "Light" to ThemeMode.LIGHT, "Dark" to ThemeMode.DARK)
                                options.forEachIndexed { index, (label, mode) ->
                                    SegmentedButton(
                                        selected = themeMode == mode,
                                        onClick = { themeMode = mode },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                    ) { Text(label) }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Text("Accent color", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(12.dp))
                            ColorPaletteRow(
                                colors = AccentPresets,
                                selected = accent,
                                onPick = { accent = it },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { if (step > 0) step-- }, enabled = step > 0) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(
                                    width = if (index == step) 24.dp else 8.dp,
                                    height = 8.dp,
                                )
                                .background(
                                    if (index == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape,
                                ),
                        )
                    }
                }
                if (step < 2) {
                    TextButton(onClick = { step++ }, enabled = step != 0 || name.isNotBlank()) {
                        Text(if (step == 0) "Next" else "Next")
                    }
                } else {
                    Button(onClick = {
                        viewModel.complete(
                            name = name,
                            currency = currency,
                            themeMode = themeMode,
                            accentArgb = accent.toArgb().toLong() and 0xFFFFFFFFL,
                        )
                    }) {
                        Text("Get started")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            if (step == 0) {
                Text(
                    "Your data stays on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selectedCode = currency,
            onDismiss = { showCurrencyPicker = false },
            onPick = { currency = it },
        )
    }
}
