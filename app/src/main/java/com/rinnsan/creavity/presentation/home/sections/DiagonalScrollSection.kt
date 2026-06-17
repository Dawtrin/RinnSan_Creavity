package com.rinnsan.creavity.presentation.home.sections

/**
 * ═══════════════════════════════════════════════════════════════════════
 * DIAGONAL SCROLL SECTION — V4 Y2K CYBERPUNK FLAGSHIP
 * ═══════════════════════════════════════════════════════════════════════
 *
 * V4 — REDESIGN HOÀN TOÀN theo yêu cầu:
 *
 * LAYOUT:
 *   ┌─────────────────────────────────────────────┐
 *   │ [THE COLLECTION]              [01 / 07]     │  ← header frame
 *   ├──────────────────────┬──────────────────────┤
 *   │                      │  DESCRIPTOR          │
 *   │   IMAGE (55% width)  │  TITLE               │
 *   │   với animation      │  SUBTITLE (stroke)   │
 *   │   scale/rotation     │  Tagline box         │
 *   │   Y2K chromatic      │                      │
 *   │                      │  ←───────  ───────→  │  ← nav arrows
 *   ├──────────────────────┴──────────────────────┤
 *   │  ●●●●●●●  progress dots                     │
 *   │  [JOIN THE COMMUNITY →]  ← luôn hiện        │
 *   └─────────────────────────────────────────────┘
 *
 * NAVIGATION:
 *   - User bấm ← → để chuyển beat (thay scroll-driven)
 *   - Animation giữ nguyên: cross-fade + slide + scale + rotation + chroma
 *   - Khi đã xem beat 7 (hasSeenAll = true) → section unlock scroll tiếp
 *   - progress vẫn được trả về HomeScreen qua onProgressUpdate callback
 *
 * JOIN CTA:
 *   - Luôn hiện ở bottom, không phụ thuộc progress
 *   - Pulse border animation giữ nguyên Y2K style
 *
 * GIỮ NGUYÊN V3:
 *   - Scanlines ambient
 *   - RGB chromatic aberration tại transition
 *   - Ghost beat number background
 *   - Per-beat ticker
 *   - Kinetic descriptor slide
 *   - Slot-machine beat counter
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
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
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.rinnsan.creavity.R
import com.rinnsan.creavity.core.theme.*

// ─── Data ─────────────────────────────────────────────────────────────────────

data class StoryBeat(
    val imageRes:    Int,
    val title:       String,
    val subtitle:    String,
    val tagline:     String,
    val signalCount: String,
    val descriptor:  String,
    val tickerText:  String
)

// ═══════════════════════════════════════════════════════════════════════
// MAIN
// ═══════════════════════════════════════════════════════════════════════

@Composable
fun DiagonalScrollSection(
    height: Dp,
    progress: Float,                          // từ HomeScreen scroll — chỉ dùng cho entry animation
    onSignalClick: () -> Unit = {},
    onProgressUpdate: (Float) -> Unit = {}    // callback cho HomeScreen biết beat progress
) {
    val storyBeats = remember {
        listOf(
            StoryBeat(R.drawable.s3, "REDEFINING", "ELEGANCE",
                "Where minimalism meets maximalism", "2.4K",
                "BOUNDARY BREAK", "SS26 · CREAVITY EDIT · BOUNDARY BREAK · REDEFINING ELEGANCE · "),
            StoryBeat(R.drawable.s4, "BREAKING", "TRADITION",
                "Fashion without boundaries", "1.8K",
                "RULE DESTROY", "SS26 · NO LIMITS · RULE DESTROY · BREAKING TRADITION · "),
            StoryBeat(R.drawable.s5, "CRAFTING", "IDENTITY",
                "Your story, our vision", "3.1K",
                "SELF ARCHIVE", "SS26 · YOUR STORY · SELF ARCHIVE · CRAFTING IDENTITY · "),
            StoryBeat(R.drawable.s6, "DESIGNING", "FUTURE",
                "Tomorrow starts today", "980",
                "FUTURE FORM", "SS26 · TOMORROW NOW · FUTURE FORM · DESIGNING FUTURE · "),
            StoryBeat(R.drawable.s7, "PUSHING", "BOUNDARIES",
                "Innovation in every stitch", "2.2K",
                "EDGE PROTOCOL", "SS26 · INNOVATION · EDGE PROTOCOL · PUSHING BOUNDARIES · "),
            StoryBeat(R.drawable.s8, "CREATING", "MOVEMENT",
                "Join the silent revolution", "4.7K",
                "SILENT RAVE", "SS26 · REVOLUTION · SILENT RAVE · CREATING MOVEMENT · "),
            StoryBeat(R.drawable.s9, "BUILDING", "LEGACY",
                "Timeless by design", "1.3K",
                "TIME CAPSULE", "SS26 · TIMELESS · TIME CAPSULE · BUILDING LEGACY · ")
        )
    }

    // ── Internal tap navigation state ────────────────────────────
    var currentIndex by remember { mutableStateOf(0) }
    var prevIndex    by remember { mutableStateOf(0) }
    var isAnimating  by remember { mutableStateOf(false) }
    var animDir      by remember { mutableStateOf(1) }   // 1 = forward, -1 = backward
    var hasSeenAll   by remember { mutableStateOf(false) }

    // animProgress: 0f = prev fully visible, 1f = current fully visible
    val animProgress = remember { Animatable(1f) }

    // Thông báo progress lên HomeScreen
    LaunchedEffect(currentIndex) {
        onProgressUpdate((currentIndex + 1f) / 7f)
        if (currentIndex == 6) hasSeenAll = true
    }

    // Easing & cross-fade
    val easing       = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
    val ap           = animProgress.value
    val slideProgress = easing.transform(ap) * 0.6f
    val fadeProgress  = ((easing.transform(ap) - 0.2f) / 0.6f).coerceIn(0f, 1f)
    val prevOpacity   = (1f - fadeProgress).coerceIn(0f, 1f)
    val currOpacity   = fadeProgress.coerceIn(0f, 1f)

    val currentBeat = storyBeats[currentIndex]
    val prevBeat    = storyBeats[prevIndex]

    // Chromatic aberration tại transition peak
    val chromaStrength = run {
        if (ap !in 0.35f..0.65f) return@run 0f
        val t = ((ap - 0.35f) / 0.15f).coerceIn(0f, 1f)
        val peak = if (ap < 0.5f) t else 1f - ((ap - 0.5f) / 0.15f).coerceIn(0f, 1f)
        peak * 3f
    }

    // Nav functions
    val goNext = {
        if (!isAnimating && currentIndex < 6) {
            prevIndex = currentIndex
            currentIndex++
            animDir = 1
            isAnimating = true
        }
    }
    val goPrev = {
        if (!isAnimating && currentIndex > 0) {
            prevIndex = currentIndex
            currentIndex--
            animDir = -1
            isAnimating = true
        }
    }

    // Trigger animation — key CHỈ là currentIndex
    // isAnimating check bên trong để tránh restart coroutine khi state thay đổi
    LaunchedEffect(currentIndex) {
        if (!isAnimating) return@LaunchedEffect
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue   = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness    = Spring.StiffnessMediumLow
            )
        )
        isAnimating = false
    }

    // Ticker scroll
    val tickerOffset by rememberInfiniteTransition(label = "ticker").animateFloat(
        0f, -700f,
        infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
        label = "ticker_x"
    )

    // ── Root ─────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(VoidBlack)
    ) {

        // ── SCANLINES ambient ─────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.035f)) {
            var y = 0f
            while (y < size.height) {
                drawLine(Color.White, Offset(0f, y), Offset(size.width, y), 1f)
                y += 3f
            }
        }

        // ── GHOST beat number ─────────────────────────────────────
        Text(
            text          = (currentIndex + 1).toString().padStart(2, '0'),
            fontFamily    = AppFonts.oswald,
            fontSize      = 200.sp,
            fontWeight    = FontWeight.Black,
            color         = Color.White.copy(alpha = 0.04f),
            letterSpacing = (-8).sp,
            modifier      = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
        )

        // ── MAIN LAYOUT: Image left | Content right ───────────────
        Column(modifier = Modifier.fillMaxSize()) {

            // ── TOP FRAME ─────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Section label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(28.dp).height(1.dp).background(CyberAcid.copy(alpha = 0.5f)))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text          = "THE COLLECTION",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 8.sp,
                        color         = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 3.sp,
                        fontWeight    = FontWeight.Light
                    )
                }

                // Beat counter — slot machine
                AnimatedContent(
                    targetState   = currentIndex,
                    transitionSpec = {
                        if (targetState > initialState)
                            slideInVertically { it } + fadeIn(tween(160)) togetherWith
                                    slideOutVertically { -it } + fadeOut(tween(120))
                        else
                            slideInVertically { -it } + fadeIn(tween(160)) togetherWith
                                    slideOutVertically { it } + fadeOut(tween(120))
                    },
                    label = "beat_counter"
                ) { idx ->
                    Text(
                        text          = "${(idx + 1).toString().padStart(2, '0')} / 07",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 10.sp,
                        color         = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 2.sp,
                        fontWeight    = FontWeight.Medium
                    )
                }
            }

            // ── CONTENT ROW: Image | Text+Nav — wrapped in Box for title overlay ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // ──────────────────────────────────────────────────
                    // LEFT: IMAGE (55% width)
                    // ──────────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .fillMaxHeight()
                            .padding(start = 14.dp, top = 8.dp, bottom = 8.dp)
                            .clipToBounds()
                    ) {
                        // Prev image (sliding out) — chỉ render khi đang animate
                        if (ap < 1f) {
                            BeatImage(
                                imageRes   = prevBeat.imageRes,
                                opacity    = prevOpacity,
                                ap         = ap,
                                isOutgoing = true,
                                animDir    = animDir
                            )
                        }
                        // Current image — idle: ap=1 → tất cả transform=0, scale=1
                        BeatImage(
                            imageRes   = currentBeat.imageRes,
                            opacity    = if (ap < 1f) currOpacity else 1f,
                            ap         = ap,
                            isOutgoing = false,
                            animDir    = animDir
                        )

                        // RGB Chroma
                        if (chromaStrength > 0.3f) {
                            BeatImage(prevBeat.imageRes, prevOpacity * 0.1f, ap, true, animDir, chromaStrength, Color.Red)
                            BeatImage(prevBeat.imageRes, prevOpacity * 0.1f, ap, true, animDir, -chromaStrength, Color.Blue)
                        }

                        // Glitch flash
                        if (ap in 0.45f..0.55f) {
                            Box(modifier = Modifier.fillMaxSize().background(CyberAcid.copy(alpha = 0.04f)))
                        }

                        // Right edge gradient fade
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        0.7f to Color.Transparent,
                                        1f   to VoidBlack.copy(alpha = 0.85f)
                                    )
                                )
                        )

                        // Bottom gradient để title đọc được trên ảnh
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        0.4f to Color.Transparent,
                                        1f   to VoidBlack.copy(alpha = 0.88f)
                                    )
                                )
                        )

                        // Ticker at image bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .padding(bottom = 8.dp)
                                .clipToBounds()
                        ) {
                            Box(modifier = Modifier.graphicsLayer { translationX = -tickerOffset; alpha = if (ap < 1f) prevOpacity else 0f }) {
                                Text(prevBeat.tickerText.repeat(4), fontFamily = AppFonts.spaceMono,
                                    fontSize = 6.sp, color = Color.White.copy(alpha = 0.15f),
                                    letterSpacing = 1.5.sp, maxLines = 1, softWrap = false)
                            }
                            Box(modifier = Modifier.graphicsLayer { translationX = -tickerOffset; alpha = currOpacity }) {
                                Text(currentBeat.tickerText.repeat(4), fontFamily = AppFonts.spaceMono,
                                    fontSize = 6.sp, color = Color.White.copy(alpha = 0.15f),
                                    letterSpacing = 1.5.sp, maxLines = 1, softWrap = false)
                            }
                        }
                    }

                    // ──────────────────────────────────────────────────
                    // RIGHT: TEXT + NAV ARROWS
                    // ──────────────────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // ── Text content (cross-fade) ──────────────────
                        Box(modifier = Modifier.weight(1f)) {

                            // Prev text fading out
                            if (ap < 1f) {
                                BeatTextContent(
                                    beat        = prevBeat,
                                    opacity     = prevOpacity,
                                    slideOffset = slideProgress * -40f * animDir
                                )
                            }

                            // Current text fading in
                            BeatTextContent(
                                beat        = currentBeat,
                                opacity     = if (isAnimating || ap < 1f) currOpacity else 1f,
                                slideOffset = if (isAnimating || ap < 1f) (1f - slideProgress) * 40f * animDir else 0f
                            )
                        }

                        // ── NAV ARROWS ─────────────────────────────────
                        Column(
                            modifier            = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Separator line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(Color.White.copy(alpha = 0.08f))
                            )

                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                // PREV button
                                NavArrowButton(
                                    label   = "←",
                                    enabled = currentIndex > 0,
                                    onClick = { goPrev() }
                                )

                                // Progress dots — kinetic, compact để không bị crop
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Center dots trong available space
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            repeat(7) { i ->
                                                val isCurrent = i == currentIndex
                                                val isPrev    = i == prevIndex && ap < 1f
                                                val isPast    = i < currentIndex
                                                // FIX: dot active max 14dp (was 18dp) để vừa với 45% column
                                                val dotWidth = when {
                                                    isCurrent -> lerp(14.dp, 14.dp, ap)
                                                    isPrev    -> lerp(14.dp, 4.dp, ap)
                                                    else      -> 4.dp
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .width(dotWidth)
                                                        .height(3.dp)
                                                        .background(
                                                            when {
                                                                isCurrent -> CyberAcid
                                                                isPast    -> CyberAcid.copy(alpha = 0.38f)
                                                                isPrev    -> CyberAcid.copy(alpha = prevOpacity * 0.6f)
                                                                else      -> Color.White.copy(alpha = 0.12f)
                                                            },
                                                            androidx.compose.foundation.shape.RoundedCornerShape(1.5.dp)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }

                                // NEXT button
                                NavArrowButton(
                                    label   = "→",
                                    enabled = currentIndex < 6,
                                    onClick = { goNext() }
                                )
                            }
                        }
                    }
                } // end inner Row

                // ── TITLE OVERLAY — đè lên ảnh, absolute trên Row ─
                // Nằm ở bottom-left, tràn qua ranh giới image/text
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 70.dp)
                ) {
                    if (ap < 1f) {
                        BeatTitleOverlay(
                            beat        = prevBeat,
                            opacity     = prevOpacity,
                            slideOffset = slideProgress * -30f * animDir
                        )
                    }
                    BeatTitleOverlay(
                        beat        = currentBeat,
                        opacity     = if (isAnimating || ap < 1f) currOpacity else 1f,
                        slideOffset = if (isAnimating || ap < 1f) (1f - slideProgress) * 30f * animDir else 0f
                    )
                }
            } // end outer Box

            // ── BOTTOM: JOIN CTA (luôn hiện) ─────────────────────
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.padding(horizontal = 18.dp).padding(bottom = 18.dp)) {
                SignalGatewayCta(onSignalClick = onSignalClick)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// NAV ARROW BUTTON — Y2K sharp border
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun NavArrowButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val alpha = if (enabled) 1f else 0.18f
    Box(
        modifier = Modifier
            .alpha(alpha)
            .border(1.dp, CyberAcid.copy(alpha = if (enabled) 0.7f else 0.2f))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontFamily = AppFonts.oswald,
            fontSize   = 18.sp,
            color      = if (enabled) CyberAcid else Color.White.copy(alpha = 0.3f),
            fontWeight = FontWeight.Black
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
// BEAT IMAGE
// Dùng ap (0→1) trực tiếp — đảm bảo khi ap=1 (idle) mọi transform = 0
// outgoing: ap=0→neutral, ap=1→đã slide ra (không còn render)
// incoming: ap=0→từ ngoài vào, ap=1→neutral (scale=1, translate=0) ✓
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun BeatImage(
    imageRes:      Int,
    opacity:       Float,
    ap:            Float,
    isOutgoing:    Boolean,
    animDir:       Int,
    chromaOffsetX: Float = 0f,
    chromaTint:    Color = Color.Unspecified
) {
    val eased = CubicBezierEasing(0.42f, 0f, 0.58f, 1f).transform(ap)

    val slideX: Float
    val slideY: Float
    val rotation: Float
    val scale: Float

    if (isOutgoing) {
        slideX   = eased * 150f * animDir       // 0 → 150 (slide ra)
        slideY   = -(eased * 40f)               // 0 → -40
        rotation = eased * 1.2f * animDir       // 0 → 1.2°
        scale    = 1f - eased * 0.15f           // 1.0 → 0.85
    } else {
        slideX   = (1f - eased) * -150f * animDir  // -150 → 0 ✓
        slideY   = (1f - eased) * 40f              // 40 → 0 ✓
        rotation = -(1f - eased) * 1.2f * animDir  // -1.2° → 0 ✓
        scale    = 0.85f + eased * 0.15f           // 0.85 → 1.0 ✓
    }

    val isChroma = chromaOffsetX != 0f && chromaTint != Color.Unspecified

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(opacity)
            .graphicsLayer {
                translationX = slideX + chromaOffsetX
                translationY = slideY
                rotationZ    = rotation
                scaleX       = scale
                scaleY       = scale
            }
    ) {
        if (isChroma) {
            Image(
                painter            = painterResource(imageRes),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                colorFilter        = ColorFilter.tint(chromaTint, BlendMode.Modulate),
                modifier           = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter            = painterResource(imageRes),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f    to VoidBlack.copy(alpha = 0.5f),
                            0.15f to Color.Transparent,
                            0.85f to Color.Transparent,
                            1f    to VoidBlack.copy(alpha = 0.7f)
                        )
                    )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// BEAT TEXT CONTENT (right column) — chỉ còn descriptor + tagline
// Title/subtitle đã chuyển sang image overlay
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun BeatTextContent(
    beat:        StoryBeat,
    opacity:     Float,
    slideOffset: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .alpha(opacity)
            .graphicsLayer { translationX = slideOffset },
        verticalArrangement = Arrangement.Center
    ) {
        // Descriptor
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(14.dp).height(1.dp).background(CyberAcid.copy(alpha = 0.8f)))
            Spacer(Modifier.width(6.dp))
            Text(
                text          = beat.descriptor,
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 7.5.sp,
                color         = CyberAcid.copy(alpha = 0.9f),
                letterSpacing = 2.5.sp,
                fontWeight    = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(16.dp))

        // Tagline box — Y2K sharp + accent shadow
        Box {
            // Shadow offset box
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .offset(x = 2.dp, y = 2.dp)
                    .border(0.8.dp, CyberAcid.copy(alpha = 0.18f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(beat.tagline, fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = Color.Transparent, lineHeight = 14.sp, maxLines = 2)
                    Spacer(Modifier.height(4.dp))
                    Text("${beat.signalCount} posts", fontFamily = AppFonts.spaceMono,
                        fontSize = 7.sp, color = Color.Transparent)
                }
            }
            // Main box
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .border(1.dp, CyberAcid.copy(alpha = 0.65f))
                    .background(VoidBlack.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text          = beat.tagline,
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 9.sp,
                        color         = Color.White.copy(alpha = 0.6f),
                        lineHeight    = 14.sp,
                        letterSpacing = 0.2.sp,
                        maxLines      = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(3.dp).background(CyberAcid, androidx.compose.foundation.shape.CircleShape))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text          = "${beat.signalCount} posts on Signal",
                            fontFamily    = AppFonts.spaceMono,
                            fontSize      = 7.sp,
                            color         = CyberAcid.copy(alpha = 0.7f),
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
// BEAT TITLE OVERLAY — đè lên ảnh, font lớn, Y2K editorial style
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun BeatTitleOverlay(
    beat:        StoryBeat,
    opacity:     Float,
    slideOffset: Float
) {
    Column(
        modifier = Modifier
            .alpha(opacity)
            .graphicsLayer { translationX = slideOffset }
    ) {
        // Title — solid white, lớn
        Text(
            text          = beat.title,
            fontFamily    = AppFonts.oswald,
            fontSize      = 54.sp,
            fontWeight    = FontWeight.Black,
            color         = Color.White,
            letterSpacing = (-2.5).sp,
            lineHeight    = 52.sp,
            softWrap      = false,
            maxLines      = 1
        )
        // Subtitle — stroke, offset lên
        Text(
            text          = beat.subtitle,
            fontFamily    = AppFonts.oswald,
            fontSize      = 54.sp,
            fontWeight    = FontWeight.Black,
            letterSpacing = (-2.5).sp,
            lineHeight    = 52.sp,
            softWrap      = false,
            maxLines      = 1,
            style         = TextStyle(
                drawStyle = Stroke(width = 2f),
                color     = Color.White.copy(alpha = 0.45f)
            ),
            modifier = Modifier.offset(y = (-14).dp)
        )
    }
}

@Composable
private fun SignalGatewayCta(onSignalClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "signal_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        0.4f, 1f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "signal_border"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        0.02f, 0.08f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "signal_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberAcid.copy(alpha = borderAlpha))
            .background(
                Brush.horizontalGradient(listOf(
                    CyberAcid.copy(alpha = glowAlpha),
                    Color.Black.copy(alpha = 0.9f),
                    CyberAcid.copy(alpha = glowAlpha)
                ))
            )
            .clickable { onSignalClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Live dot
                val pulseAlpha by rememberInfiniteTransition(label = "dot").animateFloat(
                    0.4f, 1f,
                    infiniteRepeatable(tween(650), RepeatMode.Reverse),
                    label = "dot_alpha"
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .alpha(pulseAlpha)
                        .background(CyberAcid, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text          = "JOIN THE COMMUNITY",
                        fontFamily    = AppFonts.oswald,
                        fontSize      = 18.sp,
                        fontWeight    = FontWeight.Black,
                        color         = Color.White,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text          = "THE SIGNAL · See how others wear it",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 7.sp,
                        color         = Color.White.copy(alpha = 0.35f),
                        letterSpacing = 0.3.sp
                    )
                }
            }
            Text(
                text       = "→",
                fontFamily = AppFonts.oswald,
                fontSize   = 24.sp,
                color      = CyberAcid,
                fontWeight = FontWeight.Black
            )
        }
    }
}