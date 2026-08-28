package com.budgettracker.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.budgettracker.app.domain.ReminderLogic
import com.budgettracker.app.ui.theme.ThemeMode
import com.budgettracker.app.util.Fmt
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPrefs(
    val onboardingDone: Boolean = false,
    val displayName: String = "",
    val baseCurrency: String = "USD",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentArgb: Long = 0xFF10B981,
    val useDynamicColor: Boolean = false,
    val amoledBlack: Boolean = false,
    val liquidGlass: Boolean = false,
    val biometricLock: Boolean = false,
    val googleEmail: String? = null,
    val googleName: String? = null,
    val googlePicture: String? = null,
    val lastBackupAt: Long? = null,
    val remindersEnabled: Boolean = true,
    val budgetAlerts: Boolean = true,
    val dueReminders: Boolean = true,
    val dueDaysAhead: Int = 3,
    val notificationAsked: Boolean = false,
)

@Singleton
class PrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val BASE_CURRENCY = stringPreferencesKey("base_currency")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT = longPreferencesKey("accent_argb")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
        val LIQUID_GLASS = booleanPreferencesKey("liquid_glass")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val GOOGLE_EMAIL = stringPreferencesKey("google_email")
        val GOOGLE_NAME = stringPreferencesKey("google_name")
        val GOOGLE_PICTURE = stringPreferencesKey("google_picture")
        val LAST_BACKUP = longPreferencesKey("last_backup_at")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val BUDGET_ALERTS = booleanPreferencesKey("budget_alerts")
        val DUE_REMINDERS = booleanPreferencesKey("due_reminders")
        val DUE_DAYS_AHEAD = intPreferencesKey("due_days_ahead")
        val NOTIFICATION_ASKED = booleanPreferencesKey("notification_permission_asked")
        val NOTIFIED_KEYS = stringSetPreferencesKey("notified_alert_keys")
    }

    val prefs: Flow<UserPrefs> = context.dataStore.data.map { p ->
        UserPrefs(
            onboardingDone = p[Keys.ONBOARDING_DONE] ?: false,
            displayName = p[Keys.DISPLAY_NAME] ?: "",
            baseCurrency = p[Keys.BASE_CURRENCY] ?: "USD",
            themeMode = p[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            accentArgb = p[Keys.ACCENT] ?: 0xFF10B981,
            useDynamicColor = p[Keys.DYNAMIC_COLOR] ?: false,
            amoledBlack = p[Keys.AMOLED_BLACK] ?: false,
            liquidGlass = p[Keys.LIQUID_GLASS] ?: false,
            biometricLock = p[Keys.BIOMETRIC_LOCK] ?: false,
            googleEmail = p[Keys.GOOGLE_EMAIL],
            googleName = p[Keys.GOOGLE_NAME],
            googlePicture = p[Keys.GOOGLE_PICTURE],
            lastBackupAt = p[Keys.LAST_BACKUP],
            remindersEnabled = p[Keys.REMINDERS_ENABLED] ?: true,
            budgetAlerts = p[Keys.BUDGET_ALERTS] ?: true,
            dueReminders = p[Keys.DUE_REMINDERS] ?: true,
            dueDaysAhead = p[Keys.DUE_DAYS_AHEAD] ?: 3,
            notificationAsked = p[Keys.NOTIFICATION_ASKED] ?: false,
        )
    }

    suspend fun setOnboardingDone(done: Boolean) = context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }

    suspend fun setDisplayName(name: String) = context.dataStore.edit { it[Keys.DISPLAY_NAME] = name }

    suspend fun setBaseCurrency(code: String) = context.dataStore.edit { it[Keys.BASE_CURRENCY] = code }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }

    suspend fun setAccent(argb: Long) = context.dataStore.edit { it[Keys.ACCENT] = argb }

    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }

    suspend fun setAmoledBlack(enabled: Boolean) = context.dataStore.edit { it[Keys.AMOLED_BLACK] = enabled }

    suspend fun setLiquidGlass(enabled: Boolean) = context.dataStore.edit { it[Keys.LIQUID_GLASS] = enabled }

    suspend fun setBiometricLock(enabled: Boolean) = context.dataStore.edit { it[Keys.BIOMETRIC_LOCK] = enabled }

    suspend fun setGoogleAccount(email: String?, name: String?, picture: String?) = context.dataStore.edit {
        if (email == null) {
            it.remove(Keys.GOOGLE_EMAIL); it.remove(Keys.GOOGLE_NAME); it.remove(Keys.GOOGLE_PICTURE)
        } else {
            it[Keys.GOOGLE_EMAIL] = email
            it[Keys.GOOGLE_NAME] = name ?: ""
            it[Keys.GOOGLE_PICTURE] = picture ?: ""
        }
    }

    suspend fun setLastBackupAt(at: Long) = context.dataStore.edit { it[Keys.LAST_BACKUP] = at }

    // ---------- Reminder prefs ----------

    suspend fun setRemindersEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }

    suspend fun setBudgetAlerts(enabled: Boolean) = context.dataStore.edit { it[Keys.BUDGET_ALERTS] = enabled }

    suspend fun setDueReminders(enabled: Boolean) = context.dataStore.edit { it[Keys.DUE_REMINDERS] = enabled }

    suspend fun setDueDaysAhead(days: Int) = context.dataStore.edit { it[Keys.DUE_DAYS_AHEAD] = days }

    suspend fun setNotificationAsked(asked: Boolean) = context.dataStore.edit { it[Keys.NOTIFICATION_ASKED] = asked }

    /** Keys of reminders already sent, used to avoid notifying about the same thing twice. */
    suspend fun notifiedKeys(): Set<String> = context.dataStore.data.first()[Keys.NOTIFIED_KEYS] ?: emptySet()

    /** Merges [keys] into the notified set, pruning stale entries. */
    suspend fun markNotified(keys: Set<String>, now: Long) {
        if (keys.isEmpty()) return
        context.dataStore.edit { p ->
            val merged = (p[Keys.NOTIFIED_KEYS] ?: emptySet()) + keys
            p[Keys.NOTIFIED_KEYS] = ReminderLogic.pruneNotifiedKeys(
                keys = merged,
                todayEpochDay = Fmt.toLocalDate(now).toEpochDay(),
            )
        }
    }

    /** Restore prefs from a backup (only user-visible choices, no security flags). */
    suspend fun restoreFromBackup(
        displayName: String,
        baseCurrency: String,
        themeMode: String,
        accentArgb: Long,
        dynamicColor: Boolean,
        amoledBlack: Boolean = false,
        liquidGlass: Boolean = false,
    ) {
        context.dataStore.edit {
            it[Keys.DISPLAY_NAME] = displayName
            it[Keys.BASE_CURRENCY] = baseCurrency
            it[Keys.THEME_MODE] = themeMode
            it[Keys.ACCENT] = accentArgb
            it[Keys.DYNAMIC_COLOR] = dynamicColor
            it[Keys.AMOLED_BLACK] = amoledBlack
            it[Keys.LIQUID_GLASS] = liquidGlass
        }
    }
}
