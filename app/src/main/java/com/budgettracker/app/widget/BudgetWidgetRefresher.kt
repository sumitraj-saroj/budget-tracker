package com.budgettracker.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.room.InvalidationTracker
import com.budgettracker.app.data.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Re-renders the widget. Data changes are picked up automatically by watching
 * the Room invalidation tracker, so callers don't need to remember to refresh
 * after every mutation; [refresh] remains as a safety net (e.g. app open).
 */
@Singleton
class BudgetWidgetRefresher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pending: Job? = null

    /** Starts observing data changes; call once from Application.onCreate. */
    fun startObserving() {
        database.invalidationTracker.addObserver(
            object : InvalidationTracker.Observer("transactions", "budgets", "accounts") {
                override fun onInvalidated(tables: Set<String>) {
                    onChanged()
                }
            },
        )
    }

    private fun onChanged() {
        pending?.cancel()
        pending = scope.launch {
            delay(DEBOUNCE_MS)
            refresh()
        }
    }

    suspend fun refresh() {
        runCatching {
            android.util.Log.d("BudgetWidgetRefresher", "Calling BudgetWidget().updateAll(context)")
            BudgetWidget().updateAll(context)
            android.util.Log.d("BudgetWidgetRefresher", "BudgetWidget().updateAll(context) succeeded")
        }.onFailure {
            android.util.Log.e("BudgetWidgetRefresher", "Failed to update widget", it)
        }
    }

    companion object {
        private const val DEBOUNCE_MS = 500L
    }
}
