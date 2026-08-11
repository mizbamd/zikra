package com.mizbamd.zikra.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.zIndex
import com.mizbamd.zikra.R
import com.mizbamd.zikra.data.repo.FrameToday
import com.mizbamd.zikra.ui.VolumeUpFocusEffect
import com.mizbamd.zikra.ui.components.CounterActions
import com.mizbamd.zikra.ui.components.DhikrCountSurface
import com.mizbamd.zikra.ui.components.QuietTextButton
import com.mizbamd.zikra.ui.theme.Cream
import com.mizbamd.zikra.ui.theme.ForestDark
import com.mizbamd.zikra.ui.theme.Gold
import com.mizbamd.zikra.ui.theme.zikraSafeDrawing
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
            .zikraSafeDrawing(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Cream.copy(alpha = 0.14f),
                    contentColor = Cream,
                ),
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Cream)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_frame), tint = Cream)
            }
        }
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
            Spacer(Modifier.height(12.dp))
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
