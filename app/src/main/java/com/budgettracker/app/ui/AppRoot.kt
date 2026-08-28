package com.budgettracker.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgettracker.app.AppViewModel
import com.budgettracker.app.ui.onboarding.OnboardingScreen

@Composable
fun AppRoot(appViewModel: AppViewModel) {
    val prefs = appViewModel.prefs.collectAsStateWithLifecycle().value
    val locked = appViewModel.locked.collectAsStateWithLifecycle().value

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

    when {
        // Hold a blank surface until prefs load — the splash screen stays up
        // during this window, so nothing flickers.
        prefs == null -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        !prefs.onboardingDone -> OnboardingScreen()
        locked -> LockScreen(onUnlock = appViewModel::unlock)
        else -> MainNavHost()
    }
}
