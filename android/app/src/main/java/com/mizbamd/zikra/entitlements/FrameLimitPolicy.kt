package com.mizbamd.zikra.entitlements

/**
 * How many dhikr frames a user may keep. Guest is a single counter;
 * signed-in users share [DEFAULT_MAX_FRAMES] until leveling/entitlements land.
 */
object FrameLimitPolicy {
    const val DEFAULT_MAX_FRAMES = 10
    const val GUEST_MAX_FRAMES = 1

    fun maxFramesFor(signedIn: Boolean): Int = when {
        !signedIn -> GUEST_MAX_FRAMES
        // TODO: raise via user.level / entitlements when leveling ships.
        else -> DEFAULT_MAX_FRAMES
    }

    fun canAdd(activeCount: Int, signedIn: Boolean): Boolean =
        activeCount < maxFramesFor(signedIn)
}
