package com.budgettracker.app.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.domain.Insights
import com.budgettracker.app.domain.ReminderLogic
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Periodic background check that posts budget threshold alerts and
 * subscription/debt due-soon reminders. Dedupe keys are persisted so each
 * alert fires at most once per period/occurrence.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: FinanceRepository,
    private val prefsRepository: PrefsRepository,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = prefsRepository.prefs.first()
        if (!prefs.remindersEnabled) return Result.success()
        // Without the permission nothing can be shown — bail out BEFORE marking
        // anything as notified, so alerts fire once permission is granted later.
        if (!notificationHelper.canNotify()) return Result.success()

        val now = System.currentTimeMillis()
        val base = prefs.baseCurrency
        val notified = prefsRepository.notifiedKeys()
        val freshKeys = mutableSetOf<String>()

        if (prefs.budgetAlerts) {
            val txs = repository.transactionsDetailed().first()
            val progresses = repository.budgets.first().map { Insights.budgetProgress(it, txs, base, now) }
            val alerts = ReminderLogic.budgetAlerts(progresses, now, base)
                .filter { it.key !in notified }
            val delivered = alerts.mapNotNull { alert ->
                if (notificationHelper.notifyBudgetAlert(alert)) alert.key else null
            }
            freshKeys += delivered
        }

        if (prefs.dueReminders) {
            val subs = repository.subscriptions.first()
            val debts = repository.debts.first()
            val items = ReminderLogic.dueSoonItems(subs, debts, prefs.dueDaysAhead, now, base)
                .filter { it.key !in notified }
            val delivered = items.mapNotNull { item ->
                val idBase = if (item.key.startsWith("s:")) {
                    NotificationHelper.NOTIFY_SUB_BASE
                } else {
                    NotificationHelper.NOTIFY_DEBT_BASE
                }
                if (notificationHelper.notifyDueItem(item, idBase + (item.key.hashCode() and 0xFFFF))) {
                    item.key
                } else {
                    null
                }
            }
            freshKeys += delivered
        }

        prefsRepository.markNotified(freshKeys, now)
        return Result.success()
    }

    companion object {
        const val PERIODIC_WORK = "reminder-periodic"
        const val ONE_TIME_WORK = "reminder-check-now"
    }
}
