package com.rinnsan.creavity.domain.model

data class Click(
    val id: String = "",
    val artifactId: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val commissionEarned: Double = 0.0,
    val brandId: String = "",
    val archetype: String = "" // For analytics based on user's identity profile
)
