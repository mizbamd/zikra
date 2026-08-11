package com.mizbamd.zikra.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.mizbamd.zikra.util.VolumeUpBus

/** Registers this composition as the only frame that volume-up may increment. */
@Composable
fun VolumeUpFocusEffect(frameId: String?, enabled: Boolean) {
    val owner = remember { Any() }
    DisposableEffect(frameId, enabled) {
        if (enabled && !frameId.isNullOrBlank()) {
            VolumeUpBus.acquire(owner, frameId)
        }
        onDispose { VolumeUpBus.release(owner) }
    }
}
