package com.rinnsan.creavity.presentation.signal.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.model.*
import androidx.compose.ui.graphics.graphicsLayer

/**
 * ═══════════════════════════════════════════════════════════════════
 * ENHANCED POST CARD - WITH REAL INTERACTIONS
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun EnhancedPostCard(
    post: SignalPost,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onUserClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VoidBlack)
            .border(
                width = 1.dp,
                color = GridLineColor.copy(alpha = 0.3f)
            )
    ) {
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // HEADER - User info
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        PostHeader(
            username = post.username,
            userPhotoUrl = post.userPhotoUrl,
            location = post.content.location,
            timestamp = post.getFormattedTimestamp(),
            onUserClick = onUserClick
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // IMAGES
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        if (post.content.images.isNotEmpty()) {
            PostImages(
                images = post.content.images,
                layout = post.style.layout,
                colorFilter = post.style.colorFilter
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // CONTENT
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        PostContent(
            title = post.content.title,
            description = post.content.description,
            font = post.style.font,
            textEffect = post.style.textEffect
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // TAGS
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        if (post.content.tags.isNotEmpty()) {
            PostTags(tags = post.content.tags)
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // INTERACTIONS BAR
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        PostInteractionBar(
            likes = post.interactions.likes.count,
            comments = post.interactions.comments.count,
            shares = post.interactions.shares,
            isLiked = post.isLikedBy(currentUserId),
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onShareClick = onShareClick
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HEADER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun PostHeader(
    username: String,
    userPhotoUrl: String?,
    location: String?,
    timestamp: String,
    onUserClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (userPhotoUrl != null) {
                AsyncImage(
                    model = userPhotoUrl,
                    contentDescription = username,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, ElectricBlue, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PhantomGrey, CircleShape)
                        .border(1.dp, TechSilver.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.firstOrNull()?.toString() ?: "?",
                        fontFamily = AppFonts.oswald,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TeslaWhite
                    )
                }
            }

            // Info
            Column {
                Text(
                    text = username,
                    fontFamily = AppFonts.oswald,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TeslaWhite,
                    letterSpacing = 0.5.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    location?.let {
                        Text(
                            text = it,
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 10.sp,
                            color = TechSilver.copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "//",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 10.sp,
                            color = TechSilver.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = timestamp,
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 10.sp,
                        color = TechSilver.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // More button
        IconButton(onClick = { /* TODO: Show menu */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = TechSilver,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// IMAGES
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun PostImages(
    images: List<String>,
    layout: LayoutType,
    colorFilter: ColorFilter
) {
    when (layout) {
        LayoutType.STANDARD, LayoutType.FULLBLEED -> {
            // Single image or first image
            AsyncImage(
                model = images.firstOrNull(),
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentScale = ContentScale.Crop
            )
        }

        LayoutType.GRID -> {
            // Grid layout for multiple images
            if (images.size == 1) {
                AsyncImage(
                    model = images.first(),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        images.take(2).forEach { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Post image",
                                modifier = Modifier
                                    .weight(1f)
                                    .height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    if (images.size > 2) {
                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            images.drop(2).take(2).forEach { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Post image",
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        else -> {
            AsyncImage(
                model = images.firstOrNull(),
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// CONTENT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun PostContent(
    title: String?,
    description: String,
    font: FontStyle,
    textEffect: TextEffect
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        title?.let {
            Text(
                text = it,
                fontFamily = when (font) {
                    FontStyle.OSWALD -> AppFonts.oswald
                    FontStyle.SPACE_MONO -> AppFonts.spaceMono
                    FontStyle.INTER -> AppFonts.spaceMono
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TeslaWhite,
                lineHeight = 24.sp
            )
        }

        Text(
            text = description,
            fontFamily = AppFonts.spaceMono,
            fontSize = 14.sp,
            color = TechSilver.copy(alpha = 0.9f),
            lineHeight = 20.sp
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TAGS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun PostTags(tags: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Text(
                text = "#$tag",
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                color = CyberAcid,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// INTERACTION BAR
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun PostInteractionBar(
    likes: Int,
    comments: Int,
    shares: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${formatCount(likes)} LIKES",
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLiked) ElectricBlue else TechSilver,
                letterSpacing = 1.sp
            )

            Text(
                text = "${formatCount(comments)} COMMENTS",
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                color = TechSilver,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Like button
            InteractionButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                text = "LIKE",
                isActive = isLiked,
                onClick = onLikeClick
            )

            // Comment button
            InteractionButton(
                icon = Icons.Default.ChatBubbleOutline,
                text = "COMMENT",
                isActive = false,
                onClick = onCommentClick
            )

            // Share button
            InteractionButton(
                icon = Icons.Default.Share,
                text = "SHARE",
                isActive = false,
                onClick = onShareClick
            )
        }
    }
}

@Composable
fun InteractionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isActive) ElectricBlue else TechSilver,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = text,
            fontFamily = AppFonts.spaceMono,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) ElectricBlue else TechSilver,
            letterSpacing = 1.sp
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%dK", count / 1000)
    }
}

// Colors
val ElectricBlue = Color(0xFF00D9FF)