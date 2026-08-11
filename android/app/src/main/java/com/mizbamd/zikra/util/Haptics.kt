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

object VolumeUpBus {
    var enabled: Boolean = false
    private val _ticks = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    val ticks = _ticks.asSharedFlow()
    fun emit() {
        _ticks.tryEmit(Unit)
    }
}
