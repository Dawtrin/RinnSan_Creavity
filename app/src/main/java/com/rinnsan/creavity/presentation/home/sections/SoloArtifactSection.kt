package com.rinnsan.creavity.presentation.home.sections

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.rinnsan.creavity.R
import com.rinnsan.creavity.core.theme.*
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

/**
 * ═══════════════════════════════════════════════════════════════════════
 * STYLIST GATEWAY SECTION — AI STYLIST ENTRY POINT
 * ═══════════════════════════════════════════════════════════════════════
 *
 * REBUILD hoàn toàn từ SoloArtifactSection (3D scan).
 * Mục đích: Dẫn user vào UplinkScreen (The Stylist / AI Stylist).
 *
 * SCROLL NARRATIVE (0→1):
 * - Phase 1 (0→0.3): "WHO ARE YOU?" — Archetype reveal intro
 * - Phase 2 (0.3→0.7): Identity cards animate in sequentially
 * - Phase 3 (0.7→1.0): CTA crystallizes — "CONSULT THE STYLIST"
 *
 * DESIGN:
 * - Dark room với single spotlight
 * - 4 archetype cards (GHOST/OPERATOR/GLITCH/NOMAD)
 * - Tiered reveal theo progress
 * - CTA toàn màn hình tại cuối
 */

private data class ArchetypeCard(
    val id: String,
    val label: String,
    val descriptor: String,
    val accentColor: Color,
    val imageRes: Int
)

@Composable
fun SoloArtifactSection(
    height: Dp,
    progress: Float,
    onStylistClick: () -> Unit = {}
) {
    // Phase breakpoints — tận dụng 8x duration, mỗi phase rõ ràng
    val introProgress   = (progress / 0.25f).coerceIn(0f, 1f)
    val cardsProgress   = ((progress - 0.20f) / 0.40f).coerceIn(0f, 1f)
    val ctaProgress     = ((progress - 0.65f) / 0.30f).coerceIn(0f, 1f)

    val archetypes = remember {
        listOf(
            ArchetypeCard("GHOST",    "GHOST",    "Silent. Invisible. Lethal.", Color(0xFFB0B8C1), R.drawable.s3),
            ArchetypeCard("OPERATOR", "OPERATOR", "Precision. Function. Form.",  Color(0xFF00FF9F), R.drawable.s4),
            ArchetypeCard("GLITCH",   "GLITCH",   "Break. Disrupt. Rebuild.",   Color(0xFFFF3B3B), R.drawable.s5),
            ArchetypeCard("NOMAD",    "NOMAD",    "Wander. Adapt. Transform.",  Color(0xFFFFB800), R.drawable.s6)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(VoidBlack)
    ) {
        // ── LAYER 0: AMBIENT GRID ──────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.04f)) {
            val gridSize = 55.dp.toPx()
            var x = 0f
            while (x <= size.width) {
                drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.5f)
                x += gridSize
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(Color.White, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
                y += gridSize
            }
        }

        // ── LAYER 1: CENTER SPOTLIGHT ──────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize().alpha(introProgress * 0.6f)) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0xFF1A1A1A),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.65f
                ),
                center = center,
                radius = size.minDimension * 0.65f
            )
        }

        // ── LAYER 2: PHASE 1 — WHO ARE YOU INTRO ──────────────────
        // Chỉ hiển thị khi chưa vào phase 3 (ctaProgress = 0)
        if (ctaProgress < 0.99f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .alpha((introProgress * (1f - cardsProgress * 0.5f)).coerceIn(0f, 1f))
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Section label
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(40.dp).height(1.dp).background(CyberAcid.copy(alpha = 0.4f)))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "THE STYLIST",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp,
                            color = CyberAcid.copy(alpha = 0.8f),
                            letterSpacing = 3.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Box(Modifier.width(40.dp).height(1.dp).background(CyberAcid.copy(alpha = 0.4f)))
                    }

                    Spacer(Modifier.height(20.dp))

                    // Main question
                    Text(
                        text = "WHO",
                        fontFamily = AppFonts.oswald,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Black,
                        color = TeslaWhite,
                        letterSpacing = (-2).sp,
                        lineHeight = 68.sp
                    )
                    Text(
                        text = "ARE YOU?",
                        fontFamily = AppFonts.oswald,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-2).sp,
                        lineHeight = 68.sp,
                        maxLines = 1,
                        style = TextStyle(
                            drawStyle = Stroke(width = 2f),
                            color = TeslaWhite.copy(alpha = 0.25f)
                        )
                    )

                    Spacer(Modifier.height(28.dp))

                    Text(
                        text = "Your identity shapes your wardrobe.\nLet the AI match your archetype.",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp,
                        color = TeslaWhite.copy(alpha = 0.45f),
                        lineHeight = 18.sp,
                        letterSpacing = 0.3.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }

        // ── LAYER 3: PHASE 2 — ARCHETYPE CARDS ────────────────────
        if (cardsProgress > 0.01f && ctaProgress < 0.99f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
                    .padding(horizontal = 22.dp)
                    .alpha((cardsProgress * (1f - ctaProgress)).coerceIn(0f, 1f)),
                verticalArrangement = Arrangement.Center
            ) {
                // Top label stays visible
                Spacer(Modifier.height(60.dp))

                Text(
                    text = "ARCHETYPES",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = TeslaWhite.copy(alpha = 0.35f),
                    letterSpacing = 3.sp
                )

                Spacer(Modifier.height(14.dp))

                // 2x2 grid of archetype cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        archetypes.take(2).forEachIndexed { i, archetype ->
                            val cardDelay = i * 0.1f
                            val cardReveal = ((cardsProgress - cardDelay) / 0.3f).coerceIn(0f, 1f)
                            ArchetypeIdentityCard(
                                archetype = archetype,
                                reveal = cardReveal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        archetypes.drop(2).forEachIndexed { i, archetype ->
                            val cardDelay = (i + 2) * 0.1f
                            val cardReveal = ((cardsProgress - cardDelay) / 0.3f).coerceIn(0f, 1f)
                            ArchetypeIdentityCard(
                                archetype = archetype,
                                reveal = cardReveal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ── LAYER 4: PHASE 3 — STYLIST CTA ────────────────────────
        // zIndex(3f) đảm bảo CTA luôn trên cùng, click không bị chặn
        if (ctaProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3f)
                    .background(VoidBlack.copy(alpha = (ctaProgress * 0.92f).coerceIn(0f, 1f)))
                    .graphicsLayer { alpha = ctaProgress }
            ) {

                // CTA content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Scanning animation orb
                    StylistOrb(progress = ctaProgress)

                    Spacer(Modifier.height(36.dp))

                    Text(
                        text = "YOUR PERSONAL",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp,
                        color = CyberAcid.copy(alpha = 0.7f),
                        letterSpacing = 3.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "AI STYLIST",
                        fontFamily = AppFonts.oswald,
                        fontSize = 62.sp,
                        fontWeight = FontWeight.Black,
                        color = TeslaWhite,
                        letterSpacing = (-2.5).sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Describe your vibe. We'll find\nyour next statement piece.",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 12.sp,
                        color = TeslaWhite.copy(alpha = 0.45f),
                        lineHeight = 20.sp,
                        letterSpacing = 0.3.sp
                    )

                    Spacer(Modifier.height(48.dp))

                    // MAIN CTA BUTTON
                    StylistCtaButton(onClick = onStylistClick)

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Powered by Gemini AI",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp,
                        color = TeslaWhite.copy(alpha = 0.2f),
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // ── LAYER 5: PERSISTENT TOP LABEL ─────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 28.dp, top = 52.dp)
                .alpha((1f - ctaProgress * 1.5f).coerceIn(0f, 1f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(CyberAcid, CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "IDENTITY_SCAN: ACTIVE",
                fontFamily = AppFonts.spaceMono,
                fontSize = 9.sp,
                color = CyberAcid,
                letterSpacing = 2.sp
            )
        }

        // ── ORIGINAL: Corner brackets ──────────────────────────────
        Canvas(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 40.dp)
                .size(40.dp)
                .alpha(1f - ctaProgress)
        ) {
            val paint = Paint().asFrameworkPaint().apply {
                color = CyberAcid.copy(alpha = 0.3f).toArgb()
                strokeWidth = 2f
                style = android.graphics.Paint.Style.STROKE
            }
            drawContext.canvas.nativeCanvas.apply {
                drawLine(40f, 0f, 0f, 0f, paint)
                drawLine(0f, 0f, 0f, 40f, paint)
            }
        }

        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 18.dp, top = 40.dp)
                .size(40.dp)
                .alpha(1f - ctaProgress)
        ) {
            val paint = Paint().asFrameworkPaint().apply {
                color = CyberAcid.copy(alpha = 0.3f).toArgb()
                strokeWidth = 2f
                style = android.graphics.Paint.Style.STROKE
            }
            drawContext.canvas.nativeCanvas.apply {
                drawLine(0f, 0f, 40f, 0f, paint)
                drawLine(40f, 0f, 40f, 40f, paint)
            }
        }
    }
}

// ── ARCHETYPE IDENTITY CARD ────────────────────────────────────────────
@Composable
private fun ArchetypeIdentityCard(
    archetype: ArchetypeCard,
    reveal: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .graphicsLayer {
                alpha = reveal
                translationY = (1f - reveal) * 30f
            }
            .background(Color(0xFF0D0D0D))
            .border(1.dp, archetype.accentColor.copy(alpha = 0.3f + reveal * 0.3f))
    ) {
        // Background image subtle
        Image(
            painter = painterResource(archetype.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f)
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.3f),
                        1f to Color.Black.copy(alpha = 0.85f)
                    )
                )
        )

        // Accent corner
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(30.dp)
                .height(2.dp)
                .background(archetype.accentColor.copy(alpha = 0.7f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(2.dp)
                .height(30.dp)
                .background(archetype.accentColor.copy(alpha = 0.7f))
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = archetype.label,
                fontFamily = AppFonts.oswald,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TeslaWhite,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = archetype.descriptor,
                fontFamily = AppFonts.spaceMono,
                fontSize = 8.sp,
                color = archetype.accentColor.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp,
                lineHeight = 12.sp
            )
        }
    }
}

// ── STYLIST ORB ANIMATION ──────────────────────────────────────────────
@Composable
private fun StylistOrb(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "orb_rotation"
    )
    val innerPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "inner_pulse"
    )

    Box(
        modifier = Modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation }) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = size.minDimension / 2f - 4.dp.toPx()
            drawArc(
                color = CyberAcid.copy(alpha = 0.5f),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 1.5f)
            )
            drawArc(
                color = CyberAcid.copy(alpha = 0.15f),
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = 1.5f)
            )
        }

        // Inner circle
        Canvas(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer { scaleX = innerPulse; scaleY = innerPulse }
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(CyberAcid.copy(alpha = 0.15f), Color.Transparent)
                )
            )
            drawCircle(
                color = CyberAcid.copy(alpha = 0.35f),
                style = Stroke(width = 1f)
            )
        }

        // Center dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(CyberAcid, CircleShape)
        )

        // "AI" text
        Text(
            text = "AI",
            fontFamily = AppFonts.oswald,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = CyberAcid.copy(alpha = 0.9f),
            letterSpacing = 2.sp,
            modifier = Modifier.offset(y = 2.dp)
        )
    }
}

// ── STYLIST CTA BUTTON ─────────────────────────────────────────────────
@Composable
private fun StylistCtaButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_press"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "btn_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "btn_border"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "btn_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .border(1.5.dp, CyberAcid.copy(alpha = borderAlpha))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        CyberAcid.copy(alpha = glowAlpha),
                        VoidBlack,
                        CyberAcid.copy(alpha = glowAlpha)
                    )
                )
            )
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
            .padding(vertical = 20.dp, horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "CONSULT THE STYLIST",
                fontFamily = AppFonts.oswald,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TeslaWhite,
                letterSpacing = 1.sp
            )
            Text(
                text = "→",
                fontFamily = AppFonts.oswald,
                fontSize = 24.sp,
                color = CyberAcid,
                fontWeight = FontWeight.Black
            )
        }
    }
}