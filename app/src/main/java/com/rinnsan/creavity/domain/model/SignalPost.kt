package com.rinnsan.creavity.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * ═══════════════════════════════════════════════════════════════════
 * SIGNAL POST - DOMAIN MODEL V2.0
 * ═══════════════════════════════════════════════════════════════════
 *
 * Enhanced model với:
 * - User-generated content support
 * - Real interactions (likes, comments, shares)
 * - Style customization
 * - Cloudinary image URLs
 */

data class SignalPost(
    @DocumentId
    val id: String = "",

    // Post Type
    val type: PostType = PostType.OUTFIT,

    // Author Info
    val userId: String = "",
    val username: String = "",
    val userPhotoUrl: String? = null,

    // Timestamps
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val editedAt: Timestamp? = null,

    // Content
    val content: PostContent = PostContent(),

    // Visual Style
    val style: PostStyle = PostStyle(),

    // Interactions
    val interactions: PostInteractions = PostInteractions(),

    // Metadata
    val status: PostStatus = PostStatus.PUBLISHED,
    val moderationStatus: ModerationStatus = ModerationStatus.APPROVED,
    val isEdited: Boolean = false
)

/**
 * Post Type Enum
 */
enum class PostType {
    OUTFIT,          // User outfit photos
    STYLE_GUIDE,     // Articles/tutorials
    MANIFESTO,       // Text-only thoughts
    TREND_CAROUSEL,  // Multiple images carousel
    FEATURED_MOMENT, // Hero moments
    BREAKING_SIGNAL  // Ticker-style announcements
}

/**
 * Post Content
 */
data class PostContent(
    val title: String? = null,
    val description: String = "",
    val images: List<String> = emptyList(),  // Cloudinary URLs
    val tags: List<String> = emptyList(),
    val location: String? = null,

    // For Style Guide
    val sections: List<GuideSection> = emptyList(),
    val coverImageUrl: String? = null,
    val author: String? = null,
    val readTime: String? = null
)

/**
 * Guide Section (for Style Guide posts)
 */
data class GuideSection(
    val heading: String = "",
    val text: String = "",
    val imageUrl: String? = null
)

/**
 * Visual Style Customization
 */
data class PostStyle(
    val colorFilter: ColorFilter = ColorFilter.NONE,
    val textEffect: TextEffect = TextEffect.NONE,
    val layout: LayoutType = LayoutType.STANDARD,
    val font: FontStyle = FontStyle.OSWALD
)

enum class ColorFilter {
    NONE,
    GRAYSCALE,
    NEON,
    VINTAGE,
    CYBERPUNK,
    MONOCHROME
}

enum class TextEffect {
    NONE,
    GLITCH,
    BLUR,
    SHADOW,
    NEON_GLOW,
    TYPEWRITER
}

enum class LayoutType {
    STANDARD,     // Default vertical
    GRID,         // Multi-image grid
    FULLBLEED,    // Full-width hero
    STORY,        // Instagram story-style
    CAROUSEL      // Horizontal scroll
}

enum class FontStyle {
    OSWALD,       // Bold, editorial
    SPACE_MONO,   // Technical, mono
    INTER         // Clean, readable
}

/**
 * Post Interactions
 */
data class PostInteractions(
    val likes: LikeData = LikeData(),
    val comments: CommentData = CommentData(),
    val shares: Int = 0,
    val bookmarks: Int = 0,
    val views: Int = 0
)

data class LikeData(
    val count: Int = 0,
    val userIds: List<String> = emptyList()
) {
    fun hasLiked(userId: String): Boolean = userIds.contains(userId)
}

data class CommentData(
    val count: Int = 0,
    val lastCommentTimestamp: Timestamp? = null
)

/**
 * Post Status
 */
enum class PostStatus {
    DRAFT,       // Saved but not published
    PUBLISHED,   // Live in feed
    ARCHIVED,    // Hidden from public
    DELETED      // Soft delete
}

enum class ModerationStatus {
    PENDING,     // Awaiting review
    APPROVED,    // Passed moderation
    FLAGGED,     // Reported by users
    REJECTED     // Failed moderation
}

/**
 * Helper Extensions
 */
fun SignalPost.isLikedBy(userId: String): Boolean {
    return interactions.likes.hasLiked(userId)
}

fun SignalPost.canEdit(userId: String): Boolean {
    return this.userId == userId && status == PostStatus.PUBLISHED
}

fun SignalPost.canDelete(userId: String): Boolean {
    return this.userId == userId
}

fun SignalPost.getFormattedTimestamp(): String {
    val now = System.currentTimeMillis()
    val postTime = timestamp?.toDate()?.time ?: return "Just now"
    val diffMillis = now - postTime

    val minutes = diffMillis / (1000 * 60)
    val hours = diffMillis / (1000 * 60 * 60)
    val days = diffMillis / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}M AGO"
        hours < 24 -> "${hours}H AGO"
        days < 7 -> "${days}D AGO"
        else -> "ARCHIVED"
    }
}

fun SignalPost.getFormattedLikes(): String {
    val count = interactions.likes.count
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%dK", count / 1000)
    }
}

fun SignalPost.getFormattedComments(): String {
    val count = interactions.comments.count
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%dK", count / 1000)
    }
}