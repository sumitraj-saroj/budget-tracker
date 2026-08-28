package com.budgettracker.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.budgettracker.app.MainActivity
import com.budgettracker.app.R
import com.budgettracker.app.data.FinanceRepository
import com.budgettracker.app.data.PrefsRepository
import com.budgettracker.app.domain.Insights
import com.budgettracker.app.util.Currencies
import com.budgettracker.app.util.formatMoney
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Widget-facing snapshot of the numbers shown on Home. */
data class WidgetSnapshot(
    val baseCurrency: String,
    val totalBalanceMinor: Long,
    val safeToSpendMinor: Long?,
)

@Singleton
class WidgetDataProvider @Inject constructor(
    private val repository: FinanceRepository,
    private val prefsRepository: PrefsRepository,
) {
    suspend fun snapshot(): WidgetSnapshot {
        val prefs = prefsRepository.prefs.first()
        val base = prefs.baseCurrency
        val txs = repository.transactionsDetailed().first()
        val balances = repository.accountBalances().first()
        val total = Insights.totalBalanceBase(balances, base)
        val safeToSpend = Insights.safeToSpendPerDay(
            repository.budgets.first().map { Insights.budgetProgress(it, txs, base) },
        )
        return WidgetSnapshot(baseCurrency = base, totalBalanceMinor = total, safeToSpendMinor = safeToSpend)
    }
}

/** Re-renders the widget after in-app data changes. See [BudgetWidgetRefresher]. */

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BudgetWidgetEntryPoint {
    fun widgetDataProvider(): WidgetDataProvider
}

class BudgetWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val provider = EntryPointAccessors
            .fromApplication(context.applicationContext, BudgetWidgetEntryPoint::class.java)
            .widgetDataProvider()
        // A failed snapshot must never abort the update — otherwise the widget
        // sits on the loading layout until the next refresh succeeds.
        val snapshot = runCatching { provider.snapshot() }.getOrElse {
            WidgetSnapshot(baseCurrency = "USD", totalBalanceMinor = 0, safeToSpendMinor = null)
        }
        provideContent { Content(snapshot, context) }
    }

    @Composable
    private fun Content(snapshot: WidgetSnapshot, context: Context) {
        val currency = Currencies.byCode(snapshot.baseCurrency)
        val label: String
        val amount: String
        val footer: String
        if (snapshot.safeToSpendMinor != null) {
            label = "Safe to spend / day"
            amount = formatMoney(snapshot.safeToSpendMinor, currency)
            footer = "Balance ${formatMoney(snapshot.totalBalanceMinor, currency)}"
        } else {
            label = "Total balance"
            amount = formatMoney(snapshot.totalBalanceMinor, currency)
            footer = "Add a budget to see safe-to-spend"
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(R.drawable.widget_bg),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp),
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        label.uppercase(),
                        style = TextStyle(color = ColorProvider(Muted), fontSize = 11.sp),
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    AddButton(context)
                }
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    amount,
                    style = TextStyle(color = ColorProvider(Accent), fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    footer,
                    style = TextStyle(color = ColorProvider(Faint), fontSize = 11.sp),
                    maxLines = 1,
                )
            }

            // Whole-widget tap opens the app.
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(androidx.glance.action.actionStartActivity(MainActivity::class.java)),
            ) {}
        }
    }

    @Composable
    private fun AddButton(context: Context) {
        Box(
            modifier = GlanceModifier
                .size(34.dp)
                .background(ColorProvider(Accent))
                .clickable(
                    actionStartActivity(
                        Intent(context, MainActivity::class.java)
                            .setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
                            )
                            .putExtra(MainActivity.EXTRA_QUICK_ADD_TYPE, "EXPENSE"),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            androidx.glance.Image(
                provider = ImageProvider(R.drawable.ic_widget_add),
                contentDescription = "Add expense",
                modifier = GlanceModifier.size(20.dp),
            )
        }
    }

    companion object {
        private val Accent = Color(0xFF34D399)
        private val Muted = Color(0xFF94A3B8)
        private val Faint = Color(0xFF64748B)
    }
}

class BudgetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BudgetWidget()
}
