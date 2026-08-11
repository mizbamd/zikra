package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.local.DailyCountEntity
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.components.TodayDonut
import com.mizbamd.zikra.ui.components.TodayProgressRing
import com.mizbamd.zikra.ui.components.WeekSpark
import com.mizbamd.zikra.ui.theme.ChartSlices
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.ForestMid
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.ui.theme.Ink
import com.mizbamd.zikra.util.DhikrLexicon
import java.time.LocalDate

private val PlaqueShape = RoundedCornerShape(22.dp)
private val StripShape = RoundedCornerShape(16.dp)

private enum class TodayChartKind { Empty, Progress, Donut }

@Composable
fun SummaryScreen(
    frames: List<FrameToday>,
    history: List<DailyCountEntity> = emptyList(),
    todayKey: String = "",
    streakDays: Int = 0,
    onFocus: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    val dua = remember { DhikrLexicon.duaForDay(LocalDate.now().dayOfYear) }
    val todayTotal = frames.sumOf { it.todayCount }
    val kind = when {
        frames.isEmpty() -> TodayChartKind.Empty
        frames.size == 1 -> TodayChartKind.Progress
        todayTotal == 0 -> TodayChartKind.Empty
        else -> TodayChartKind.Donut
    }
    val slices = remember(frames) {
        frames.mapIndexedNotNull { index, item ->
            if (item.todayCount <= 0) null
            else Triple(item, ChartSlices[index % ChartSlices.size], item.todayCount.toFloat())
        }
    }
    val weekTotals = remember(history, todayKey) { weekTotals(history, todayKey) }
    val showWeek = weekTotals.dropLast(1).any { it > 0 }
    val chartCd = stringResource(R.string.summary_chart_cd, todayTotal)

    Scaffold(
        containerColor = Forest,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.summary),
                    color = Cream.copy(alpha = 0.72f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                if (streakDays > 0) {
                    Text(
                        pluralStringResource(R.plurals.streak_days, streakDays, streakDays),
                        color = Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (frames.isEmpty()) {
                Text(
                    stringResource(R.string.summary_empty),
                    color = GoldLight,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PlaqueShape)
                        .background(Cream)
                        .border(1.5.dp, Gold, PlaqueShape)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.summary_hero_label),
                        color = Ink.copy(alpha = 0.55f),
                        fontSize = 15.sp,
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 8.dp)
                            .semantics { contentDescription = chartCd },
                        contentAlignment = Alignment.Center,
                    ) {
                        when (kind) {
                            TodayChartKind.Progress -> {
                                val item = frames.first()
                                val target = item.target
                                val fraction = if (target != null && target > 0) {
                                    item.todayCount.toFloat() / target
                                } else {
                                    if (item.todayCount > 0) 1f else 0f
                                }
                                TodayProgressRing(fraction = fraction)
                            }
                            TodayChartKind.Donut -> TodayDonut(
                                slices = slices.map { it.second to it.third },
                            )
                            TodayChartKind.Empty -> TodayProgressRing(fraction = 0f)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(R.string.summary_hero_count, todayTotal),
                                color = Gold,
                                fontSize = 40.sp,
                                lineHeight = 44.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                stringResource(R.string.today).lowercase(),
                                color = Ink.copy(alpha = 0.45f),
                                fontSize = 13.sp,
                            )
                        }
                    }
                    if (todayTotal == 0) {
                        Text(
                            stringResource(R.string.summary_empty_today),
                            color = Ink.copy(alpha = 0.45f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                        )
                    }
                    frames.forEachIndexed { index, item ->
                        DhikrLegendRow(
                            arabic = item.frame.arabic,
                            count = item.todayCount,
                            target = item.target,
                            swatch = ChartSlices[index % ChartSlices.size],
                            showSwatch = kind == TodayChartKind.Donut && item.todayCount > 0,
                            onClick = { onFocus(item.frame.id) },
                        )
                    }
                    if (showWeek) {
                        Text(
                            stringResource(R.string.summary_last_seven),
                            color = Ink.copy(alpha = 0.45f),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = 16.dp, bottom = 8.dp),
                        )
                        WeekSpark(totals = weekTotals)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(StripShape)
                    .background(ForestMid)
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
            Spacer(Modifier.height(8.dp))
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
private fun DhikrLegendRow(
    arabic: String,
    count: Int,
    target: Int?,
    swatch: Color,
    showSwatch: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (showSwatch) swatch else Gold.copy(alpha = 0.28f)),
        )
        Text(
            arabic,
            color = Ink,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = buildString {
                append(count)
                target?.let { append(" / $it") }
            },
            color = Gold.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun weekTotals(history: List<DailyCountEntity>, todayKey: String): List<Int> {
    val today = runCatching { LocalDate.parse(todayKey) }.getOrNull() ?: return emptyList()
    val byDate = history.groupBy { it.date }.mapValues { (_, rows) -> rows.sumOf { it.count } }
    return (6 downTo 0).map { offset ->
        byDate[today.minusDays(offset.toLong()).toString()] ?: 0
    }
}
