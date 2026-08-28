package com.budgettracker.app.domain

import com.budgettracker.app.data.db.DebtDirection
import com.budgettracker.app.data.db.DebtEntity
import com.budgettracker.app.data.db.SubscriptionEntity
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.Fmt
import com.budgettracker.app.util.formatMoney
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class BudgetAlertLevel { NEARLY, OVER }

/** A user-facing budget alert, ready to be shown as a notification. */
data class BudgetAlertSpec(
    val key: String,
    val budgetId: Long,
    val budgetName: String,
    val level: BudgetAlertLevel,
    val percentUsed: Int,
    val title: String,
    val body: String,
)

/** A user-facing due-soon item (subscription or debt), ready to be shown. */
data class DueItemSpec(
    val key: String,
    val title: String,
    val body: String,
    val dueMillis: Long,
    val overdue: Boolean,
)

/**
 * Pure reminder logic: which budgets have crossed a threshold and which
 * subscriptions/debts are due soon. No Android dependencies — unit tested on the JVM.
 */
object ReminderLogic {

    const val NEARLY_THRESHOLD_PCT = 80
    const val DEFAULT_DAYS_AHEAD = 3

    private const val OVERDUE_MAX_DAYS = 30L
    const val KEEP_NOTIFIED_DAYS = 45L

    /**
     * Alerts for budgets that have crossed [NEARLY_THRESHOLD_PCT]% or 100% in
     * their current period, most-used first. The dedupe [BudgetAlertSpec.key]
     * embeds the period start and level, so alerts re-fire once per period
     * per level.
     */
    fun budgetAlerts(
        progresses: List<BudgetProgress>,
        now: Long = System.currentTimeMillis(),
        baseCode: String = "USD",
    ): List<BudgetAlertSpec> {
        val currency = Currencies.byCode(baseCode)
        val alerts = mutableListOf<BudgetAlertSpec>()
        for (progress in progresses) {
            val amount = progress.budget.amountMinor
            if (amount <= 0) continue
            val percent = ((progress.spentMinor * 100) / amount).toInt()
            if (percent < NEARLY_THRESHOLD_PCT) continue
            val level = if (percent >= 100) BudgetAlertLevel.OVER else BudgetAlertLevel.NEARLY
            val keyLevel = if (level == BudgetAlertLevel.OVER) 100 else NEARLY_THRESHOLD_PCT
            val spent = formatMoney(progress.spentMinor, currency)
            val budgeted = formatMoney(amount, currency)
            val name = progress.budget.name
            val (title, body) = if (level == BudgetAlertLevel.OVER) {
                val over = formatMoney(progress.spentMinor - amount, currency)
                "$name: budget exceeded" to "$spent of $budgeted spent — $over over"
            } else {
                val left = formatMoney(progress.remainingMinor, currency)
                "$name: $percent% of budget used" to "$spent of $budgeted spent — $left left"
            }
            alerts += BudgetAlertSpec(
                key = "b:${progress.budget.id}:${Fmt.toLocalDate(progress.window.startMillis).toEpochDay()}:$keyLevel",
                budgetId = progress.budget.id,
                budgetName = name,
                level = level,
                percentUsed = percent,
                title = title,
                body = body,
            )
        }
        return alerts.sortedByDescending { it.percentUsed }
    }

    /**
     * Subscriptions and debts due within [daysAhead] days (or overdue by up to
     * [OVERDUE_MAX_DAYS] days). The dedupe [DueItemSpec.key] embeds the due
     * day, so each occurrence reminds at most once.
     */
    fun dueSoonItems(
        subs: List<SubscriptionEntity>,
        debts: List<DebtEntity>,
        daysAhead: Int,
        now: Long = System.currentTimeMillis(),
        baseCode: String = "USD",
    ): List<DueItemSpec> {
        val today = Fmt.toLocalDate(now)
        val earliest = today.minusDays(OVERDUE_MAX_DAYS)
        val latest = today.plusDays(daysAhead.toLong())
        val currency = Currencies.byCode(baseCode)
        val items = mutableListOf<DueItemSpec>()

        for (sub in subs) {
            if (!sub.isActive) continue
            val due = Fmt.toLocalDate(sub.nextDue)
            if (due !in earliest..latest) continue
            items += DueItemSpec(
                key = "s:${sub.id}:${due.toEpochDay()}",
                title = sub.name,
                body = "${formatMoney(sub.amountMinor, currency)} ${dueLabel(due, today)}",
                dueMillis = sub.nextDue,
                overdue = due.isBefore(today),
            )
        }

        for (debt in debts) {
            if (debt.isSettled) continue
            val dueMillis = debt.dueDate ?: continue
            val due = Fmt.toLocalDate(dueMillis)
            if (due !in earliest..latest) continue
            val title = when (debt.direction) {
                DebtDirection.LENT -> "${debt.personName} owes you"
                DebtDirection.BORROWED -> "You owe ${debt.personName}"
            }
            val note = debt.note.trim()
            val body = buildString {
                append(formatMoney(debt.amountMinor, currency))
                append(' ')
                append(dueLabel(due, today))
                if (note.isNotEmpty()) {
                    append(" · ")
                    append(note)
                }
            }
            items += DueItemSpec(
                key = "d:${debt.id}:${due.toEpochDay()}",
                title = title,
                body = body,
                dueMillis = dueMillis,
                overdue = due.isBefore(today),
            )
        }
        return items
    }

    fun dueLabel(due: LocalDate, today: LocalDate): String {
        val diff = ChronoUnit.DAYS.between(today, due)
        return when {
            diff == 0L -> "due today"
            diff == 1L -> "due tomorrow"
            diff > 1L -> "due in $diff days"
            diff == -1L -> "overdue by 1 day"
            else -> "overdue by ${-diff} days"
        }
    }

    /**
     * Drops notified keys whose embedded day is older than [keepDays] days.
     * Unparseable keys are kept untouched.
     */
    fun pruneNotifiedKeys(keys: Set<String>, todayEpochDay: Long, keepDays: Long = KEEP_NOTIFIED_DAYS): Set<String> {
        val cutoff = todayEpochDay - keepDays
        val result = mutableSetOf<String>()
        for (key in keys) {
            val day = key.split(':').getOrNull(2)?.substringBefore('-')?.toLongOrNull()
            if (day == null || day >= cutoff) result += key
        }
        return result
    }
}
