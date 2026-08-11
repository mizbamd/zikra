package com.mizbamd.zikra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.ForestDark
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.ui.theme.Ink
import com.mizbamd.zikra.ui.theme.OnGreen
import com.mizbamd.zikra.util.DisplayDates
import com.mizbamd.zikra.util.LocationLabel

@Composable
fun DateHeader(dates: DisplayDates, streakDays: Int = 0, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            dates.gregorian,
            color = Cream,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            dates.hijri,
            color = GoldLight,
            fontSize = 15.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            stringResource(
                if (dates.locationLabelKey == LocationLabel.REAL) {
                    R.string.based_on_location
                } else {
                    R.string.sample_location
                },
            ),
            color = Cream.copy(alpha = 0.65f),
            fontSize = 12.sp,
        )
        if (streakDays > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                pluralStringResource(R.plurals.streak_days, streakDays, streakDays),
                color = Gold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun FrameCard(
    item: FrameToday,
    onCount: () -> Unit,
    onArabic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val plaque = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp)
            .background(Cream, plaque)
            .border(1.5.dp, Gold, plaque)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Gold.copy(alpha = 0.08f))
                .clickable(onClick = onCount)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = buildString {
                    append(item.todayCount)
                    item.target?.let { append("/$it") }
                },
                color = Gold.copy(alpha = 0.78f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Text(
                "${stringResource(R.string.lifetime)} ${item.frame.lifetimeCount}",
                color = Ink.copy(alpha = 0.38f),
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onArabic),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                item.frame.arabic,
                color = Ink,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                item.frame.transliteration,
                color = Ink.copy(alpha = 0.55f),
                fontSize = 13.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
        }
    }
}

/** Immersive count surface: Arabic is the hero; digits are tucked and secondary. */
@Composable
fun DhikrCountSurface(
    arabic: String,
    transliteration: String,
    todayCount: Int,
    target: Int?,
    showDone: Boolean,
    onCount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val plaque = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(plaque)
                    .background(Cream.copy(alpha = 0.06f))
                    .border(1.5.dp, Gold.copy(alpha = 0.9f), plaque)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCount,
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    arabic,
                    color = Cream,
                    fontSize = 64.sp,
                    lineHeight = 76.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    transliteration,
                    color = Cream.copy(alpha = 0.55f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = buildString {
                    append(todayCount)
                    target?.let { append(" / $it") }
                },
                color = Gold.copy(alpha = 0.48f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            if (showDone) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.done_quiet),
                    color = GoldLight.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
fun GoldButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = ForestDark,
            disabledContainerColor = Gold.copy(alpha = 0.28f),
            disabledContentColor = OnGreen,
        ),
    ) {
        Text(
            text,
            color = if (enabled) ForestDark else OnGreen,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun QuietTextButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text, color = if (enabled) GoldLight else GoldLight.copy(alpha = 0.4f))
    }
}

@Composable
fun CounterActions(
    onUndo: () -> Unit,
    onReset: () -> Unit,
    undoEnabled: Boolean = true,
    resetEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GoldActionButton(
            text = stringResource(R.string.undo),
            enabled = undoEnabled,
            modifier = Modifier.weight(1f),
            onClick = onUndo,
        )
        GoldActionButton(
            text = stringResource(R.string.reset_today),
            enabled = resetEnabled,
            modifier = Modifier.weight(1f),
            onClick = onReset,
        )
    }
}

@Composable
private fun GoldActionButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold,
            contentColor = ForestDark,
            disabledContainerColor = Gold.copy(alpha = 0.28f),
            disabledContentColor = OnGreen,
        ),
    ) {
        Text(
            text,
            color = if (enabled) ForestDark else OnGreen,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            maxLines = 1,
        )
    }
}
