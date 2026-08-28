package com.budgettracker.app.notifications

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enqueues the reminder checks. A cheap periodic job re-checks while the app
 * is closed; a one-time job runs whenever the app is opened so freshly added
 * budgets/subs get noticed immediately.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
) {
    fun ensureScheduled() {
        workManager.enqueueUniquePeriodicWork(
            ReminderWorker.PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ReminderWorker>(CHECK_EVERY_HOURS, TimeUnit.HOURS).build(),
        )
        workManager.enqueueUniqueWork(
            ReminderWorker.ONE_TIME_WORK,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<ReminderWorker>().build(),
        )
    }

    /** Re-checks soon after data changed (REPLACE coalesces bursts of saves). */
    fun checkNow() {
        workManager.enqueueUniqueWork(
            ReminderWorker.ONE_TIME_WORK,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ReminderWorker>().build(),
        )
    }

    fun cancelAll() {
        workManager.cancelUniqueWork(ReminderWorker.PERIODIC_WORK)
        workManager.cancelUniqueWork(ReminderWorker.ONE_TIME_WORK)
    }

    companion object {
        const val CHECK_EVERY_HOURS = 6L
    }
}
