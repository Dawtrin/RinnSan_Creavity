package com.rinnsan.creavity.presentation.stylist

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.models.*
import com.rinnsan.creavity.presentation.uplink.stylist.StylistState
import com.rinnsan.creavity.presentation.uplink.stylist.Suggestion
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * ═══════════════════════════════════════════════════════════════════
 * VIRTUAL STYLIST - A MACHINE WITH TASTE
 * ═══════════════════════════════════════════════════════════════════
 *
 * This is not a chatbot.
 * This is not a friendly assistant.
 * This is an intelligence with presence.
 *
 * Design Philosophy:
 *
 * WHAT IT IS:
 * - A fashion archive terminal
 * - A creative director's private console
 * - A system that observes, not converses
 *
 * WHAT IT IS NOT:
 * - Customer service
 * - Social interaction
 * - Emotional support
 *
 * Tone Reference:
 * - Fashion house internal document
 * - Editorial caption (not ad copy)
 * - Private notes from creative director
 *
 * Language:
 * - Interpretive, not conversational
 * - Acknowledgment without empathy
 * - Intelligence, not friendliness
 */

@Composable
fun VirtualStylistBox(
    profile: IdentityProfile?,
    stylistState: StylistState,
    navController: NavController,
    onNavigateToTerminal: () -> Unit,
    onNavigateToArtifact: ((String) -> Unit)? = null,
    onNavigateToScanner: (() -> Unit)? = null,  // ← FIX: propagate scanner nav
    modifier: Modifier = Modifier
) {

    // Intentional timing delay (authority through silence)
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            delay(400) // 400ms pause = thoughtful response
            isVisible = true
        } catch (e: Exception) {
            isVisible = true // Fallback to visible if error
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(560.dp)
            .background(VoidBlack)
            .border(1.dp, GridLineColor)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // Header cũng luôn hiển thị ONLINE khi có profile
                val headerState = when (stylistState) {
                    is StylistState.ANALYZE,
                    is StylistState.SUGGEST,
                    is StylistState.ARCHIVE -> StylistState.ONLINE
                    else -> stylistState
                }
                TerminalHeader(stylistState = headerState)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GridLineColor)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    // Box chỉ hiển thị trạng thái cơ bản (ONLINE/OFFLINE/ERROR)
                    // ANALYZE, SUGGEST, ARCHIVE — chỉ hiển trong Terminal (StylistChatScreen)
                    // — box luôn ở trạng thái ONLINE khi user đã có profile
                    val displayState = when (stylistState) {
                        is StylistState.ANALYZE,
                        is StylistState.SUGGEST,
                        is StylistState.ARCHIVE -> StylistState.ONLINE
                        else -> stylistState
                    }

                    when (displayState) {
                        is StylistState.OFFLINE -> {
                            OfflineView(onNavigateToScanner = {
                                // FIX: navigate đúng về Identity Scanner
                                onNavigateToScanner?.invoke()
                            })
                        }

                        is StylistState.ONLINE -> {
                            OnlineView(
                                profile = profile,
                                onNavigateToTerminal = onNavigateToTerminal
                            )
                        }

                        is StylistState.ERROR -> {
                            ErrorView()
                        }

                        is StylistState.INVALID -> {
                            InvalidView()
                        }

                        is StylistState.LOADING -> {
                            LoadingView()
                        }

                        // Các state không bao giờ xảy ra ở đây (ANALYZE/SUGGEST/ARCHIVE đã map về ONLINE)
                        else -> {
                            OnlineView(
                                profile = profile,
                                onNavigateToTerminal = onNavigateToTerminal
                            )
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TERMINAL HEADER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun TerminalHeader(stylistState: StylistState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PhantomGrey)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SystemStatusDot(state = stylistState)

            Text(
                text = "VIRTUAL STYLIST",
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TeslaWhite,
                letterSpacing = 1.sp
            )
        }

        Text(
            text = stylistState.displayName,
            fontFamily = AppFonts.spaceMono,
            fontSize = 10.sp,
            color = when (stylistState) {
                is StylistState.OFFLINE -> GlitchRed
                is StylistState.ONLINE -> CyberAcid
                is StylistState.ANALYZE -> CyberAcid
                is StylistState.SUGGEST -> CyberAcid
                is StylistState.ARCHIVE -> TechSilver
                is StylistState.ERROR -> GlitchRed
                is StylistState.INVALID -> GlitchRed
                is StylistState.LOADING -> TechSilver
            },
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SystemStatusDot(state: StylistState) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    val dotColor = when (state) {
        is StylistState.OFFLINE -> GlitchRed
        is StylistState.ONLINE -> CyberAcid
        is StylistState.ANALYZE -> CyberAcid
        is StylistState.SUGGEST -> CyberAcid
        is StylistState.ARCHIVE -> TechSilver
        is StylistState.ERROR -> GlitchRed
        is StylistState.INVALID -> GlitchRed
        is StylistState.LOADING -> TechSilver
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(dotColor.copy(alpha = alpha), shape = androidx.compose.foundation.shape.CircleShape)
    )
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// OFFLINE VIEW - SYSTEM LOCKED
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun OfflineView(onNavigateToScanner: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = GlitchRed,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "SYSTEM LOCKED",
            fontFamily = AppFonts.oswald,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = GlitchRed,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Softened: Editorial observation, not error message
        Text(
            text = "Analysis requires identity data.",
            fontFamily = AppFonts.spaceMono,
            fontSize = 12.sp,
            color = TechSilver,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Complete scan to proceed.",
            fontFamily = AppFonts.spaceMono,
            fontSize = 11.sp,
            color = TechSilver.copy(alpha = 0.7f),
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .background(VoidBlack)
                .border(2.dp, GlitchRed)
                .clickable { onNavigateToScanner() }
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "INITIATE SCAN",
                fontFamily = AppFonts.spaceMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GlitchRed,
                letterSpacing = 1.sp
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ONLINE VIEW - SYSTEM AWARE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun OnlineView(
    profile: IdentityProfile?,
    onNavigateToTerminal: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "SYSTEM STATUS: ONLINE",
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                color = CyberAcid,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            profile?.let { prof ->
                // Archetype display
                Text(
                    text = "ARCHETYPE LOADED",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = TechSilver.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = try {
                        prof.getArchetypeLabel()
                    } catch (e: Exception) {
                        prof.dominantArchetype.toString()
                    },
                    fontFamily = AppFonts.oswald,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TeslaWhite,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ✅ Softened: Interpretive language (editorial style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PhantomGrey.copy(alpha = 0.2f))
                        .border(1.dp, GridLineColor.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "INTERPRETATION",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp,
                            color = CyberAcid.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Editorial insight based on archetype
                        Text(
                            text = try {
                                getArchetypeInterpretation(prof.dominantArchetype)
                            } catch (e: Exception) {
                                "Identity profile analyzed."
                            },
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp,
                            color = TeslaWhite.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = try {
                        "CONFIDENCE: ${prof.getConfidencePercentage()}%"
                    } catch (e: Exception) {
                        "CONFIDENCE: ${(prof.confidenceLevel * 100).toInt()}%"
                    },
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = TechSilver,
                    letterSpacing = 1.sp
                )
            }
        }

        Waveform(isActive = true)

        Column {
            // ✅ Softened: "Awaiting directive" instead of "Awaiting input"
            Text(
                text = "Recommendations available.",
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                color = TechSilver.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Awaiting directive.",
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                color = TechSilver.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PhantomGrey.copy(alpha = 0.3f))
                    .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                    .clickable { onNavigateToTerminal() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = ">",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 16.sp,
                        color = CyberAcid,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "OPEN TERMINAL",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 12.sp,
                        color = TeslaWhite,
                        letterSpacing = 1.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CyberAcid,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Editorial interpretation of each archetype.
 * This is NOT personality. This is fashion analysis.
 */
fun getArchetypeInterpretation(archetype: Archetype): String {
    return when (archetype) {
        Archetype.GHOST -> "Visibility is optional.\nPrecision is not.\nEvery piece must earn its place."
        Archetype.OPERATOR -> "Function dictates form.\nUtility shapes aesthetic.\nPurpose over decoration."
        Archetype.GLITCH -> "Chaos is controlled.\nAsymmetry is intentional.\nRules exist to be rewritten."
        Archetype.NOMAD -> "History informs present.\nRemixing over acquiring.\nContext creates meaning."
    }
}

@Composable
fun Waveform(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(30) { index ->
            val amplitude = if (isActive) {
                sin((phase + index * 12) * Math.PI / 180.0).toFloat()
            } else {
                0f
            }

            val height = (20 + amplitude * 15).dp

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height)
                    .background(CyberAcid.copy(alpha = 0.6f))
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ANALYZE VIEW - PROCESSING WITH PRESENCE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun AnalyzeView(state: StylistState.ANALYZE) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // ✅ Softened: Acknowledgment without empathy
            Text(
                text = "INPUT REGISTERED",
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                color = CyberAcid,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "> ${state.userMessage}",
                fontFamily = AppFonts.spaceMono,
                fontSize = 14.sp,
                color = TeslaWhite,
                lineHeight = 20.sp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Waveform(isActive = true)

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Softened: "Analyzing context" instead of just "Processing"
            Text(
                text = "Analyzing context.",
                fontFamily = AppFonts.spaceMono,
                fontSize = 12.sp,
                color = CyberAcid.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pattern recognition in progress.",
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                color = TechSilver.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SUGGEST VIEW - INTELLIGENT OUTPUT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun SuggestView(
    state: StylistState.SUGGEST,
    profile: IdentityProfile?,
    onNavigateToDetail: ((String) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // User query
        Text(
            text = "> ${state.userMessage}",
            fontFamily = AppFonts.spaceMono,
            fontSize = 12.sp,
            color = TechSilver,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // System response header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(CyberAcid, shape = androidx.compose.foundation.shape.CircleShape)
            )

            Text(
                text = "ANALYSIS",
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                color = CyberAcid,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Response content (Gemini AI or Local fallback)
        Text(
            text = state.systemResponse,
            fontFamily = AppFonts.spaceMono,
            fontSize = 13.sp,
            color = TeslaWhite,
            lineHeight = 21.sp
        )

        // Product Suggestions from Firestore
        if (state.suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GridLineColor.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "EXTRACTED ARTIFACTS",
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                color = CyberAcid.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${state.suggestions.size} items matched from vault.",
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                color = TechSilver.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            state.suggestions.forEach { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    onNavigateToDetail = onNavigateToDetail
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: Suggestion,
    onNavigateToDetail: ((String) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PhantomGrey.copy(alpha = 0.2f))
            .border(1.dp, GridLineColor.copy(alpha = 0.5f))
            .clickable {
                if (suggestion.artifactId.isNotEmpty()) {
                    onNavigateToDetail?.invoke(suggestion.artifactId)
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Product Image
        if (suggestion.imageUrl.isNotEmpty()) {
            AsyncImage(
                model = suggestion.imageUrl,
                contentDescription = suggestion.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PhantomGrey),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(PhantomGrey, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "→",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 18.sp,
                    color = CyberAcid,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Product Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.title,
                fontFamily = AppFonts.oswald,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TeslaWhite,
                letterSpacing = 0.5.sp,
                maxLines = 2
            )

            if (suggestion.price.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suggestion.price,
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 12.sp,
                    color = CyberAcid,
                    fontWeight = FontWeight.Bold
                )
            }

            if (suggestion.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = suggestion.description,
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = TechSilver.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "VIEW ARTIFACT →",
                fontFamily = AppFonts.spaceMono,
                fontSize = 9.sp,
                color = TechSilver.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ARCHIVE VIEW - SESSION COMPLETE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ArchiveView(state: StylistState.ARCHIVE) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = CyberAcid,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SESSION ARCHIVED",
            fontFamily = AppFonts.oswald,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = TeslaWhite,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Softened: Editorial closure
        Text(
            text = "Analysis preserved.",
            fontFamily = AppFonts.spaceMono,
            fontSize = 12.sp,
            color = TechSilver,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${state.messageCount} exchanges saved.",
            fontFamily = AppFonts.spaceMono,
            fontSize = 11.sp,
            color = TechSilver.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = state.getArchiveTimeAgo().uppercase(),
            fontFamily = AppFonts.spaceMono,
            fontSize = 10.sp,
            color = TechSilver.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * DESIGN NOTES: INTELLIGENCE WITHOUT FRIENDLINESS
 * ═══════════════════════════════════════════════════════════════════
 *
 * WHAT CHANGED:
 *
 * 1. Language Quality
 *    ❌ Before: "AWAITING INPUT"
 *    ✅ After: "Recommendations available. Awaiting directive."
 *
 *    Why: Editorial tone. Not friendly, just precise.
 *
 * 2. Interpretive Statements
 *    ❌ Before: Raw data only
 *    ✅ After: "Visibility is optional. Precision is not."
 *
 *    Why: Fashion intelligence. System understands archetypes.
 *
 * 3. Acknowledgment Without Empathy
 *    ❌ Before: Silent processing
 *    ✅ After: "INPUT REGISTERED. Pattern recognition in progress."
 *
 *    Why: System confirms it's listening. Not emotional.
 *
 * 4. Intentional Timing
 *    400ms delay before display = thoughtful response
 *    Not instant = system is considering
 *
 * WHAT DIDN'T CHANGE:
 * - No avatars
 * - No chat bubbles
 * - No emojis
 * - No questions
 * - No friendliness
 * - System remains a TOOL
 *
 * USER FEELING:
 * "This system doesn't talk to me. It observes me."
 *
 * TONE REFERENCES:
 * - Vogue editorial caption
 * - Creative director's notes
 * - Fashion house internal memo
 * - NOT customer service
 * - NOT social media
 */
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ERROR VIEW - SYSTEM FAILURE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ErrorView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = GlitchRed,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SYSTEM ERROR",
            fontFamily = AppFonts.oswald,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = GlitchRed,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Unable to process request.",
            fontFamily = AppFonts.spaceMono,
            fontSize = 12.sp,
            color = TechSilver,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please try again.",
            fontFamily = AppFonts.spaceMono,
            fontSize = 11.sp,
            color = TechSilver.copy(alpha = 0.7f)
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// INVALID VIEW - INVALID STATE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun InvalidView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = GlitchRed,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "INVALID STATE",
            fontFamily = AppFonts.oswald,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = GlitchRed,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Identity verification failed.",
            fontFamily = AppFonts.spaceMono,
            fontSize = 12.sp,
            color = TechSilver,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Confidence level insufficient.",
            fontFamily = AppFonts.spaceMono,
            fontSize = 11.sp,
            color = TechSilver.copy(alpha = 0.7f)
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// LOADING VIEW - PROCESSING
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun LoadingView() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_pulse"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = CyberAcid.copy(alpha = alpha),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "PROCESSING",
            fontFamily = AppFonts.oswald,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = TeslaWhite.copy(alpha = alpha),
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "System analyzing patterns...",
            fontFamily = AppFonts.spaceMono,
            fontSize = 12.sp,
            color = TechSilver.copy(alpha = 0.7f),
            lineHeight = 18.sp
        )
    }
}