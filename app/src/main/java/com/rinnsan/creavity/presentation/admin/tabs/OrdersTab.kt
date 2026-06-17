package com.rinnsan.creavity.presentation.admin.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.presentation.admin.*
import com.rinnsan.creavity.domain.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STATUS CONFIG
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
data class StatusConfig(val label: String, val color: Color, val nextStatus: String?, val nextLabel: String?)

fun getStatusConfig(status: String): StatusConfig = when (status.lowercase()) {
    "pending"   -> StatusConfig("PENDING",   Color(0xFFFF8C00), "shipped",   "MARK SHIPPED")
    "shipped"   -> StatusConfig("SHIPPED",   Color(0xFF00BFFF), "delivered", "MARK DELIVERED")
    "delivered" -> StatusConfig("DELIVERED", Color(0xFF00FF88), null,        null)
    "cancelled" -> StatusConfig("CANCELLED", AdminRed,          null,        null)
    "paid"      -> StatusConfig("PAID",      Color(0xFF00FF88), "shipped",   "MARK SHIPPED")
    else        -> StatusConfig(status.uppercase(), TechSilver,  null,        null)
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ROOT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun OrdersTab(viewModel: AdminViewModel) {
    val orders by viewModel.orders.collectAsState()

    var filterStatus     by remember { mutableStateOf("ALL") }
    var searchQuery      by remember { mutableStateOf("") }
    var cancellingOrder  by remember { mutableStateOf<AdminOrderItem?>(null) }
    var trackingOrder    by remember { mutableStateOf<AdminOrderItem?>(null) }

    val statusFilters = listOf("ALL", "PENDING", "SHIPPED", "DELIVERED", "CANCELLED")

    val filtered = orders.filter { order ->
        val matchStatus = filterStatus == "ALL" ||
                order.status.equals(filterStatus, ignoreCase = true)
        val matchSearch = searchQuery.isEmpty() ||
                order.docId.contains(searchQuery, ignoreCase = true) ||
                order.userId.contains(searchQuery, ignoreCase = true)
        matchStatus && matchSearch
    }

    // Summary counts
    val pendingCount   = orders.count { it.status.equals("pending",   ignoreCase = true) }
    val shippedCount   = orders.count { it.status.equals("shipped",   ignoreCase = true) }
    val deliveredCount = orders.count { it.status.equals("delivered", ignoreCase = true) }
    val cancelledCount = orders.count { it.status.equals("cancelled", ignoreCase = true) }
    val totalRevenue   = orders
        .filter { it.status.equals("delivered", ignoreCase = true) ||
                it.status.equals("paid",      ignoreCase = true) }
        .sumOf { it.totalAmount }

    LazyColumn(
        modifier       = Modifier.fillMaxSize().background(VoidBlack),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────
        item {
            AdminSectionLabel(text = "ORDER MANAGEMENT")
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Pipeline visual ────────────────────────────────────────
        item {
            OrderPipelineBar(
                pending   = pendingCount,
                shipped   = shippedCount,
                delivered = deliveredCount,
                cancelled = cancelledCount,
                total     = orders.size
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ── Stat cards ─────────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("TOTAL\nORDERS",  orders.size.toString(),         TeslaWhite,        Modifier.weight(1f))
                StatCard("PENDING",        pendingCount.toString(),         Color(0xFFFF8C00), Modifier.weight(1f))
                StatCard("REVENUE\nEARNED", formatVnd(totalRevenue) + "đ", Color(0xFF00FF88), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Search ─────────────────────────────────────────────────
        item {
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder).background(ScanlineGray)) {
                TextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    placeholder   = {
                        Text("SEARCH ORDER ID / USER ID...", fontFamily = AppFonts.spaceMono,
                            fontSize = 11.sp, color = TechSilver.copy(alpha = 0.4f))
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite),
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = TechSilver,
                            modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {{
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null, tint = TechSilver,
                                modifier = Modifier.size(14.dp))
                        }
                    }} else null,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = ScanlineGray,
                        unfocusedContainerColor = ScanlineGray,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = CyberAcid
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // ── Status filter chips ────────────────────────────────────
        item {
            Row(
                modifier              = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                statusFilters.forEach { status ->
                    val selected = filterStatus == status
                    val color    = if (status == "ALL") CyberAcid
                    else getStatusConfig(status).color

                    Box(
                        modifier = Modifier
                            .border(1.dp, if (selected) color else DimBorder)
                            .background(if (selected) color.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { filterStatus = status }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        val count = when (status) {
                            "ALL"       -> orders.size
                            "PENDING"   -> pendingCount
                            "SHIPPED"   -> shippedCount
                            "DELIVERED" -> deliveredCount
                            "CANCELLED" -> cancelledCount
                            else        -> 0
                        }
                        Text("$status ($count)", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) color else TechSilver)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("${filtered.size} ORDER${if (filtered.size != 1) "S" else ""}",
                fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Order list ─────────────────────────────────────────────
        if (filtered.isEmpty()) {
            item { EmptyState("NO ORDERS FOUND") }
        } else {
            items(filtered) { order ->
                OrderCard(
                    order    = order,
                    onAdvanceStatus = { nextStatus ->
                        viewModel.updateOrderStatus(order.docId, nextStatus)
                    },
                    onCancel = { cancellingOrder = order },
                    onUpdateTracking = { trackingOrder = order }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Cancel confirm dialog
    cancellingOrder?.let { order ->
        CancelOrderDialog(
            orderId   = order.docId,
            onDismiss = { cancellingOrder = null },
            onConfirm = {
                viewModel.updateOrderStatus(order.docId, "cancelled")
                cancellingOrder = null
            }
        )
    }

    trackingOrder?.let { order ->
        UpdateTrackingDialog(
            orderId = order.docId,
            onDismiss = { trackingOrder = null },
            onConfirm = { lat, lng ->
                viewModel.updateOrderTracking(order.docId, lat, lng)
            }
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// PIPELINE BAR — visual flow indicator
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun OrderPipelineBar(
    pending: Int, shipped: Int, delivered: Int, cancelled: Int, total: Int
) {
    val safeTotal = total.coerceAtLeast(1).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
            .padding(16.dp)
    ) {
        Text("ORDER PIPELINE", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = TechSilver, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Stacked bar
        Row(
            modifier = Modifier.fillMaxWidth().height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val segments = listOf(
                pending   to Color(0xFFFF8C00),
                shipped   to Color(0xFF00BFFF),
                delivered to Color(0xFF00FF88),
                cancelled to AdminRed
            )
            segments.forEach { (count, color) ->
                val frac = count / safeTotal
                if (frac > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(frac)
                            .fillMaxHeight()
                            .background(color)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(
                "PENDING"   to Color(0xFFFF8C00) to pending,
                "SHIPPED"   to Color(0xFF00BFFF) to shipped,
                "DELIVERED" to Color(0xFF00FF88) to delivered,
                "CANCELLED" to AdminRed          to cancelled
            ).forEach { (pair, count) ->
                val (label, color) = pair
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(color))
                    Text("$label: $count", fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                        color = TechSilver)
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ORDER CARD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun OrderCard(
    order: AdminOrderItem,
    onAdvanceStatus: (String) -> Unit,
    onCancel: () -> Unit,
    onUpdateTracking: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val config    = getStatusConfig(order.status)
    val timeStr   = remember(order.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.timestamp))
    }
    val shortId   = order.docId.take(10).uppercase()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, config.color.copy(alpha = 0.25f))
            .background(ScanlineGray)
    ) {
        // ── Top color bar ──────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().height(2.dp).background(
                Brush.horizontalGradient(
                    0f to config.color,
                    1f to config.color.copy(alpha = 0.2f)
                )
            )
        )

        // ── Main row ───────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .background(config.color.copy(alpha = 0.15f))
                            .border(1.dp, config.color.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(config.label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, color = config.color)
                    }
                    // Payment method
                    Text(order.paymentMethod, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = TechSilver.copy(alpha = 0.6f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("#$shortId", fontFamily = AppFonts.oswald, fontSize = 16.sp,
                    fontWeight = FontWeight.Black, color = TeslaWhite)
                Text(timeStr, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                    color = TechSilver.copy(alpha = 0.5f))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(formatVnd(order.totalAmount) + "đ",
                    fontFamily = AppFonts.oswald, fontSize = 18.sp,
                    fontWeight = FontWeight.Black, color = config.color)
                Text("${order.items.size} ITEM${if (order.items.size != 1) "S" else ""}",
                    fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, tint = TechSilver, modifier = Modifier.size(18.dp)
            )
        }

        // ── Expanded detail ────────────────────────────────────────
        AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VoidBlack)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // User ID
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    InfoChipOrder("ORDER ID", order.docId.uppercase())
                    InfoChipOrder("USER ID",  order.userId.take(16).uppercase())
                }

                // Items list — dùng itemDetails nếu có (có size), fallback sang items
                val hasDetails = order.itemDetails.isNotEmpty()
                Text("ITEMS", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                    color = TechSilver, letterSpacing = 1.sp)

                if (hasDetails) {
                    order.itemDetails.forEach { detail ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.Top
                        ) {
                            Row(modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.size(4.dp).background(CyberAcid)
                                    .padding(top = 4.dp))
                                Column {
                                    Text(
                                        text = buildString {
                                            append(detail.title)
                                            if (detail.quantity > 1) append(" × ${detail.quantity}")
                                        },
                                        fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                        color = TeslaWhite, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (detail.size.isNotBlank()) {
                                        Text(
                                            text          = "SIZE: ${detail.size}",
                                            fontFamily    = AppFonts.spaceMono,
                                            fontSize      = 9.sp,
                                            fontWeight    = FontWeight.Bold,
                                            color         = CyberAcid,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                            Text(
                                text       = formatVnd(detail.subtotal) + "đ",
                                fontFamily = AppFonts.spaceMono,
                                fontSize   = 9.sp,
                                color      = TechSilver
                            )
                        }
                    }
                } else {
                    // Fallback: đơn cũ chưa có itemDetails
                    order.items.forEach { itemName ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(4.dp).background(CyberAcid))
                            Text(itemName, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                color = TeslaWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // Status pipeline steps
                StatusStepsRow(currentStatus = order.status)

                // Action buttons
                val canCancel = order.status.equals("pending", ignoreCase = true) ||
                        order.status.equals("paid",    ignoreCase = true)

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Advance status button
                    config.nextStatus?.let { next ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(config.color)
                                .clickable { onAdvanceStatus(next) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ArrowForward, null, tint = VoidBlack,
                                    modifier = Modifier.size(14.dp))
                                Text(config.nextLabel ?: "ADVANCE",
                                    fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold, color = VoidBlack)
                            }
                        }
                    }

                    // Cancel button (only for pending/paid)
                    if (canCancel) {
                        Box(
                            modifier = Modifier
                                .weight(if (config.nextStatus != null) 0.7f else 1f)
                                .border(1.dp, AdminRed.copy(alpha = 0.5f))
                                .background(AdminRed.copy(alpha = 0.05f))
                                .clickable { onCancel() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Cancel, null, tint = AdminRed,
                                    modifier = Modifier.size(14.dp))
                                Text("CANCEL", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold, color = AdminRed)
                            }
                        }
                    }

                    // Mock Tracking button (only for shipped)
                    if (order.status.equals("shipped", ignoreCase = true)) {
                        Box(
                            modifier = Modifier
                                .weight(if (config.nextStatus != null) 0.7f else 1f)
                                .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                                .background(CyberAcid.copy(alpha = 0.05f))
                                .clickable { onUpdateTracking() }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.LocationOn, null, tint = CyberAcid,
                                    modifier = Modifier.size(14.dp))
                                Text("MOCK GPS", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold, color = CyberAcid)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Status steps row (pipeline visual) ────────────────────────────
@Composable
private fun StatusStepsRow(currentStatus: String) {
    val steps   = listOf("pending", "shipped", "delivered")
    val current = currentStatus.lowercase()
    val isCancelled = current == "cancelled"

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = when {
                isCancelled -> false
                step == "pending"   -> true
                step == "shipped"   -> current == "shipped" || current == "delivered"
                step == "delivered" -> current == "delivered"
                else -> false
            }
            val isCurrent   = current == step && !isCancelled
            val color       = getStatusConfig(step).color

            // Step dot
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 10.dp else 7.dp)
                    .background(
                        when {
                            isCancelled -> DimBorder
                            isCompleted -> color
                            else        -> DimBorder
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {}

            // Step label below — using Column overlay not ideal, use simpler approach
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(if (isCompleted && !isCancelled) color else DimBorder)
                )
            }
        }
    }

    // Labels row
    Row(modifier = Modifier.fillMaxWidth()) {
        steps.forEach { step ->
            Text(
                text       = step.uppercase(),
                fontFamily = AppFonts.spaceMono,
                fontSize   = 7.sp,
                color      = if (current == step && !isCancelled)
                    getStatusConfig(step).color
                else TechSilver.copy(alpha = 0.4f),
                modifier   = Modifier.weight(1f)
            )
        }
    }
}

// ── Cancel confirm dialog ─────────────────────────────────────────
@Composable
private fun CancelOrderDialog(orderId: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, AdminRed).padding(24.dp)
        ) {
            Text("CANCEL ORDER", fontFamily = AppFonts.oswald, fontSize = 20.sp,
                fontWeight = FontWeight.Black, color = AdminRed)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Xác nhận hủy đơn:\n#${orderId.take(10).uppercase()}",
                fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                color = TechSilver, lineHeight = 18.sp)
            Text("Trạng thái sẽ chuyển sang CANCELLED.",
                fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                color = AdminRed.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                    .clickable { onDismiss() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("KEEP", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TechSilver)
                }
                Box(modifier = Modifier.weight(1f).background(AdminRed)
                    .clickable { onConfirm() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("CANCEL ORDER", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TeslaWhite)
                }
            }
        }
    }
}

@Composable
private fun InfoChipOrder(label: String, value: String) {
    Column {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
            color = TechSilver.copy(alpha = 0.6f), letterSpacing = 1.sp)
        Text(value, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = TeslaWhite,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ── Mock tracking dialog ─────────────────────────────────────────
@Composable
private fun UpdateTrackingDialog(orderId: String, onDismiss: () -> Unit, onConfirm: (Double, Double) -> Unit) {
    // Default location: Da Nang
    var selectedLocation by remember { mutableStateOf(LatLng(16.0544, 108.2022)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 14f)
    }
    
    val scope = rememberCoroutineScope()
    var isAutoRunning by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, CyberAcid).padding(16.dp)
        ) {
            Text("UPDATE MOCK GPS", fontFamily = AppFonts.oswald, fontSize = 20.sp,
                fontWeight = FontWeight.Black, color = CyberAcid)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Chạm vào bản đồ để chọn vị trí Shipper, hoặc bấm AUTO RUN để giả lập lộ trình.",
                fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                color = TechSilver, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Mini Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .border(1.dp, DimBorder)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    onMapClick = { latLng -> selectedLocation = latLng }
                ) {
                    Marker(
                        state = MarkerState(position = selectedLocation),
                        title = "Shipper Mock"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                    .clickable { onDismiss() }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center) {
                    Text("CANCEL", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = TechSilver)
                }
                
                Box(modifier = Modifier.weight(1f).background(if (isAutoRunning) DimBorder else CyberAcid)
                    .clickable(enabled = !isAutoRunning) {
                        isAutoRunning = true
                        scope.launch {
                            // Dummy route in Da Nang
                            val route = listOf(
                                LatLng(16.0544, 108.2022), // Center Da Nang
                                LatLng(16.0583, 108.2144), // Con Market
                                LatLng(16.0611, 108.2285), // Dragon Bridge
                                LatLng(16.0558, 108.2452), // My Khe Beach
                                LatLng(16.0401, 108.2272)  // Asia Park
                            )
                            for (point in route) {
                                selectedLocation = point
                                onConfirm(point.latitude, point.longitude)
                                delay(3000)
                            }
                            isAutoRunning = false
                        }
                    }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center) {
                    Text(if (isAutoRunning) "RUNNING..." else "AUTO RUN", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = VoidBlack)
                }

                Box(modifier = Modifier.weight(1f).border(1.dp, CyberAcid)
                    .clickable(enabled = !isAutoRunning) {
                        onConfirm(selectedLocation.latitude, selectedLocation.longitude)
                        onDismiss()
                    }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center) {
                    Text("UPDATE", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = CyberAcid)
                }
            }
        }
    }
}
