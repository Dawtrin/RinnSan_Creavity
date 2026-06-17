package com.rinnsan.creavity.presentation.home.sections

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinnsan.creavity.R
import com.rinnsan.creavity.core.theme.*
import androidx.compose.ui.graphics.graphicsLayer

/**
 * ═══════════════════════════════════════════════════════════════════════
 * RUNWAY SECTION - SCROLL-DRIVEN ANIMATION
 * ═══════════════════════════════════════════════════════════════════════
 *
 * THAY ĐỔI:
 * - Bỏ stickyHeader, giờ là item {} với height = 7x screen
 * - Progress nhận từ HomeScreen: 0f → 1f tuyến tính
 * - Animation phản hồi tức thì theo scroll
 *
 * ANIMATION:
 * - Image scale: 0.95 → 1.0
 * - Text slide: Kinetic horizontal movement
 * - Fade in/out ở đầu và cuối section
 */

@Composable
fun RunwayPinnedSection(height: Dp, progress: Float) {
    // Animation value - Áp dụng easing CHỈ cho visual effect, KHÔNG cho scroll
    val animValue = FastOutLinearInEasing.transform(progress)

    // Fade in ở đầu (0-20%), fade out ở cuối (90-100%)
    val fadeIn = (progress / 0.2f).coerceIn(0f, 1f)
    val fadeOut = ((progress - 0.9f) / 0.1f).coerceIn(0f, 1f)
    val contentOpacity = (fadeIn - fadeOut + 1f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(VoidBlack)
    ) {
        // ───────────────────────────────────────────────────────────
        // LAYER 1: FASHION IMAGE
        // ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentOpacity),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.s2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .fillMaxHeight(0.68f)
                    .graphicsLayer {
                        scaleX = 0.95f + (0.05f * animValue)
                        scaleY = 0.95f + (0.05f * animValue)
                    }
            )
        }

        // ───────────────────────────────────────────────────────────
        // LAYER 2: KINETIC TEXT LINES
        // ───────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentOpacity),
            verticalArrangement = Arrangement.Center
        ) {
            SlidingText(
                text = "DECONSTRUCTED ELEGANCE // ",
                offset = (1f - animValue) * 1500f - 750f,
                isStroke = false
            )
            Spacer(modifier = Modifier.height(18.dp))

            SlidingText(
                text = "WHERE TRADITION BREAKS // ",
                offset = (animValue * 1500f) - 750f,
                isStroke = true
            )
            Spacer(modifier = Modifier.height(18.dp))

            SlidingText(
                text = "ASYMMETRY IS POETRY // ",
                offset = (1f - animValue) * 1700f - 850f,
                isStroke = false
            )
            Spacer(modifier = Modifier.height(18.dp))

            SlidingText(
                text = "FASHION WITHOUT BOUNDARIES // ",
                offset = (animValue * 1600f) - 800f,
                isStroke = true
            )
        }

        // ───────────────────────────────────────────────────────────
        // LAYER 3: UI ELEMENTS
        // ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentOpacity * 0.6f)
                .padding(28.dp)
        ) {
            // Top left label
            Row(
                modifier = Modifier.align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(35.dp)
                        .height(1.dp)
                        .background(CyberAcid.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "THE RUNWAY",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Light
                )
            }

            // Top right info
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "VOL. 04",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 11.sp,
                    color = CyberAcid.copy(alpha = 0.7f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2026",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Light
                )
            }

            // Bottom left philosophy
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 55.dp)
                    .widthIn(max = 200.dp)
            ) {
                Text(
                    text = "MANIFESTO",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 8.sp,
                    color = CyberAcid.copy(alpha = 0.6f),
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fashion is not about fitting in.\nIt's about standing out with intention.",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.35f),
                    lineHeight = 14.sp,
                    letterSpacing = 0.3.sp,
                    fontWeight = FontWeight.Light
                )
            }

            // Bottom right stats
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 55.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${(animValue * 100).toInt()}%",
                    fontFamily = AppFonts.oswald,
                    fontSize = 22.sp,
                    color = CyberAcid.copy(alpha = 0.8f),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SEQUENCE",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Light
                )
            }

            // Center scroll indicator
            if (progress < 0.9f) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .alpha((1f - progress).coerceIn(0f, 1f) * 0.4f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0f),
                                        Color.White.copy(alpha = 0.15f),
                                        Color.White.copy(alpha = 0f)
                                    )
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(CyberAcid, androidx.compose.foundation.shape.CircleShape)
                                .align(Alignment.TopCenter)
                                .offset(y = (animValue * 56).dp)
                        )
                    }
                }
            }
        }

        // Glitch effect
        if (progress > 0.05f && progress < 0.1f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CyberAcid.copy(alpha = 0.04f))
            )
        }
    }
}

@Composable
private fun SlidingText(text: String, offset: Float, isStroke: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = offset }
    ) {
        Text(
            text = text.repeat(20),
            fontFamily = AppFonts.oswald,
            fontSize = 85.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-2.5).sp,
            lineHeight = 85.sp,
            maxLines = 1,
            softWrap = false,
            style = if (isStroke) {
                TextStyle(
                    drawStyle = Stroke(width = 1.8f),
                    color = Color.White.copy(alpha = 0.35f)
                )
            } else {
                TextStyle(color = Color.White.copy(alpha = 0.92f))
            }
        )
    }
}