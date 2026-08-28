package com.budgettracker.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.budgettracker.app.notifications.NotificationHelper
import com.budgettracker.app.notifications.ReminderScheduler
import com.budgettracker.app.widget.BudgetWidgetRefresher
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BudgetApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var reminderScheduler: ReminderScheduler
    @Inject lateinit var widgetRefresher: BudgetWidgetRefresher

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannels()
        reminderScheduler.ensureScheduled()
        widgetRefresher.startObserving()
    }
}
