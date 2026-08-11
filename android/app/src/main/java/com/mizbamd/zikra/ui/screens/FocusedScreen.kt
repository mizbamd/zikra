package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.components.CounterActions
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.ForestDark
import com.mizbamd.zikra.ui.theme.ForestMid
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.util.VolumeUpBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FocusedScreen(
    frame: FrameToday?,
    volumeUpEnabled: Boolean,
    showDone: Boolean,
    onCount: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onClearDone: () -> Unit,
) {
    DisposableEffect(volumeUpEnabled) {
        VolumeUpBus.enabled = volumeUpEnabled
        onDispose { VolumeUpBus.enabled = false }
    }
    LaunchedEffect(volumeUpEnabled, frame?.frame?.id) {
        if (volumeUpEnabled) {
            VolumeUpBus.ticks.collectLatest { onCount() }
        }
    }
    LaunchedEffect(showDone) {
        if (showDone) {
            delay(1600)
            onClearDone()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestDark)
            .padding(20.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Cream)
        }
        IconButton(onClick = onEdit, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_frame), tint = Cream)
        }
        if (frame != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    frame.frame.arabic,
                    color = Cream,
                    fontSize = 48.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(8.dp))
                Text(frame.frame.transliteration, color = Cream.copy(alpha = 0.7f), fontSize = 18.sp)
                Spacer(Modifier.height(36.dp))
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(ForestMid)
                        .clickable(onClick = onCount),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            frame.todayCount.toString(),
                            color = Gold,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        frame.target?.let { Text("/ $it", color = GoldLight, fontSize = 20.sp) }
                    }
                }
                if (showDone) {
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.done_quiet), color = GoldLight, fontSize = 20.sp)
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "${stringResource(R.string.lifetime)} ${frame.frame.lifetimeCount}",
                    color = Cream.copy(alpha = 0.55f),
                )
                CounterActions(onUndo, onReset)
            }
        }
    }
}
