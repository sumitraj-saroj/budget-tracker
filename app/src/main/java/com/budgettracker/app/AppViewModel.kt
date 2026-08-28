package com.budgettracker.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.UserPrefs
import com.budgettracker.app.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val prefsRepository: PrefsRepository,
    private val reminderScheduler: ReminderScheduler,
    financeRepository: FinanceRepository,
) : ViewModel() {

    /**
     * Null until DataStore has emitted once — lets the UI (and the splash
     * screen) hold until real prefs are available, avoiding flashes of
     * default state like the onboarding screen on every cold launch.
     */
    val prefs: StateFlow<UserPrefs?> = prefsRepository.prefs
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked

    /** Used for the backup badge on the nav bar. */
    val txCount: StateFlow<Int> = financeRepository.transactions
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private var pausedAt: Long? = null

    init {
        viewModelScope.launch {
            // Seed defaults once onboarding has been completed (or on restore).
            val p = prefsRepository.prefs.first { it.onboardingDone }
            financeRepository.ensureDefaultData(p.baseCurrency)
            _locked.value = p.biometricLock
        }
    }

    fun onAppPaused() {
        if (!_locked.value) pausedAt = System.currentTimeMillis()
    }

    fun onAppResumed() {
        val paused = pausedAt ?: return
        pausedAt = null
        val away = System.currentTimeMillis() - paused
        if (prefs.value?.biometricLock == true && away > 30_000L) {
            _locked.value = true
        }
    }

    fun unlock() {
        _locked.value = false
    }

    /** Called once when the one-time notification permission prompt finishes. */
    fun onNotificationPermissionResult(granted: Boolean) {
        viewModelScope.launch { prefsRepository.setNotificationAsked(true) }
        if (granted) reminderScheduler.ensureScheduled()
    }
}
