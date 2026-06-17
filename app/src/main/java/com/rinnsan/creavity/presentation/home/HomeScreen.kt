package com.rinnsan.creavity.presentation.home

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.rinnsan.creavity.R
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.presentation.home.sections.RunwayPinnedSection
import com.rinnsan.creavity.presentation.home.sections.VaultStripSection
import com.rinnsan.creavity.presentation.home.sections.DiagonalScrollSection
import com.rinnsan.creavity.presentation.home.sections.SoloArtifactSection
import com.rinnsan.creavity.presentation.archive.VaultViewModel
import com.rinnsan.creavity.presentation.auth.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.rinnsan.creavity.data.vault.AffiliateArtifact
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun HomeScreen(
    navController: NavController,
    vaultViewModel: VaultViewModel = hiltViewModel()
) {
    val density       = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight  = with(configuration) { screenHeightDp.dp }

    // ── ViewModels ─────────────────────────────────────────────────
    val vaultArtifacts by vaultViewModel.allArtifacts.collectAsState()
    val randomArtifacts = remember(vaultArtifacts) { vaultArtifacts.shuffled() }
    val vaultIsLoading  by vaultViewModel.isLoading.collectAsState()

    val screenHeightPx = remember(configuration.screenHeightDp, density) {
        with(density) {
            val px = configuration.screenHeightDp.dp.toPx()
            if (px <= 0f) 1f else px
        }
    }

    val scrollState = rememberScrollState()
    val scrollY = scrollState.value.toFloat()

    // ── SCROLL MATH ────────────────────────────────────────────────
    val heroHeight       = screenHeightPx
    val runwayStart      = heroHeight
    val runwayDuration   = screenHeightPx * 3f
    val vaultStart       = runwayStart + runwayDuration
    val vaultDuration    = screenHeightPx * 5f
    val diagonalStart    = vaultStart + vaultDuration
    val diagonalDuration = screenHeightPx * 1.2f   // V4: tap-driven, pin 1.2x screen
    val soloStart        = diagonalStart + diagonalDuration
    val soloDuration     = screenHeightPx * 8f
    val footerStart      = soloStart + soloDuration

    val totalScrollHeight = footerStart + screenHeightPx

    // ── PROGRESS VALUES ────────────────────────────────────────────
    val runwayProgress   = ((scrollY - runwayStart)   / runwayDuration).coerceIn(0f, 1f)
    val vaultProgress    = ((scrollY - vaultStart)    / vaultDuration).coerceIn(0f, 1f)
    val diagonalProgress = ((scrollY - diagonalStart) / diagonalDuration).coerceIn(0f, 1f)
    val soloProgress     = ((scrollY - soloStart)     / soloDuration).coerceIn(0f, 1f)

    // ── SMOOTH TRANSITION — section slide vào từ 0.5 screen trước điểm bắt đầu ──
    // transitionRange: khoảng scroll để section trượt từ bottom lên đúng vị trí
    val transitionRange = screenHeightPx * 0.6f

    // Vault: bắt đầu slide khi còn cách vaultStart 0.6 screen
    val vaultSlideStart = vaultStart - transitionRange
    val vaultSlideRaw   = ((scrollY - vaultSlideStart) / transitionRange).coerceIn(0f, 1f)
    // Eased: nhanh lúc đầu, chậm dần khi đến đúng vị trí
    val vaultSlideT     = FastOutSlowInEasing.transform(vaultSlideRaw)
    val vaultTranslation = ((1f - vaultSlideT) * screenHeightPx).coerceAtLeast(0f)

    // Diagonal
    val diagSlideStart  = diagonalStart - transitionRange
    val diagSlideRaw    = ((scrollY - diagSlideStart) / transitionRange).coerceIn(0f, 1f)
    val diagSlideT      = FastOutSlowInEasing.transform(diagSlideRaw)
    val diagonalTranslation = ((1f - diagSlideT) * screenHeightPx).coerceAtLeast(0f)

    // Solo
    val soloSlideStart  = soloStart - transitionRange
    val soloSlideRaw    = ((scrollY - soloSlideStart) / transitionRange).coerceIn(0f, 1f)
    val soloSlideT      = FastOutSlowInEasing.transform(soloSlideRaw)
    val soloTranslation = ((1f - soloSlideT) * screenHeightPx).coerceAtLeast(0f)

    // Footer
    val footerTranslation = (footerStart - scrollY).coerceAtLeast(0f)

    // ── ENTRY ALPHA — fade in khi section slide vào ────────────────
    val vaultEntryAlpha    = vaultSlideT.coerceIn(0f, 1f)
    val diagonalEntryAlpha = diagSlideT.coerceIn(0f, 1f)
    val soloEntryAlpha     = soloSlideT.coerceIn(0f, 1f)

    // ── VISIBILITY GATES — hiện sớm hơn để slide animation chạy ───
    val isHeroVisible     = scrollY < heroHeight
    val isRunwayVisible   = scrollY >= runwayStart - 50f && vaultTranslation > 0f
    val isVaultVisible    = scrollY >= vaultSlideStart && diagonalTranslation > 0f
    val isDiagonalVisible = scrollY >= diagSlideStart  && soloTranslation > 0f

    // ── NAV STATE ──────────────────────────────────────────────────
    var isMenuOpen   by remember { mutableStateOf(false) }
    var lastScrollY  by remember { mutableFloatStateOf(0f) }
    var isNavVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.value }
            .collect { currentScroll ->
                val delta = currentScroll - lastScrollY
                when {
                    currentScroll < 50 -> isNavVisible = true
                    delta < -10        -> isNavVisible = true
                    delta > 10         -> isNavVisible = false
                }
                lastScrollY = currentScroll.toFloat()
            }
    }

    val shouldScrollToTop by remember {
        derivedStateOf { scrollState.value > runwayStart.toInt() }
    }

    val scope = rememberCoroutineScope()

    // ══════════════════════════════════════════════════════════════
    // ROOT BOX — FIX SCROLL + CLICK
    //
    // Vấn đề gốc rễ:
    //   - Nếu scroll driver (verticalScroll Column) nằm z=0 (dưới):
    //     sections ở z cao hơn block scroll → không lướt được.
    //   - Nếu scroll driver nằm z=100 (trên):
    //     nó block tap → click card không hoạt động.
    //
    // Giải pháp đúng:
    //   - Dùng pointerInput + detectVerticalDragGestures trên ROOT BOX.
    //   - Drag gesture được handle ở Initial pass (trước khi con nhận),
    //     nhưng KHÔNG consume event → con (card, button) vẫn nhận tap.
    //   - Scroll state được update thủ công qua scrollState.scrollBy().
    //   - Không còn Column verticalScroll overlay nào cả.
    // ══════════════════════════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .pointerInput(Unit) {
                // Custom scroll: intercept drag + fling, KHÔNG consume tap
                // → card click / button click vẫn hoạt động bình thường
                val velocityTracker = VelocityTracker()
                detectVerticalDragGestures(
                    onDragStart  = { velocityTracker.resetTracking() },
                    onDragCancel = { velocityTracker.resetTracking() },
                    onDragEnd    = {
                        val velocity = velocityTracker.calculateVelocity().y
                        scope.launch {
                            scrollState.animateScrollBy(
                                value = -velocity * 0.3f,
                                animationSpec = tween(400, easing = FastOutLinearInEasing)
                            )
                        }
                        velocityTracker.resetTracking()
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(
                            change.uptimeMillis,
                            change.position
                        )
                        scope.launch { scrollState.scrollBy(-dragAmount) }
                    }
                )
            }
    ) {
        // ── RUNWAY (pinned, bị Vault đè lên) ──────────────────────
        if (isRunwayVisible) {
            Box(modifier = Modifier.fillMaxSize()) {
                RunwayPinnedSection(height = screenHeight, progress = runwayProgress)
            }
        }

        // ── HERO (scroll lên và ra) ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = -scrollY.coerceIn(0f, heroHeight) }
        ) {
            HeroSection(screenHeight = screenHeight, shouldPlay = isHeroVisible)
        }

        // ── VAULT STRIP ───────────────────────────────────────────
        if (isVaultVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = vaultTranslation
                        alpha        = vaultEntryAlpha
                    }
            ) {
                VaultStripSection(
                    height         = screenHeight,
                    progress       = vaultProgress,
                    artifacts      = randomArtifacts.take(4),
                    isLoading      = vaultIsLoading,
                    onProductClick = { id -> navController.navigate("${Routes.ARTIFACT_DETAIL}/$id") },
                    onViewAllClick = { navController.navigate(Routes.ARTIFACT_ARCHIVE) }
                )
            }
        }

        // ── DIAGONAL SCROLL ───────────────────────────────────────
        if (isDiagonalVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = diagonalTranslation
                        alpha        = diagonalEntryAlpha
                    }
            ) {
                DiagonalScrollSection(
                    height           = screenHeight,
                    progress         = diagonalProgress,
                    onSignalClick    = { navController.navigate(Routes.SIGNAL) },
                    onProgressUpdate = { /* beat progress — reserved */ }
                )
            }
        }

        // ── SOLO / STYLIST GATEWAY ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = soloTranslation
                    alpha        = soloEntryAlpha
                }
        ) {
            SoloArtifactSection(
                height         = screenHeight,
                progress       = soloProgress,
                onStylistClick = { navController.navigate(Routes.STYLIST) }
            )
        }

        // ── FOOTER ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = footerTranslation },
            contentAlignment = Alignment.BottomCenter
        ) {
            FooterSection()
        }

        // ── SCROLL HINT ARROW (shows only before Diagonal) ────────
        AnimatedVisibility(
            visible = scrollY < diagonalStart,
            enter   = fadeIn(tween(300)),
            exit    = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .zIndex(201f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val infiniteTransition = rememberInfiniteTransition(label = "arrow")
                val offsetY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue  = 8f,
                    animationSpec = infiniteRepeatable(
                        animation  = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "arrow_bounce"
                )

                Text(
                    text          = if (shouldScrollToTop) "BACK TO TOP" else "SCROLL TO ENTER",
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 9.sp,
                    color         = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(
                    onClick = {
                        scope.launch {
                            if (shouldScrollToTop) {
                                scrollState.animateScrollTo(0)
                            } else {
                                scrollState.animateScrollTo((runwayStart + screenHeightPx * 0.25f).toInt())
                            }
                        }
                    },
                    modifier = Modifier.offset(y = offsetY.dp)
                ) {
                    Icon(
                        imageVector = if (shouldScrollToTop) {
                            Icons.Default.ArrowUpward
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint               = CyberAcid,
                        modifier           = Modifier.size(28.dp)
                    )
                }
            }
        }

        // ── FLOATING NAV BAR ───────────────────────────────────────
        AnimatedVisibility(
            visible = isNavVisible,
            enter   = slideInVertically(
                initialOffsetY = { it },
                animationSpec  = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(200)),
            exit    = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250, easing = FastOutLinearInEasing)
            ) + fadeOut(tween(150)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .zIndex(200f)
        ) {
            FloatingNavBar(
                onShopClick   = { navController.navigate(Routes.ARTIFACT_ARCHIVE) },
                onMenuClick   = { isMenuOpen = true },
                onScrollAction = {
                    scope.launch {
                        if (shouldScrollToTop) {
                            scrollState.animateScrollTo(0)
                        } else {
                            scrollState.animateScrollTo((runwayStart + screenHeightPx * 0.25f).toInt())
                        }
                    }
                },
                showUpArrow = shouldScrollToTop
            )
        }

        // ── FULLSCREEN MENU OVERLAY ────────────────────────────────
        AnimatedVisibility(
            visible  = isMenuOpen,
            enter    = fadeIn(tween(300)),
            exit     = fadeOut(tween(300)),
            modifier = Modifier.zIndex(300f)
        ) {
            FullscreenMenu(
                onClose       = { isMenuOpen = false },
                navController = navController
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HERO SECTION — giữ nguyên hoàn toàn
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@OptIn(UnstableApi::class)
@Composable
fun HeroSection(
    screenHeight: Dp,
    shouldPlay: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight)
    ) {
        val context = LocalContext.current
        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.intro_loop}")
                setMediaItem(MediaItem.fromUri(uri))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                prepare()
            }
        }

        LaunchedEffect(shouldPlay) {
            if (shouldPlay) {
                exoPlayer.playWhenReady = true
            } else {
                exoPlayer.playWhenReady = false
            }
        }

        DisposableEffect(Unit) {
            onDispose { exoPlayer.release() }
        }

        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player       = exoPlayer
                    useController = false
                    resizeMode   = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f    to Color.Black.copy(alpha = 0.7f),
                        0.25f to Color.Transparent,
                        0.6f  to Color.Black.copy(alpha = 0.3f),
                        1f    to Color.Black.copy(alpha = 0.95f)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = screenHeight * 0.3f)
        ) {
            InfiniteMarqueeText(
                text     = "FUTURE WEAR // Y-3 // CYBERPUNK // RINNSAN // ",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .rotate(-8.6f)
                    .alpha(0.08f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 15.dp)
                .align(Alignment.CenterEnd)
        ) {
            VerticalMarqueeText(
                text = " // SYSTEM: ONLINE // RENDER: KINETIC // DATA: ENCRYPTED"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TechText("35.689° N")
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(12.dp)
                        .background(CyberAcid.copy(alpha = 0.3f))
                )
                TechText("TOKYO // 2026")
            }

            Spacer(modifier = Modifier.weight(3f))

            Column {
                Text(
                    text       = "RinnSan",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 58.sp,
                    color      = Color.White,
                    lineHeight = 58.sp,
                    style      = TextStyle(
                        shadow = Shadow(
                            color      = Color.Black.copy(alpha = 0.8f),
                            blurRadius = 12f,
                            offset     = Offset(2f, 2f)
                        )
                    )
                )

                // AutoSize: scale xuống nếu chữ quá rộng
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val availableWidth = maxWidth
                    // Tính fontSize tương đối theo width — 90sp cần ~340dp, scale nếu hẹp hơn
                    val scaledFontSize = with(LocalDensity.current) {
                        val targetPx = availableWidth.toPx() * 0.92f
                        val basePx   = 90.sp.toPx()
                        val ratio    = (targetPx / basePx).coerceAtMost(1f)
                        (90 * ratio).sp
                    }
                    Text(
                        text          = "CREAVITY",
                        fontFamily    = AppFonts.oswald,
                        fontWeight    = FontWeight.Black,
                        fontSize      = scaledFontSize,
                        color         = TeslaWhite,
                        lineHeight    = scaledFontSize,
                        letterSpacing = (-3).sp,
                        maxLines      = 1,
                        softWrap      = false,
                        style         = TextStyle(
                            shadow = Shadow(
                                color      = CyberAcid.copy(alpha = 0.3f),
                                blurRadius = 20f
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                CyberAcid.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RectangleShape
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    StatItem("COLLECTION", "VOL.04")
                    VerticalDivider()
                    StatItem("DESIGN", "Y-3")
                    VerticalDivider()
                    StatItem("MODE", "KINETIC")
                }
            }

            Spacer(modifier = Modifier.weight(2f))

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// FOOTER SECTION — giữ nguyên
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun FooterSection() {
    Box(
        modifier          = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(VoidBlack),
        contentAlignment  = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(1.dp)
                    .background(CyberAcid)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text          = "END OF TRANSMISSION",
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 11.sp,
                color         = TeslaWhite.copy(alpha = 0.3f),
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(1.dp)
                    .background(CyberAcid)
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// FLOATING NAV BAR — giữ nguyên
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun FloatingNavBar(
    onShopClick: () -> Unit,
    onMenuClick: () -> Unit,
    onScrollAction: () -> Unit,
    showUpArrow: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = Modifier
            .width(320.dp)
            .height(68.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
            .padding(horizontal = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = "VAULT",
                fontFamily = AppFonts.oswald,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                letterSpacing = 2.sp,
                color      = TeslaWhite.copy(alpha = 0.9f),
                modifier   = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onShopClick() }
                    .padding(vertical = 10.dp, horizontal = 5.dp)
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CyberAcid)
                    .clickable { onScrollAction() }
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (showUpArrow) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint               = Color.Black,
                    modifier           = Modifier.size(22.dp)
                )
            }

            Text(
                text       = "MENU",
                fontFamily = AppFonts.oswald,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                letterSpacing = 2.sp,
                color      = TeslaWhite.copy(alpha = 0.9f),
                modifier   = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onMenuClick() }
                    .padding(vertical = 10.dp, horizontal = 5.dp)
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// FULLSCREEN MENU — giữ nguyên
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun FullscreenMenu(onClose: () -> Unit, navController: NavController) {
    val authViewModel: AuthViewModel = hiltViewModel()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { onClose() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // ── Brand wordmark ──────────────────────────────────────
            Column {
                Text(
                    text          = "RINNSAN",
                    fontFamily    = AppFonts.oswald,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 28.sp,
                    color         = TeslaWhite,
                    letterSpacing = (-1).sp
                )
                Text(
                    text          = "CREATIVITY",
                    fontFamily    = AppFonts.oswald,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 28.sp,
                    letterSpacing = (-1).sp,
                    style         = TextStyle(
                        drawStyle = Stroke(width = 1.5f),
                        color     = TeslaWhite
                    )
                )
            }

            Spacer(modifier = Modifier.weight(2f))

            // ── Main nav items ──────────────────────────────────────
            MenuItem("01", "SIGNAL", 0) {
                navController.navigate(Routes.SIGNAL); onClose()
            }
            MenuItem("02", "BRAND", 100) {
                navController.navigate(Routes.BRAND); onClose()
            }
            MenuItem("03", "STYLIST", 200) {
                navController.navigate(Routes.STYLIST); onClose()
            }
            MenuItem("04", "CONTACT", 300) {
                navController.navigate(Routes.CONTACT); onClose()
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Divider ─────────────────────────────────────────────
            MenuSecondaryDivider(delayMs = 400)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Utility row ─────────────────────────────────────────
            MenuUtilRow(
                items = listOf(
                    MenuUtilItem("PROFILE",   "ID", 450) {
                        navController.navigate(Routes.PROFILE); onClose()
                    },
                    MenuUtilItem("WISHLIST",  "★",  500) {
                        navController.navigate(Routes.WISHLIST); onClose()
                    },
                    MenuUtilItem("BODY DATA", "//", 550) {
                        navController.navigate(Routes.BODY_DATA); onClose()
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Logout ──────────────────────────────────────────────
            MenuLogoutButton(delayMs = 600) {
                authViewModel.logout()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
                onClose()
            }

            Spacer(modifier = Modifier.weight(2f))

            // ── Footer ──────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text          = "TOKYO // 2026",
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 10.sp,
                    color         = TeslaWhite.copy(alpha = 0.3f),
                    letterSpacing = 2.sp
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CyberAcid, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // ── Close button ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 32.dp)
                .size(48.dp)
                .border(1.dp, TeslaWhite.copy(alpha = 0.2f), CircleShape)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Close,
                contentDescription = "Close",
                tint               = TeslaWhite,
                modifier           = Modifier.size(24.dp)
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MENU COMPONENTS — giữ nguyên
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun MenuLogoutButton(delayMs: Int, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMs.toLong()); visible = true }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label         = "logout_fade"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .border(1.dp, GlitchRed.copy(alpha = 0.35f))
            .background(GlitchRed.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text       = "//",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = GlitchRed.copy(alpha = 0.7f)
            )
            Text(
                text          = "TERMINATE SESSION",
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = GlitchRed.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )
        }
        Icon(
            imageVector        = Icons.Default.ArrowForward,
            contentDescription = null,
            tint               = GlitchRed.copy(alpha = 0.5f),
            modifier           = Modifier.size(16.dp)
        )
    }
}

@Composable
fun MenuItem(index: String, title: String, delayMs: Int, onClick: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        isVisible = true
    }

    val slideY by animateFloatAsState(
        targetValue   = if (isVisible) 0f else 0.3f,
        animationSpec = tween(600, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
        label         = "slide_$title"
    )

    val alpha by animateFloatAsState(
        targetValue   = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
        label         = "fade_$title"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = slideY * 100f
                this.alpha   = alpha
            }
            .clickable { onClick() }
            .padding(bottom = 32.dp)
    ) {
        Column {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text          = index,
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 14.sp,
                    color         = CyberAcid,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.width(32.dp))

                Text(
                    text          = title,
                    fontFamily    = AppFonts.oswald,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 48.sp,
                    color         = TeslaWhite,
                    letterSpacing = (-2).sp,
                    lineHeight    = 52.sp,
                    modifier      = Modifier.weight(1f)
                )

                Icon(
                    imageVector        = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint               = TeslaWhite.copy(alpha = 0.5f),
                    modifier           = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TeslaWhite.copy(alpha = 0.1f))
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// UTILITY COMPOSABLES — giữ nguyên
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun InfiniteMarqueeText(text: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    val offsetX by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = -300f,
        animationSpec = infiniteRepeatable(
            animation  = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_offset"
    )

    Text(
        text       = text.repeat(20),
        fontFamily = AppFonts.oswald,
        fontWeight = FontWeight.Black,
        fontSize   = 130.sp,
        color      = Color.White,
        maxLines   = 1,
        softWrap   = false,
        modifier   = modifier.graphicsLayer { translationX = offsetX }
    )
}

@Composable
fun VerticalMarqueeText(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "v_marquee")
    val offsetX by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = -200f,
        animationSpec = infiniteRepeatable(
            animation  = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "v_marquee_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .rotate(90f)
    ) {
        Text(
            text          = text.repeat(10),
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 10.sp,
            color         = TechSilver.copy(alpha = 0.5f),
            letterSpacing = 4.sp,
            maxLines      = 1,
            softWrap      = false,
            modifier      = Modifier.graphicsLayer { translationX = offsetX }
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text          = label,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 9.sp,
            color         = TechSilver.copy(alpha = 0.6f),
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text          = value,
            fontFamily    = AppFonts.oswald,
            fontWeight    = FontWeight.Bold,
            fontSize      = 18.sp,
            color         = TeslaWhite,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun TechText(text: String) {
    Text(
        text          = text,
        fontFamily    = AppFonts.spaceMono,
        fontSize      = 10.sp,
        color         = TechSilver.copy(alpha = 0.7f),
        letterSpacing = 2.5.sp
    )
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(TeslaWhite.copy(alpha = 0.1f))
    )
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MENU SECONDARY DIVIDER — giữ nguyên
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun MenuSecondaryDivider(delayMs: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMs.toLong()); visible = true }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label         = "divider_fade"
    )

    Row(
        modifier              = Modifier.fillMaxWidth().alpha(alpha),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(4.dp).background(CyberAcid))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        0f to TeslaWhite.copy(alpha = 0.12f),
                        1f to Color.Transparent
                    )
                )
        )
        Text(
            text          = "UTILITY",
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 9.sp,
            color         = TechSilver.copy(alpha = 0.45f),
            letterSpacing = 2.sp
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MENU UTIL CHIP ROW — giữ nguyên
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
data class MenuUtilItem(
    val label:   String,
    val tag:     String,
    val delayMs: Int,
    val onClick: () -> Unit
)

@Composable
private fun MenuUtilRow(items: List<MenuUtilItem>) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            MenuUtilChip(
                label    = item.label,
                tag      = item.tag,
                delayMs  = item.delayMs,
                onClick  = item.onClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MenuUtilChip(
    label:    String,
    tag:      String,
    delayMs:  Int,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMs.toLong()); visible = true }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(350),
        label         = "chip_$label"
    )
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 20f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label         = "chip_y_$label"
    )

    Box(
        modifier = modifier
            .alpha(alpha)
            .graphicsLayer { translationY = offsetY }
            .border(1.dp, TeslaWhite.copy(alpha = 0.1f))
            .background(TeslaWhite.copy(alpha = 0.03f))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = tag,
                fontFamily = AppFonts.spaceMono,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = CyberAcid
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text          = label,
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 8.sp,
                color         = TechSilver,
                letterSpacing = 1.sp
            )
        }
    }
}