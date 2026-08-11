package com.mizbamd.zikra.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/**
 * Arabic hero text that wraps fully. Grows with content first; only then scales
 * down a little if [maxLinesBeforeScale] (or [heightBudget]) would be exceeded.
 * Never uses ellipsis as the only strategy — the floor size still wraps.
 */
@Composable
internal fun AutoFitDhikrText(
    text: String,
    color: Color,
    textAlign: TextAlign,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    lineHeightRatio: Float,
    maxLinesBeforeScale: Int,
    modifier: Modifier = Modifier,
    heightBudget: Dp? = null,
    fontFamily: FontFamily = FontFamily.Serif,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val baseStyle = remember(color, textAlign, fontFamily, fontWeight) {
        TextStyle(
            color = color,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            textAlign = textAlign,
        )
    }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthPx = constraints.maxWidth
        val heightPx = heightBudget?.let { budget ->
            if (budget.isSpecified) with(density) { budget.roundToPx() } else null
        }
        val fontSp = remember(
            text,
            widthPx,
            heightPx,
            maxFontSize,
            minFontSize,
            lineHeightRatio,
            maxLinesBeforeScale,
        ) {
            chooseDhikrFontSp(
                text = text,
                textMeasurer = textMeasurer,
                maxWidthPx = widthPx,
                maxHeightPx = heightPx,
                maxFontSp = maxFontSize.value,
                minFontSp = minFontSize.value,
                lineHeightRatio = lineHeightRatio,
                maxLinesBeforeScale = maxLinesBeforeScale,
                baseStyle = baseStyle,
            )
        }
        Text(
            text = text,
            color = color,
            fontSize = fontSp.sp,
            lineHeight = (fontSp * lineHeightRatio).sp,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            textAlign = textAlign,
            softWrap = true,
            overflow = TextOverflow.Visible,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

internal fun chooseDhikrFontSp(
    text: String,
    textMeasurer: TextMeasurer,
    maxWidthPx: Int,
    maxHeightPx: Int?,
    maxFontSp: Float,
    minFontSp: Float,
    lineHeightRatio: Float,
    maxLinesBeforeScale: Int,
    baseStyle: TextStyle,
): Float {
    if (text.isEmpty() || maxWidthPx <= 0 || maxWidthPx == Constraints.Infinity) {
        return maxFontSp
    }
    if (maxHeightPx != null && maxHeightPx <= 0) return minFontSp

    fun fits(fontSp: Float): Boolean {
        val layout = textMeasurer.measure(
            text = AnnotatedString(text),
            style = baseStyle.copy(
                fontSize = fontSp.sp,
                lineHeight = (fontSp * lineHeightRatio).sp,
            ),
            overflow = TextOverflow.Visible,
            softWrap = true,
            constraints = Constraints(maxWidth = maxWidthPx),
        )
        if (layout.lineCount > maxLinesBeforeScale) return false
        if (maxHeightPx != null && layout.size.height > maxHeightPx) return false
        return true
    }

    if (fits(maxFontSp)) return maxFontSp
    if (!fits(minFontSp)) return minFontSp

    var lo = minFontSp
    var hi = maxFontSp
    repeat(12) {
        val mid = (lo + hi) / 2f
        if (fits(mid)) lo = mid else hi = mid
    }
    return lo
}
