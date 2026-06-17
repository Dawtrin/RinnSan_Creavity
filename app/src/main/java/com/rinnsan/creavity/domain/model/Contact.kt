package com.rinnsan.creavity.domain.model

data class Contact(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val status: String = "new", // "new", "in_progress", "resolved"
    val timestamp: Long = System.currentTimeMillis()
)
