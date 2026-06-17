package com.rinnsan.creavity.presentation.admin.tabs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.BasicTextField
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.presentation.admin.*
import com.rinnsan.creavity.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STATUS CONFIG
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
data class TicketStatusConfig(
    val label: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun ticketStatusConfig(status: String): TicketStatusConfig = when (status.lowercase()) {
    "new"         -> TicketStatusConfig("NEW",         AdminRed,          Icons.Default.FiberNew)
    "in_progress" -> TicketStatusConfig("IN PROGRESS", Color(0xFFFF8C00), Icons.Default.Pending)
    "resolved"    -> TicketStatusConfig("RESOLVED",    Color(0xFF00FF88), Icons.Default.CheckCircle)
    else          -> TicketStatusConfig(status.uppercase(), TechSilver,   Icons.Default.Help)
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ROOT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun SupportTab(viewModel: AdminViewModel) {
    val tickets by viewModel.tickets.collectAsState()

    var filterStatus  by remember { mutableStateOf("ALL") }
    var searchQuery   by remember { mutableStateOf("") }
    var viewingTicket by remember { mutableStateOf<TicketItem?>(null) }

    val filtered = tickets.filter { ticket ->
        val matchStatus = filterStatus == "ALL" ||
                ticket.status.equals(filterStatus.replace(" ", "_"), ignoreCase = true)
        val matchSearch = searchQuery.isEmpty() ||
                ticket.title.contains(searchQuery, ignoreCase = true) ||
                ticket.userEmail.contains(searchQuery, ignoreCase = true) ||
                ticket.message.contains(searchQuery, ignoreCase = true)
        matchStatus && matchSearch
    }

    // Counts
    val newCount        = tickets.count { it.status.equals("new",         true) }
    val inProgressCount = tickets.count { it.status.equals("in_progress", true) }
    val resolvedCount   = tickets.count { it.status.equals("resolved",    true) }

    Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Header ─────────────────────────────────────────────
            item {
                AdminSectionLabel(text = "CUSTOMER SUPPORT")
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Stats + Status overview ────────────────────────────
            item {
                SupportOverviewPanel(
                    newCount        = newCount,
                    inProgressCount = inProgressCount,
                    resolvedCount   = resolvedCount,
                    total           = tickets.size
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Search ─────────────────────────────────────────────
            item {
                Box(modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder)
                    .background(ScanlineGray)) {
                    TextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        placeholder = {
                            Text("SEARCH TITLE / EMAIL / MESSAGE...",
                                fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                                color = TechSilver.copy(alpha = 0.4f))
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TechSilver,
                            modifier = Modifier.size(16.dp)) },
                        trailingIcon = if (searchQuery.isNotEmpty()) {{
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null, tint = TechSilver,
                                    modifier = Modifier.size(14.dp))
                            }
                        }} else null,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor   = ScanlineGray, unfocusedContainerColor = ScanlineGray,
                            focusedIndicatorColor   = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            cursorColor             = CyberAcid)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ── Status filter ──────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL"         to TechSilver,
                        "NEW"         to AdminRed,
                        "IN_PROGRESS" to Color(0xFFFF8C00),
                        "RESOLVED"    to Color(0xFF00FF88)
                    ).forEach { (status, color) ->
                        val count = when (status) {
                            "ALL"         -> tickets.size
                            "NEW"         -> newCount
                            "IN_PROGRESS" -> inProgressCount
                            "RESOLVED"    -> resolvedCount
                            else          -> 0
                        }
                        val isSel = filterStatus == status
                        Box(
                            modifier = Modifier.weight(1f)
                                .border(1.dp, if (isSel) color else DimBorder)
                                .background(if (isSel) color.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { filterStatus = status }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(count.toString(), fontFamily = AppFonts.oswald, fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) color else TechSilver.copy(alpha = 0.5f))
                                Text(status.replace("_", " "), fontFamily = AppFonts.spaceMono,
                                    fontSize = 7.sp,
                                    color = if (isSel) color else TechSilver.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("${filtered.size} TICKET${if (filtered.size != 1) "S" else ""}",
                    fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Ticket list ────────────────────────────────────────
            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        if (filterStatus == "ALL") "NO SUPPORT TICKETS YET"
                        else "NO ${filterStatus.replace("_"," ")} TICKETS"
                    )
                }
            } else {
                items(filtered, key = { it.docId }) { ticket ->
                    TicketCard(
                        ticket   = ticket,
                        onView   = { viewingTicket = ticket },
                        onAdvance = { next ->
                            viewModel.updateTicketStatus(ticket.docId, next)
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // ── Ticket detail dialog ───────────────────────────────────
        viewingTicket?.let { ticket ->
            TicketDetailDialog(
                ticket    = ticket,
                onDismiss = { viewingTicket = null },
                onAdvance = { next ->
                    viewModel.updateTicketStatus(ticket.docId, next)
                    viewingTicket = null
                },
                onReply = { docId, replyText ->
                    viewModel.replyToTicket(docId, replyText)
                    viewingTicket = null
                }
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// OVERVIEW PANEL
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun SupportOverviewPanel(
    newCount: Int, inProgressCount: Int, resolvedCount: Int, total: Int
) {
    val resolvedRate = if (total > 0) (resolvedCount * 100 / total) else 0
    val animRate by animateFloatAsState(
        targetValue   = resolvedRate.toFloat() / 100f,
        animationSpec = tween(800), label = "resolve_rate"
    )

    Column(
        modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder)
            .background(ScanlineGray).padding(16.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("SUPPORT OVERVIEW", fontFamily = AppFonts.spaceMono,
                fontSize = 9.sp, color = TechSilver, letterSpacing = 2.sp)
            Text("${resolvedRate}% RESOLVED", fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF88))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Resolution rate bar
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(DimBorder)) {
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(animRate)
                    .background(
                        Brush.horizontalGradient(
                            0f to Color(0xFF00FF88),
                            1f to Color(0xFF00FF88).copy(alpha = 0.5f)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3 stat cards
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SupportStatCard(
                label = "NEW",
                count = newCount,
                color = AdminRed,
                modifier = Modifier.weight(1f)
            )
            SupportStatCard(
                label = "IN PROGRESS",
                count = inProgressCount,
                color = Color(0xFFFF8C00),
                modifier = Modifier.weight(1f)
            )
            SupportStatCard(
                label = "RESOLVED",
                count = resolvedCount,
                color = Color(0xFF00FF88),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SupportStatCard(label: String, count: Int, color: Color, modifier: Modifier) {
    Column(
        modifier            = modifier.background(VoidBlack).border(1.dp, color.copy(0.2f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(count.toString(), fontFamily = AppFonts.oswald, fontSize = 24.sp,
            fontWeight = FontWeight.Black, color = color)
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
            color = TechSilver.copy(alpha = 0.6f), textAlign = TextAlign.Center)
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TICKET CARD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun TicketCard(
    ticket: TicketItem,
    onView: () -> Unit,
    onAdvance: (String) -> Unit
) {
    val config  = ticketStatusConfig(ticket.status)
    val timeStr = remember(ticket.timestamp) {
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(ticket.timestamp))
    }
    val isNew      = ticket.status.equals("new", true)
    val isProgress = ticket.status.equals("in_progress", true)
    val isResolved = ticket.status.equals("resolved", true)

    // Blink for new tickets
    val blinkAlpha by rememberInfiniteTransition(label = "tick_blink").animateFloat(
        initialValue  = 1f, targetValue = if (isNew) 0.3f else 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label         = "tick_alpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, config.color.copy(alpha = if (isNew) 0.5f else 0.2f))
            .background(if (isNew) config.color.copy(alpha = 0.04f) else ScanlineGray)
    ) {
        // Top stripe
        Box(
            modifier = Modifier.fillMaxWidth().height(2.dp).background(
                Brush.horizontalGradient(0f to config.color, 1f to config.color.copy(alpha = 0.2f))
            )
        )

        Row(
            modifier          = Modifier.fillMaxWidth().clickable { onView() }.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Status icon
            Box(
                modifier         = Modifier.size(36.dp)
                    .background(config.color.copy(alpha = 0.1f))
                    .border(1.dp, config.color.copy(alpha = 0.3f))
                    .alpha(if (isNew) blinkAlpha else 1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(config.icon, null, tint = config.color, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title + time
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Text(
                        text       = ticket.title,
                        fontFamily = AppFonts.oswald,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Black,
                        color      = TeslaWhite,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(timeStr, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = TechSilver.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // User email
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = TechSilver.copy(alpha = 0.4f),
                        modifier = Modifier.size(10.dp))
                    Text(ticket.userEmail, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = TechSilver.copy(alpha = 0.6f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Message preview
                Text(
                    text       = ticket.message,
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 10.sp,
                    color      = TechSilver.copy(alpha = 0.7f),
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick action buttons
                if (!isResolved) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isNew) {
                            QuickActionChip(
                                label   = "START",
                                color   = Color(0xFFFF8C00),
                                onClick = { onAdvance("in_progress") }
                            )
                        }
                        if (isNew || isProgress) {
                            QuickActionChip(
                                label   = "RESOLVE",
                                color   = Color(0xFF00FF88),
                                onClick = { onAdvance("resolved") }
                            )
                        }
                    }
                } else {
                    // Resolved indicator
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = Color(0xFF00FF88).copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp))
                        Text("TICKET RESOLVED", fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp, color = Color(0xFF00FF88).copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.5f))
            .background(color.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            fontWeight = FontWeight.Bold, color = color)
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TICKET DETAIL DIALOG — full message + actions
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun TicketDetailDialog(
    ticket: TicketItem,
    onDismiss: () -> Unit,
    onAdvance: (String) -> Unit,
    onReply: (String, String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val config     = ticketStatusConfig(ticket.status)
    val isNew      = ticket.status.equals("new", true)
    val isProgress = ticket.status.equals("in_progress", true)
    val isResolved = ticket.status.equals("resolved", true)

    val timeStr = remember(ticket.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ticket.timestamp))
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, config.color.copy(alpha = 0.4f))
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ─────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to config.color.copy(alpha = 0.12f),
                            1f to VoidBlack
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Status badge
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(config.icon, null, tint = config.color,
                                modifier = Modifier.size(16.dp))
                            Text(config.label, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                fontWeight = FontWeight.Bold, color = config.color)
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, null, tint = TechSilver,
                                modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(ticket.title, fontFamily = AppFonts.oswald, fontSize = 22.sp,
                        fontWeight = FontWeight.Black, color = TeslaWhite, lineHeight = 24.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Meta info
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Person, null, tint = TechSilver.copy(alpha = 0.5f),
                                modifier = Modifier.size(11.dp))
                            Text(ticket.userEmail, fontFamily = AppFonts.spaceMono,
                                fontSize = 9.sp, color = TechSilver.copy(alpha = 0.7f),
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Schedule, null, tint = TechSilver.copy(alpha = 0.5f),
                                modifier = Modifier.size(11.dp))
                            Text(timeStr, fontFamily = AppFonts.spaceMono,
                                fontSize = 9.sp, color = TechSilver.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // ── Message box ────────────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .border(1.dp, DimBorder)
                        .background(ScanlineGray)
                        .padding(16.dp)
                ) {
                    Text("// MESSAGE", fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = TechSilver.copy(alpha = 0.6f),
                        letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(ticket.message, fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        color = TeslaWhite, lineHeight = 20.sp)
                        
                    if (ticket.adminReply != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("// ADMIN REPLY", fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp, color = Color(0xFF00FF88).copy(alpha = 0.6f),
                            letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(ticket.adminReply, fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                            color = Color(0xFF00FF88), lineHeight = 20.sp)
                    }
                }

                // ── Ticket metadata ────────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder)
                        .background(ScanlineGray).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    TicketMetaRow("TICKET ID",  ticket.docId.take(16).uppercase())
                    TicketMetaRow("USER ID",    ticket.userId.take(16).uppercase())
                    TicketMetaRow("SUBMITTED",  timeStr)
                    TicketMetaRow("STATUS",     config.label)
                }

                // ── Status pipeline ────────────────────────────────
                TicketPipeline(currentStatus = ticket.status)

                // ── Action buttons ─────────────────────────────────
                if (!isResolved) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Reply input field
                        BasicTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = TeslaWhite,
                                fontFamily = AppFonts.spaceMono,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder).background(ScanlineGray).padding(16.dp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (replyText.isEmpty()) {
                                        Text("Type reply to user...", color = TechSilver.copy(alpha = 0.4f), fontFamily = AppFonts.spaceMono, fontSize = 11.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        
                        if (isNew) {
                            TicketActionButton(
                                label    = "START PROCESSING",
                                subtitle = "Đánh dấu đang xử lý, user sẽ biết ticket được nhận",
                                color    = Color(0xFFFF8C00),
                                icon     = Icons.Default.Pending,
                                onClick  = { onAdvance("in_progress") }
                            )
                        }
                        TicketActionButton(
                            label    = if (replyText.isNotBlank()) "REPLY & RESOLVE" else "MARK AS RESOLVED",
                            subtitle = if (replyText.isNotBlank()) "Gửi tin nhắn và đóng ticket" else "Đóng ticket, đánh dấu đã giải quyết xong",
                            color    = Color(0xFF00FF88),
                            icon     = Icons.Default.CheckCircle,
                            onClick  = { 
                                if (replyText.isNotBlank()) onReply(ticket.docId, replyText)
                                else onAdvance("resolved") 
                            }
                        )
                    }
                } else {
                    // Already resolved state
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0xFF00FF88).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFF00FF88).copy(alpha = 0.3f))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = Color(0xFF00FF88), modifier = Modifier.size(20.dp))
                            Column {
                                Text("TICKET CLOSED", fontFamily = AppFonts.spaceMono,
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FF88))
                                Text("Ticket này đã được giải quyết.",
                                    fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                                    color = TechSilver)
                            }
                        }
                    }

                    // Reopen option
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, DimBorder)
                            .clickable { onAdvance("in_progress") }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("REOPEN TICKET", fontFamily = AppFonts.spaceMono,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = TechSilver.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TICKET PIPELINE VISUAL
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun TicketPipeline(currentStatus: String) {
    val steps = listOf(
        "new"         to "NEW",
        "in_progress" to "IN PROGRESS",
        "resolved"    to "RESOLVED"
    )
    val currentIdx = steps.indexOfFirst { it.first == currentStatus.lowercase() }

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, (key, label) ->
            val isDone    = idx <= currentIdx
            val isCurrent = idx == currentIdx
            val config    = ticketStatusConfig(key)

            Column(
                modifier            = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier         = Modifier
                        .size(if (isCurrent) 32.dp else 24.dp)
                        .background(
                            if (isDone) config.color.copy(alpha = 0.15f) else DimBorder
                        )
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isDone) config.color else DimBorder
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(config.icon, null, tint = config.color,
                            modifier = Modifier.size(if (isCurrent) 16.dp else 12.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, fontFamily = AppFonts.spaceMono, fontSize = 7.sp,
                    color = if (isDone) config.color else TechSilver.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center)
            }

            if (idx < steps.size - 1) {
                Box(
                    modifier = Modifier.weight(0.3f).height(1.dp)
                        .background(if (idx < currentIdx) CyberAcid.copy(0.4f) else DimBorder)
                )
            }
        }
    }
}

// ── Helper composables ─────────────────────────────────────────────
@Composable
private fun TicketMetaRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = TechSilver.copy(alpha = 0.6f), letterSpacing = 1.sp)
        Text(value, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = TeslaWhite,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
}

@Composable
private fun TicketActionButton(
    label: String, subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.4f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(36.dp).background(color.copy(0.15f))
            .border(1.dp, color.copy(0.3f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                color = TechSilver.copy(alpha = 0.6f), lineHeight = 14.sp)
        }
        Icon(Icons.Default.ArrowForward, null, tint = color.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp))
    }
}
