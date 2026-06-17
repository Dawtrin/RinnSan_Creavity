package com.rinnsan.creavity.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * ═══════════════════════════════════════════════════════════════════
 * COMMENT MODEL
 * ═══════════════════════════════════════════════════════════════════
 */

data class Comment(
    @DocumentId
    val id: String = "",

    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhotoUrl: String? = null,

    val text: String = "",

    @ServerTimestamp
    val timestamp: Timestamp? = null,

    // Reply system
    val replyTo: String? = null,  // Comment ID if this is a reply
    val replyToUsername: String? = null,

    // Interactions
    val likes: LikeData = LikeData(),

    // Metadata
    val isEdited: Boolean = false,
    val editedAt: Timestamp? = null,
    val isDeleted: Boolean = false
)

/**
 * Helper Extensions
 */
fun Comment.isLikedBy(userId: String): Boolean {
    return likes.hasLiked(userId)
}

fun Comment.canEdit(userId: String): Boolean {
    return this.userId == userId && !isDeleted
}

fun Comment.canDelete(userId: String): Boolean {
    return this.userId == userId
}

fun Comment.getFormattedTimestamp(): String {
    val now = System.currentTimeMillis()
    val commentTime = timestamp?.toDate()?.time ?: return "Just now"
    val diffMillis = now - commentTime

    val minutes = diffMillis / (1000 * 60)
    val hours = diffMillis / (1000 * 60 * 60)
    val days = diffMillis / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> "${days / 7}w"
    }
}