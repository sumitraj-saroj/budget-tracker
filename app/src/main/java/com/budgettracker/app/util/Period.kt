package com.budgettracker.app.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object Fmt {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun date(millis: Long): String =
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
            .format(Instant.ofEpochMilli(millis).atZone(zone))

    fun dateShort(millis: Long): String =
        DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
            .format(Instant.ofEpochMilli(millis).atZone(zone))

    fun dayAndMonth(millis: Long): String =
        DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
            .format(Instant.ofEpochMilli(millis).atZone(zone))

    fun toLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun fromLocalDate(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

    /** DatePicker selections are UTC day starts; convert to local-day millis. */
    fun utcDayStartToLocalMillis(utcMillis: Long): Long =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate().let { fromLocalDate(it) }

    fun monthYear(yearMonth: YearMonth): String = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + yearMonth.year

    fun monthShort(millis: Long): String =
        DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH).format(Instant.ofEpochMilli(millis).atZone(zone))

    fun relativeDay(millis: Long): String {
        val today = LocalDate.now(zone)
        return when (toLocalDate(millis)) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            today.plusDays(1) -> "Tomorrow"
            else -> date(millis)
        }
    }

    fun greeting(): String {
        val hour = LocalDateTime.now(zone).hour
        return when {
            hour < 5 -> "Good night"
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}

/** A half-open date range in epoch millis [start, endExclusive). */
data class DateWindow(val startMillis: Long, val endMillis: Long) {
    val days: Int get() = ((endMillis - startMillis) / 86_400_000L).toInt()
}

enum class BudgetPeriodType { WEEKLY, MONTHLY, CUSTOM }

object Periods {

    fun startOfDay(millis: Long): Long {
        val d = Fmt.toLocalDate(millis)
        return Fmt.fromLocalDate(d)
    }

    fun currentMonth(now: Long = System.currentTimeMillis()): DateWindow {
        val today = Fmt.toLocalDate(now)
        val ym = YearMonth.from(today)
        return DateWindow(
            Fmt.fromLocalDate(ym.atDay(1)),
            Fmt.fromLocalDate(ym.plusMonths(1).atDay(1)),
        )
    }

    fun lastNDays(days: Int, now: Long = System.currentTimeMillis()): DateWindow =
        DateWindow(Fmt.fromLocalDate(Fmt.toLocalDate(now).minusDays(days.toLong() - 1)), startOfDay(now) + 86_400_000L)

    /** The period window of [budget] that contains [now]. */
    fun budgetWindow(periodType: BudgetPeriodType, customStart: Long?, customLengthDays: Int?, now: Long = System.currentTimeMillis()): DateWindow {
        val today = Fmt.toLocalDate(now)
        return when (periodType) {
            BudgetPeriodType.WEEKLY -> {
                val monday = today.with(DayOfWeek.MONDAY)
                DateWindow(Fmt.fromLocalDate(monday), Fmt.fromLocalDate(monday.plusWeeks(1)))
            }
            BudgetPeriodType.MONTHLY -> currentMonth(now)
            BudgetPeriodType.CUSTOM -> {
                val length = (customLengthDays ?: 30).coerceIn(1, 3650)
                val start = Fmt.toLocalDate(customStart ?: Fmt.fromLocalDate(today))
                val days = java.time.temporal.ChronoUnit.DAYS.between(start, today)
                val index = if (days < 0) days / length else days / length // floor division for negatives
                val windowStart = start.plusDays(index * length)
                DateWindow(Fmt.fromLocalDate(windowStart), Fmt.fromLocalDate(windowStart.plusDays(length.toLong())))
            }
        }
    }

    fun periodLabel(periodType: BudgetPeriodType, customLengthDays: Int?): String = when (periodType) {
        BudgetPeriodType.WEEKLY -> "Weekly"
        BudgetPeriodType.MONTHLY -> "Monthly"
        BudgetPeriodType.CUSTOM -> "Every ${customLengthDays ?: 30} days"
    }
}
