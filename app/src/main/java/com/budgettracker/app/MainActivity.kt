package com.budgettracker.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.budgettracker.app.data.UserPrefs
import com.budgettracker.app.ui.AppRoot
import com.budgettracker.app.ui.theme.BudgetTrackerTheme
import com.budgettracker.app.widget.BudgetWidgetRefresher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val appViewModel: AppViewModel by viewModels()

    @Inject lateinit var widgetRefresher: BudgetWidgetRefresher

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            appViewModel.prefs.value == null
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Fresh launches only: after process death the system redelivers the
        // original launch intent, and handling it again would stack a second
        // editor on top of the restored navigation state.
        if (savedInstanceState == null) handleWidgetIntent(intent)
        setContent {
            val prefs by appViewModel.prefs.collectAsStateWithLifecycle()
            val p = prefs ?: UserPrefs()
            BudgetTrackerTheme(
                themeMode = p.themeMode,
                accent = Color(p.accentArgb.toInt()),
                useDynamicColor = p.useDynamicColor,
                amoledBlack = p.amoledBlack,
                liquidGlass = p.liquidGlass,
            ) {
                AppRoot(appViewModel = appViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleWidgetIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { widgetRefresher.refresh() }
    }

    private fun handleWidgetIntent(intent: Intent?) {
        if (intent?.hasExtra(EXTRA_QUICK_ADD_TYPE) == true) {
            appViewModel.requestQuickAdd(intent.getStringExtra(EXTRA_QUICK_ADD_TYPE) ?: "EXPENSE")
            intent.removeExtra(EXTRA_QUICK_ADD_TYPE)
        }
    }

    companion object {
        const val EXTRA_QUICK_ADD_TYPE = "extra_quick_add_type"
    }
}
