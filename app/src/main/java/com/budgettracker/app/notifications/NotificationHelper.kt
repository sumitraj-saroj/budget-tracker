package com.budgettracker.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.budgettracker.app.MainActivity
import com.budgettracker.app.R
import com.budgettracker.app.domain.BudgetAlertLevel
import com.budgettracker.app.domain.BudgetAlertSpec
import com.budgettracker.app.domain.DueItemSpec
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    CHANNEL_BUDGET,
                    "Budget alerts",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Alerts when a budget is nearly used or exceeded"
                },
                NotificationChannel(
                    CHANNEL_DUE,
                    "Due soon",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Reminders for upcoming subscription payments and debts"
                },
            ),
        )
    }

    fun canNotify(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    /** Posts the alert. Returns false when nothing was shown (permission missing or error). */
    fun notifyBudgetAlert(alert: BudgetAlertSpec): Boolean {
        val builder = baseBuilder(CHANNEL_BUDGET, alert.budgetId.toInt())
            .setContentTitle(alert.title)
            .setContentText(alert.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(alert.body))
        if (alert.level == BudgetAlertLevel.OVER) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        }
        return notify(NOTIFY_BUDGET_BASE + alert.budgetId.toInt(), builder)
    }

    /** Posts the reminder. Returns false when nothing was shown (permission missing or error). */
    fun notifyDueItem(item: DueItemSpec, id: Int): Boolean {
        val builder = baseBuilder(CHANNEL_DUE, id)
            .setContentTitle(item.title)
            .setContentText(item.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.body))
        return notify(id, builder)
    }

    private fun baseBuilder(channel: String, requestCode: Int): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(requestCode))

    private fun notify(id: Int, builder: NotificationCompat.Builder): Boolean =
        runCatching {
            NotificationManagerCompat.from(context).notify(id, builder.build())
            true
        }.getOrDefault(false)

    private fun contentIntent(requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP,
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    companion object {
        const val CHANNEL_BUDGET = "budget_alerts"
        const val CHANNEL_DUE = "due_reminders"
        const val NOTIFY_BUDGET_BASE = 100_000
        const val NOTIFY_SUB_BASE = 200_000
        const val NOTIFY_DEBT_BASE = 300_000
    }
}
