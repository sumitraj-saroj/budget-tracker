package com.budgettracker.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.data.UserPrefs
import com.budgettracker.app.ui.AppRoot
import com.budgettracker.app.ui.theme.BudgetTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            appViewModel.prefs.value == null
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val prefs by appViewModel.prefs.collectAsStateWithLifecycle()
            val p = prefs ?: UserPrefs()
            BudgetTrackerTheme(
                themeMode = p.themeMode,
                accent = Color(p.accentArgb.toInt()),
                useDynamicColor = p.useDynamicColor,
                amoledBlack = p.amoledBlack,
            ) {
                AppRoot(appViewModel = appViewModel)
            }
        }
    }
}
