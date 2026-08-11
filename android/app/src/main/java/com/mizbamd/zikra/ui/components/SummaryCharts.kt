package com.mizbamd.zikra.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mizbamd.zikra.ui.theme.Gold

private val ChartAnim = tween<Float>(durationMillis = 720, easing = FastOutSlowInEasing)

@Composable
fun TodayDonut(
    slices: List<Pair<Color, Float>>,
    modifier: Modifier = Modifier,
    diameter: Dp = 208.dp,
    stroke: Dp = 26.dp,
) {
    var revealed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(slices) { revealed = 1f }
    val progress by animateFloatAsState(revealed, ChartAnim, label = "donut")
    val total = slices.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    Canvas(modifier.size(diameter)) {
        val strokePx = stroke.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(size.minDimension - strokePx, size.minDimension - strokePx)
        val topLeft = Offset(inset, inset)
        val gap = if (slices.size > 1) 2.4f else 0f
        var start = -90f
        slices.forEach { (color, value) ->
            val sweep = (value / total) * 360f * progress
            val visible = (sweep - gap).coerceAtLeast(0f)
            if (visible > 0.4f) {
                drawArc(
                    color = color,
                    startAngle = start + gap / 2f,
                    sweepAngle = visible,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                )
            }
            start += sweep
        }
    }
}

@Composable
fun TodayProgressRing(
    fraction: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 208.dp,
    stroke: Dp = 22.dp,
    track: Color = Gold.copy(alpha = 0.22f),
    fill: Color = Gold,
) {
    var revealed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(fraction) { revealed = fraction.coerceIn(0f, 1f) }
    val progress by animateFloatAsState(revealed, ChartAnim, label = "ring")
    Canvas(modifier.size(diameter)) {
        val strokePx = stroke.toPx()
        val inset = strokePx / 2f
        val arcSize = Size(size.minDimension - strokePx, size.minDimension - strokePx)
        val topLeft = Offset(inset, inset)
        drawArc(
            color = track,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
        if (progress > 0.002f) {
            drawArc(
                color = fill,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun WeekSpark(
    totals: List<Int>,
    modifier: Modifier = Modifier,
    bar: Color = Gold,
    track: Color = Gold.copy(alpha = 0.16f),
) {
    var revealed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(totals) { revealed = 1f }
    val reveal by animateFloatAsState(revealed, ChartAnim, label = "spark")
    val peak = totals.maxOrNull()?.coerceAtLeast(1) ?: 1
    Canvas(
        modifier
            .fillMaxWidth()
            .height(40.dp),
    ) {
        if (totals.isEmpty()) return@Canvas
        val n = totals.size
        val gap = 5.dp.toPx()
        val barW = ((size.width - gap * (n - 1)) / n).coerceAtLeast(4.dp.toPx())
        val radius = CornerRadius(barW / 2f, barW / 2f)
        totals.forEachIndexed { i, value ->
            val x = i * (barW + gap)
            drawRoundRect(
                color = track,
                topLeft = Offset(x, 0f),
                size = Size(barW, size.height),
                cornerRadius = radius,
            )
            val h = (size.height * (value.toFloat() / peak) * reveal)
                .coerceAtLeast(if (value > 0) 4.dp.toPx() else 0f)
            if (h > 0f) {
                val alpha = if (i == n - 1) 1f else 0.72f
                drawRoundRect(
                    color = bar.copy(alpha = alpha),
                    topLeft = Offset(x, size.height - h),
                    size = Size(barW, h),
                    cornerRadius = radius,
                )
            }
        }
    }
}
