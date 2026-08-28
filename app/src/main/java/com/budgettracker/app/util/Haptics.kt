package com.budgettracker.app.util

import android.view.HapticFeedbackConstants
import android.view.View

/** Light tick for selections and toggles. */
fun View.hapticTick() = performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)

/** Keyboard-style tap for keypads and confirmations. */
fun View.hapticKey() = performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
