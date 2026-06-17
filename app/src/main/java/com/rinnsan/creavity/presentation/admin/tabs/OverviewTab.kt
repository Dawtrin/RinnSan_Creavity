package com.rinnsan.creavity.presentation.admin.tabs

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.presentation.admin.*
import com.rinnsan.creavity.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OverviewTab(viewModel: AdminViewModel) {
    val summary       by viewModel.summary.collectAsState()
    val recentClicks  by viewModel.recentClicks.collectAsState()
    val dayStats      by viewModel.dayStats.collectAsState()
    val brandStats    by viewModel.brandStats.collectAsState()
    val salesDayStats by viewModel.salesDayStats.collectAsState()
    val recentOrders  by viewModel.recentOrders.collectAsState()

    // Tab nội bộ cho Live Feed
    var feedTab by remember { mutableIntStateOf(0) } // 0 = ORDERS, 1 = CLICKS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // ══════════════════════════════════════════════════════════════
        // TẦNG 1 — REVENUE COMMAND BANNER
        // Tổng doanh thu gộp nổi bật nhất, con số CEO nhìn đầu tiên
        // ══════════════════════════════════════════════════════════════
        val totalGross = summary.totalRevenue + summary.totalCommission
        RevenueCommandBanner(
            totalGross      = totalGross,
            totalRevenue    = summary.totalRevenue,
            totalCommission = summary.totalCommission
        )

        // ══════════════════════════════════════════════════════════════
        // SYSTEM PULSE — 4 chip nhỏ gọn 1 hàng
        // ══════════════════════════════════════════════════════════════
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MicroStatChip("USERS",     summary.totalUsers.toString(),     TeslaWhite,        Modifier.weight(1f))
            MicroStatChip("ARTIFACTS", summary.totalArtifacts.toString(), TeslaWhite,        Modifier.weight(1f))
            MicroStatChip("TICKETS",   summary.openTickets.toString(),    AdminRed,          Modifier.weight(1f))
            MicroStatChip("ORDERS",    summary.totalOrders.toString(),    Color(0xFFFF8C00), Modifier.weight(1f))
        }

        // ══════════════════════════════════════════════════════════════
        // TẦNG 2 — DUAL CHART (2 biểu đồ song song cùng chiều cao)
        // ══════════════════════════════════════════════════════════════
        if (dayStats.isNotEmpty() || salesDayStats.isNotEmpty()) {
            AdminSectionLabel(text = "14-DAY PULSE")
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Commission chart (affiliate)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(ScanlineGray)
                        .border(1.dp, DimBorder)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("COMMISSION", fontFamily = AppFonts.spaceMono, fontSize = 7.sp,
                            color = CyberAcid, letterSpacing = 1.sp)
                        Text(formatVnd(summary.totalCommission) + "đ",
                            fontFamily = AppFonts.oswald, fontSize = 14.sp,
                            fontWeight = FontWeight.Black, color = CyberAcid)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (dayStats.isNotEmpty())
                            CompactBarChart(
                                bars     = dayStats.map { it.commission },
                                labels   = dayStats.map { it.dateKey.takeLast(5) },
                                barColor = CyberAcid
                            )
                        else
                            Box(modifier = Modifier.height(70.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center) {
                                Text("NO DATA", fontFamily = AppFonts.spaceMono,
                                    fontSize = 8.sp, color = TechSilver.copy(0.3f))
                            }
                    }
                }

                // Revenue chart (direct sales)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(ScanlineGray)
                        .border(1.dp, DimBorder)
                        .padding(10.dp)
                ) {
                    Column {
                        Text("REVENUE", fontFamily = AppFonts.spaceMono, fontSize = 7.sp,
                            color = Color(0xFF00FF88), letterSpacing = 1.sp)
                        Text(formatVnd(summary.totalRevenue) + "đ",
                            fontFamily = AppFonts.oswald, fontSize = 14.sp,
                            fontWeight = FontWeight.Black, color = Color(0xFF00FF88))
                        Spacer(modifier = Modifier.height(6.dp))
                        if (salesDayStats.isNotEmpty())
                            CompactBarChart(
                                bars     = salesDayStats.map { it.revenue },
                                labels   = salesDayStats.map { it.dateKey.takeLast(5) },
                                barColor = Color(0xFF00FF88)
                            )
                        else
                            Box(modifier = Modifier.height(70.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center) {
                                Text("NO DATA", fontFamily = AppFonts.spaceMono,
                                    fontSize = 8.sp, color = TechSilver.copy(0.3f))
                            }
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════════
        // ORDER STATUS ROW — pending / delivered / clicks
        // ══════════════════════════════════════════════════════════════
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatCard("PENDING",   summary.pendingOrders.toString(),   Color(0xFFFF8C00), Modifier.weight(1f))
            StatCard("DELIVERED", summary.deliveredOrders.toString(), Color(0xFF00FF88), Modifier.weight(1f))
            StatCard("CLICKS",    summary.totalClicks.toString(),     CyberAcid,         Modifier.weight(1f))
        }

        // ══════════════════════════════════════════════════════════════
        // BRAND SHARE DOUGHNUT
        // ══════════════════════════════════════════════════════════════
        if (brandStats.isNotEmpty()) {
            AdminSectionLabel(text = "BRAND SHARE")
            BrandDoughnutChart(brandStats = brandStats)
        }

        // ══════════════════════════════════════════════════════════════
        // TẦNG 3 — LIVE FEED với tab toggle nội bộ
        // Chỉ 7 item gần nhất — ai muốn xem đủ thì sang tab ORDERS / BRANDS
        // ══════════════════════════════════════════════════════════════
        AdminSectionLabel(text = "LIVE FEED")

        // Toggle ORDERS | CLICKS
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("ORDERS", "CLICKS").forEachIndexed { idx, label ->
                val selected = feedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f).height(28.dp)
                        .background(if (selected) CyberAcid.copy(0.15f) else Color.Transparent)
                        .border(1.dp, if (selected) CyberAcid else DimBorder)
                        .clickable { feedTab = idx },
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) CyberAcid else TechSilver.copy(0.5f),
                        letterSpacing = 1.sp)
                }
            }
        }

        // Feed content — tối đa 7 item
        if (feedTab == 0) {
            val feed = recentOrders.take(7)
            if (feed.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    feed.forEach { order -> RecentOrderRow(order = order) }
                }
                if (recentOrders.size > 7) {
                    Text("+ ${recentOrders.size - 7} đơn nữa → xem tab ORDERS",
                        fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = TechSilver.copy(0.4f),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = TextAlign.Center)
                }
            } else {
                EmptyState(message = "CHƯA CÓ ĐƠN HÀNG NÀO")
            }
        } else {
            val feed = recentClicks.take(7)
            if (feed.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    feed.forEach { click -> RecentClickRow(click = click) }
                }
                if (recentClicks.size > 7) {
                    Text("+ ${recentClicks.size - 7} click nữa → xem tab BRANDS",
                        fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = TechSilver.copy(0.4f),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        textAlign = TextAlign.Center)
                }
            } else {
                EmptyState(message = "CHƯA CÓ AFFILIATE CLICKS")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// REVENUE COMMAND BANNER — tổng gộp nổi bật
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun RevenueCommandBanner(
    totalGross: Long,
    totalRevenue: Long,
    totalCommission: Long
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF0D1F0D),
                    1f to Color(0xFF050F05)
                )
            )
            .border(
                BorderStroke( // Bọc vào trong BorderStroke
                    1.dp,
                    Brush.horizontalGradient(
                        0f to Color(0xFF00FF88).copy(0.6f),
                        1f to Color(0xFF00FF88).copy(0.1f)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Text("TOTAL REVENUE", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                color = Color(0xFF00FF88).copy(0.6f), letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatVnd(totalGross) + "đ",
                fontFamily = AppFonts.oswald, fontSize = 42.sp,
                fontWeight = FontWeight.Black, color = Color(0xFF00FF88),
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text("DIRECT SALE", fontFamily = AppFonts.spaceMono, fontSize = 7.sp,
                        color = TechSilver.copy(0.5f), letterSpacing = 0.5.sp)
                    Text(formatVnd(totalRevenue) + "đ", fontFamily = AppFonts.spaceMono,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF88).copy(0.8f))
                }
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(DimBorder))
                Column {
                    Text("AFFILIATE", fontFamily = AppFonts.spaceMono, fontSize = 7.sp,
                        color = TechSilver.copy(0.5f), letterSpacing = 0.5.sp)
                    Text(formatVnd(totalCommission) + "đ", fontFamily = AppFonts.spaceMono,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = CyberAcid.copy(0.8f))
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MICRO STAT CHIP — nhỏ gọn, dùng cho hàng 4 cột
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun MicroStatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ScanlineGray)
            .border(1.dp, DimBorder)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontFamily = AppFonts.oswald, fontSize = 18.sp,
            fontWeight = FontWeight.Black, color = color, letterSpacing = (-0.5).sp)
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 7.sp,
            color = TechSilver.copy(0.5f), letterSpacing = 0.5.sp, maxLines = 1)
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STAT CARD — dùng cho hàng 3 cột
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(ScanlineGray)
            .border(1.dp, DimBorder)
            .padding(14.dp)
    ) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = TechSilver, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, fontFamily = AppFonts.oswald, fontSize = 22.sp,
            fontWeight = FontWeight.Black, color = color, letterSpacing = (-0.5).sp)
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COMPACT BAR CHART — dùng chung cho cả 2 chart trong dual panel
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun CompactBarChart(
    bars: List<Long>,
    labels: List<String>,
    barColor: Color,
    height: Dp = 70.dp
) {
    val maxVal = bars.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    Column {
        Row(
            modifier              = Modifier.fillMaxWidth().height(height),
            verticalAlignment     = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            bars.forEachIndexed { idx, value ->
                val fraction = (value.toFloat() / maxVal).coerceIn(0.02f, 1f)
                val animFraction by animateFloatAsState(
                    targetValue   = fraction,
                    animationSpec = tween(800, easing = FastOutSlowInEasing),
                    label         = "bar_$idx"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(animFraction)
                        .background(
                            Brush.verticalGradient(
                                0f to barColor.copy(alpha = 0.2f),
                                1f to barColor
                            )
                        )
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
        Spacer(modifier = Modifier.height(3.dp))
        // Chỉ show ngày đầu và ngày cuối để tránh chật
        if (labels.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(labels.first(), fontFamily = AppFonts.spaceMono, fontSize = 6.sp,
                    color = TechSilver.copy(0.35f))
                Spacer(modifier = Modifier.weight(1f))
                Text(labels.last(), fontFamily = AppFonts.spaceMono, fontSize = 6.sp,
                    color = TechSilver.copy(0.35f))
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// RECENT ORDER ROW
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun RecentOrderRow(order: RecentOrder) {
    val timeStr = remember(order.timestamp) {
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(order.timestamp))
    }
    val statusColor = when (order.status.lowercase()) {
        "pending"           -> Color(0xFFFF8C00)
        "shipped"           -> Color(0xFF00BFFF)
        "delivered", "paid" -> Color(0xFF00FF88)
        "cancelled"         -> AdminRed
        else                -> TechSilver
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.2f))
            .background(ScanlineGray)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${order.items.size} SẢN PHẨM • #${order.docId.takeLast(6).uppercase()}",
                fontFamily = AppFonts.oswald, fontSize = 13.sp,
                fontWeight = FontWeight.Bold, color = TeslaWhite,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(0.12f))
                        .border(1.dp, statusColor.copy(0.3f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(order.status.uppercase(), fontFamily = AppFonts.spaceMono,
                        fontSize = 8.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
                Text(order.paymentMethod, fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp, color = TechSilver.copy(0.5f))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatVnd(order.totalAmount) + "đ", fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF88))
            Text(timeStr, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                color = TechSilver.copy(alpha = 0.5f))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// RECENT CLICK ROW
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun RecentClickRow(click: RecentClick) {
    val timeStr = remember(click.timestamp) {
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(click.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(click.artifactTitle, fontFamily = AppFonts.oswald, fontSize = 13.sp,
                fontWeight = FontWeight.Bold, color = TeslaWhite,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(click.vendor, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                    color = TechSilver)
                Text(click.archetype, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                    color = CyberAcid.copy(alpha = 0.7f))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("+${formatVnd(click.commissionEarned)}đ", fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberAcid)
            Text(timeStr, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                color = TechSilver.copy(alpha = 0.5f))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// BRAND DOUGHNUT CHART
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun BrandDoughnutChart(brandStats: List<BrandStat>) {
    val total  = brandStats.sumOf { it.commission }.toFloat().coerceAtLeast(1f)
    val colors = listOf(
        CyberAcid, Color(0xFF00BFFF), Color(0xFFFF8C00),
        Color(0xFFBF00FF), Color(0xFF00FF88), Color(0xFFFF4444)
    )

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(120.dp).padding(8.dp)
        ) {
            var startAngle = -90f
            brandStats.forEachIndexed { index, stat ->
                val sweep = (stat.commission / total) * 360f
                val color = colors[index % colors.size]
                drawArc(color = color, startAngle = startAngle, sweepAngle = sweep,
                    useCenter = false, style = Stroke(width = 24f, cap = StrokeCap.Butt))
                startAngle += sweep
            }
            drawCircle(color = Color(0xFF111111), radius = size.minDimension / 2f - 24f)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            brandStats.take(6).forEachIndexed { index, stat ->
                val pct = ((stat.commission / total) * 100).toInt()
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp)
                        .background(colors[index % colors.size], CircleShape))
                    Text(stat.vendor, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = TechSilver, modifier = Modifier.weight(1f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text("$pct%", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, color = TeslaWhite)
                }
            }
        }
    }
}
