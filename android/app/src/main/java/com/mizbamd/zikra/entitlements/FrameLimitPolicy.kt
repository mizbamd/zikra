package com.mizbamd.zikra.entitlements

/**
 * How many dhikr frames a user may keep. Guest is a single counter;
 * signed-in users share [UserLevel.maxFrames] (level 1 → [DEFAULT_MAX_FRAMES]).
 */
object FrameLimitPolicy {
    const val DEFAULT_MAX_FRAMES = 10
    const val GUEST_MAX_FRAMES = 1

    fun maxFramesFor(signedIn: Boolean): Int = when {
        !signedIn -> GUEST_MAX_FRAMES
        else -> UserLevel.maxFrames()
    }

    fun canAdd(activeCount: Int, signedIn: Boolean): Boolean =
        activeCount < maxFramesFor(signedIn)
}
