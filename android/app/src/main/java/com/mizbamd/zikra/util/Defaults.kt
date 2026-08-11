package com.mizbamd.zikra.util

import com.mizbamd.zikra.data.local.FrameEntity
import com.mizbamd.zikra.data.local.GUEST_USER_ID
import java.util.UUID

data class DefaultFrame(
    val arabic: String,
    val transliteration: String,
    val target: Int,
)

object Defaults {
    val signedIn = listOf(
        DefaultFrame("سبحان الله", "SubhanAllah", 33),
        DefaultFrame("الحمد لله", "Alhamdulillah", 33),
        DefaultFrame("الله أكبر", "Allahu Akbar", 34),
        DefaultFrame("أستغفر الله", "Astaghfirullah", 100),
    )

    val guest = DefaultFrame("سبحان الله", "SubhanAllah", 33)

    fun seedGuest(): List<FrameEntity> = listOf(toEntity(guest, GUEST_USER_ID, 0))

    fun seedSignedIn(userId: String): List<FrameEntity> =
        signedIn.mapIndexed { index, frame -> toEntity(frame, userId, index) }

    private fun toEntity(frame: DefaultFrame, userId: String, order: Int): FrameEntity {
        val now = ZikraTime.nowIso()
        return FrameEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            arabic = frame.arabic,
            transliteration = frame.transliteration,
            target = frame.target,
            lifetimeCount = 0,
            sortOrder = order,
            createdAt = now,
            updatedAt = now,
            deleted = false,
            dirty = true,
        )
    }
}
