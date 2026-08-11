package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.mizbamd.zikra.ui.VolumeUpFocusEffect
import com.mizbamd.zikra.ui.components.CounterActions
import com.mizbamd.zikra.ui.components.DateHeader
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.ForestMid
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import com.mizbamd.zikra.util.DisplayDates
import kotlinx.coroutines.delay

@Composable
fun GuestScreen(
    dates: DisplayDates,
    frame: FrameToday?,
    volumeUpEnabled: Boolean,
    showDone: Boolean,
    onCount: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onArabic: () -> Unit,
    onYou: () -> Unit,
    onSignIn: () -> Unit,
    onClearDone: () -> Unit,
    streakDays: Int = 0,
) {
    VolumeUpFocusEffect(frameId = frame?.frame?.id, enabled = volumeUpEnabled)
    LaunchedEffect(showDone) {
        if (showDone) {
            delay(1600)
            onClearDone()
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth()) {
            IconButton(onClick = onYou, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.you), tint = Cream)
            }
            IconButton(onClick = onSignIn, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Outlined.Person, contentDescription = stringResource(R.string.sign_in), tint = Cream)
            }
        }
        DateHeader(dates, streakDays = streakDays)
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (frame != null) {
                Text(
                    frame.frame.arabic,
                    color = Cream,
                    fontSize = 40.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.clickable(onClick = onArabic).padding(12.dp),
                )
                Text(frame.frame.transliteration, color = Cream.copy(alpha = 0.7f), fontSize = 16.sp)
                Spacer(Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .size(280.dp)
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
                        frame.target?.let {
                            Text("/ $it", color = GoldLight, fontSize = 20.sp)
                        }
                    }
                }
                if (showDone) {
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.done_quiet), color = GoldLight, fontSize = 18.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "${stringResource(R.string.lifetime)} ${frame.frame.lifetimeCount}",
                    color = Cream.copy(alpha = 0.55f),
                )
                CounterActions(
                    onUndo = onUndo,
                    onReset = onReset,
                    undoEnabled = frame.todayCount > 0,
                    resetEnabled = frame.todayCount > 0,
                )
            }
        }
    }
}
