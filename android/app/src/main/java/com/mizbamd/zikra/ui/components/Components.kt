package com.mizbamd.zikra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.theme.CardWhite
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.ForestDark
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.ui.theme.Ink
import com.mizbamd.zikra.util.DisplayDates
import com.mizbamd.zikra.util.LocationLabel

@Composable
fun DateHeader(dates: DisplayDates, modifier: Modifier = Modifier) {
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
    }
}

@Composable
fun FrameCard(
    item: FrameToday,
    onCount: () -> Unit,
    onArabic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardWhite)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            item.frame.arabic,
            color = Ink,
            fontSize = 26.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onArabic)
                .padding(vertical = 8.dp),
        )
        Text(
            item.frame.transliteration,
            color = Ink.copy(alpha = 0.55f),
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onCount)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = buildString {
                    append(item.todayCount)
                    item.target?.let { append(" / $it") }
                },
                color = Gold,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            "${stringResource(R.string.lifetime)} ${item.frame.lifetimeCount}",
            color = Ink.copy(alpha = 0.45f),
            fontSize = 11.sp,
        )
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
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = ForestDark),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
fun QuietTextButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = GoldLight)
    }
}

@Composable
fun CounterActions(onUndo: () -> Unit, onReset: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        QuietTextButton(stringResource(R.string.undo), onClick = onUndo)
        QuietTextButton(stringResource(R.string.reset_today), onClick = onReset)
    }
}
