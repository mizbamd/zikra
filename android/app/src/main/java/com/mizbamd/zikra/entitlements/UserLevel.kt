package com.mizbamd.zikra.entitlements

/**
 * User level maps to frame caps. Level 1 is the default and stays at
 * [FrameLimitPolicy.DEFAULT_MAX_FRAMES] (10 dhikr).
 *
 * TODO: raise [current] when conditions land — e.g. a 7-day streak, a
 * lifetime count threshold, or an entitlement from the server. Do not
 * raise the cap here until those conditions exist.
 */
object UserLevel {
    const val LEVEL_1 = 1

    fun current(): Int = LEVEL_1

    fun maxFrames(level: Int = current()): Int = when {
        level >= LEVEL_1 -> FrameLimitPolicy.DEFAULT_MAX_FRAMES
        else -> FrameLimitPolicy.DEFAULT_MAX_FRAMES
    }
}
