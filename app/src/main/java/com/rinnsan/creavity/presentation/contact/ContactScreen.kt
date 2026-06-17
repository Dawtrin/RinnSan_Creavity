package com.rinnsan.creavity.presentation.contact

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rinnsan.creavity.core.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    navController: NavController,
    viewModel: ContactViewModel = hiltViewModel()
) {
    // Chỉ cần state cho message, name/email lấy từ ViewModel (FirebaseAuth)
    var messageText by remember { mutableStateOf("") }

    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val myTickets by viewModel.myTickets.collectAsStateWithLifecycle()
    val isLoading = submitState is ContactSubmitState.Loading

    // Lấy thông tin user đã đăng nhập để hiển thị
    val displayName  = viewModel.currentUserName
    val displayEmail = viewModel.currentUserEmail
    val isVerified   = viewModel.isCurrentUserVerified

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Xử lý kết quả submit
    LaunchedEffect(submitState) {
        when (val s = submitState) {
            is ContactSubmitState.Success -> {
                snackbarHostState.showSnackbar(
                    message     = "TRANSMISSION SENT SUCCESSFULLY",
                    actionLabel = "DISMISS",
                    duration    = SnackbarDuration.Short
                )
                messageText = ""
                viewModel.resetState()
            }
            is ContactSubmitState.Error -> {
                snackbarHostState.showSnackbar(
                    message     = "// ERROR: ${s.message}",
                    actionLabel = "DISMISS",
                    duration    = SnackbarDuration.Short
                )
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = VoidBlack,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData     = data,
                    containerColor   = CyberAcid,
                    contentColor     = Color.Black,
                    actionColor      = Color.Black,
                    shape            = RoundedCornerShape(0.dp)
                )
            }
        },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text         = "ENCRYPTED_CHANNEL",
                            fontFamily   = AppFonts.spaceMono,
                            fontSize     = 12.sp,
                            color        = CyberAcid,
                            letterSpacing = 3.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector      = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint             = Color.White,
                                    modifier         = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor    = Color.Black,
                        titleContentColor = CyberAcid
                    )
                )
                // Bottom border line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 1. HEADER SECTION
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector    = Icons.Default.Hub,
                        contentDescription = null,
                        tint           = CyberAcid,
                        modifier       = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text        = "INITIATE_CONTACT",
                        fontFamily  = AppFonts.oswald,
                        color       = Color.White,
                        fontSize    = 32.sp,
                        fontWeight  = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text       = "Establish a direct secure line to RinnSan HQ.\nEncrypted Protocol: SSL-256 // Response time: < 24h.",
                    fontFamily = AppFonts.spaceMono,
                    color      = Color.White.copy(alpha = 0.54f),
                    fontSize   = 12.sp,
                    lineHeight = 18.sp
                )
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 2. TRANSMISSION FORM (với fade-in animation)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            var isFormVisible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(200)
                isFormVisible = true
            }

            val formAlpha by animateFloatAsState(
                targetValue  = if (isFormVisible) 1f else 0f,
                animationSpec = tween(600),
                label        = "form_fade"
            )

            val formOffset by animateFloatAsState(
                targetValue  = if (isFormVisible) 0f else 0.1f,
                animationSpec = tween(600),
                label        = "form_slide"
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .alpha(formAlpha)
                    .graphicsLayer {
                        translationY = formOffset * 50f
                    }
            ) {
                // ── Sender info (read-only, lấy từ tài khoản đăng nhập) ──
                Text(
                    text       = "SENDER // IDENTITY",
                    fontFamily = AppFonts.spaceMono,
                    color      = CyberAcid,
                    fontSize   = 10.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector    = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint           = CyberAcid,
                            modifier       = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = displayName,
                                fontFamily = AppFonts.spaceMono,
                                color      = Color.White,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text       = displayEmail,
                                fontFamily = AppFonts.spaceMono,
                                color      = Color.White.copy(alpha = 0.5f),
                                fontSize   = 11.sp
                            )
                        }
                        // Badge — real verification status
                        val badgeColor = if (isVerified) CyberAcid else Color(0xFFFF8C00)
                        Box(
                            modifier = Modifier
                                .border(1.dp, badgeColor.copy(alpha = 0.5f))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text       = if (isVerified) "VERIFIED" else "UNVERIFIED",
                                fontFamily = AppFonts.spaceMono,
                                color      = badgeColor,
                                fontSize   = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Message input ──
                TechInput(
                    label        = "DATA_PACKET // MESSAGE",
                    icon         = Icons.Default.Notes,
                    value        = messageText,
                    onValueChange = { messageText = it },
                    maxLines     = 5,
                    minHeight    = 120.dp
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ── Send Button ──
                Button(
                    onClick  = { viewModel.submitTicket(message = messageText) },
                    enabled  = !isLoading && messageText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = CyberAcid,
                        contentColor   = Color.Black
                    ),
                    shape     = RoundedCornerShape(0.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            color       = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text          = "TRANSMITTING...",
                            fontFamily    = AppFonts.oswald,
                            fontSize      = 18.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    } else {
                        Icon(
                            imageVector    = Icons.Default.Send,
                            contentDescription = null,
                            modifier       = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text          = "TRANSMIT_DATA",
                            fontFamily    = AppFonts.oswald,
                            fontSize      = 18.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 3. TRANSMISSION HISTORY
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Spacer(modifier = Modifier.height(30.dp))
            TransmissionHistorySection(myTickets)

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 4. SATELLITE MAP SECTION
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Spacer(modifier = Modifier.height(50.dp))

            SatelliteMapSection(
                onCopyCoordinates = {
                    clipboardManager.setText(AnnotatedString("15.9784° N, 108.2621° E"))
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message  = "COORDINATES COPIED TO CLIPBOARD",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // 5. DIRECT UPLINKS SECTION
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            DirectUplinksSection()

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COMPONENT: TECH INPUT FIELD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun TechInput(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    maxLines: Int = 1,
    minHeight: Dp = 56.dp
) {
    Column {
        Text(
            text       = label,
            fontFamily = AppFonts.spaceMono,
            color      = CyberAcid,
            fontSize   = 10.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = if (maxLines == 1) Alignment.CenterVertically else Alignment.Top
            ) {
                Icon(
                    imageVector    = icon,
                    contentDescription = null,
                    tint           = Color.White.copy(alpha = 0.54f),
                    modifier       = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                BasicTextField(
                    value         = value,
                    onValueChange = onValueChange,
                    textStyle     = androidx.compose.ui.text.TextStyle(
                        fontFamily = AppFonts.spaceMono,
                        color      = Color.White,
                        fontSize   = 14.sp
                    ),
                    maxLines      = maxLines,
                    modifier      = Modifier.weight(1f),
                    cursorBrush   = androidx.compose.ui.graphics.SolidColor(CyberAcid),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text       = "Waiting for input...",
                                fontFamily = AppFonts.spaceMono,
                                color      = Color.White.copy(alpha = 0.15f),
                                fontSize   = 12.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COMPONENT: SATELLITE MAP WITH COORDINATES
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun SatelliteMapSection(onCopyCoordinates: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "gps_blink")
    val gpsAlpha by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gps_alpha"
    )

    var isCoordVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isCoordVisible = true }

    val lat1Offset by animateFloatAsState(
        targetValue   = if (isCoordVisible) 0f else -0.2f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label         = "lat_slide"
    )
    val lat2Offset by animateFloatAsState(
        targetValue   = if (isCoordVisible) 0f else 0.2f,
        animationSpec = tween(800, 100, easing = FastOutSlowInEasing),
        label         = "lng_slide"
    )

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = "HQ_COORDINATES // LOCKED",
                fontFamily = AppFonts.spaceMono,
                color      = CyberAcid,
                fontSize   = 10.sp
            )
            Icon(
                imageVector    = Icons.Default.GpsFixed,
                contentDescription = null,
                tint           = CyberAcid,
                modifier       = Modifier.size(16.dp).alpha(gpsAlpha)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .border(1.dp, CyberAcid)
                .drawBehind {
                    drawRect(color = CyberAcid.copy(alpha = 0.1f), size = size)
                }
                .padding(30.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CornerDecoration()
                    CornerDecoration()
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text          = "15.9784° N",
                    fontFamily    = AppFonts.oswald,
                    color         = Color.White,
                    fontSize      = 42.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier      = Modifier.graphicsLayer { translationX = lat1Offset * size.width }
                )
                Text(
                    text          = "108.2621° E",
                    fontFamily    = AppFonts.oswald,
                    color         = Color.White,
                    fontSize      = 42.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier      = Modifier.graphicsLayer { translationX = lat2Offset * size.width }
                )

                Spacer(modifier = Modifier.height(10.dp))
                Divider(
                    color    = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text       = "FPT COMPLEX // NGU HANH SON // DA NANG // VN",
                    fontFamily = AppFonts.spaceMono,
                    color      = CyberAcid,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(30.dp))

                OutlinedButton(
                    onClick          = onCopyCoordinates,
                    modifier         = Modifier.fillMaxWidth(),
                    colors           = ButtonDefaults.outlinedButtonColors(contentColor = CyberAcid),
                    border           = BorderStroke(1.dp, CyberAcid),
                    shape            = RoundedCornerShape(0.dp),
                    contentPadding   = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "COPY_DATA",
                        fontFamily = AppFonts.spaceMono,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CornerDecoration()
                    CornerDecoration()
                }
            }
        }
    }
}

@Composable
fun CornerDecoration() {
    Box(
        modifier = Modifier
            .size(10.dp)
            .border(1.dp, Color.White.copy(alpha = 0.15f))
    )
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COMPONENT: DIRECT UPLINKS (Contact Info Grid)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun DirectUplinksSection() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text       = "DIRECT_UPLINKS",
            fontFamily = AppFonts.spaceMono,
            color      = CyberAcid,
            fontSize   = 10.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                icon     = Icons.Default.Call,
                title    = "HOTLINE",
                content  = "0396 704 484",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(15.dp))
            InfoCard(
                icon     = Icons.Default.Mail,
                title    = "SUPPORT",
                content  = "dattrandn@gmail.com",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.02f))
                .border(1.dp, Color.White.copy(alpha = 0.12f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = "OPERATIONAL HOURS (GMT+7)",
                    fontFamily = AppFonts.spaceMono,
                    color      = Color.White.copy(alpha = 0.54f),
                    fontSize   = 10.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text          = "09:00 - 21:00",
                    fontFamily    = AppFonts.oswald,
                    color         = Color.White,
                    fontSize      = 24.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.12f))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector    = icon,
                contentDescription = null,
                tint           = Color.White,
                modifier       = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text       = title,
                fontFamily = AppFonts.spaceMono,
                color      = Color.White.copy(alpha = 0.38f),
                fontSize   = 10.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text      = content,
                fontFamily = AppFonts.spaceMono,
                color      = CyberAcid,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COMPONENT: TRANSMISSION HISTORY
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun TransmissionHistorySection(myTickets: List<UserTicket>) {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = CyberAcid,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "TRANSMISSION HISTORY",
                fontFamily = AppFonts.spaceMono,
                color = CyberAcid,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        if (myTickets.isEmpty()) {
            Text(
                text = "No prior transmissions recorded.",
                fontFamily = AppFonts.spaceMono,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                myTickets.forEach { ticket ->
                    UserTicketCard(ticket)
                }
            }
        }
    }
}

@Composable
fun UserTicketCard(ticket: UserTicket) {
    val isResolved = ticket.status.equals("resolved", ignoreCase = true)
    val statusColor = if (isResolved) Color(0xFF00FF88) else Color(0xFFFF8C00)
    
    val timeStr = remember(ticket.timestamp) {
        java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(ticket.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeStr,
                    fontFamily = AppFonts.spaceMono,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
                Box(
                    modifier = Modifier.border(1.dp, statusColor.copy(alpha = 0.5f)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ticket.status.uppercase(),
                        fontFamily = AppFonts.spaceMono,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // User Message
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "YOU WROTE:",
                    fontFamily = AppFonts.spaceMono,
                    color = TechSilver,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = ticket.message,
                    fontFamily = AppFonts.spaceMono,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                
                // Admin Reply
                if (ticket.adminReply != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF00FF88).copy(alpha = 0.05f))
                            .border(1.dp, Color(0xFF00FF88).copy(alpha = 0.3f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AdminPanelSettings, null, tint = Color(0xFF00FF88), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RINNSAN HQ REPLY:",
                                    fontFamily = AppFonts.spaceMono,
                                    color = Color(0xFF00FF88),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ticket.adminReply,
                                fontFamily = AppFonts.spaceMono,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}