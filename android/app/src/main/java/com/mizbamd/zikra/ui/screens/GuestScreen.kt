package com.mizbamd.zikra.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.VolumeUpFocusEffect
import com.mizbamd.zikra.ui.components.CounterActions
import com.mizbamd.zikra.ui.components.DateHeader
import com.mizbamd.zikra.ui.components.DhikrCountSurface
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.zikraSafeDrawing
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
    onResetLifetime: () -> Unit,
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
    var confirmLifetime by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .zikraSafeDrawing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onYou, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.you), tint = Cream)
            }
            IconButton(onClick = onSignIn, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Outlined.Person, contentDescription = stringResource(R.string.sign_in), tint = Cream)
            }
        }
        DateHeader(dates, streakDays = streakDays, modifier = Modifier.padding(horizontal = 20.dp))
        if (frame != null) {
            DhikrCountSurface(
                arabic = frame.frame.arabic,
                transliteration = frame.frame.transliteration,
                todayCount = frame.todayCount,
                target = frame.target,
                showDone = showDone,
                onCount = onCount,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )
            Text(
                "${stringResource(R.string.lifetime)} ${frame.frame.lifetimeCount}",
                color = Cream.copy(alpha = 0.4f),
            )
            Spacer(Modifier.height(8.dp))
            CounterActions(
                onUndo = onUndo,
                onReset = onReset,
                undoEnabled = frame.todayCount > 0,
                resetEnabled = frame.todayCount > 0,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            QuietTextButton(
                stringResource(R.string.reset_lifetime),
                enabled = frame.frame.lifetimeCount > 0 || frame.todayCount > 0,
            ) { confirmLifetime = true }
            Spacer(Modifier.height(8.dp))
        }
    }
    if (confirmLifetime) {
        AlertDialog(
            onDismissRequest = { confirmLifetime = false },
            title = { Text(stringResource(R.string.reset_lifetime_confirm_title)) },
            text = { Text(stringResource(R.string.reset_lifetime_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLifetime = false
                    onResetLifetime()
                }) {
                    Text(stringResource(R.string.reset_lifetime), color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLifetime = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
