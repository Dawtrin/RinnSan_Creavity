package com.rinnsan.creavity.presentation.admin

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.presentation.admin.tabs.*

val AdminRed     = Color(0xFFFF003C)
val DimBorder    = Color(0xFF2A2A2A)
val ScanlineGray = Color(0xFF111111)

fun formatVnd(amount: Long): String = when {
    amount >= 1_000_000L -> "%.1fM".format(amount / 1_000_000f)
    amount >= 1_000L     -> "${amount / 1_000}K"
    else                 -> amount.toString()
}

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state       by viewModel.state.collectAsState()
    val isAdmin     by viewModel.isAdmin.collectAsState()
    val actionMsg   by viewModel.actionMessage.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue  = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "blink"
    )

    // Snackbar cho action feedback
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMsg) {
        actionMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(ScanlineGray)
                        .border(1.dp, CyberAcid.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(data.visuals.message, fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp, color = CyberAcid)
                }
            }
        },
        containerColor = VoidBlack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VoidBlack)
        ) {
            when {
                state is AdminState.Error && !isAdmin ->
                    AccessDeniedScreen(onBack = { navController.popBackStack() })

                state is AdminState.Loading ->
                    AdminLoadingScreen()

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    // ── Fixed header ───────────────────────────────
                    AdminHeader(
                        blinkAlpha = blinkAlpha,
                        onBack     = { navController.popBackStack() },
                        onRefresh  = { viewModel.loadAllStats() }
                    )

                    // ── Tab bar ────────────────────────────────────
                    AdminTabBar(
                        selectedTab = selectedTab,
                        onTabSelect = { selectedTab = it }
                    )

                    Spacer(modifier = Modifier.height(1.dp).background(DimBorder))

                    // ── Tab content (scrollable) ───────────────────
                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> OverviewTab(viewModel = viewModel)
                            1 -> InventoryTab(viewModel = viewModel)
                            2 -> OrdersTab(viewModel = viewModel)
                            3 -> BrandsAdminTab(viewModel = viewModel)
                            4 -> UsersTab(viewModel = viewModel)
                            5 -> SupportTab(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HEADER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun AdminHeader(blinkAlpha: Float, onBack: () -> Unit, onRefresh: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack,
                modifier = Modifier.size(36.dp).border(1.dp, DimBorder)) {
                Icon(Icons.Default.ArrowBack, null, tint = TeslaWhite,
                    modifier = Modifier.size(18.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.background(AdminRed)
                    .padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("// ADMIN", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = TeslaWhite,
                        modifier = Modifier.alpha(blinkAlpha))
                }
                IconButton(onClick = onRefresh,
                    modifier = Modifier.size(36.dp).border(1.dp, DimBorder)) {
                    Icon(Icons.Default.Refresh, null, tint = CyberAcid,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("COMMAND\nCENTER", fontFamily = AppFonts.oswald, fontSize = 40.sp,
            fontWeight = FontWeight.Black, color = TeslaWhite,
            letterSpacing = (-1).sp, lineHeight = 42.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("RINNSAN_CREAVITY // ADMIN DASHBOARD",
            fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            color = TechSilver, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(
            Brush.horizontalGradient(0f to AdminRed.copy(alpha = 0.9f), 1f to Color.Transparent)
        ))
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TAB BAR — 6 tabs, 2 hàng × 3
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun AdminTabBar(selectedTab: Int, onTabSelect: (Int) -> Unit) {
    val tabs = listOf("OVERVIEW","INVENTORY","ORDERS","BRANDS","USERS","SUPPORT")
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(tabs.take(3), tabs.drop(3)).forEachIndexed { rowIdx, rowTabs ->
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowTabs.forEachIndexed { colIdx, label ->
                    val index      = rowIdx * 3 + colIdx
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier.weight(1f).height(34.dp)
                            .background(if (isSelected) CyberAcid else ScanlineGray)
                            .border(1.dp, if (isSelected) CyberAcid else DimBorder)
                            .clickable { onTabSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) VoidBlack else TechSilver,
                            letterSpacing = 0.3.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SHARED COMPONENTS (dùng trong tất cả tabs)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun AdminSectionLabel(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(4.dp).background(CyberAcid))
        Text(text, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = CyberAcid, letterSpacing = 2.sp)
        Box(modifier = Modifier.weight(1f).height(1.dp).background(
            Brush.horizontalGradient(0f to CyberAcid.copy(alpha = 0.3f), 1f to Color.Transparent)
        ))
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(message, fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
            color = TechSilver.copy(alpha = 0.5f), textAlign = TextAlign.Center)
    }
}

@Composable
fun AdminLoadingScreen() {
    val alpha by rememberInfiniteTransition(label = "l").animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "la")
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("LOADING\nDATA...", fontFamily = AppFonts.oswald, fontSize = 32.sp,
                fontWeight = FontWeight.Black, color = CyberAcid.copy(alpha = alpha),
                textAlign = TextAlign.Center, letterSpacing = (-0.5).sp)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = CyberAcid, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
fun AccessDeniedScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)) {
            Text("ACCESS\nDENIED", fontFamily = AppFonts.oswald, fontSize = 48.sp,
                fontWeight = FontWeight.Black, color = AdminRed,
                textAlign = TextAlign.Center, letterSpacing = (-1).sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Tài khoản không có quyền Admin.\nVào Firestore → users/{uid} → thêm role: \"admin\"",
                fontFamily = AppFonts.spaceMono, fontSize = 11.sp, color = TechSilver,
                textAlign = TextAlign.Center, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.border(1.dp, AdminRed).clickable { onBack() }
                .padding(horizontal = 24.dp, vertical = 12.dp)) {
                Text("[ GO BACK ]", fontFamily = AppFonts.spaceMono, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, color = AdminRed)
            }
        }
    }
}