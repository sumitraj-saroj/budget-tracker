package com.budgettracker.app.domain

import com.budgettracker.app.data.TxDetailed
import com.budgettracker.app.data.db.AccountEntity
import com.budgettracker.app.data.db.CategoryEntity
import com.budgettracker.app.data.db.CategoryType
import com.budgettracker.app.data.db.Cycle
import com.budgettracker.app.data.db.DebtDirection
import com.budgettracker.app.data.db.DebtEntity
import com.budgettracker.app.data.db.SubscriptionEntity
import com.budgettracker.app.data.db.TxEntity
import com.budgettracker.app.data.db.TxType
import com.budgettracker.app.util.Fmt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * JVM tests for reminder alert logic. All date fixtures go through [Fmt]
 * conversions so the tests are timezone-independent.
 */
class ReminderLogicTest {

    private val now: Long = Fmt.fromLocalDate(LocalDate.of(2026, 8, 28))

    private val account = AccountEntity(id = 1, name = "Cash", currencyCode = "USD")
    private val food = CategoryEntity(id = 2, name = "Food", type = CategoryType.EXPENSE)
    private val transport = CategoryEntity(id = 3, name = "Transport", type = CategoryType.EXPENSE)

    private fun tx(amountMinor: Long, categoryId: Long?, dayOfMonth: Int): TxDetailed {
        val date = Fmt.fromLocalDate(LocalDate.of(2026, 8, dayOfMonth))
        return TxDetailed(
            tx = TxEntity(
                amountMinor = amountMinor,
                type = TxType.EXPENSE,
                accountId = account.id,
                categoryId = categoryId,
                date = date,
            ),
            category = listOf(food, transport).firstOrNull { it.id == categoryId },
            account = account,
            toAccount = null,
        )
    }

    private fun progress(spentMinor: Long, amountMinor: Long = 100_000L) =
        Insights.budgetProgress(
            budget = com.budgettracker.app.data.db.BudgetEntity(
                id = 1,
                name = "Monthly spending",
                amountMinor = amountMinor,
            ),
            txs = listOf(tx(spentMinor, food.id, 10)),
            baseCode = "USD",
            now = now,
        )

    // ---------- Budget alerts ----------

    @Test
    fun `no alert when nothing spent`() {
        val alerts = ReminderLogic.budgetAlerts(listOf(progress(0)), now)
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `no alert below threshold`() {
        val alerts = ReminderLogic.budgetAlerts(listOf(progress(79_000)), now)
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `nearly alert fires at 80 percent`() {
        val alerts = ReminderLogic.budgetAlerts(listOf(progress(80_000)), now)
        assertEquals(1, alerts.size)
        val alert = alerts.first()
        assertEquals(BudgetAlertLevel.NEARLY, alert.level)
        assertEquals(80, alert.percentUsed)
        assertTrue(alert.key.startsWith("b:1:"))
        assertTrue(alert.key.endsWith(":80"))
    }

    @Test
    fun `nearly alert stays nearly between 80 and 100`() {
        val alerts = ReminderLogic.budgetAlerts(listOf(progress(90_000)), now)
        assertEquals(1, alerts.size)
        assertEquals(BudgetAlertLevel.NEARLY, alerts.first().level)
        assertEquals(90, alerts.first().percentUsed)
    }

    @Test
    fun `over alert fires at 100 percent`() {
        val alerts = ReminderLogic.budgetAlerts(listOf(progress(100_000)), now)
        assertEquals(1, alerts.size)
        assertEquals(BudgetAlertLevel.OVER, alerts.first().level)
        assertEquals(100, alerts.first().percentUsed)
    }

    @Test
    fun `over alert beyond 100 percent reports over amount`() {
        val alerts = ReminderLogic.budgetAlerts(listOf(progress(150_000)), now)
        assertEquals(1, alerts.size)
        val alert = alerts.first()
        assertEquals(BudgetAlertLevel.OVER, alert.level)
        assertEquals(150, alert.percentUsed)
        assertTrue(alert.body.contains("$500.00 over"))
    }

    @Test
    fun `nearly alert copy contains amounts`() {
        val alerts = ReminderLogic.budgetAlerts(listOf(progress(80_000)), now)
        val alert = alerts.first()
        assertEquals("Monthly spending: 80% of budget used", alert.title)
        assertTrue(alert.body.contains("$800.00"))
        assertTrue(alert.body.contains("$1,000.00"))
        assertTrue(alert.body.contains("$200.00 left"))
    }

    @Test
    fun `zero amount budget is skipped`() {
        val alerts = ReminderLogic.budgetAlerts(listOf(progress(50_000, amountMinor = 0)), now)
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `alerts sorted by percent descending`() {
        val high = progress(120_000).let {
            it.copy(budget = it.budget.copy(id = 11, name = "High"))
        }
        val low = progress(85_000).let {
            it.copy(budget = it.budget.copy(id = 22, name = "Low"))
        }
        val alerts = ReminderLogic.budgetAlerts(listOf(low, high), now)
        assertEquals(listOf(11L, 22L), alerts.map { it.budgetId })
    }

    @Test
    fun `category scoped budget only counts scoped categories`() {
        val scoped = com.budgettracker.app.data.db.BudgetEntity(
            id = 9,
            name = "Food only",
            amountMinor = 100_000L,
            categoryIds = listOf(food.id),
        )
        val txs = listOf(tx(60_000, transport.id, 5), tx(90_000, food.id, 6))
        val progress = Insights.budgetProgress(scoped, txs, "USD", now)
        val alerts = ReminderLogic.budgetAlerts(listOf(progress), now)
        assertEquals(1, alerts.size)
        assertEquals(90, alerts.first().percentUsed)
    }

    @Test
    fun `alert key resets each period`() {
        val p1 = progress(80_000)
        val p2 = p1.copy(window = p1.window.copy(startMillis = p1.window.startMillis + 86_400_000L * 30))
        val keys = ReminderLogic.budgetAlerts(listOf(p1, p2), now).map { it.key }
        assertTrue(keys.distinct().size == 2)
    }

    // ---------- Due-soon items ----------

    private fun sub(dueInDays: Long, active: Boolean = true, id: Long = 5) = SubscriptionEntity(
        id = id,
        name = "Netflix",
        amountMinor = 6_490,
        accountId = 1,
        cycle = Cycle.MONTHLY,
        nextDue = now + dueInDays * 86_400_000L,
        isActive = active,
    )

    private fun debt(dueInDays: Long?, direction: DebtDirection = DebtDirection.LENT, settled: Boolean = false, id: Long = 7) = DebtEntity(
        id = id,
        direction = direction,
        personName = "Amit",
        amountMinor = 500,
        note = "Lunch money",
        dueDate = dueInDays?.let { now + it * 86_400_000L },
        isSettled = settled,
    )

    @Test
    fun `subscription due within window is included`() {
        val items = ReminderLogic.dueSoonItems(listOf(sub(2)), emptyList(), daysAhead = 3, now = now)
        assertEquals(1, items.size)
        val item = items.first()
        assertEquals("Netflix", item.title)
        assertTrue(item.body.contains("$64.90"))
        assertTrue(item.body.contains("due in 2 days"))
        assertTrue(item.key.startsWith("s:5:"))
    }

    @Test
    fun `subscription due today wording`() {
        val items = ReminderLogic.dueSoonItems(listOf(sub(0)), emptyList(), daysAhead = 3, now = now)
        assertTrue(items.first().body.contains("due today"))
    }

    @Test
    fun `subscription due tomorrow wording`() {
        val items = ReminderLogic.dueSoonItems(listOf(sub(1)), emptyList(), daysAhead = 3, now = now)
        assertTrue(items.first().body.contains("due tomorrow"))
    }

    @Test
    fun `subscription outside window is excluded`() {
        val items = ReminderLogic.dueSoonItems(listOf(sub(5)), emptyList(), daysAhead = 3, now = now)
        assertTrue(items.isEmpty())
    }

    @Test
    fun `inactive subscription excluded`() {
        val items = ReminderLogic.dueSoonItems(listOf(sub(2, active = false)), emptyList(), daysAhead = 3, now = now)
        assertTrue(items.isEmpty())
    }

    @Test
    fun `overdue subscription included with overdue wording`() {
        val items = ReminderLogic.dueSoonItems(listOf(sub(-2)), emptyList(), daysAhead = 3, now = now)
        assertEquals(1, items.size)
        assertTrue(items.first().body.contains("overdue by 2 days"))
        assertTrue(items.first().overdue)
    }

    @Test
    fun `ancient overdue subscription excluded`() {
        val items = ReminderLogic.dueSoonItems(listOf(sub(-40)), emptyList(), daysAhead = 3, now = now)
        assertTrue(items.isEmpty())
    }

    @Test
    fun `lent debt wording`() {
        val items = ReminderLogic.dueSoonItems(emptyList(), listOf(debt(1)), daysAhead = 3, now = now)
        assertEquals(1, items.size)
        assertEquals("Amit owes you", items.first().title)
        assertTrue(items.first().body.contains("$5.00"))
        assertTrue(items.first().body.contains("due tomorrow"))
        assertTrue(items.first().body.contains("Lunch money"))
        assertTrue(items.first().key.startsWith("d:7:"))
    }

    @Test
    fun `borrowed debt wording`() {
        val items = ReminderLogic.dueSoonItems(emptyList(), listOf(debt(1, direction = DebtDirection.BORROWED)), daysAhead = 3, now = now)
        assertEquals("You owe Amit", items.first().title)
    }

    @Test
    fun `settled debt excluded`() {
        val items = ReminderLogic.dueSoonItems(emptyList(), listOf(debt(1, settled = true)), daysAhead = 3, now = now)
        assertTrue(items.isEmpty())
    }

    @Test
    fun `debt without due date excluded`() {
        val items = ReminderLogic.dueSoonItems(emptyList(), listOf(debt(null)), daysAhead = 3, now = now)
        assertTrue(items.isEmpty())
    }

    @Test
    fun `overdue debt included`() {
        val items = ReminderLogic.dueSoonItems(emptyList(), listOf(debt(-3)), daysAhead = 3, now = now)
        assertEquals(1, items.size)
        assertTrue(items.first().body.contains("overdue by 3 days"))
    }

    // ---------- Notified-key pruning ----------

    @Test
    fun `prune removes old keys and keeps recent`() {
        val today = 20_000L
        val keys = setOf(
            "s:5:${today}", // today → keep
            "s:5:${today - 10}", // 10 days old → keep
            "d:7:${today - 44}", // 44 days old → keep
            "d:7:${today - 46}", // 46 days old → drop
            "b:1:${today - 100}:80", // old period → drop
        )
        val pruned = ReminderLogic.pruneNotifiedKeys(keys, todayEpochDay = today)
        assertEquals(setOf("s:5:$today", "s:5:${today - 10}", "d:7:${today - 44}"), pruned)
    }

    @Test
    fun `prune keeps malformed keys untouched`() {
        val keys = setOf("garbage", "b:1:x:80", "s:5:notanumber")
        assertEquals(keys, ReminderLogic.pruneNotifiedKeys(keys, todayEpochDay = 20_000L))
    }
}
