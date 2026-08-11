package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.ForestMid
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.util.DhikrLexicon
import java.time.LocalDate

private val HeroShape = RoundedCornerShape(28.dp)
private val TileShape = RoundedCornerShape(18.dp)
private val StripShape = RoundedCornerShape(16.dp)

@Composable
fun SummaryScreen(
    frames: List<FrameToday>,
    onFocus: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    val dua = remember { DhikrLexicon.duaForDay(LocalDate.now().dayOfYear) }
    val todayTotal = frames.sumOf { it.todayCount }

    Scaffold(containerColor = Forest, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.summary),
                color = Cream.copy(alpha = 0.72f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HeroShape)
                    .background(ForestMid)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
            ) {
                Text(
                    stringResource(R.string.summary_hero_label),
                    color = Cream.copy(alpha = 0.78f),
                    fontSize = 16.sp,
                )
                Text(
                    stringResource(R.string.summary_hero_count, todayTotal),
                    color = Gold,
                    fontSize = 56.sp,
                    lineHeight = 62.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    stringResource(R.string.today).lowercase(),
                    color = Cream.copy(alpha = 0.5f),
                    fontSize = 15.sp,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(StripShape)
                    .background(Cream.copy(alpha = 0.08f))
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    dua.arabic,
                    color = Cream,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    dua.latin,
                    color = GoldLight.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
            if (frames.isEmpty()) {
                Text(
                    stringResource(R.string.summary_empty),
                    color = GoldLight,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                frames.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        pair.forEach { item ->
                            DhikrBentoTile(
                                item = item,
                                onClick = { onFocus(item.frame.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.summary_thanks),
                color = Cream.copy(alpha = 0.55f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.summary_thanks_signoff),
                color = Gold.copy(alpha = 0.72f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DhikrBentoTile(
    item: FrameToday,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val target = item.target
    val fraction = if (target != null && target > 0) {
        (item.todayCount.toFloat() / target).coerceIn(0f, 1f)
    } else {
        null
    }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .heightIn(min = 108.dp)
            .clip(TileShape)
            .background(ForestMid)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            item.frame.arabic,
            color = Cream,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f))
        if (fraction != null) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Cream.copy(alpha = 0.12f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(Gold),
                )
            }
        }
        Text(
            text = buildString {
                append(item.todayCount)
                target?.let { append(" / $it") }
            },
            color = Gold.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
