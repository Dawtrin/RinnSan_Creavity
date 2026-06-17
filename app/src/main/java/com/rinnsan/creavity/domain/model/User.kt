package com.rinnsan.creavity.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val role: String = "user" // "admin" or "user"
)
