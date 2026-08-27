package com.budgettracker.app.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.pow

/**
 * Generates Material 3 style tonal palettes from a seed color using an HSL
 * approximation of Material's HCT color space. Good enough to feel native,
 * with zero external dependencies.
 */

private const val ERROR_HUE = 25f
private const val ERROR_SAT = 0.78f

fun lightScheme(seed: Color): androidx.compose.material3.ColorScheme {
    val (h, s, _) = seed.toHsl()
    val neutralSat = s * 0.10f
    val neutralVariantSat = s * 0.22f
    val secondarySat = s * 0.32f
    val tertiaryHue = (h + 60f) % 360f
    val tertiarySat = s * 0.65f

    return androidx.compose.material3.ColorScheme(
        primary = tone(h, s, 40),
        onPrimary = tone(h, s, 100),
        primaryContainer = tone(h, s, 90),
        onPrimaryContainer = tone(h, s, 10),
        inversePrimary = tone(h, s, 80),
        secondary = tone(h, secondarySat, 40),
        onSecondary = tone(h, secondarySat, 100),
        secondaryContainer = tone(h, secondarySat, 90),
        onSecondaryContainer = tone(h, secondarySat, 10),
        tertiary = tone(tertiaryHue, tertiarySat, 40),
        onTertiary = tone(tertiaryHue, tertiarySat, 100),
        tertiaryContainer = tone(tertiaryHue, tertiarySat, 90),
        onTertiaryContainer = tone(tertiaryHue, tertiarySat, 10),
        background = tone(h, neutralSat, 98),
        onBackground = tone(h, neutralSat, 10),
        surface = tone(h, neutralSat, 98),
        onSurface = tone(h, neutralSat, 10),
        surfaceVariant = tone(h, neutralVariantSat, 90),
        onSurfaceVariant = tone(h, neutralVariantSat, 30),
        surfaceTint = tone(h, s, 40),
        inverseSurface = tone(h, neutralSat, 20),
        inverseOnSurface = tone(h, neutralSat, 95),
        error = tone(ERROR_HUE, ERROR_SAT, 40),
        onError = tone(ERROR_HUE, ERROR_SAT, 100),
        errorContainer = tone(ERROR_HUE, ERROR_SAT, 90),
        onErrorContainer = tone(ERROR_HUE, ERROR_SAT, 10),
        outline = tone(h, neutralVariantSat, 50),
        outlineVariant = tone(h, neutralVariantSat, 80),
        scrim = Color.Black,
        surfaceBright = tone(h, neutralSat, 98),
        surfaceDim = tone(h, neutralSat, 87),
        surfaceContainer = tone(h, neutralSat, 94),
        surfaceContainerHigh = tone(h, neutralSat, 92),
        surfaceContainerHighest = tone(h, neutralSat, 90),
        surfaceContainerLow = tone(h, neutralSat, 96),
        surfaceContainerLowest = tone(h, neutralSat, 100),
    )
}

fun darkScheme(seed: Color): androidx.compose.material3.ColorScheme {
    val (h, s, _) = seed.toHsl()
    val neutralSat = s * 0.10f
    val neutralVariantSat = s * 0.22f
    val secondarySat = s * 0.32f
    val tertiaryHue = (h + 60f) % 360f
    val tertiarySat = s * 0.65f

    return androidx.compose.material3.ColorScheme(
        primary = tone(h, s, 80),
        onPrimary = tone(h, s, 20),
        primaryContainer = tone(h, s, 30),
        onPrimaryContainer = tone(h, s, 90),
        inversePrimary = tone(h, s, 40),
        secondary = tone(h, secondarySat, 80),
        onSecondary = tone(h, secondarySat, 20),
        secondaryContainer = tone(h, secondarySat, 30),
        onSecondaryContainer = tone(h, secondarySat, 90),
        tertiary = tone(tertiaryHue, tertiarySat, 80),
        onTertiary = tone(tertiaryHue, tertiarySat, 20),
        tertiaryContainer = tone(tertiaryHue, tertiarySat, 30),
        onTertiaryContainer = tone(tertiaryHue, tertiarySat, 90),
        background = tone(h, neutralSat, 6),
        onBackground = tone(h, neutralSat, 90),
        surface = tone(h, neutralSat, 6),
        onSurface = tone(h, neutralSat, 90),
        surfaceVariant = tone(h, neutralVariantSat, 30),
        onSurfaceVariant = tone(h, neutralVariantSat, 80),
        surfaceTint = tone(h, s, 80),
        inverseSurface = tone(h, neutralSat, 90),
        inverseOnSurface = tone(h, neutralSat, 20),
        error = tone(ERROR_HUE, ERROR_SAT, 80),
        onError = tone(ERROR_HUE, ERROR_SAT, 20),
        errorContainer = tone(ERROR_HUE, ERROR_SAT, 30),
        onErrorContainer = tone(ERROR_HUE, ERROR_SAT, 90),
        outline = tone(h, neutralVariantSat, 60),
        outlineVariant = tone(h, neutralVariantSat, 30),
        scrim = Color.Black,
        surfaceBright = tone(h, neutralSat, 24),
        surfaceDim = tone(h, neutralSat, 6),
        surfaceContainer = tone(h, neutralSat, 12),
        surfaceContainerHigh = tone(h, neutralSat, 17),
        surfaceContainerHighest = tone(h, neutralSat, 22),
        surfaceContainerLow = tone(h, neutralSat, 10),
        surfaceContainerLowest = tone(h, neutralSat, 4),
    )
}

/** Tone ramp: t=0 black, t=50 pure seed hue, t=100 white. */
private fun tone(h: Float, s: Float, t: Int): Color {
    val l = t / 100f
    val distance = abs(t - 50f) / 50f
    val sat = s * (1f - distance.pow(2.2f))
    return hslColor(h, sat.coerceIn(0f, 1f), l)
}

private fun Color.toHsl(): FloatArray {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val l = (max + min) / 2f
    if (max == min) return floatArrayOf(0f, 0f, l)
    val d = max - min
    val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
    var h = when (max) {
        r -> (g - b) / d + (if (g < b) 6f else 0f)
        g -> (b - r) / d + 2f
        else -> (r - g) / d + 4f
    }
    h *= 60f
    return floatArrayOf(h, s, l)
}

private fun hslColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val hp = (h % 360f + 360f) % 360f / 60f
    val x = c * (1f - abs(hp % 2f - 1f))
    val m = l - c / 2f
    val (r, g, b) = when (hp.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r + m, g + m, b + m)
}
