package com.rinnsan.creavity.presentation.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.presentation.auth.AuthViewModel

private val DimBorder    = Color(0xFF2A2A2A)
private val ScanlineGray = Color(0xFF111111)

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val email         by profileViewModel.email.collectAsState()
    val uid           by profileViewModel.agentId.collectAsState()
    val isVerified    by profileViewModel.isVerified.collectAsState()
    val createdAt     by profileViewModel.enrolledDate.collectAsState()
    val role          by profileViewModel.role.collectAsState()
    val username      by profileViewModel.username.collectAsState()
    val archetype     by profileViewModel.archetype.collectAsState()
    val postCount     by profileViewModel.postCount.collectAsState()
    val wishlistCount by profileViewModel.wishlistCount.collectAsState()
    val isSaving      by profileViewModel.isSavingUsername.collectAsState()
    val saveSuccess   by profileViewModel.saveSuccess.collectAsState()

    // Local edit state
    var isEditingUsername by remember { mutableStateOf(false) }
    var usernameInput     by remember(username) { mutableStateOf(username) }

    // Thông báo lưu thành công
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            isEditingUsername = false
            profileViewModel.resetSaveSuccess()
        }
    }

    val scrollState = rememberScrollState()

    // Blink animation
    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue  = 1f,
        targetValue   = 0.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── Top bar ────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick  = { navController.popBackStack() },
                    modifier = Modifier.size(36.dp).border(1.dp, DimBorder)
                ) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint               = TeslaWhite,
                        modifier           = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(if (isVerified) CyberAcid else GlitchRed)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = if (isVerified) "// VERIFIED" else "// UNVERIFIED",
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = VoidBlack,
                        modifier   = Modifier.alpha(blinkAlpha)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── AVATAR + USERNAME (phần mới) ───────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Avatar hình tròn — chữ cái đầu của username
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(CyberAcid.copy(alpha = 0.8f), CyberAcid.copy(alpha = 0.2f))
                            )
                        )
                        .border(2.dp, CyberAcid, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = username.take(2).ifBlank { "??" },
                        fontFamily = AppFonts.oswald,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Black,
                        color      = VoidBlack
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Archetype badge
                    if (archetype.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(CyberAcid.copy(alpha = 0.15f))
                                .border(1.dp, CyberAcid.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text       = "// $archetype",
                                fontFamily = AppFonts.spaceMono,
                                fontSize   = 9.sp,
                                color      = CyberAcid,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text          = "IDENTITY PROFILE",
                        fontFamily    = AppFonts.oswald,
                        fontSize      = 28.sp,
                        fontWeight    = FontWeight.Black,
                        color         = TeslaWhite,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text          = "AGENT DATA // CLASSIFIED ACCESS",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 9.sp,
                        color         = TechSilver,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── STATS BAR ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScanlineGray)
                    .border(1.dp, DimBorder)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "SIGNALS", value = postCount.toString())
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(DimBorder))
                StatItem(label = "SAVED", value = wishlistCount.toString())
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(DimBorder))
                StatItem(label = "RANK", value = role, accent = true)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Acid divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f to CyberAcid.copy(alpha = 0.9f),
                            1f to Color.Transparent
                        )
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── USERNAME EDITOR ────────────────────────────────────
            SectionLabel(text = "CALLSIGN")
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScanlineGray)
                    .border(
                        width = 1.dp,
                        color = if (isEditingUsername) CyberAcid else DimBorder
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "USERNAME",
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 9.sp,
                        color      = TechSilver,
                        letterSpacing = 1.5.sp
                    )
                    if (!isEditingUsername) {
                        TextButton(
                            onClick = { isEditingUsername = true }
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint               = CyberAcid,
                                modifier           = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text       = "EDIT",
                                fontFamily = AppFonts.spaceMono,
                                fontSize   = 9.sp,
                                color      = CyberAcid
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isEditingUsername) {
                    // Input field
                    OutlinedTextField(
                        value        = usernameInput,
                        onValueChange = { usernameInput = it.uppercase().take(20) },
                        modifier     = Modifier.fillMaxWidth(),
                        placeholder  = {
                            Text(
                                "TYPE YOUR CALLSIGN",
                                fontFamily = AppFonts.spaceMono,
                                fontSize   = 13.sp,
                                color      = TechSilver.copy(alpha = 0.3f)
                            )
                        },
                        textStyle    = LocalTextStyle.current.copy(
                            fontFamily = AppFonts.oswald,
                            fontSize   = 20.sp,
                            color      = TeslaWhite
                        ),
                        singleLine   = true,
                        colors       = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = CyberAcid,
                            unfocusedBorderColor = DimBorder,
                            cursorColor          = CyberAcid
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Cancel
                        OutlinedButton(
                            onClick  = {
                                usernameInput     = username
                                isEditingUsername = false
                            },
                            modifier = Modifier.weight(1f),
                            border   = BorderStroke(1.dp, TechSilver.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "CANCEL",
                                fontFamily = AppFonts.spaceMono,
                                fontSize   = 11.sp,
                                color      = TechSilver
                            )
                        }
                        // Save
                        Button(
                            onClick  = { profileViewModel.saveUsername(usernameInput) },
                            enabled  = !isSaving && usernameInput.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors   = ButtonDefaults.buttonColors(containerColor = CyberAcid)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color    = VoidBlack,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "CONFIRM",
                                    fontFamily = AppFonts.spaceMono,
                                    fontSize   = 11.sp,
                                    color      = VoidBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text       = username.ifBlank { "NOT SET — TAP EDIT" },
                        fontFamily = AppFonts.oswald,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Black,
                        color      = if (username.isBlank()) TechSilver.copy(alpha = 0.4f) else TeslaWhite,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── AGENT CARD ─────────────────────────────────────────
            SectionLabel(text = "AGENT DATA")
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScanlineGray)
                    .border(1.dp, DimBorder)
                    .padding(20.dp)
            ) {
                ProfileDataRow(label = "AGENT ID", value = uid)
                ProfileDivider()
                ProfileDataRow(label = "EMAIL",    value = email)
                ProfileDivider()
                ProfileDataRow(label = "STATUS",   value = if (isVerified) "VERIFIED" else "PENDING VERIFICATION")
                ProfileDivider()
                ProfileDataRow(label = "ENROLLED", value = createdAt)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── LINKED SYSTEMS ─────────────────────────────────────
            SectionLabel(text = "LINKED SYSTEMS")
            Spacer(modifier = Modifier.height(12.dp))

            ProfileLinkRow(
                label = "IDENTITY ARCHETYPE",
                sub   = "View & retake your style test",
                icon  = Icons.Default.Person
            ) { navController.navigate(Routes.STYLIST) }

            ProfileLinkRow(
                label = "WISHLIST",
                sub   = "$wishlistCount artifacts saved",
                icon  = Icons.Default.Favorite
            ) { navController.navigate(Routes.WISHLIST) }

            ProfileLinkRow(
                label = "BODY DATA",
                sub   = "Measurements & fit profile",
                icon  = Icons.Default.Settings
            ) { navController.navigate(Routes.BODY_DATA) }

            ProfileLinkRow(
                label = "MY ORDERS",
                sub   = "Order history",
                icon  = Icons.Default.List
            ) { navController.navigate(Routes.MY_ORDERS) }

            ProfileLinkRow(
                label = "CART",
                sub   = "View pending products",
                icon  = Icons.Default.ShoppingCart
            ) { navController.navigate(Routes.CART) }

            ProfileLinkRow(
                label = "THE VAULT",
                sub   = "Browse & acquire artifacts",
                icon  = Icons.Default.Store
            ) { navController.navigate(Routes.ARTIFACT_ARCHIVE) }

            if (role.uppercase() == "ADMIN") {
                ProfileLinkRow(
                    label = "ADMIN DASHBOARD",
                    sub   = "Command center",
                    icon  = Icons.Default.Security
                ) {
                    navController.navigate(Routes.ADMIN_DASHBOARD)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── DANGER ZONE ────────────────────────────────────────
            SectionLabel(text = "DANGER ZONE")
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlitchRed.copy(alpha = 0.35f))
                    .background(GlitchRed.copy(alpha = 0.05f))
                    .clickable {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text          = "TERMINATE SESSION",
                            fontFamily    = AppFonts.oswald,
                            fontSize      = 18.sp,
                            fontWeight    = FontWeight.Black,
                            color         = GlitchRed,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text       = "Đăng xuất khỏi hệ thống",
                            fontFamily = AppFonts.spaceMono,
                            fontSize   = 10.sp,
                            color      = GlitchRed.copy(alpha = 0.6f)
                        )
                    }
                    Icon(
                        imageVector        = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint               = GlitchRed.copy(alpha = 0.6f),
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ── COMPONENTS ────────────────────────────────────────────────────

@Composable
private fun StatItem(label: String, value: String, accent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = value,
            fontFamily = AppFonts.oswald,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Black,
            color      = if (accent) CyberAcid else TeslaWhite
        )
        Text(
            text       = label,
            fontFamily = AppFonts.spaceMono,
            fontSize   = 8.sp,
            color      = TechSilver,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ProfileDataRow(label: String, value: String, colorOverride: Color = TeslaWhite) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text          = label,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 9.sp,
            color         = TechSilver,
            letterSpacing = 1.5.sp
        )
        Text(
            text          = value,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            color         = colorOverride,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ProfileDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(4.dp).background(CyberAcid))
        Text(
            text          = text,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            color         = CyberAcid,
            letterSpacing = 2.sp
        )
        Box(
            modifier = Modifier.weight(1f).height(1.dp)
                .background(Brush.horizontalGradient(
                    0f to CyberAcid.copy(alpha = 0.3f),
                    1f to Color.Transparent
                ))
        )
    }
}

@Composable
private fun ProfileLinkRow(
    label: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DimBorder)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = CyberAcid.copy(alpha = 0.7f),
                modifier           = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text          = label,
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = TeslaWhite,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text       = sub,
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 9.sp,
                    color      = TechSilver
                )
            }
        }
        Icon(
            imageVector        = Icons.Default.ArrowForward,
            contentDescription = null,
            tint               = TechSilver.copy(alpha = 0.5f),
            modifier           = Modifier.size(16.dp)
        )
    }
    Spacer(modifier = Modifier.height(2.dp))
}