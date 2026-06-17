package com.rinnsan.creavity.presentation.uplink.stylist

import com.rinnsan.creavity.domain.models.IdentityProfile

/**
 * ═══════════════════════════════════════════════════════════════════
 * STYLIST STATE - UI STATE MACHINE
 * ═══════════════════════════════════════════════════════════════════
 */

sealed class StylistState {
    abstract val displayName: String

    object OFFLINE : StylistState() {
        override val displayName = "OFFLINE"
    }

    object ONLINE : StylistState() {
        override val displayName = "ONLINE"
    }

    data class ANALYZE(
        val userMessage: String = "",
        val analysisText: String = ""
    ) : StylistState() {
        override val displayName = "ANALYZE"
    }

    data class SUGGEST(
        val userMessage: String = "",
        val systemResponse: String = "",
        val suggestions: List<Suggestion> = emptyList()
    ) : StylistState() {
        override val displayName = "SUGGEST"
    }

    data class ARCHIVE(
        val messageCount: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    ) : StylistState() {
        override val displayName = "ARCHIVE"

        fun getArchiveTimeAgo(): String {
            val now = System.currentTimeMillis()
            val diffMinutes = (now - timestamp) / (1000 * 60)
            return when {
                diffMinutes < 1 -> "just now"
                diffMinutes < 60 -> "$diffMinutes minutes ago"
                else -> "${diffMinutes / 60} hours ago"
            }
        }
    }

    object ERROR : StylistState() {
        override val displayName = "ERROR"
    }

    object INVALID : StylistState() {
        override val displayName = "INVALID"
    }

    object LOADING : StylistState() {
        override val displayName = "LOADING"
    }
}

/**
 * Data class for suggestions
 */
data class Suggestion(
    val title: String,
    val description: String,
    val imageUrl: String = "",
    val artifactId: String = "",
    val price: String = "",
    val isDirectSale: Boolean = false,   // true = bán trực tiếp trong app
    val affiliateUrl: String = "",        // non-empty = affiliate link
    val inStock: Boolean = true           // chỉ ý nghĩa khi isDirectSale = true
)

/**
 * Helper function to determine stylist state
 */
fun getStylistState(
    identityProfile: IdentityProfile?,
    isLoading: Boolean = false,
    hasError: Boolean = false
): StylistState {
    return when {
        hasError -> StylistState.ERROR
        isLoading -> StylistState.LOADING
        identityProfile == null -> StylistState.OFFLINE
        identityProfile.confidenceLevel == 0f -> StylistState.INVALID
        else -> StylistState.ONLINE
    }
}