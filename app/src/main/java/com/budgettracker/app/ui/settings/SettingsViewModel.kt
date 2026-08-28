package com.budgettracker.app.ui.settings

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.auth.GoogleAuthClient
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.UserPrefs
import com.budgettracker.app.data.backup.BackupManager
import com.budgettracker.app.notifications.ReminderScheduler
import com.budgettracker.app.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepository: PrefsRepository,
    private val backupManager: BackupManager,
    private val googleAuth: GoogleAuthClient,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val prefs: StateFlow<UserPrefs> = prefsRepository.prefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPrefs())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    val googleConfigured: Boolean get() = googleAuth.isConfigured

    fun consumeMessage() {
        _message.value = null
    }

    fun showMessage(text: String) {
        _message.value = text
    }

    fun setDisplayName(name: String) {
        viewModelScope.launch { prefsRepository.setDisplayName(name) }
    }

    fun setBaseCurrency(code: String) {
        viewModelScope.launch { prefsRepository.setBaseCurrency(code) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepository.setThemeMode(mode) }
    }

    fun setAccent(argb: Long) {
        viewModelScope.launch { prefsRepository.setAccent(argb) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setDynamicColor(enabled) }
    }

    fun setAmoledBlack(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setAmoledBlack(enabled) }
    }

    fun setLiquidGlass(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setLiquidGlass(enabled) }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setBiometricLock(enabled) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setRemindersEnabled(enabled)
        }
        if (enabled) {
            reminderScheduler.ensureScheduled()
        } else {
            reminderScheduler.cancelAll()
        }
    }

    fun setBudgetAlerts(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setBudgetAlerts(enabled) }
    }

    fun setDueReminders(enabled: Boolean) {
        viewModelScope.launch { prefsRepository.setDueReminders(enabled) }
    }

    fun setDueDaysAhead(days: Int) {
        viewModelScope.launch { prefsRepository.setDueDaysAhead(days) }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _busy.value = true
            googleAuth.signIn(activity)
                .onSuccess { account ->
                    prefsRepository.setGoogleAccount(account.email, account.displayName, account.photoUrl)
                    if (account.displayName != null && prefs.value.displayName.isBlank()) {
                        prefsRepository.setDisplayName(account.displayName!!)
                    }
                    showMessage("Signed in as ${account.email}")
                }
                .onFailure { e ->
                    showMessage(
                        if (!googleAuth.isConfigured) {
                            "Google Sign-In isn't configured yet — see README for setup steps"
                        } else {
                            "Sign-in failed: ${e.message ?: "unknown error"}"
                        },
                    )
                }
            _busy.value = false
        }
    }

    fun signOut(activity: Activity) {
        viewModelScope.launch {
            googleAuth.signOut(activity)
            prefsRepository.setGoogleAccount(null, null, null)
            showMessage("Signed out")
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _busy.value = true
            backupManager.export(context, uri)
                .onSuccess { showMessage("Backup exported") }
                .onFailure { showMessage("Export failed: ${it.message}") }
            _busy.value = false
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _busy.value = true
            backupManager.import(context, uri)
                .onSuccess { backup ->
                    showMessage("Restored ${backup.transactions.size} transactions and ${backup.accounts.size} accounts")
                }
                .onFailure { showMessage("Import failed: ${it.message}") }
            _busy.value = false
        }
    }
}
