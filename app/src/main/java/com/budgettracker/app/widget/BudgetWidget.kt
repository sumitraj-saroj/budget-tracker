package com.budgettracker.app.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
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
import com.budgettracker.app.util.Periods
import com.budgettracker.app.util.formatMoney
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Widget-facing snapshot of the numbers shown on Home. */
data class WidgetSnapshot(
    val baseCurrency: String,
    val totalBalanceMinor: Long,
    val monthExpenseMinor: Long,
    val totalExpenseMinor: Long,
    val expenseCount: Int,
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
        val totalBalance = Insights.totalBalanceBase(balances, base)
        val month = Periods.currentMonth()
        val monthExpenses = Insights.sumBetween(txs, com.budgettracker.app.data.db.TxType.EXPENSE, month, base)
        val allExpenses = txs.filter { it.tx.type == com.budgettracker.app.data.db.TxType.EXPENSE }
        val totalExpenses = allExpenses.sumOf { Insights.txAmountInBase(it, base) }
        return WidgetSnapshot(
            baseCurrency = base,
            totalBalanceMinor = totalBalance,
            monthExpenseMinor = monthExpenses,
            totalExpenseMinor = totalExpenses,
            expenseCount = allExpenses.size,
        )
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BudgetWidgetEntryPoint {
    fun widgetDataProvider(): WidgetDataProvider
}

class BudgetWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        android.util.Log.d("BudgetWidget", "provideGlance started for id: $id")
        val snapshot = runCatching {
            val provider = EntryPointAccessors
                .fromApplication(context.applicationContext, BudgetWidgetEntryPoint::class.java)
                .widgetDataProvider()
            provider.snapshot()
        }.onFailure {
            android.util.Log.e("BudgetWidget", "Failed to fetch widget snapshot", it)
        }.getOrElse {
            WidgetSnapshot(
                baseCurrency = "USD",
                totalBalanceMinor = 0,
                monthExpenseMinor = 0,
                totalExpenseMinor = 0,
                expenseCount = 0,
            )
        }
        android.util.Log.d("BudgetWidget", "provideGlance snapshot: $snapshot")
        provideContent {
            Content(snapshot, context)
        }
    }

    @Composable
    private fun Content(snapshot: WidgetSnapshot, context: Context) {
        val currency = Currencies.byCode(snapshot.baseCurrency)
        val monthExpenseFormatted = formatMoney(snapshot.monthExpenseMinor, currency)
        val balanceFormatted = formatMoney(snapshot.totalBalanceMinor, currency)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }

        val addIconBitmap = remember(context) { createAddIconBitmap(context) }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(WidgetBg)
                .cornerRadius(22.dp)
                .clickable(actionStartActivity(openAppIntent)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(14.dp),
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "THIS MONTH'S EXPENSES",
                        style = TextStyle(
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    AddButton(context, addIconBitmap)
                }
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    monthExpenseFormatted,
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    "Balance $balanceFormatted • Tap to see all",
                    style = TextStyle(
                        color = TextFaint,
                        fontSize = 11.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
    }

    @Composable
    private fun AddButton(context: Context, iconBitmap: Bitmap) {
        val quickAddIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.budgettracker.app.ACTION_QUICK_ADD_EXPENSE"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_QUICK_ADD_TYPE, "EXPENSE")
        }

        Box(
            modifier = GlanceModifier
                .size(34.dp)
                .background(BtnBg)
                .cornerRadius(17.dp)
                .clickable(actionStartActivity(quickAddIntent)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(iconBitmap),
                contentDescription = "Add expense",
                modifier = GlanceModifier.size(18.dp),
            )
        }
    }

    companion object {
        private val WidgetBg = ColorProvider(R.color.widget_bg)
        private val TextPrimary = ColorProvider(R.color.widget_text_primary)
        private val TextMuted = ColorProvider(R.color.widget_text_muted)
        private val TextFaint = ColorProvider(R.color.widget_text_faint)
        private val BtnBg = ColorProvider(R.color.widget_btn_bg)

        private fun createAddIconBitmap(context: Context): Bitmap {
            val sizePx = (20 * context.resources.displayMetrics.density).toInt().coerceAtLeast(24)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                strokeWidth = sizePx * 0.16f
                strokeCap = Paint.Cap.ROUND
            }
            val mid = sizePx / 2f
            val pad = sizePx * 0.22f
            canvas.drawLine(pad, mid, sizePx - pad, mid, paint)
            canvas.drawLine(mid, pad, mid, sizePx - pad, paint)
            return bitmap
        }
    }
}

class BudgetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BudgetWidget()
}
