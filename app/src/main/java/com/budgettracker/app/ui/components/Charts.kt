package com.budgettracker.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

/**
 * An animated donut chart. [fractions] holds colors with values normalized so
 * they sum to 1.0.
 */
@Composable
fun DonutChart(
    fractions: List<Pair<Color, Float>>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 24.dp,
    gapAngle: Float = 3f,
    trackColor: Color = Color.Transparent,
    centerContent: (@Composable BoxScope.() -> Unit)? = null,
) {
    val animation = remember { Animatable(0f) }
    LaunchedEffect(fractions) {
        animation.snapTo(0f)
        animation.animateTo(1f, tween(750, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
    }
    Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = min(size.width, size.height) - stroke
            if (diameter <= 0f) return@Canvas
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            if (trackColor != Color.Transparent && fractions.isNotEmpty()) {
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
            if (fractions.isEmpty()) return@Canvas
            val totalGap = gapAngle * fractions.size
            val available = (360f - totalGap).coerceAtLeast(1f)
            var start = -90f
            fractions.forEach { (color, fraction) ->
                val sweep = fraction * available * animation.value
                if (sweep > 0.4f) {
                    drawArc(
                        color = color,
                        startAngle = start + gapAngle / 2f,
                        sweepAngle = (sweep - gapAngle).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
                start += fraction * available
            }
        }
        centerContent?.invoke(this)
    }
}

data class BarEntry(
    val label: String,
    val income: Long,
    val expense: Long,
)

/** Grouped income/expense bar chart with a dashed grid. */
@Composable
fun GroupedBarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    incomeColor: Color = Color(0xFF22C55E),
    expenseColor: Color = Color(0xFFEF4444),
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant,
    formatValue: (Long) -> String,
) {
    val textMeasurer = rememberTextMeasurer()
    val animation = remember(entries) { Animatable(0f) }
    LaunchedEffect(entries) {
        animation.snapTo(0f)
        animation.animateTo(1f, tween(650, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
    }
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxWidth().height(190.dp)) {
        if (entries.isEmpty()) return@Canvas
        val maxValue = max(entries.maxOf { max(it.income, it.expense) }, 1L)
        val labelHeight = 26.dp.toPx()
        val topPad = 22.dp.toPx()
        val chartHeight = size.height - labelHeight - topPad

        val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)
        for (i in 0..3) {
            val y = topPad + chartHeight * (1f - i / 3f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)),
            )
            val gridLabel = formatValue(maxValue * i / 3)
            drawText(textMeasurer, gridLabel, topLeft = Offset(0f, y - 13.dp.toPx()), style = labelStyle)
        }

        val slot = size.width / entries.size
        val barWidth = (slot * 0.24f).coerceAtMost(16.dp.toPx())
        val corner = CornerRadius(barWidth / 2f, barWidth / 2f)
        entries.forEachIndexed { index, entry ->
            val centerX = slot * index + slot / 2f
            val incomeHeight = chartHeight * (entry.income.toFloat() / maxValue) * animation.value
            val expenseHeight = chartHeight * (entry.expense.toFloat() / maxValue) * animation.value
            if (entry.income > 0) {
                drawRoundRect(
                    color = incomeColor,
                    topLeft = Offset(centerX - barWidth - 3.dp.toPx(), topPad + chartHeight - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = corner,
                )
            }
            if (entry.expense > 0) {
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(centerX + 3.dp.toPx(), topPad + chartHeight - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = corner,
                )
            }
            val layout = textMeasurer.measure(entry.label, labelStyle)
            drawText(
                layout,
                topLeft = Offset(centerX - layout.size.width / 2f, size.height - labelHeight + 6.dp.toPx()),
            )
        }
    }
}

/**
 * Smoothed line chart with a gradient fill. [points] are normalized 0..1.
 */
@Composable
fun TrendChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    xLabels: List<String> = emptyList(),
    formatValue: (Float) -> String = { "" },
) {
    val textMeasurer = rememberTextMeasurer()
    val animation = remember(points) { Animatable(0f) }
    LaunchedEffect(points) {
        animation.snapTo(0f)
        animation.animateTo(1f, tween(650, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
    }
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxWidth().height(150.dp)) {
        if (points.size < 2) return@Canvas
        val labelHeight = if (xLabels.isEmpty()) 0f else 22.dp.toPx()
        val topPad = 12.dp.toPx()
        val chartHeight = size.height - labelHeight - topPad

        val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)
        for (i in 0..2) {
            val y = topPad + chartHeight * (1f - i / 2f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)),
            )
            if (i > 0) {
                drawText(textMeasurer, formatValue(i / 2f), topLeft = Offset(0f, y - 13.dp.toPx()), style = labelStyle)
            }
        }

        fun pointAt(index: Int): Offset {
            val x = size.width * (index.toFloat() / (points.size - 1))
            val y = topPad + chartHeight * (1f - points[index] * animation.value)
            return Offset(x, y)
        }

        val linePath = Path().apply {
            moveTo(pointAt(0).x, pointAt(0).y)
            for (i in 1 until points.size) {
                val prev = pointAt(i - 1)
                val curr = pointAt(i)
                val midX = (prev.x + curr.x) / 2f
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            }
        }
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(size.width, topPad + chartHeight)
            lineTo(0f, topPad + chartHeight)
            close()
        }
        drawPath(fillPath, brush = Brush.verticalGradient(listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0.01f)), startY = topPad, endY = topPad + chartHeight))
        drawPath(linePath, color = color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))

        if (xLabels.isNotEmpty()) {
            val labelLayouts = xLabels.map { textMeasurer.measure(it, labelStyle) }
            xLabels.forEachIndexed { i, _ ->
                val fraction = if (xLabels.size == 1) 0.5f else i.toFloat() / (xLabels.size - 1)
                val layout = labelLayouts[i]
                val x = (size.width * fraction - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width)
                drawText(layout, topLeft = Offset(x, size.height - labelHeight + 4.dp.toPx()))
            }
        }
    }
}
