package com.mizbamd.zikra.util

import android.media.AudioManager
import android.media.ToneGenerator

/** Short system beep on count. Default off; keeps the APK free of bundled media. */
object CountTick {
    @Volatile
    private var tone: ToneGenerator? = null

    fun play() {
        runCatching {
            val tg = tone ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, 40).also { tone = it }
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 28)
        }
    }
}
