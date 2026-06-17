package com.rinnsan.creavity.presentation.uplink

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.models.*
import com.rinnsan.creavity.presentation.uplink.stylist.StylistState
import com.rinnsan.creavity.presentation.stylist.VirtualStylistBox
import kotlinx.coroutines.delay

/**
 * ═══════════════════════════════════════════════════════════════════
 * THE UPLINK V3.0 - HIGH FASHION WITH VISUAL EXCELLENCE
 * ═══════════════════════════════════════════════════════════════════
 *
 * NEW IN V3.0:
 * ✨ Expanded color palette (ElectricBlue, NeonPurple, LaserGreen)
 * ✨ Glass morphism design
 * ✨ Breathing animations
 * ✨ Glitch text effects
 * ✨ Scan line animations
 * ✨ Particle background
 * ✨ Geometric shapes
 * ✨ Progress bars
 * ✨ Archetype visualization
 * ✨ Enhanced depth & shadows
 */

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COLOR PALETTE V3.0
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

val ElectricBlue = Color(0xFF00D9FF)
val NeonPurple = Color(0xFFB366FF)
val LaserGreen = Color(0xFF39FF14)
val DeepBlack = Color(0xFF0A0A0A)

@Composable
fun UplinkScreen(
    navController: NavController,
    identityProfile: IdentityProfile? = null,
    stylistState: StylistState = if (identityProfile != null) StylistState.ONLINE else StylistState.OFFLINE,
    onReset: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BOOT SEQUENCE STATE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    var headerVisible by remember { mutableStateOf(false) }
    var heroVisible by remember { mutableStateOf(false) }
    var boxAVisible by remember { mutableStateOf(false) }
    var boxBVisible by remember { mutableStateOf(false) }
    var accentVisible by remember { mutableStateOf(false) }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BOOT SEQUENCE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    LaunchedEffect(Unit) {
        delay(200)
        headerVisible = true

        delay(300)
        heroVisible = true

        delay(400)
        boxAVisible = true

        delay(500)
        boxBVisible = true

        delay(300)
        accentVisible = true
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // STYLIST STATE — nhận từ ViewModel qua parameter
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // MAIN LAYOUT
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // ═════════════════════════════════════════════════════════════
        // LAYER 1: ENHANCED NOISE TEXTURE + PARTICLES
        // ═════════════════════════════════════════════════════════════
        EnhancedNoiseTexture()
        ParticleBackground()

        // ═════════════════════════════════════════════════════════════
        // LAYER 2: SCROLLABLE CONTENT
        // ═════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 140.dp)
        ) {

            // ─────────────────────────────────────────────────────────
            // MASTHEAD V3.0
            // ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn(tween(800)) + slideInVertically(
                    initialOffsetY = { -it / 4 },
                    animationSpec = tween(800)
                )
            ) {
                FashionMastheadV3()
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ─────────────────────────────────────────────────────────
            // HERO SECTION V3.0 - WITH VISUALS
            // ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = heroVisible,
                enter = fadeIn(tween(1000)) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(1000)
                )
            ) {
                HeroSectionV3(identityProfile = identityProfile)
            }

            Spacer(modifier = Modifier.height(60.dp))

            // ─────────────────────────────────────────────────────────
            // ACCENT LINE WITH GLOW
            // ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = accentVisible,
                enter = fadeIn(tween(600))
            ) {
                GlowingAccentLine()
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ─────────────────────────────────────────────────────────
            // BOX A - IDENTITY CARD V3.0
            // ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = boxAVisible,
                enter = fadeIn(tween(1000)) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(1000)
                )
            ) {
                Column {
                    EditorialLabelV3(
                        number = "01",
                        title = "IDENTITY VERIFICATION",
                        subtitle = "Foundation of the system"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IdentityCardV3(
                        profile = identityProfile,
                        navController = navController,
                        onReset = onReset
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // ─────────────────────────────────────────────────────────
            // BOX B - VIRTUAL STYLIST
            // ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = boxBVisible,
                enter = fadeIn(tween(1000)) + slideInHorizontally(
                    initialOffsetX = { it / 3 },
                    animationSpec = tween(1000)
                )
            ) {
                Column {
                    EditorialLabelV3(
                        number = "02",
                        title = "VIRTUAL STYLIST",
                        subtitle = "Intelligence with presence"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, end = 12.dp)
                    ) {
                        // ✅ Identity Gate
                        if (identityProfile != null) {
                            VirtualStylistBox(
                                profile = identityProfile,
                                stylistState = stylistState,
                                navController = navController,
                                onNavigateToTerminal = {
                                    navController.navigate(Routes.STYLIST_CHAT)
                                },
                                onNavigateToArtifact = { artifactId ->
                                    navController.navigate("${Routes.ARTIFACT_DETAIL}/$artifactId")
                                }
                            )
                        } else {
                            StylistLockedStateV3()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // ─────────────────────────────────────────────────────────
            // SYSTEM SIGNATURE
            // ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = accentVisible,
                enter = fadeIn(tween(800, delayMillis = 400))
            ) {
                SystemSignatureV3()
            }
        }

        // ═════════════════════════════════════════════════════════════
        // LAYER 3: KINETIC TYPOGRAPHY
        // ═════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = accentVisible,
            enter = fadeIn(tween(1000)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            KineticTypography()
        }

        // ═════════════════════════════════════════════════════════════
        // LAYER 4: SCAN LINE EFFECT
        // ═════════════════════════════════════════════════════════════
        ScanLineEffect()

        // ═════════════════════════════════════════════════════════════
        // LAYER 5: FLOATING ACCENT
        // ═════════════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = accentVisible,
            enter = fadeIn(tween(1200)),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            FloatingAccent()
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ENHANCED COMPONENTS V3.0
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun EnhancedNoiseTexture() {
    val infiniteTransition = rememberInfiniteTransition(label = "noise")

    val noiseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "noise_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = noiseAlpha * 0.5f),
                        Color.Transparent,
                        NeonPurple.copy(alpha = noiseAlpha * 0.3f),
                        Color.Transparent,
                        LaserGreen.copy(alpha = noiseAlpha * 0.2f)
                    )
                )
            )
    )
}

@Composable
fun ParticleBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    // Tạo animations bên ngoài Canvas
    val particleOffsets = List(30) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (5000 + index * 200),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_$index"
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particleOffsets.forEachIndexed { index, offsetAnim ->
            val x = (index * size.width / 30)
            val y = offsetAnim.value * size.height
            val color = when (index % 3) {
                0 -> ElectricBlue
                1 -> NeonPurple
                else -> LaserGreen
            }

            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = 1.5f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun ScanLineEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .offset(y = offsetY.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        ElectricBlue.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun KineticTypography() {
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")

    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier.offset(x = offsetX.dp),
            horizontalArrangement = Arrangement.spacedBy(60.dp)
        ) {
            repeat(10) {
                Text(
                    text = "SYSTEM READY // SCANNING // AWAITING INPUT // INTELLIGENCE ACTIVE //",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = ElectricBlue.copy(alpha = 0.4f),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MASTHEAD V3.0
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun FashionMastheadV3() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 40.dp)
    ) {
        // Top bar with indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BreathingDot(color = LaserGreen)

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "THE UPLINK",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 8.sp,
                    color = ElectricBlue.copy(alpha = 0.8f),
                    letterSpacing = 3.sp
                )
            }

            Text(
                text = "FW26",
                fontFamily = AppFonts.spaceMono,
                fontSize = 8.sp,
                color = TechSilver.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main title with spacing
        Text(
            text = "W E A R",
            fontFamily = AppFonts.oswald,
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = TeslaWhite,
            lineHeight = 44.sp,
            letterSpacing = 8.sp
        )

        Text(
            text = "Y O U R",
            fontFamily = AppFonts.oswald,
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = TeslaWhite,
            lineHeight = 44.sp,
            letterSpacing = 8.sp
        )

        // "ALGORITHM" in glowing box
        GlowingBox(
            brush = Brush.linearGradient(
                colors = listOf(ElectricBlue, NeonPurple)
            )
        ) {
            Text(
                text = "ALGORITHM",
                fontFamily = AppFonts.oswald,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = DeepBlack,
                lineHeight = 36.sp,
                letterSpacing = 4.sp,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle
        Text(
            text = "Fashion intelligence system",
            fontFamily = AppFonts.spaceMono,
            fontSize = 13.sp,
            color = TechSilver.copy(alpha = 0.8f),
            letterSpacing = 1.sp
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HERO SECTION V3.0 - WITH GEOMETRIC SHAPES
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun HeroSectionV3(identityProfile: IdentityProfile?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(horizontal = 20.dp)
    ) {
        // Glass container
        GlassBox {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                // Background geometric shapes
                GeometricShapes()

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top metadata bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BreathingDot(color = LaserGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EDITORIAL_V3.0",
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 9.sp,
                                color = ElectricBlue,
                                letterSpacing = 2.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚡",
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE",
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 9.sp,
                                color = LaserGreen,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    // Center content
                    if (identityProfile != null) {
                        Column(
                            modifier = Modifier.padding(vertical = 20.dp)
                        ) {
                            // Glitch text for archetype
                            GlitchText(
                                text = identityProfile.dominantArchetype.displayName,
                                fontSize = 42.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Tagline in glass box
                            GlassBox {
                                Text(
                                    text = identityProfile.dominantArchetype.tagline,
                                    fontFamily = AppFonts.spaceMono,
                                    fontSize = 15.sp,
                                    color = TeslaWhite,
                                    lineHeight = 22.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(vertical = 20.dp)
                        ) {
                            Text(
                                text = "DISCOVER",
                                fontFamily = AppFonts.oswald,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = TeslaWhite.copy(alpha = 0.9f),
                                letterSpacing = 8.sp
                            )

                            Text(
                                text = "YOUR",
                                fontFamily = AppFonts.oswald,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = TeslaWhite.copy(alpha = 0.9f),
                                letterSpacing = 8.sp
                            )

                            GlowingBox(
                                brush = Brush.linearGradient(
                                    colors = listOf(LaserGreen, ElectricBlue)
                                )
                            ) {
                                Text(
                                    text = "IDENTITY",
                                    fontFamily = AppFonts.oswald,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DeepBlack,
                                    letterSpacing = 8.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Bottom metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "16°04'N 108°13'E // DA NANG",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 8.sp,
                            color = ElectricBlue.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BreathingDot(color = LaserGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (identityProfile != null) "VERIFIED" else "STANDBY",
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 8.sp,
                                color = if (identityProfile != null) LaserGreen else TechSilver,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// IDENTITY CARD V3.0 - WITH GLASS MORPHISM & VISUALIZATION
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun IdentityCardV3(
    profile: IdentityProfile?,
    navController: NavController,
    onReset: () -> Unit = {}
) {
    GlassBox {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .then(
                    if (profile == null) {
                        Modifier.clickable {
                            navController.navigate(Routes.IDENTITY_SCANNER)
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            if (profile != null) {
                // ═══════════════════════════════════════════════════
                // VERIFIED STATE
                // ═══════════════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    // Status bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BreathingDot(color = LaserGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VERIFIED",
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 10.sp,
                                color = LaserGreen,
                                letterSpacing = 2.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LOCKED",
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 9.sp,
                                color = ElectricBlue.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Archetype name
                    GlitchText(
                        text = profile.dominantArchetype.displayName,
                        fontSize = 52.sp
                    )

                    if (profile.isHybrid && profile.secondaryArchetype != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(2.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                ElectricBlue,
                                                NeonPurple
                                            )
                                        )
                                    )
                            )

                            Text(
                                text = profile.secondaryArchetype.displayName,
                                fontFamily = AppFonts.oswald,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechSilver,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tagline
                    Text(
                        text = profile.dominantArchetype.tagline,
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 14.sp,
                        color = TechSilver.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Archetype visualization
                    ArchetypeVisualization(profile = profile)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Confidence meter
                    Column {
                        Text(
                            text = "CONFIDENCE LEVEL",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp,
                            color = TechSilver.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ConfidenceProgressBar(confidence = profile.confidenceLevel)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${(profile.confidenceLevel * 100).toInt()}%",
                            fontFamily = AppFonts.oswald,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── RESET BUTTON ─────────────────────────────
                    var showConfirm by remember { mutableStateOf(false) }

                    if (!showConfirm) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlitchRed.copy(alpha = 0.35f))
                                .clickable { showConfirm = true }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RESET IDENTITY",
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 11.sp,
                                color = GlitchRed.copy(alpha = 0.45f),
                                letterSpacing = 2.sp
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "CONFIRM RESET? THIS CANNOT BE UNDONE.",
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 9.sp,
                                color = GlitchRed,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, TechSilver.copy(alpha = 0.4f))
                                        .clickable { showConfirm = false }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "CANCEL",
                                        fontFamily = AppFonts.spaceMono,
                                        fontSize = 11.sp,
                                        color = TechSilver,
                                        letterSpacing = 2.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(GlitchRed.copy(alpha = 0.15f))
                                        .border(1.dp, GlitchRed)
                                        .clickable { onReset() }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "CONFIRM",
                                        fontFamily = AppFonts.spaceMono,
                                        fontSize = 11.sp,
                                        color = GlitchRed,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }

                } // end Column VERIFIED

            } else {
                // ═══════════════════════════════════════════════════
                // LOCKED STATE
                // ═══════════════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = GlitchRed.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "IDENTITY",
                        fontFamily = AppFonts.oswald,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = GlitchRed.copy(alpha = 0.3f),
                        letterSpacing = 6.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "REQUIRED",
                        fontFamily = AppFonts.oswald,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = GlitchRed.copy(alpha = 0.2f),
                        letterSpacing = 4.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(200.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        GlitchRed.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "System cannot proceed without verification",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp,
                        color = TechSilver.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // CTA Button
                    GlowingBox(
                        brush = Brush.linearGradient(
                            colors = listOf(GlitchRed, Color(0xFFFF6666))
                        )
                    ) {
                        Text(
                            text = "INITIATE SCAN",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TeslaWhite,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ARCHETYPE VISUALIZATION
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ArchetypeVisualization(profile: IdentityProfile) {
    Column {
        Text(
            text = "ARCHETYPE BREAKDOWN",
            fontFamily = AppFonts.spaceMono,
            fontSize = 9.sp,
            color = TechSilver.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        profile.getSortedArchetypes().forEach { (archetype, score) ->
            ArchetypeBar(
                archetype = archetype,
                score = score,
                isMain = archetype == profile.dominantArchetype
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ArchetypeBar(archetype: Archetype, score: Float, isMain: Boolean) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = archetype.name,
                fontFamily = AppFonts.spaceMono,
                fontSize = if (isMain) 11.sp else 10.sp,
                fontWeight = if (isMain) FontWeight.Bold else FontWeight.Normal,
                color = if (isMain) ElectricBlue else TechSilver.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )

            Text(
                text = "${(score * 100).toInt()}%",
                fontFamily = AppFonts.spaceMono,
                fontSize = if (isMain) 11.sp else 10.sp,
                fontWeight = if (isMain) FontWeight.Bold else FontWeight.Normal,
                color = if (isMain) ElectricBlue else TechSilver.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isMain) 8.dp else 6.dp)
                .background(PhantomGrey.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(score)
                    .fillMaxHeight()
                    .background(
                        if (isMain) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    ElectricBlue,
                                    NeonPurple
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    TechSilver.copy(alpha = 0.5f),
                                    TechSilver.copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
            )
        }
    }
}

@Composable
fun ConfidenceProgressBar(confidence: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(PhantomGrey.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(confidence)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            LaserGreen,
                            ElectricBlue,
                            NeonPurple
                        )
                    )
                )
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STYLIST LOCKED STATE V3.0
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun StylistLockedStateV3() {
    GlassBox {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = GlitchRed.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "SYSTEM LOCKED",
                    fontFamily = AppFonts.oswald,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = GlitchRed.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "REQUIRES IDENTITY VERIFICATION",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 11.sp,
                    color = TechSilver.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Complete Box A to proceed",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = TechSilver.copy(alpha = 0.5f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HELPER COMPONENTS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun BreathingDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    Box(
        modifier = Modifier
            .size((8 * scale).dp)
            .background(color, shape = CircleShape)
    )
}

@Composable
fun GlowingBox(
    brush: Brush,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.background(brush)
    ) {
        content()
    }
}

@Composable
fun GlassBox(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = TeslaWhite.copy(alpha = 0.03f),
                shape = RoundedCornerShape(0.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.3f),
                        NeonPurple.copy(alpha = 0.2f),
                        ElectricBlue.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(0.dp)
            ),
        content = content
    )
}

@Composable
fun GlitchText(text: String, fontSize: TextUnit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glitch")

    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitch_offset"
    )

    Box {
        // Red shadow
        Text(
            text = text,
            fontFamily = AppFonts.oswald,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFF0044).copy(alpha = 0.4f),
            modifier = Modifier.offset(x = (-offsetX).dp, y = offsetX.dp)
        )

        // Cyan shadow
        Text(
            text = text,
            fontFamily = AppFonts.oswald,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = Color(0xFF00FFFF).copy(alpha = 0.4f),
            modifier = Modifier.offset(x = offsetX.dp, y = (-offsetX).dp)
        )

        // Main text
        Text(
            text = text,
            fontFamily = AppFonts.oswald,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = TeslaWhite
        )
    }
}

@Composable
fun GeometricShapes() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.15f)
    ) {
        // Triangle
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.3f)
                lineTo(size.width * 0.2f, size.height * 0.1f)
                lineTo(size.width * 0.3f, size.height * 0.3f)
                close()
            },
            color = ElectricBlue
        )

        // Circle
        drawCircle(
            color = NeonPurple,
            radius = 40f,
            center = Offset(size.width * 0.8f, size.height * 0.2f)
        )

        // Rectangle
        drawRect(
            color = LaserGreen,
            topLeft = Offset(size.width * 0.7f, size.height * 0.7f),
            size = Size(80f, 50f)
        )

        // Small circles scattered
        repeat(5) { index ->
            drawCircle(
                color = ElectricBlue,
                radius = 5f,
                center = Offset(
                    size.width * (0.2f + index * 0.15f),
                    size.height * (0.5f + index * 0.05f)
                )
            )
        }
    }
}

@Composable
fun EditorialLabelV3(number: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large number with gradient
            Box {
                Text(
                    text = number,
                    fontFamily = AppFonts.oswald,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricBlue,
                    letterSpacing = 2.sp
                )
            }

            Column {
                Text(
                    text = title,
                    fontFamily = AppFonts.oswald,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TeslaWhite,
                    letterSpacing = 1.sp
                )

                Text(
                    text = subtitle,
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = TechSilver.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun GlowingAccentLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ElectricBlue,
                            NeonPurple,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun FloatingAccent() {
    Box(
        modifier = Modifier
            .padding(top = 120.dp, end = 20.dp)
            .rotate(-90f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                ElectricBlue.copy(alpha = 0.5f)
                            )
                        )
                    )
            )

            Text(
                text = "FW26",
                fontFamily = AppFonts.spaceMono,
                fontSize = 8.sp,
                color = TechSilver.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun SystemSignatureV3() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ElectricBlue.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            BreathingDot(color = LaserGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SYSTEM ONLINE",
                fontFamily = AppFonts.spaceMono,
                fontSize = 8.sp,
                color = LaserGreen.copy(alpha = 0.6f),
                letterSpacing = 3.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "© RINNSAN CREAVITY 2026",
            fontFamily = AppFonts.spaceMono,
            fontSize = 7.sp,
            color = TechSilver.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * V3.0 FEATURES SUMMARY
 * ═══════════════════════════════════════════════════════════════════
 *
 * ✨ NEW VISUAL ELEMENTS:
 * 1. Expanded color palette (ElectricBlue, NeonPurple, LaserGreen)
 * 2. Glass morphism containers
 * 3. Breathing dot animations
 * 4. Glitch text effects
 * 5. Scan line animation
 * 6. Particle background
 * 7. Geometric shapes decoration
 * 8. Archetype visualization bars
 * 9. Confidence progress bar
 * 10. Glowing boxes and accents
 *
 * 🎨 DESIGN IMPROVEMENTS:
 * - Enhanced depth with layers
 * - Better visual hierarchy
 * - More engaging animations
 * - Professional glass effects
 * - Dynamic color gradients
 *
 * 🔒 ARCHITECTURE:
 * - Identity gate maintained
 * - Irreversible lock maintained
 * - Clean separation maintained
 *
 * 📱 PERFORMANCE:
 * - Optimized animations
 * - Efficient Canvas usage
 * - Proper state management
 */