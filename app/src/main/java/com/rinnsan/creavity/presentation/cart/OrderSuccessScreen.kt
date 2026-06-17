package com.rinnsan.creavity.presentation.cart

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*
import kotlinx.coroutines.delay

private val DimBorder = Color(0xFF2A2A2A)

@Composable
fun OrderSuccessScreen(
    orderId: String,
    navController: NavController
) {
    // Entry animations
    var showContent by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200);  showContent = true
        delay(600);  showDetails = true
        delay(400);  showButtons = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue   = if (showContent) 1f else 0f,
        animationSpec = tween(600), label = "content_alpha"
    )
    val contentScale by animateFloatAsState(
        targetValue   = if (showContent) 1f else 0.8f,
        animationSpec = tween(600, easing = FastOutSlowInEasing), label = "content_scale"
    )
    val detailsAlpha by animateFloatAsState(
        targetValue   = if (showDetails) 1f else 0f,
        animationSpec = tween(500), label = "details_alpha"
    )
    val buttonsAlpha by animateFloatAsState(
        targetValue   = if (showButtons) 1f else 0f,
        animationSpec = tween(500), label = "buttons_alpha"
    )

    // Pulsing checkmark
    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue  = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label         = "pulse_scale"
    )

    Box(
        modifier         = Modifier.fillMaxSize().background(VoidBlack),
        contentAlignment = Alignment.Center
    ) {
        // Background glow
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    0f to CyberAcid.copy(alpha = 0.06f),
                    0.5f to Color.Transparent
                )
            )
        )

        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // ── Checkmark ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale * contentScale)
                    .alpha(contentAlpha)
                    .background(CyberAcid.copy(alpha = 0.1f))
                    .border(2.dp, CyberAcid.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = CyberAcid,
                    modifier = Modifier.size(52.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Title ──────────────────────────────────────────────
            Box(
                modifier = Modifier.alpha(contentAlpha).scale(contentScale),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("// ORDER TRANSMITTED", fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp, color = CyberAcid, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("ACQUISITION\nCONFIRMED", fontFamily = AppFonts.oswald,
                        fontSize = 44.sp, fontWeight = FontWeight.Black, color = TeslaWhite,
                        textAlign = TextAlign.Center, letterSpacing = (-1).sp, lineHeight = 46.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Đơn hàng của bạn đã được tiếp nhận.\nChúng tôi sẽ xử lý trong thời gian sớm nhất.",
                        fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        color = TechSilver, textAlign = TextAlign.Center, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Order details card ─────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(detailsAlpha)
                    .border(1.dp, CyberAcid.copy(alpha = 0.3f))
                    .background(Color(0xFF111111))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("// ORDER DETAILS", fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp, color = CyberAcid, letterSpacing = 2.sp)

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))

                OrderDetailRow("ORDER ID",  "#${orderId.take(12).uppercase()}")
                OrderDetailRow("STATUS",    "PENDING")
                OrderDetailRow("PAYMENT",   "Sẽ hiển thị sau khi xác nhận")

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))

                // Next steps
                Text("NEXT STEPS", fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp)

                listOf(
                    "01" to "Admin sẽ xác nhận đơn hàng của bạn",
                    "02" to "Đơn hàng sẽ được đóng gói và gửi đi",
                    "03" to "Theo dõi trạng thái trong My Orders"
                ).forEach { (num, step) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(num, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = CyberAcid,
                            modifier = Modifier.width(24.dp))
                        Text(step, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                            color = TechSilver, lineHeight = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Action buttons ─────────────────────────────────────
            Column(
                modifier            = Modifier.fillMaxWidth().alpha(buttonsAlpha),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // View my orders
                Box(
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                        .background(CyberAcid)
                        .clickable { navController.navigate(Routes.MY_ORDERS) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Receipt, null, tint = VoidBlack,
                            modifier = Modifier.size(18.dp))
                        Text("VIEW MY ORDERS", fontFamily = AppFonts.oswald, fontSize = 16.sp,
                            fontWeight = FontWeight.Black, color = VoidBlack, letterSpacing = 1.sp)
                    }
                }

                // Back to vault
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                        .border(1.dp, DimBorder)
                        .clickable {
                            navController.navigate(Routes.ARTIFACT_ARCHIVE) {
                                popUpTo(Routes.HOME)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("BACK TO VAULT", fontFamily = AppFonts.spaceMono,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TechSilver)
                }

                // Back home
                Box(
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                        .clickable {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = false }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("RETURN TO HOME", fontFamily = AppFonts.spaceMono,
                        fontSize = 10.sp, color = TechSilver.copy(alpha = 0.4f))
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun OrderDetailRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = TechSilver.copy(alpha = 0.6f), letterSpacing = 1.sp)
        Text(value, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = TeslaWhite)
    }
}