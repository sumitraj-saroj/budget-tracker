package com.budgettracker.app.domain

import com.budgettracker.app.util.DateWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SafeToSpendTest {

    private fun progress(remainingMinor: Long, daysLeft: Int) = BudgetProgress(
        budget = com.budgettracker.app.data.db.BudgetEntity(
            id = 1,
            name = "b",
            amountMinor = remainingMinor.coerceAtLeast(0),
        ),
        window = DateWindow(0, 86_400_000L * daysLeft.coerceAtLeast(1)),
        spentMinor = 0,
        remainingMinor = remainingMinor,
        daysLeft = daysLeft,
    )

    @Test
    fun `null when no budgets`() {
        assertNull(Insights.safeToSpendPerDay(emptyList()))
    }

    @Test
    fun `divides remaining by max days left`() {
        val daily = Insights.safeToSpendPerDay(
            listOf(progress(10_000, daysLeft = 4), progress(2_000, daysLeft = 2)),
        )
        assertEquals(12_000L / 4, daily)
    }

    @Test
    fun `negative remaining counts as zero`() {
        val daily = Insights.safeToSpendPerDay(
            listOf(progress(-5_000, daysLeft = 5), progress(10_000, daysLeft = 5)),
        )
        assertEquals(2_000L, daily)
    }

    @Test
    fun `clamps days left to at least one`() {
        val daily = Insights.safeToSpendPerDay(listOf(progress(3_000, daysLeft = 0)))
        assertEquals(3_000L, daily)
    }

    @Test
    fun `all spent returns zero not null`() {
        assertEquals(0L, Insights.safeToSpendPerDay(listOf(progress(0, daysLeft = 7))))
    }
}
