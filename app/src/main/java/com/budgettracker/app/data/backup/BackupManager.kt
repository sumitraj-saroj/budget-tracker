package com.budgettracker.app.data.backup

import android.content.Context
import android.net.Uri
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.data.db.AppDatabase
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.BudgetEntity
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.data.db.DebtEntity
import com.budgettracker.app.data.db.GoalEntity
import com.budgettracker.app.data.db.SubscriptionEntity
import com.budgettracker.app.data.db.TxEntity
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class PrefsSnapshot(
    val displayName: String = "",
    val baseCurrency: String = "USD",
    val themeMode: String = "SYSTEM",
    val accentArgb: Long = 0xFF10B981,
    val useDynamicColor: Boolean = false,
    val amoledBlack: Boolean = false,
    val liquidGlass: Boolean = false,
)

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long,
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TxEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val subscriptions: List<SubscriptionEntity> = emptyList(),
    val prefs: PrefsSnapshot = PrefsSnapshot(),
)

@Singleton
class BackupManager @Inject constructor(
    private val db: AppDatabase,
    private val prefsRepository: PrefsRepository,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun export(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val backup = snapshot()
            context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                out.write(json.encodeToString(BackupData.serializer(), backup).toByteArray(Charsets.UTF_8))
            } ?: error("Could not open file for writing")
            prefsRepository.setLastBackupAt(System.currentTimeMillis())
            Unit
        }
    }

    suspend fun import(context: Context, uri: Uri): Result<BackupData> = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: error("Could not read file")
            val backup = json.decodeFromString(BackupData.serializer(), text)
            require(backup.version <= CURRENT_VERSION) { "Unsupported backup version" }
            restore(backup)
            backup
        }
    }

    private suspend fun snapshot(): BackupData {
        val prefs = prefsRepository.prefs.first()
        return BackupData(
            exportedAt = System.currentTimeMillis(),
            accounts = db.accountDao().snapshot(),
            categories = db.categoryDao().snapshot(),
            transactions = db.txDao().snapshot(),
            budgets = db.budgetDao().snapshot(),
            goals = db.goalDao().snapshot(),
            debts = db.debtDao().snapshot(),
            subscriptions = db.subscriptionDao().snapshot(),
            prefs = PrefsSnapshot(
                displayName = prefs.displayName,
                baseCurrency = prefs.baseCurrency,
                themeMode = prefs.themeMode.name,
                accentArgb = prefs.accentArgb,
                useDynamicColor = prefs.useDynamicColor,
                amoledBlack = prefs.amoledBlack,
                liquidGlass = prefs.liquidGlass,
            ),
        )
    }

    private suspend fun restore(backup: BackupData) {
        db.withTransaction {
            db.accountDao().deleteAll()
            db.categoryDao().deleteAll()
            db.txDao().deleteAll()
            db.budgetDao().deleteAll()
            db.goalDao().deleteAll()
            db.debtDao().deleteAll()
            db.subscriptionDao().deleteAll()
            db.accountDao().insertAll(backup.accounts)
            db.categoryDao().insertAll(backup.categories)
            db.txDao().insertAll(backup.transactions)
            db.budgetDao().insertAll(backup.budgets)
            db.goalDao().insertAll(backup.goals)
            db.debtDao().insertAll(backup.debts)
            db.subscriptionDao().insertAll(backup.subscriptions)
        }
        prefsRepository.restoreFromBackup(
            displayName = backup.prefs.displayName,
            baseCurrency = backup.prefs.baseCurrency,
            themeMode = backup.prefs.themeMode,
            accentArgb = backup.prefs.accentArgb,
            dynamicColor = backup.prefs.useDynamicColor,
            amoledBlack = backup.prefs.amoledBlack,
            liquidGlass = backup.prefs.liquidGlass,
        )
    }

    companion object {
        const val CURRENT_VERSION = 1
    }
}
