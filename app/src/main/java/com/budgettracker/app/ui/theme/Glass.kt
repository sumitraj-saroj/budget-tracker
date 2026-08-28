package com.budgettracker.app.ui.theme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** Whether the user enabled the "Liquid glass" style (Settings → Appearance). */
val LocalLiquidGlass = staticCompositionLocalOf { false }

/** The [HazeState] shared by the app's glass surfaces; null when no source is active. */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/** Blur radius kept modest for performance. */
private val GlassBlurRadius = 18.dp

/** Alpha of the surface-colored tint drawn over the blurred backdrop. */
private const val GlassTintAlpha = 0.6f

/** Faint white border stroke on glass surfaces. */
private val GlassBorderStroke = 1.dp
private const val GlassBorderAlpha = 0.2f

/**
 * Whether glass surfaces should render the blur effect right now. Requires the
 * user toggle, Android 12+ (RenderEffect), and no battery saver. When false,
 * callers must fall back to their solid colors.
 */
@Composable
fun rememberGlassEnabled(): Boolean {
    val liquidGlass = LocalLiquidGlass.current
    val context = LocalContext.current
    val powerManager = remember(context) {
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    }
    var powerSave by remember { mutableStateOf(powerManager?.isPowerSaveMode ?: false) }
    // Track battery saver live: the system broadcast, plus a refresh on resume
    // since some OEM skins don't deliver the broadcast reliably.
    DisposableEffect(context, powerManager) {
        if (powerManager == null) return@DisposableEffect onDispose { }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                powerSave = powerManager.isPowerSaveMode
            }
        }
        // Protected system broadcast — only the system can send it.
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                powerSave = powerManager?.isPowerSaveMode ?: false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return liquidGlass &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        !powerSave
}

/**
 * Glass style for a translucent surface: surface-colored tint (~60% alpha) over
 * the blurred backdrop, with an opaque-ish [fallbackTint] for scrim fallbacks.
 *
 * [backgroundColor] is required by Haze's blur path and is drawn behind the
 * blurred content — use the screen background for a uniform frosted look.
 *
 * @param tint base color of the tint; defaults to the theme surface color.
 * @param backgroundColor opaque color drawn behind the blurred content.
 */
@Composable
fun rememberGlassStyle(
    tint: Color = MaterialTheme.colorScheme.surface,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
): HazeStyle = HazeStyle(
    backgroundColor = backgroundColor,
    tints = listOf(HazeTint(tint.copy(alpha = GlassTintAlpha))),
    blurRadius = GlassBlurRadius,
    noiseFactor = 0.15f,
    fallbackTint = HazeTint(tint.copy(alpha = 0.9f)),
)

/** Applies the haze blur effect when [enabled]; no-op otherwise. */
fun Modifier.glassEffect(
    state: HazeState?,
    enabled: Boolean,
    style: HazeStyle,
): Modifier = when {
    !enabled || state == null -> this
    else -> hazeEffect(state = state, style = style) { blurEnabled = true }
}

/** Faint white border stroke that sells the glass edge; no-op when disabled. */
fun Modifier.glassBorder(shape: Shape, enabled: Boolean): Modifier = when {
    !enabled -> this
    else -> border(GlassBorderStroke, Color.White.copy(alpha = GlassBorderAlpha), shape)
}

/** Surface color: fully transparent when glass is on so the blur shows through. */
fun glassSurfaceColor(solid: Color, glass: Boolean): Color =
    if (glass) Color.Transparent else solid
