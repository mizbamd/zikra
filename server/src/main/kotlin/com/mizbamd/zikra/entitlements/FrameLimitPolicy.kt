package com.mizbamd.zikra.entitlements

/**
 * Server-side cap on active dhikr frames per signed-in user.
 * Guests are local-only (one dhikr) and never hit this API.
 *
 * TODO: raise via user.level / entitlements when leveling ships.
 */
object FrameLimitPolicy {
    const val DEFAULT_MAX_FRAMES = 10

    fun maxFramesForSignedIn(): Int = DEFAULT_MAX_FRAMES

    fun message(max: Int = maxFramesForSignedIn()): String =
        "You can have up to $max dhikr for now"
}
