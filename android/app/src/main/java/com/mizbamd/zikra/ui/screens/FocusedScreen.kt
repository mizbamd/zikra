package com.mizbamd.zikra.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
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
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.ForestDark
import com.mizbamd.zikra.ui.theme.ForestMid
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.GoldLight
import kotlinx.coroutines.delay

@Composable
fun FocusedScreen(
    frame: FrameToday?,
    volumeUpEnabled: Boolean,
    showDone: Boolean,
    onCount: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
    onResetLifetime: () -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onClearDone: () -> Unit,
) {
    BackHandler(onBack = onBack)
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
            .background(ForestDark)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Cream)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_frame), tint = Cream)
            }
        }
        if (frame != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    frame.frame.arabic,
                    color = Cream,
                    fontSize = 48.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(frame.frame.transliteration, color = Cream.copy(alpha = 0.7f), fontSize = 18.sp)
                Spacer(Modifier.height(36.dp))
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
                            fontSize = 80.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        frame.target?.let { Text("/ $it", color = GoldLight, fontSize = 22.sp) }
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
                Spacer(Modifier.height(12.dp))
                CounterActions(
                    onUndo = onUndo,
                    onReset = onReset,
                    undoEnabled = frame.todayCount > 0,
                    resetEnabled = frame.todayCount > 0,
                )
                QuietTextButton(
                    stringResource(R.string.reset_lifetime),
                    enabled = frame.frame.lifetimeCount > 0 || frame.todayCount > 0,
                ) { confirmLifetime = true }
                Spacer(Modifier.height(24.dp))
            }
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
