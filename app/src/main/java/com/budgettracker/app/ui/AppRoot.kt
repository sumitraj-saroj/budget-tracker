package com.budgettracker.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

    androidx.compose.animation.AnimatedContent(
        targetState = when {
            !prefs.onboardingDone -> 0
            locked -> 1
            else -> 2
        },
        transitionSpec = { androidx.compose.animation.fadeIn() togetherWith androidx.compose.animation.fadeOut() },
        label = "root",
        modifier = Modifier,
    ) { state ->
        when (state) {
            0 -> com.budgettracker.app.ui.onboarding.OnboardingScreen()
            1 -> LockScreen(onUnlock = appViewModel::unlock)
            else -> MainNavHost()
        }
    }
}
