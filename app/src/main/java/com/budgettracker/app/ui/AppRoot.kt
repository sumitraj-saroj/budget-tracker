package com.budgettracker.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.AppViewModel
import com.budgettracker.app.ui.onboarding.OnboardingScreen

@Composable
fun AppRoot(appViewModel: AppViewModel) {
    val prefs by appViewModel.prefs.collectAsStateWithLifecycle()
    val locked by appViewModel.locked.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> appViewModel.onAppPaused()
                Lifecycle.Event.ON_RESUME -> appViewModel.onAppResumed()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AnimatedContent(
        targetState = when {
            !prefs.onboardingDone -> 0
            locked -> 1
            else -> 2
        },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "root",
    ) { state ->
        when (state) {
            0 -> OnboardingScreen()
            1 -> LockScreen(onUnlock = appViewModel::unlock)
            else -> MainNavHost()
        }
    }
}
