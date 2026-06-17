package com.rinnsan.creavity.presentation.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.model.ChatMessage
import com.rinnsan.creavity.domain.models.IdentityProfile
import com.rinnsan.creavity.presentation.uplink.UplinkViewModel
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════
 * STYLIST CHAT SCREEN - TERMINAL INTERFACE
 * ═══════════════════════════════════════════════════════════════════
 *
 * FIXES:
 * ✅ Vietnamese input support (IME compatible)
 * ✅ Suggestions remain visible after selection
 * ✅ Auto-scroll to latest message
 */

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.presentation.uplink.stylist.StylistState
import com.rinnsan.creavity.presentation.uplink.stylist.Suggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylistChatScreen(
    profile: IdentityProfile?,
    navController: NavController,
    viewModel: UplinkViewModel = hiltViewModel()
) {
    val messages by viewModel.chatMessages.collectAsState()
    val stylistState by viewModel.stylistState.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Cache: giữ lại suggestions cuối cùng ngay cả khi state chuyển sang ANALYZE
    var lastKnownSuggestions by remember { mutableStateOf<List<com.rinnsan.creavity.presentation.uplink.stylist.Suggestion>>(emptyList()) }
    LaunchedEffect(stylistState) {
        val s = stylistState
        if (s is StylistState.SUGGEST && s.suggestions.isNotEmpty()) {
            lastKnownSuggestions = s.suggestions
        }
    }

    // Input state - Sử dụng mutableStateOf thay vì remember để IME hoạt động đúng
    var inputText by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(true) }

    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto scroll to bottom when new message arrives or typing indicator appears
    LaunchedEffect(messages.size, isProcessing) {
        if (messages.isNotEmpty()) {
            // Delay để đảm bảo item đã được render
            kotlinx.coroutines.delay(100)
            val targetIndex = if (isProcessing) messages.size else messages.size - 1
            listState.animateScrollToItem(targetIndex)
        }
    }

    // Suggestions
    val suggestions = remember {
        listOf(
            "Gợi ý outfit cho buổi tối",
            "Phân tích phong cách của tôi",
            "Màu sắc phù hợp với tôi",
            "Xu hướng thời trang 2026"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ═══════════════════════════════════════════════════════════
            // HEADER
            // ═══════════════════════════════════════════════════════════
            ChatHeader(
                profile = profile,
                onBackClick = { navController.popBackStack() },
                messageCount = messages.size
            )

            // ═══════════════════════════════════════════════════════════
            // MESSAGES LIST
            // ═══════════════════════════════════════════════════════════
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TechSilver.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "NO MESSAGES YET",
                            fontFamily = AppFonts.oswald,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TeslaWhite.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Start a conversation with the AI Stylist",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 11.sp,
                            color = TechSilver.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        reverseLayout = false
                    ) {
                        items(
                            items = messages,
                            key = { it.id }
                        ) { message ->
                            ChatMessageItem(message = message)
                        }

                        if (isProcessing) {
                            item {
                                TypingIndicatorBubble()
                            }
                        }

                        // ═══════════════════════════════════════════════════════
                        // PRODUCT CARDS — Hiển thị sản phẩm gợi ý từ Firestore
                        // Sử dụng lastKnownSuggestions để không mất card khi đang analyze
                        val displaySuggestions = when {
                            stylistState is StylistState.SUGGEST -> (stylistState as StylistState.SUGGEST).suggestions
                            stylistState is StylistState.ANALYZE && lastKnownSuggestions.isNotEmpty() -> lastKnownSuggestions
                            else -> lastKnownSuggestions
                        }
                        if (displaySuggestions.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "EXTRACTED ARTIFACTS",
                                    fontFamily = AppFonts.spaceMono,
                                    fontSize = 10.sp,
                                    color = CyberAcid.copy(alpha = 0.8f),
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${displaySuggestions.size} items matched from vault.",
                                    fontFamily = AppFonts.spaceMono,
                                    fontSize = 9.sp,
                                    color = TechSilver.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    displaySuggestions.forEach { sug ->
                                        ChatProductCard(
                                            suggestion = sug,
                                            onClick = {
                                                if (sug.artifactId.isNotEmpty()) {
                                                    navController.navigate("${Routes.ARTIFACT_DETAIL}/${sug.artifactId}")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // ═══════════════════════════════════════════════════════
                        // SUGGESTIONS (hiển thị khi chưa có tin nhắn hoặc showSuggestions = true)
                        // ═══════════════════════════════════════════════════════
                        if (messages.size <= 1 && showSuggestions) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "QUICK SUGGESTIONS",
                                    fontFamily = AppFonts.spaceMono,
                                    fontSize = 10.sp,
                                    color = TechSilver.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    suggestions.forEach { suggestion ->
                                        SuggestionChip(
                                            text = suggestion,
                                            onClick = {
                                                inputText = suggestion
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════
            // INPUT AREA - FIXED FOR VIETNAMESE
            // ═══════════════════════════════════════════════════════════
            ChatInputArea(
                inputText = inputText,
                onInputChange = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                        showSuggestions = false
                        keyboardController?.hide()
                    }
                }
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// CHAT HEADER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ChatHeader(
    profile: IdentityProfile?,
    onBackClick: () -> Unit,
    messageCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TeslaWhite
                )
            }

            Column {
                Text(
                    text = "VIRTUAL STYLIST",
                    fontFamily = AppFonts.oswald,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TeslaWhite,
                    letterSpacing = 1.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (profile != null) {
                        Text(
                            text = "ARCHETYPE: ${profile.dominantArchetype.displayName}",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp,
                            color = CyberAcid,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Debug: Message count
                    if (messageCount > 0) {
                        Text(
                            text = "• $messageCount msgs",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 8.sp,
                            color = TechSilver.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Status indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusDot()
            Text(
                text = "ONLINE",
                fontFamily = AppFonts.spaceMono,
                fontSize = 9.sp,
                color = CyberAcid,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun StatusDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(CyberAcid.copy(alpha = alpha), CircleShape)
    )
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// CHAT MESSAGE ITEM
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .background(
                    color = if (message.isFromUser) {
                        Color(0xFF1E1E1E)
                    } else {
                        Color(0xFF2A2A2A)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (message.isFromUser) {
                        CyberAcid.copy(alpha = 0.3f)
                    } else {
                        TechSilver.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                // Message text
                Text(
                    text = message.text,
                    fontFamily = if (message.isFromUser) AppFonts.spaceMono else AppFonts.spaceMono,
                    fontSize = 13.sp,
                    color = if (message.isFromUser) TeslaWhite else TeslaWhite.copy(alpha = 0.95f),
                    lineHeight = 18.sp
                )

                // Timestamp
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.getFormattedTime(),
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 8.sp,
                    color = TechSilver.copy(alpha = 0.5f),
                    textAlign = if (message.isFromUser) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Sender label
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (message.isFromUser) "YOU" else "AI STYLIST",
            fontFamily = AppFonts.spaceMono,
            fontSize = 8.sp,
            color = TechSilver.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SUGGESTION CHIP
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = CyberAcid.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = CyberAcid,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = text,
                fontFamily = AppFonts.spaceMono,
                fontSize = 12.sp,
                color = TeslaWhite.copy(alpha = 0.9f)
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// CHAT INPUT AREA - FIXED FOR VIETNAMESE INPUT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ChatInputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ═══════════════════════════════════════════════════════════════
        // INPUT FIELD - FIX: Sử dụng BasicTextField với IME support
        // ═══════════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = Color(0xFF0A0A0A),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = TechSilver.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                textStyle = TextStyle(
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 14.sp,
                    color = TeslaWhite
                ),
                cursorBrush = SolidColor(CyberAcid),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onSendClick() }
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Type your message...",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 14.sp,
                            color = TechSilver.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════
        // SEND BUTTON
        // ═══════════════════════════════════════════════════════════════
        IconButton(
            onClick = onSendClick,
            enabled = inputText.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (inputText.isNotBlank()) CyberAcid else TechSilver.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = if (inputText.isNotBlank()) Color.Black else TechSilver.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * FIXES APPLIED:
 * ═══════════════════════════════════════════════════════════════════
 *
 * 1. VIETNAMESE INPUT FIX:
 *    - Sử dụng BasicTextField thay vì TextField/OutlinedTextField
 *    - Thêm KeyboardOptions với autoCorrect = true
 *    - Thêm KeyboardCapitalization.Sentences
 *    - Thêm IME support đầy đủ
 *
 * 2. SUGGESTIONS FIX:
 *    - Không ẩn suggestions khi click (bỏ showSuggestions = false)
 *    - Chỉ fill text vào input field
 *    - Suggestions vẫn visible cho lần nhập tiếp theo
 *
 * 3. UX IMPROVEMENTS:
 *    - Auto-scroll to latest message
 *    - Keyboard actions (ImeAction.Send)
 *    - Proper placeholder text
 *    - Disabled send button when empty
 */

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// CHAT PRODUCT CARD — Phân biệt Direct Sale vs Affiliate
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun ChatProductCard(
    suggestion: Suggestion,
    onClick: () -> Unit
) {
    // Màu sắc và label theo loại sản phẩm
    val (borderColor, badgeColor, badgeText, actionText) = when {
        suggestion.isDirectSale && suggestion.inStock ->
            Quadruple(CyberAcid, CyberAcid, "● DIRECT SALE", "MUA NGAY →")
        suggestion.isDirectSale && !suggestion.inStock ->
            Quadruple(TechSilver.copy(alpha = 0.4f), TechSilver.copy(alpha = 0.5f), "○ HẾT HÀNG", "XEM CHI TIẾT →")
        suggestion.affiliateUrl.isNotEmpty() ->
            Quadruple(Color(0xFFFFC107), Color(0xFFFFC107), "⬡ AFFILIATE", "XEM LINK →")
        else ->
            Quadruple(TechSilver.copy(alpha = 0.3f), TechSilver.copy(alpha = 0.5f), "○ SẢN PHẨM", "XEM →")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
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
                    .background(Color(0xFF2A2A2A)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (suggestion.isDirectSale) "🛒" else "🔗",
                    fontSize = 20.sp
                )
            }
        }

        // Product Info
        Column(modifier = Modifier.weight(1f)) {
            // Badge loại sản phẩm
            Text(
                text = badgeText,
                fontFamily = AppFonts.spaceMono,
                fontSize = 8.sp,
                color = badgeColor,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

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

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = actionText,
                fontFamily = AppFonts.spaceMono,
                fontSize = 9.sp,
                color = badgeColor.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
        }
    }
}

// Helper data class cho destructuring
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TYPING INDICATOR
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun TypingIndicatorBubble() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFF2A2A2A),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = TechSilver.copy(alpha = 0.2f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "typing")
                val dot1Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(400, delayMillis = 0),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ), label = "dot1"
                )
                val dot2Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(400, delayMillis = 200),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ), label = "dot2"
                )
                val dot3Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f, targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(400, delayMillis = 400),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ), label = "dot3"
                )

                Box(modifier = Modifier.size(6.dp).background(CyberAcid.copy(alpha = dot1Alpha), androidx.compose.foundation.shape.CircleShape))
                Box(modifier = Modifier.size(6.dp).background(CyberAcid.copy(alpha = dot2Alpha), androidx.compose.foundation.shape.CircleShape))
                Box(modifier = Modifier.size(6.dp).background(CyberAcid.copy(alpha = dot3Alpha), androidx.compose.foundation.shape.CircleShape))
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "AI STYLIST IS TYPING...",
            fontFamily = AppFonts.spaceMono,
            fontSize = 8.sp,
            color = TechSilver.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )
    }
}