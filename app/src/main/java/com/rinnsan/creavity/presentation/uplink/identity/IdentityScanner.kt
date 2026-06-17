package com.rinnsan.creavity.presentation.uplink.identity

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.engine.IdentityEngine
import com.rinnsan.creavity.domain.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════
 * IDENTITY SCANNER v2.0 - BOX A UI
 * ═══════════════════════════════════════════════════════════════════
 *
 * Upgrades:
 * - QuestionCard answers → LazyVerticalGrid 2-col
 * - AnswerOption → AsyncImage background + dark overlay + CyberAcid glow on select
 * - Selected state persisted until navigation (visual feedback)
 * - Image loading optimized: 600×800 remote crop, Coil memory/disk cache
 */

@Composable
fun IdentityScanner(
    questions: List<Question>,
    engine: IdentityEngine,
    navController: NavController,
    onComplete: (IdentityProfile) -> Unit,
    modifier: Modifier = Modifier
) {

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // STATE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var responses by remember { mutableStateOf<List<QuestionResponse>>(emptyList()) }
    var selectedAnswerId by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var computedProfile by remember { mutableStateOf<IdentityProfile?>(null) }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    val totalQuestions = questions.size
    val scope = rememberCoroutineScope()

    // Reset selection when question changes
    LaunchedEffect(currentQuestionIndex) {
        selectedAnswerId = null
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // MAIN CONTAINER
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {

            ScannerHeader(isLocked = isLocked, isScanning = isScanning)

            Spacer(modifier = Modifier.height(24.dp))

            ScanningLine(isActive = !isLocked)

            Spacer(modifier = Modifier.height(20.dp))

            ProgressCounter(
                current = currentQuestionIndex + 1,
                total = totalQuestions,
                isLocked = isLocked
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ─────────────────────────────────────────────────────
            // QUESTION DISPLAY OR LOCKED STATE
            // ─────────────────────────────────────────────────────
            AnimatedContent(
                targetState = isLocked,
                transitionSpec = {
                    fadeIn(tween(600)) togetherWith fadeOut(tween(300))
                },
                label = "question_state"
            ) { locked ->
                if (locked) {
                    LockedState(profile = computedProfile)
                } else {
                    currentQuestion?.let { question ->
                        QuestionCard(
                            question = question,
                            questionNumber = currentQuestionIndex + 1,
                            selectedAnswerId = selectedAnswerId,
                            onAnswerSelected = { answer ->
                                if (selectedAnswerId != null) return@QuestionCard // block double-tap

                                selectedAnswerId = answer.id

                                val response = QuestionResponse(
                                    questionId = question.id,
                                    answerId = answer.id,
                                    answer = answer,
                                    weight = question.weight
                                )
                                responses = responses + response

                                scope.launch {
                                    delay(420) // brief glow moment before advancing

                                    if (currentQuestionIndex < totalQuestions - 1) {
                                        currentQuestionIndex++
                                    } else {
                                        isScanning = true
                                        delay(1500)
                                        val profile = engine.computeIdentity(responses)
                                        computedProfile = profile
                                        isScanning = false
                                        isLocked = true
                                        onComplete(profile)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// QUESTION CARD — 2-COL GRID LAYOUT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun QuestionCard(
    question: Question,
    questionNumber: Int,
    selectedAnswerId: String?,
    onAnswerSelected: (Answer) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GridLineColor)
            .background(VoidBlack)
            .padding(20.dp)
    ) {
        // System prompt label
        Text(
            text = "SYSTEM PROMPT // ${String.format("%02d", questionNumber)}",
            fontFamily = AppFonts.spaceMono,
            fontSize = 10.sp,
            color = CyberAcid,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Question text
        Text(
            text = question.text,
            fontFamily = AppFonts.oswald,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TeslaWhite,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ─── 2-COL GRID (non-lazy, fixed answer count = 4) ───────
        // Using LazyVerticalGrid inside scrollable Column requires
        // a fixed height. We derive it from item height × rows.
        val itemHeight = 180.dp
        val gridHeight = itemHeight * 2 + 12.dp // 2 rows + gap

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            userScrollEnabled = false // parent handles scroll
        ) {
            items(
                items = question.answers,
                key = { it.id }
            ) { answer ->
                AnswerOptionGrid(
                    answer = answer,
                    isSelected = selectedAnswerId == answer.id,
                    isDisabled = selectedAnswerId != null && selectedAnswerId != answer.id,
                    onClick = { onAnswerSelected(answer) }
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ANSWER OPTION — IMAGE BG + OVERLAY + CYBERACID GLOW
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun AnswerOptionGrid(
    answer: Answer,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Animated border glow on select
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> CyberAcid
            isDisabled -> TechSilver.copy(alpha = 0.15f)
            else       -> TechSilver.copy(alpha = 0.45f)
        },
        animationSpec = tween(300),
        label = "border_color"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 1.dp,
        animationSpec = tween(300),
        label = "border_width"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isDisabled) 0.75f else 0.55f,
        animationSpec = tween(300),
        label = "overlay_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .border(borderWidth, borderColor)
            .clip(RectangleShape)
            .clickable(enabled = !isDisabled) { onClick() }
    ) {

        // ── Background image (Coil) ────────────────────────────
        if (!answer.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(answer.imageUrl)
                    .crossfade(true)
                    .size(600, 800) // match Unsplash crop params
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            // Fallback solid
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(PhantomGrey)
            )
        }

        // ── Dark gradient overlay ──────────────────────────────
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = overlayAlpha * 0.4f),
                            0.45f to Color.Black.copy(alpha = overlayAlpha * 0.6f),
                            1.0f to Color.Black.copy(alpha = overlayAlpha)
                        )
                    )
                )
        )

        // ── CyberAcid tint overlay when selected ──────────────
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(CyberAcid.copy(alpha = 0.12f))
            )
        }

        // ── Text content (bottom-anchored) ────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            // First line = keyword (bold)
            val lines = answer.text.split("\n")
            Text(
                text = lines.getOrElse(0) { answer.text },
                fontFamily = AppFonts.oswald,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) CyberAcid else TeslaWhite,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Second line = fashion descriptor
            if (lines.size > 1) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = lines[1],
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp,
                    color = TeslaWhite.copy(alpha = if (isSelected) 0.95f else 0.75f),
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ── Selected indicator (top-right corner) ─────────────
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(8.dp)
                    .background(CyberAcid)
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HEADER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ScannerHeader(
    isLocked: Boolean,
    isScanning: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "IDENTITY SCAN",
                fontFamily = AppFonts.oswald,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TeslaWhite,
                letterSpacing = 2.sp
            )
            Text(
                text = when {
                    isLocked   -> "LOCKED"
                    isScanning -> "PROCESSING..."
                    else       -> "IN PROGRESS"
                },
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                color = if (isLocked) GlitchRed else CyberAcid,
                letterSpacing = 1.sp
            )
        }

        AnimatedVisibility(
            visible = isLocked,
            enter = scaleIn(tween(400, easing = FastOutSlowInEasing)) + fadeIn(tween(400))
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Identity Locked",
                tint = GlitchRed,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SCANNING LINE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ScanningLine(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(TechSilver.copy(alpha = 0.3f))
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .fillMaxHeight()
                    .offset(x = (offset * 1000).dp)
                    .background(CyberAcid)
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// PROGRESS COUNTER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ProgressCounter(
    current: Int,
    total: Int,
    isLocked: Boolean
) {
    Text(
        text = if (isLocked) "SCAN COMPLETE" else String.format("%02d/%02d", current, total),
        fontFamily = AppFonts.spaceMono,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = if (isLocked) GlitchRed else TeslaWhite,
        letterSpacing = 2.sp
    )
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// LOCKED STATE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun LockedState(profile: IdentityProfile?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VoidBlack)
            .border(2.dp, GlitchRed)
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = GlitchRed,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "IDENTITY LOCKED",
            fontFamily = AppFonts.oswald,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = GlitchRed,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        profile?.let {
            Text(
                text = it.dominantArchetype.displayName,
                fontFamily = AppFonts.oswald,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CyberAcid,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CONFIDENCE: ${(it.confidenceLevel * 100).toInt()}%",
                fontFamily = AppFonts.spaceMono,
                fontSize = 12.sp,
                color = TeslaWhite,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "SCAN DATA ENCRYPTED // SESSION TERMINATED",
            fontFamily = AppFonts.spaceMono,
            fontSize = 10.sp,
            color = TechSilver,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
    }
}