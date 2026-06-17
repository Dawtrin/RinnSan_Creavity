package com.rinnsan.creavity.presentation.cart

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.core.router.Routes
import java.text.SimpleDateFormat
import java.util.*

private val DimBorder    = Color(0xFF2A2A2A)
private val ScanlineGray = Color(0xFF111111)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MODELS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
data class UserOrder(
    val docId: String,
    val totalAmount: Long,
    val status: String,
    val paymentMethod: String,
    val timestamp: Long,
    val items: List<String>,
    val address: Map<String, String> = emptyMap(),
    val tracking: Map<String, Double> = emptyMap()
)

data class TrackingStep(
    val status: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

private val TRACKING_STEPS = listOf(
    TrackingStep("pending",   "ĐẶT HÀNG",       "Đơn hàng đã được tiếp nhận",         Icons.Default.CheckCircle,    Color(0xFFFF8C00)),
    TrackingStep("confirmed", "XÁC NHẬN",        "Admin đã xác nhận đơn hàng",          Icons.Default.Verified,       Color(0xFF00BFFF)),
    TrackingStep("shipped",   "ĐANG GIAO",        "Đơn hàng đang trên đường giao",       Icons.Default.LocalShipping,  Color(0xFF9B9BFF)),
    TrackingStep("delivered", "ĐÃ NHẬN HÀNG",    "Giao hàng thành công",                Icons.Default.Done,           Color(0xFF00FF88))
)

private fun statusIndex(status: String): Int = when (status.lowercase()) {
    "pending"   -> 0
    "confirmed" -> 1
    "shipped"   -> 2
    "delivered", "paid" -> 3
    else -> 0
}

private fun statusColor(status: String): Color = when (status.lowercase()) {
    "pending"   -> Color(0xFFFF8C00)
    "confirmed" -> Color(0xFF00BFFF)
    "shipped"   -> Color(0xFF9B9BFF)
    "delivered", "paid" -> Color(0xFF00FF88)
    "cancelled" -> Color(0xFFFF003C)
    else        -> Color(0xFF888888)
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ROOT SCREEN
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun MyOrdersScreen(navController: NavController) {
    val db  = remember { FirebaseFirestore.getInstance() }
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid }

    var orders    by remember { mutableStateOf<List<UserOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filterStatus by remember { mutableStateOf("ALL") }

    // Realtime listener — tự cập nhật khi admin thay đổi status
    DisposableEffect(uid) {
        if (uid == null) { isLoading = false; return@DisposableEffect onDispose {} }

        val listener = db.collection("orders")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                snapshot ?: return@addSnapshotListener
                @Suppress("UNCHECKED_CAST")
                val list = snapshot.documents.map { doc ->
                    UserOrder(
                        docId         = doc.id,
                        totalAmount   = doc.getLong("totalAmount") ?: 0L,
                        status        = doc.getString("status") ?: "pending",
                        paymentMethod = doc.getString("paymentMethod") ?: "COD",
                        timestamp     = doc.getLong("timestamp") ?: 0L,
                        items         = (doc.get("items") as? List<String>) ?: emptyList(),
                        address       = (doc.get("address") as? Map<String, String>) ?: emptyMap(),
                        tracking      = (doc.get("tracking") as? Map<String, Double>) ?: emptyMap()
                    )
                }
                orders = list.sortedByDescending { it.timestamp }
                isLoading = false
            }

        onDispose { listener.remove() }
    }

    val filtered = if (filterStatus == "ALL") orders
    else orders.filter { it.status.equals(filterStatus, true) }

    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue  = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "blink"
    )

    Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── HEADER ─────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(52.dp))

                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() },
                            modifier = Modifier.size(36.dp).border(1.dp, DimBorder)) {
                            Icon(Icons.Default.ArrowBack, null, tint = TeslaWhite,
                                modifier = Modifier.size(18.dp))
                        }
                        if (!isLoading) {
                            Box(modifier = Modifier.background(CyberAcid)
                                .padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text("${orders.size} ĐƠN HÀNG",
                                    fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold, color = VoidBlack,
                                    modifier = Modifier.alpha(blinkAlpha))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("ORDER\nHISTORY", fontFamily = AppFonts.oswald, fontSize = 44.sp,
                        fontWeight = FontWeight.Black, color = TeslaWhite,
                        letterSpacing = (-1).sp, lineHeight = 46.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("LỊCH SỬ ĐẶT HÀNG // REALTIME",
                        fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        color = TechSilver, letterSpacing = 0.5.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(
                        Brush.horizontalGradient(0f to CyberAcid.copy(0.9f), 1f to Color.Transparent)))
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── STATUS FILTER ──────────────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL","pending","shipped","delivered","cancelled").forEach { status ->
                        val isSel  = filterStatus == status
                        val color  = if (status == "ALL") CyberAcid else statusColor(status)
                        val count  = if (status == "ALL") orders.size
                        else orders.count { it.status.equals(status, true) }
                        Box(modifier = Modifier
                            .border(1.dp, if (isSel) color else DimBorder)
                            .background(if (isSel) color.copy(0.1f) else Color.Transparent)
                            .clickable { filterStatus = status }
                            .padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("${status.uppercase()} ($count)",
                                fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) color else TechSilver.copy(0.6f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── LOADING / EMPTY / LIST ─────────────────────────────
            when {
                isLoading -> item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CyberAcid,
                                modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("LOADING...", fontFamily = AppFonts.spaceMono,
                                fontSize = 10.sp, color = TechSilver)
                        }
                    }
                }
                filtered.isEmpty() -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TRỐNG", fontFamily = AppFonts.oswald, fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = TeslaWhite.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Chưa có đơn hàng nào.", fontFamily = AppFonts.spaceMono,
                                fontSize = 11.sp, color = TechSilver)
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(modifier = Modifier.border(1.dp, CyberAcid).clickable { navController.navigate(Routes.ARTIFACT_ARCHIVE) }
                                .padding(horizontal = 24.dp, vertical = 12.dp)) {
                                Text("[ ENTER THE VAULT ]", fontFamily = AppFonts.spaceMono,
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberAcid)
                            }
                        }
                    }
                }
                else -> items(filtered, key = { it.docId }) { order ->
                    UserOrderCard(
                        order = order, 
                        modifier = Modifier.padding(horizontal = 20.dp),
                        onTrackClicked = {
                            navController.navigate(Routes.LIVE_TRACKING.replace("{orderId}", order.docId))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ORDER CARD WITH TRACKING TIMELINE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun UserOrderCard(order: UserOrder, modifier: Modifier = Modifier, onTrackClicked: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }

    val color   = statusColor(order.status)
    val isCancelled = order.status.equals("cancelled", true)
    val timeStr = remember(order.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.timestamp))
    }

    Column(modifier = modifier.fillMaxWidth()
        .border(1.dp, color.copy(alpha = 0.25f))
        .background(ScanlineGray)) {

        // Color stripe top
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(
            Brush.horizontalGradient(0f to color, 1f to color.copy(0.2f))
        ))

        // Main row
        Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically) {

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.background(color.copy(0.12f))
                        .border(1.dp, color.copy(0.4f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(order.status.uppercase(), fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                    Text(order.paymentMethod, fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = TechSilver.copy(0.5f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("#${order.docId.take(10).uppercase()}",
                    fontFamily = AppFonts.oswald, fontSize = 16.sp,
                    fontWeight = FontWeight.Black, color = TeslaWhite)
                Text(timeStr, fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp, color = TechSilver.copy(0.5f))

                if (order.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(order.items.joinToString(" • "),
                        fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = TechSilver.copy(0.55f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(formatPrice(order.totalAmount) + "đ",
                    fontFamily = AppFonts.oswald, fontSize = 18.sp,
                    fontWeight = FontWeight.Black, color = color)
                Text("${order.items.size} SẢN PHẨM",
                    fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)
            }

            Spacer(modifier = Modifier.width(6.dp))
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, tint = TechSilver, modifier = Modifier.size(18.dp))
        }

        // Expanded detail
        AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF0A0A0A))
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // ── TRACKING TIMELINE ──────────────────────────────
                if (!isCancelled) {
                    TrackingTimeline(currentStatus = order.status)
                    
                    if (order.status.lowercase() == "shipped") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, color)
                            .background(color.copy(0.1f))
                            .clickable { onTrackClicked() }
                            .padding(14.dp),
                            contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                                Text("[ XEM LIVE TRACKING BẢN ĐỒ ]", fontFamily = AppFonts.spaceMono,
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                            }
                        }
                    }
                } else {
                    CancelledBanner()
                }

                // ── ORDER ITEMS ────────────────────────────────────
                if (order.items.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(4.dp).background(CyberAcid))
                            Text("SẢN PHẨM", fontFamily = AppFonts.spaceMono,
                                fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp)
                        }
                        order.items.forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("—", fontFamily = AppFonts.spaceMono,
                                    fontSize = 9.sp, color = TechSilver.copy(0.4f))
                                Text(item, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                    color = TeslaWhite)
                            }
                        }
                    }
                }

                // ── SHIPPING ADDRESS ───────────────────────────────
                if (order.address.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()
                        .border(1.dp, DimBorder).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocationOn, null,
                                tint = CyberAcid.copy(0.6f), modifier = Modifier.size(12.dp))
                            Text("ĐỊA CHỈ GIAO HÀNG", fontFamily = AppFonts.spaceMono,
                                fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp)
                        }
                        val name    = order.address["fullName"] ?: ""
                        val phone   = order.address["phone"] ?: ""
                        val street  = order.address["street"] ?: ""
                        val city    = order.address["city"] ?: ""
                        if (name.isNotEmpty())
                            Text("$name • $phone", fontFamily = AppFonts.spaceMono,
                                fontSize = 10.sp, color = TeslaWhite, fontWeight = FontWeight.Bold)
                        if (street.isNotEmpty())
                            Text("$street, $city", fontFamily = AppFonts.spaceMono,
                                fontSize = 10.sp, color = TechSilver)
                    }
                }

                // ── PAYMENT INFO ───────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("THANH TOÁN", fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp, color = TechSilver.copy(0.6f), letterSpacing = 1.sp)
                        Text(order.paymentMethod, fontFamily = AppFonts.spaceMono,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TeslaWhite)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TỔNG TIỀN", fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp, color = TechSilver.copy(0.6f), letterSpacing = 1.sp)
                        Text(formatPrice(order.totalAmount) + "đ",
                            fontFamily = AppFonts.oswald, fontSize = 18.sp,
                            fontWeight = FontWeight.Black, color = CyberAcid)
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TRACKING TIMELINE — vertical steps
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun TrackingTimeline(currentStatus: String) {
    val currentIdx = statusIndex(currentStatus)

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text("TRẠNG THÁI ĐƠN HÀNG", fontFamily = AppFonts.spaceMono,
            fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        TRACKING_STEPS.forEachIndexed { idx, step ->
            val isDone    = idx <= currentIdx
            val isCurrent = idx == currentIdx

            val animScale by animateFloatAsState(
                targetValue   = if (isDone) 1f else 0.7f,
                animationSpec = tween(400, delayMillis = idx * 100),
                label         = "scale_$idx"
            )

            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top) {

                // ── Left column: dot + line ─────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    // Dot
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 32.dp else 26.dp)
                            .background(
                                if (isDone) step.color.copy(alpha = 0.15f) else DimBorder
                            )
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isDone) step.color else DimBorder
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(step.icon, null,
                            tint = if (isDone) step.color else TechSilver.copy(0.25f),
                            modifier = Modifier.size(if (isCurrent) 16.dp else 13.dp))
                    }

                    // Connector line (except last)
                    if (idx < TRACKING_STEPS.size - 1) {
                        Box(modifier = Modifier.width(2.dp).height(32.dp).background(
                            if (idx < currentIdx) step.color.copy(0.5f) else DimBorder
                        ))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // ── Right column: text ──────────────────────────────
                Column(modifier = Modifier.weight(1f)
                    .padding(bottom = if (idx < TRACKING_STEPS.size - 1) 20.dp else 0.dp)) {
                    Text(step.label, fontFamily = AppFonts.spaceMono,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = if (isDone) step.color else TechSilver.copy(0.3f),
                        letterSpacing = 1.sp)
                    if (isDone || isCurrent) {
                        Text(step.description, fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp, color = TechSilver.copy(0.5f))
                    }
                    // "ĐANG XỬ LÝ" badge on current step
                    if (isCurrent && currentStatus != "delivered") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier
                            .background(step.color.copy(0.1f))
                            .border(1.dp, step.color.copy(0.4f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("ĐANG XỬ LÝ", fontFamily = AppFonts.spaceMono,
                                fontSize = 8.sp, fontWeight = FontWeight.Bold, color = step.color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CancelledBanner() {
    Row(modifier = Modifier.fillMaxWidth()
        .background(Color(0xFFFF003C).copy(0.08f))
        .border(1.dp, Color(0xFFFF003C).copy(0.3f))
        .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Default.Cancel, null, tint = Color(0xFFFF003C),
            modifier = Modifier.size(20.dp))
        Column {
            Text("ĐƠN HÀNG ĐÃ HỦY", fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF003C))
            Text("Đơn hàng này đã bị hủy. Liên hệ support nếu cần hỗ trợ.",
                fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)
        }
    }
}