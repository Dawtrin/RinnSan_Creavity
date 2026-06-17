package com.rinnsan.creavity.presentation.home.sections

/**
 * VAULT STRIP SECTION v4 — Y2K Editorial Layout
 *
 * Layout: 1 Hero card full-width (artifact[0]) + Row 3 mini cards (artifact[1..3])
 * Data: realtime từ Firestore qua VaultViewModel.displayArtifacts (4 items)
 * Style: Y2K — CyberAcid border, corner brackets, ticker, scanlines overlay
 *
 * Progress 0→1 qua 5x screen height:
 *   0.00→0.12: Header + ticker reveal
 *   0.12→0.35: Hero card slide-up
 *   0.28→0.65: Mini cards stagger (delay 0.08 masing)
 *   0.65→0.85: Bottom bar + VIEW ALL CTA
 *
 * FIX v4.1:
 *   - Thay pointerInput + detectTapGestures → clickable() trên HeroCard, MiniCard, VIEW ALL
 *   - detectTapGestures consume toàn bộ pointer event kể cả scroll → LazyColumn bị block
 *   - clickable() propagate scroll event lên parent đúng cách
 *   - pointerInput key Unit → stale lambda; không còn dùng nên không cần fix
 */

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.data.vault.AffiliateArtifact
import com.rinnsan.creavity.domain.models.Archetype

// ── Archetype accent color ─────────────────────────────────────────────
private fun archetypeAccent(archetype: Archetype): Color = when (archetype) {
    Archetype.GHOST    -> Color(0xFFB0B8C1)
    Archetype.OPERATOR -> Color(0xFF00FF9F)
    Archetype.GLITCH   -> Color(0xFFFF3B3B)
    Archetype.NOMAD    -> Color(0xFFFFB800)
}

// ── Y2K Corner bracket decoration ─────────────────────────────────────
@Composable
private fun CornerBrackets(
    color: Color = CyberAcid,
    size: Dp = 10.dp,
    thickness: Dp = 1.dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left
        Canvas(modifier = Modifier.size(size).align(Alignment.TopStart)) {
            drawLine(color, Offset(0f, 0f), Offset(this.size.width, 0f), thickness.toPx())
            drawLine(color, Offset(0f, 0f), Offset(0f, this.size.height), thickness.toPx())
        }
        // Top-right
        Canvas(modifier = Modifier.size(size).align(Alignment.TopEnd)) {
            drawLine(color, Offset(0f, 0f), Offset(this.size.width, 0f), thickness.toPx())
            drawLine(color, Offset(this.size.width, 0f), Offset(this.size.width, this.size.height), thickness.toPx())
        }
        // Bottom-left
        Canvas(modifier = Modifier.size(size).align(Alignment.BottomStart)) {
            drawLine(color, Offset(0f, this.size.height), Offset(this.size.width, this.size.height), thickness.toPx())
            drawLine(color, Offset(0f, 0f), Offset(0f, this.size.height), thickness.toPx())
        }
        // Bottom-right
        Canvas(modifier = Modifier.size(size).align(Alignment.BottomEnd)) {
            drawLine(color, Offset(0f, this.size.height), Offset(this.size.width, this.size.height), thickness.toPx())
            drawLine(color, Offset(this.size.width, 0f), Offset(this.size.width, this.size.height), thickness.toPx())
        }
    }
}

// ── Skeleton shimmer placeholder ───────────────────────────────────────
@Composable
private fun SkeletonBox(modifier: Modifier = Modifier) {
    val shimmer by rememberInfiniteTransition(label = "sk").animateFloat(
        0.05f, 0.18f,
        infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "sk_alpha"
    )
    Box(modifier = modifier.background(Color.White.copy(alpha = shimmer)))
}

// ═══════════════════════════════════════════════════════════════════════
// MAIN COMPOSABLE
// ═══════════════════════════════════════════════════════════════════════
@Composable
fun VaultStripSection(
    height: Dp,
    progress: Float,
    artifacts: List<AffiliateArtifact>,   // take(4) từ HomeScreen
    isLoading: Boolean = false,            // từ VaultViewModel.isLoading
    onProductClick: (String) -> Unit,
    onViewAllClick: () -> Unit
) {
    // ── Stagger reveals ───────────────────────────────────────────
    val headerReveal = (progress / 0.12f).coerceIn(0f, 1f)
    val heroReveal   = ((progress - 0.12f) / 0.22f).coerceIn(0f, 1f)
    val mini1Reveal  = ((progress - 0.28f) / 0.20f).coerceIn(0f, 1f)
    val mini2Reveal  = ((progress - 0.36f) / 0.20f).coerceIn(0f, 1f)
    val mini3Reveal  = ((progress - 0.44f) / 0.20f).coerceIn(0f, 1f)
    val ctaReveal    = ((progress - 0.65f) / 0.20f).coerceIn(0f, 1f)

    // ── Ticker animation ──────────────────────────────────────────
    val tickerOffset by rememberInfiniteTransition(label = "ticker").animateFloat(
        initialValue = 0f,
        targetValue  = -600f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "ticker_x"
    )

    // ── Live dot pulse ────────────────────────────────────────────
    val livePulse by rememberInfiniteTransition(label = "live").animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "live_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(VoidBlack)
    ) {

        // ── Y2K scanline overlay ──────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.04f)) {
            var y = 0f
            while (y < size.height) {
                drawLine(Color.White, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += 4f
            }
        }

        // ── Left accent line ──────────────────────────────────────
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .padding(start = 20.dp)
                .alpha(headerReveal)
                .background(
                    Brush.verticalGradient(listOf(
                        Color.Transparent,
                        CyberAcid.copy(alpha = 0.4f),
                        CyberAcid.copy(alpha = 0.1f),
                        Color.Transparent
                    ))
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 30.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
        ) {

            // ── HEADER ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .alpha(headerReveal)
                    .graphicsLayer { translationY = (1f - headerReveal) * 20f }
            ) {
                // Section tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(CyberAcid, androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text          = "THE VAULT",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 9.sp,
                        color         = CyberAcid.copy(alpha = 0.85f),
                        letterSpacing = 3.5.sp,
                        fontWeight    = FontWeight.Medium
                    )
                    Spacer(Modifier.weight(1f))
                    // Artifact count badge
                    if (artifacts.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, CyberAcid.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text          = "${artifacts.size} ITEMS",
                                fontFamily    = AppFonts.spaceMono,
                                fontSize      = 7.sp,
                                color         = CyberAcid.copy(alpha = 0.6f),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Title
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text          = "SELECTED",
                            fontFamily    = AppFonts.oswald,
                            fontSize      = 40.sp,
                            fontWeight    = FontWeight.Black,
                            color         = TeslaWhite,
                            letterSpacing = (-2).sp,
                            lineHeight    = 38.sp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text          = "ARTIFACTS",
                            fontFamily    = AppFonts.oswald,
                            fontSize      = 40.sp,
                            fontWeight    = FontWeight.Black,
                            letterSpacing = (-2).sp,
                            lineHeight    = 38.sp,
                            style         = TextStyle(
                                drawStyle = Stroke(width = 1.5f),
                                color     = TeslaWhite.copy(alpha = 0.28f)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Y2K Ticker bar ────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(CyberAcid)
                        .clip(androidx.compose.ui.graphics.RectangleShape)
                ) {
                    val tickerText = "▶ FIREBASE LIVE  ·  ${artifacts.size} ARTIFACTS  ·  SCROLL TO BROWSE  ·  CREAVITY VAULT  ·  "
                    Row(modifier = Modifier.graphicsLayer { translationX = tickerOffset }) {
                        Text(
                            text          = tickerText.repeat(6),
                            fontFamily    = AppFonts.spaceMono,
                            fontSize      = 8.sp,
                            color         = VoidBlack,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            maxLines      = 1,
                            softWrap      = false,
                            modifier      = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // ── HERO CARD — full width, tall ──────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.52f)
                    .graphicsLayer {
                        alpha        = heroReveal
                        translationY = (1f - heroReveal) * 40f
                    }
            ) {
                val hero = artifacts.getOrNull(0)
                if (isLoading || hero == null) {
                    SkeletonBox(modifier = Modifier.fillMaxSize())
                } else {
                    HeroCard(artifact = hero, onClick = { onProductClick(hero.id) })
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── MINI CARDS ROW — 3 cards horizontal ───────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .weight(0.36f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair(artifacts.getOrNull(1), mini1Reveal),
                    Pair(artifacts.getOrNull(2), mini2Reveal),
                    Pair(artifacts.getOrNull(3), mini3Reveal),
                ).forEach { (artifact, reveal) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                alpha        = reveal
                                translationY = (1f - reveal) * 30f
                            }
                    ) {
                        if (isLoading || artifact == null) {
                            SkeletonBox(modifier = Modifier.fillMaxSize())
                        } else {
                            MiniCard(artifact = artifact, onClick = { onProductClick(artifact.id) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── BOTTOM BAR ────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .alpha(ctaReveal),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Live indicator
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .alpha(livePulse)
                            .background(CyberAcid, androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text          = "LIVE",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 7.sp,
                        color         = CyberAcid.copy(alpha = 0.8f),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text          = "· ${artifacts.size} ARTIFACTS IN VAULT",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 7.sp,
                        color         = TeslaWhite.copy(alpha = 0.28f),
                        letterSpacing = 1.sp
                    )
                }

                // ── FIX: VIEW ALL button — clickable thay pointerInput ──
                Box(
                    modifier = Modifier
                        .border(1.dp, CyberAcid)
                        .clickable { onViewAllClick() }              // ✅ FIXED
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text          = "VIEW ALL →",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 8.sp,
                        color         = CyberAcid,
                        letterSpacing = 2.sp,
                        fontWeight    = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// HERO CARD — full width, image lớn, overlay gradient
// ═══════════════════════════════════════════════════════════════════════
@Composable
private fun HeroCard(artifact: AffiliateArtifact, onClick: () -> Unit) {
    val accent = archetypeAccent(artifact.archetype)
    val imageUrl = artifact.imageUrl.ifBlank { artifact.images.firstOrNull() ?: "" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E0E))
            .border(1.dp, CyberAcid.copy(alpha = 0.6f))
            .clickable { onClick() }                                 // ✅ FIXED: thay pointerInput + detectTapGestures
    ) {
        // Product image
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model              = imageUrl,
                contentDescription = artifact.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        }

        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(
                    0f    to Color.Transparent,
                    0.45f to Color.Transparent,
                    1f    to Color.Black.copy(alpha = 0.95f)
                ))
        )

        // Scanline overlay
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.04f)) {
            var y = 0f
            while (y < size.height) {
                drawLine(Color.White, Offset(0f, y), Offset(size.width, y), 1f)
                y += 4f
            }
        }

        // Corner brackets
        CornerBrackets(color = CyberAcid, size = 14.dp)

        // FEATURED badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .background(CyberAcid)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text          = "FEATURED",
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 7.sp,
                color         = VoidBlack,
                letterSpacing = 2.sp,
                fontWeight    = FontWeight.Bold
            )
        }

        // Top-right archetype
        Text(
            text          = artifact.archetype.name,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 7.sp,
            color         = accent.copy(alpha = 0.7f),
            letterSpacing = 1.5.sp,
            modifier      = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        )

        // Bottom info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Accent line above title
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(1.dp)
                    .background(accent.copy(alpha = 0.8f))
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text          = artifact.category.uppercase(),
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 7.sp,
                color         = TeslaWhite.copy(alpha = 0.45f),
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text          = artifact.title.uppercase(),
                fontFamily    = AppFonts.oswald,
                fontSize      = 20.sp,
                fontWeight    = FontWeight.Black,
                color         = TeslaWhite,
                letterSpacing = (-0.5).sp,
                lineHeight    = 20.sp,
                maxLines      = 2
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text          = artifact.price,
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 12.sp,
                    color         = CyberAcid,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text          = "→",
                    fontFamily    = AppFonts.oswald,
                    fontSize      = 16.sp,
                    color         = CyberAcid
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// MINI CARD — 1/3 width, vertical layout, image top
// ═══════════════════════════════════════════════════════════════════════
@Composable
private fun MiniCard(artifact: AffiliateArtifact, onClick: () -> Unit) {
    val accent   = archetypeAccent(artifact.archetype)
    val imageUrl = artifact.imageUrl.ifBlank { artifact.images.firstOrNull() ?: "" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E0E))
            .border(1.dp, CyberAcid.copy(alpha = 0.22f))
            .clickable { onClick() }                                 // ✅ FIXED: thay pointerInput + detectTapGestures
    ) {
        // Accent top border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(accent.copy(alpha = 0.7f))
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Image — top 60%
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model              = imageUrl,
                        contentDescription = artifact.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1A1A1A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawRect(color = CyberAcid.copy(alpha = 0.2f), style = Stroke(1f))
                            drawLine(CyberAcid.copy(alpha = 0.1f), Offset(0f, 0f), Offset(size.width, size.height), 1f)
                            drawLine(CyberAcid.copy(alpha = 0.1f), Offset(size.width, 0f), Offset(0f, size.height), 1f)
                        }
                    }
                }
                // Gradient fade bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(
                            0.5f to Color.Transparent,
                            1f   to Color(0xFF0E0E0E).copy(alpha = 0.85f)
                        ))
                )
                // Corner brackets mini
                CornerBrackets(color = CyberAcid.copy(alpha = 0.5f), size = 7.dp, thickness = 0.7.dp)
            }

            // Info — bottom 40%
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text          = artifact.category.uppercase(),
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 6.sp,
                        color         = TeslaWhite.copy(alpha = 0.38f),
                        letterSpacing = 1.sp,
                        maxLines      = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text          = artifact.title.uppercase(),
                        fontFamily    = AppFonts.oswald,
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = TeslaWhite,
                        letterSpacing = (-0.3).sp,
                        lineHeight    = 10.sp,
                        maxLines      = 2
                    )
                }
                Text(
                    text          = artifact.price,
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 8.sp,
                    color         = CyberAcid,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}