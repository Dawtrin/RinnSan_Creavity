package com.rinnsan.creavity.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ═══════════════════════════════════════════════════════════════════
 * CHAT MESSAGE MODEL
 * ═══════════════════════════════════════════════════════════════════
 *
 * Represents a single message in the Virtual Stylist chat.
 */

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
) {
    /**
     * Get formatted time (e.g., "2:30 PM")
     */
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Get time ago (e.g., "2 minutes ago")
     */
    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            else -> "${diff / 86400_000}d ago"
        }
    }
}