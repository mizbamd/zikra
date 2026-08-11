package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.components.DateHeader
import com.mizbamd.zikra.ui.components.FrameCard
import com.mizbamd.zikra.ui.theme.Forest
import com.mizbamd.zikra.ui.theme.ForestDark
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.util.DisplayDates
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    dates: DisplayDates,
    frames: List<FrameToday>,
    doneFrameId: String?,
    canAddFrame: Boolean,
    maxFrames: Int,
    streakDays: Int = 0,
    onCount: (String) -> Unit,
    onFocus: (String) -> Unit,
    onAdd: () -> Unit,
    onClearDone: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    LaunchedEffect(doneFrameId) {
        if (doneFrameId != null) {
            delay(1600)
            onClearDone()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Forest),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val scroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DateHeader(dates, streakDays = streakDays)
                    if (!canAddFrame) {
                        Text(
                            stringResource(R.string.frame_limit_reached, maxFrames),
                            color = GoldLight,
                            fontSize = 15.sp,
                        )
                    }
                    if (doneFrameId != null) {
                        Text(
                            stringResource(R.string.done_quiet),
                            color = GoldLight,
                            fontSize = 16.sp,
                        )
                    }
                    frames.forEach { item ->
                        FrameCard(
                            item = item,
                            onCount = { onCount(item.frame.id) },
                            onArabic = { onFocus(item.frame.id) },
                        )
                    }
                    Spacer(Modifier.height(88.dp))
                }
                HomeScrollbar(scroll)
                if (canAddFrame) {
                    FloatingActionButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp),
                        containerColor = Gold,
                        contentColor = ForestDark,
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_frame))
                    }
                }
            }
            bottomBar()
        }
    }
}

@Composable
private fun BoxScope.HomeScrollbar(scroll: ScrollState) {
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .width(8.dp)
            .padding(end = 2.dp, top = 8.dp, bottom = 8.dp),
    ) {
        val viewportPx = constraints.maxHeight.toFloat()
        val maxPx = scroll.maxValue.toFloat()
        val canScroll = maxPx > 0f
        val track = Gold.copy(alpha = if (canScroll) 0.18f else 0.08f)
        val shape = RoundedCornerShape(99.dp)

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(4.dp)
                .clip(shape)
                .background(track),
        )

        if (canScroll && viewportPx > 0f) {
            val contentPx = viewportPx + maxPx
            val minThumbPx = with(density) { 28.dp.toPx() }
            val thumbHeightPx = (viewportPx * viewportPx / contentPx)
                .coerceAtLeast(minThumbPx)
                .coerceAtMost(viewportPx)
            val travel = (viewportPx - thumbHeightPx).coerceAtLeast(0f)
            val fraction = (scroll.value / maxPx).coerceIn(0f, 1f)
            val thumbOffsetPx = (fraction * travel).roundToInt()

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, thumbOffsetPx) }
                    .width(4.dp)
                    .height(with(density) { thumbHeightPx.toDp() })
                    .clip(shape)
                    .background(Gold),
            )
        }
    }
}
