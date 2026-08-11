package com.mizbamd.zikra.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

fun Context.tapHaptic() {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val mgr = getSystemService(VibratorManager::class.java)
        mgr.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Vibrator::class.java)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    } else {
        vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

/**
 * Volume-up counting is bound to one focused tasbih, never the home list.
 * [acquire] from Focused/Guest counter UI; [release] on leave so home
 * leaves volume keys to the system.
 */
object VolumeUpBus {
    private val _ticks = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val ticks = _ticks.asSharedFlow()

    @Volatile
    private var owner: Any? = null

    @Volatile
    private var focusedFrameId: String? = null

    val shouldHandle: Boolean
        get() = focusedFrameId != null

    fun acquire(owner: Any, frameId: String) {
        this.owner = owner
        this.focusedFrameId = frameId
    }

    fun release(owner: Any) {
        if (this.owner === owner) {
            this.owner = null
            this.focusedFrameId = null
        }
    }

    fun emit() {
        focusedFrameId?.let { _ticks.tryEmit(it) }
    }
}
