package com.budgettracker.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.ui.AppRoot
import com.budgettracker.app.ui.theme.BudgetTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val appViewModel: AppViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val prefs by appViewModel.prefs.collectAsStateWithLifecycle()
            BudgetTrackerTheme(
                themeMode = prefs.themeMode,
                accent = Color(prefs.accentArgb.toInt()),
                useDynamicColor = prefs.useDynamicColor,
                amoledBlack = prefs.amoledBlack,
            ) {
                AppRoot(appViewModel = appViewModel)
            }
        }
    }
}
