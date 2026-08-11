package com.mizbamd.zikra.models

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String = "ok",
    val service: String = "zikra",
)

@Serializable
data class ErrorResponse(val error: String)

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class GoogleSignInRequest(
    val idToken: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
)

@Serializable
data class FrameDto(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val target: Int? = null,
    val lifetimeCount: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: String,
    val updatedAt: String,
    val deleted: Boolean = false,
)

@Serializable
data class DailyCountDto(
    val id: String,
    val frameId: String,
    val date: String,
    val count: Int,
    val updatedAt: String,
)

@Serializable
data class SyncPullResponse(
    val frames: List<FrameDto>,
    val dailyCounts: List<DailyCountDto>,
)

@Serializable
data class SyncPushRequest(
    val frames: List<FrameDto> = emptyList(),
    val dailyCounts: List<DailyCountDto> = emptyList(),
)

@Serializable
data class SyncPushResponse(
    val ok: Boolean = true,
    val frames: List<FrameDto> = emptyList(),
    val dailyCounts: List<DailyCountDto> = emptyList(),
)
