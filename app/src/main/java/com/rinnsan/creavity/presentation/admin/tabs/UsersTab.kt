package com.rinnsan.creavity.presentation.admin.tabs

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.presentation.admin.*
import com.rinnsan.creavity.domain.model.*

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ROOT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun UsersTab(viewModel: AdminViewModel) {
    val users by viewModel.users.collectAsState()

    var searchQuery    by remember { mutableStateOf("") }
    var filterRole     by remember { mutableStateOf("ALL") }   // ALL | admin | user
    var filterStatus   by remember { mutableStateOf("ALL") }   // ALL | active | banned
    var viewingUser    by remember { mutableStateOf<UserItem?>(null) }
    var actionDialog   by remember { mutableStateOf<Pair<UserItem, String>?>(null) } // user + action

    val filtered = users.filter { user ->
        val matchSearch = searchQuery.isEmpty() ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.uid.contains(searchQuery, ignoreCase = true)
        val matchRole      = filterRole == "ALL"      || user.role.equals(filterRole, true)
        val matchStatus    = filterStatus == "ALL"    || user.status.equals(filterStatus, true)
        matchSearch && matchRole && matchStatus
    }

    // Stats
    val adminCount   = users.count { it.role.equals("admin", true) }
    val bannedCount  = users.count { it.status.equals("banned", true) }

    Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Header ─────────────────────────────────────────────
            item {
                AdminSectionLabel(text = "USER ACCOUNTS")
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Stat cards ─────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("TOTAL\nUSERS",  users.size.toString(),  TeslaWhite,        Modifier.weight(1f))
                    StatCard("ADMINS",         adminCount.toString(),  CyberAcid,         Modifier.weight(1f))
                    StatCard("BANNED",         bannedCount.toString(), AdminRed,          Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ── Search bar ─────────────────────────────────────────
            item {
                UserSearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ── Filter chips ───────────────────────────────────────
            item {
                // Role filter
                FilterChipRow(
                    label   = "ROLE",
                    options = listOf("ALL", "admin", "user"),
                    selected = filterRole,
                    onSelect = { filterRole = it }
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Status filter
                FilterChipRow(
                    label    = "STATUS",
                    options  = listOf("ALL", "active", "banned"),
                    selected = filterStatus,
                    onSelect = { filterStatus = it },
                    getColor = { opt ->
                        when (opt) {
                            "active" -> Color(0xFF00FF88)
                            "banned" -> AdminRed
                            else     -> TechSilver
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("${filtered.size} USER${if (filtered.size != 1) "S" else ""}",
                    fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── User list ──────────────────────────────────────────
            if (filtered.isEmpty()) {
                item { EmptyState("NO USERS FOUND") }
            } else {
                items(filtered, key = { it.uid }) { user ->
                    UserCard(
                        user      = user,
                        onView    = { viewingUser = user },
                        onAction  = { action -> actionDialog = Pair(user, action) }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // ── User detail dialog ─────────────────────────────────────
        viewingUser?.let { user ->
            UserDetailDialog(
                user      = user,
                onDismiss = { viewingUser = null },
                onAction  = { action -> actionDialog = Pair(user, action); viewingUser = null }
            )
        }

        // ── Action confirm dialog ──────────────────────────────────
        actionDialog?.let { (user, action) ->
            UserActionDialog(
                user      = user,
                action    = action,
                onDismiss = { actionDialog = null },
                onConfirm = {
                    when (action) {
                        "ban"         -> viewModel.updateUserStatus(user.uid, "banned")
                        "unban"       -> viewModel.updateUserStatus(user.uid, "active")
                        "make_admin"  -> viewModel.updateUserRole(user.uid, "admin")
                        "make_user"   -> viewModel.updateUserRole(user.uid, "user")
                    }
                    actionDialog = null
                }
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ARCHETYPE DISTRIBUTION BAR
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun ArchetypeDistributionBar(distribution: Map<String, Int>, total: Int) {
    if (total == 0) return

    val archetypeColors = mapOf(
        "GHOST"    to Color(0xFF9B9BFF),
        "OPERATOR" to CyberAcid,
        "GLITCH"   to AdminRed,
        "NOMAD"    to Color(0xFFFF8C00),
        "UNKNOWN"  to TechSilver
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
            .padding(16.dp)
    ) {
        Text("IDENTITY DISTRIBUTION", fontFamily = AppFonts.spaceMono,
            fontSize = 9.sp, color = TechSilver, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(12.dp))

        // Stacked bar
        Row(
            modifier = Modifier.fillMaxWidth().height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            archetypeColors.forEach { (archetype, color) ->
                val count = distribution[archetype] ?: 0
                val frac  = count.toFloat() / total.toFloat()
                if (frac > 0f) {
                    val animFrac by animateFloatAsState(
                        targetValue   = frac,
                        animationSpec = tween(800, easing = FastOutSlowInEasing),
                        label         = "arch_$archetype"
                    )
                    Box(modifier = Modifier.weight(animFrac.coerceAtLeast(0.01f))
                        .fillMaxHeight().background(color))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            archetypeColors.forEach { (archetype, color) ->
                val count = distribution[archetype] ?: 0
                if (count > 0) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
                        Text("$archetype: $count",
                            fontFamily = AppFonts.spaceMono, fontSize = 8.sp, color = TechSilver)
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SEARCH BAR
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun UserSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder).background(ScanlineGray)) {
        TextField(
            value = query, onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = {
                Text("SEARCH EMAIL / USER ID...", fontFamily = AppFonts.spaceMono,
                    fontSize = 11.sp, color = TechSilver.copy(alpha = 0.4f))
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TechSilver,
                modifier = Modifier.size(16.dp)) },
            trailingIcon = if (query.isNotEmpty()) {{
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, null, tint = TechSilver,
                        modifier = Modifier.size(14.dp))
                }
            }} else null,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ScanlineGray, unfocusedContainerColor = ScanlineGray,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                cursorColor = CyberAcid)
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// FILTER CHIP ROW
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun FilterChipRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    getColor: (String) -> Color = { CyberAcid }
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
            color = TechSilver.copy(alpha = 0.5f), letterSpacing = 1.sp,
            modifier = Modifier.width(48.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { opt ->
                val isSel  = selected == opt
                val color  = if (opt == "ALL") CyberAcid else getColor(opt)
                Box(
                    modifier = Modifier
                        .border(1.dp, if (isSel) color else DimBorder)
                        .background(if (isSel) color.copy(alpha = 0.12f) else Color.Transparent)
                        .clickable { onSelect(opt) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(opt.uppercase(), fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) color else TechSilver.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// USER CARD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun UserCard(
    user: UserItem,
    onView: () -> Unit,
    onAction: (String) -> Unit
) {
    val isAdmin  = user.role.equals("admin", true)
    val isBanned = user.status.equals("banned", true)

    val avatarColor    = if (isAdmin) CyberAcid else TechSilver
    val statusColor    = if (isBanned) AdminRed else Color(0xFF00FF88)
    val borderColor    = when {
        isBanned -> AdminRed.copy(alpha = 0.3f)
        isAdmin  -> CyberAcid.copy(alpha = 0.3f)
        else     -> DimBorder
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor)
            .background(if (isBanned) AdminRed.copy(alpha = 0.04f) else ScanlineGray)
            .clickable { onView() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Avatar circle ──────────────────────────────────────────
        Box(
            modifier         = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(avatarColor.copy(alpha = 0.3f), DimBorder)
                    )
                )
                .border(1.dp, avatarColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = user.username.take(2).uppercase(),
                fontFamily = AppFonts.oswald,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Black,
                color      = avatarColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ── Info ───────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text          = user.username.uppercase(),
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = if (isBanned) TechSilver.copy(alpha = 0.5f) else TeslaWhite,
                    maxLines      = 1,
                    overflow      = TextOverflow.Ellipsis,
                    modifier      = Modifier.weight(1f)
                )
            }
            Text(
                text          = user.email,
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 9.sp,
                color         = TechSilver,
                maxLines      = 1,
                overflow      = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Role badge
                Box(
                    modifier = Modifier
                        .background(if (isAdmin) CyberAcid.copy(0.15f) else Color.Transparent)
                        .border(1.dp, if (isAdmin) CyberAcid.copy(0.5f) else DimBorder)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(user.role.uppercase(), fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdmin) CyberAcid else TechSilver)
                }


                // Status badge
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(user.status.uppercase(), fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                        fontWeight = FontWeight.Bold, color = statusColor)
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // ── Quick actions ──────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Wishlist count
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(Icons.Default.Favorite, null, tint = CyberAcid.copy(alpha = 0.5f),
                    modifier = Modifier.size(10.dp))
                Text("${user.wishlistCount}", fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp, color = TechSilver)
            }

            // Arrow
            Icon(Icons.Default.ArrowForward, null, tint = TechSilver.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// USER DETAIL DIALOG — full profile view
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun UserDetailDialog(
    user: UserItem,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    val isAdmin  = user.role.equals("admin", true)
    val isBanned = user.status.equals("banned", true)
    val arcColor = if (isAdmin) CyberAcid else TechSilver

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, arcColor.copy(alpha = 0.3f))
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero header ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to arcColor.copy(alpha = 0.15f),
                            1f to VoidBlack
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    // Close button
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("// AGENT FILE", fontFamily = AppFonts.spaceMono,
                            fontSize = 10.sp, color = arcColor, letterSpacing = 1.sp)
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, null, tint = TechSilver,
                                modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big avatar
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier         = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(
                                    listOf(arcColor.copy(alpha = 0.4f), DimBorder)))
                                .border(2.dp, arcColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.username.take(2).uppercase(), fontFamily = AppFonts.oswald,
                                fontSize = 24.sp, fontWeight = FontWeight.Black, color = arcColor)
                        }

                        Column {
                            Text(user.username.uppercase(), fontFamily = AppFonts.spaceMono, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, color = TeslaWhite,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(user.email, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                color = TechSilver, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                RolePill(user.role)
                                StatusPill(user.status)
                            }
                        }
                    }
                }
            }

            // ── Data rows ──────────────────────────────────────────
            Column(modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)) {

                UserDataRow("USER ID",    user.uid)
                UserDataRow("USERNAME",   user.username)
                UserDataRow("EMAIL",      user.email)
                UserDataRow("ROLE",       user.role.uppercase())
                UserDataRow("STATUS",     user.status.uppercase())

                UserDataRow("WISHLIST",   "${user.wishlistCount} items saved")

                Spacer(modifier = Modifier.height(20.dp))

                // ── Action buttons grid ────────────────────────────
                Text("ACTIONS", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                    color = TechSilver, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // Ban / Unban
                if (!isBanned) {
                    ActionButton(
                        label    = "BAN USER",
                        subtitle = "Khóa tài khoản, ngăn đăng nhập",
                        icon     = Icons.Default.Block,
                        color    = AdminRed,
                        onClick  = { onAction("ban") }
                    )
                } else {
                    ActionButton(
                        label    = "RESTORE USER",
                        subtitle = "Mở khóa tài khoản",
                        icon     = Icons.Default.LockOpen,
                        color    = Color(0xFF00FF88),
                        onClick  = { onAction("unban") }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Promote / Demote
                if (!isAdmin) {
                    ActionButton(
                        label    = "PROMOTE TO ADMIN",
                        subtitle = "Cấp quyền quản trị viên",
                        icon     = Icons.Default.AdminPanelSettings,
                        color    = CyberAcid,
                        onClick  = { onAction("make_admin") }
                    )
                } else {
                    ActionButton(
                        label    = "REVOKE ADMIN",
                        subtitle = "Thu hồi quyền admin, về user thường",
                        icon     = Icons.Default.PersonOff,
                        color    = Color(0xFFFF8C00),
                        onClick  = { onAction("make_user") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ACTION CONFIRM DIALOG
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun UserActionDialog(
    user: UserItem,
    action: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val (title, desc, color, confirmLabel) = when (action) {
        "ban"        -> ActionConfig("BAN USER",           "Tài khoản sẽ bị khóa. User không thể đăng nhập.",       AdminRed,          "BAN")
        "unban"      -> ActionConfig("RESTORE USER",       "Mở khóa tài khoản. User có thể đăng nhập lại.",          Color(0xFF00FF88),  "RESTORE")
        "make_admin" -> ActionConfig("PROMOTE TO ADMIN",   "Cấp quyền Admin. User có thể vào Command Center.",       CyberAcid,         "PROMOTE")
        "make_user"  -> ActionConfig("REVOKE ADMIN",       "Thu hồi quyền Admin. Trở về tài khoản người dùng.",      Color(0xFFFF8C00),  "REVOKE")
        else         -> ActionConfig("CONFIRM ACTION",      "Xác nhận hành động này.",                                TechSilver,        "CONFIRM")
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, color.copy(alpha = 0.4f)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon + Title
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(40.dp).background(color.copy(0.1f))
                    .border(1.dp, color.copy(0.4f)), contentAlignment = Alignment.Center) {
                    Text("!", fontFamily = AppFonts.oswald, fontSize = 20.sp,
                        fontWeight = FontWeight.Black, color = color)
                }
                Column {
                    Text(title, fontFamily = AppFonts.oswald, fontSize = 18.sp,
                        fontWeight = FontWeight.Black, color = color)
                    Text(user.email, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        color = TechSilver, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))

            Text(desc, fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                color = TechSilver, lineHeight = 18.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                    .clickable { onDismiss() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("CANCEL", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TechSilver)
                }
                Box(modifier = Modifier.weight(1f).background(color)
                    .clickable { onConfirm() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text(confirmLabel, fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (action == "unban" || action == "make_admin") VoidBlack else TeslaWhite)
                }
            }
        }
    }
}

data class ActionConfig(val title: String, val desc: String, val color: Color, val confirmLabel: String)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SHARED SMALL COMPONENTS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun UserDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = TechSilver.copy(alpha = 0.6f), letterSpacing = 1.sp)
        Text(value, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = TeslaWhite,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
}

@Composable
private fun ActionButton(
    label: String, subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color, onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.3f))
            .background(color.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
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
                color = TechSilver.copy(alpha = 0.6f))
        }
        Icon(Icons.Default.ArrowForward, null, tint = color.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun RolePill(role: String) {
    val isAdmin = role.equals("admin", true)
    Box(modifier = Modifier
        .background(if (isAdmin) CyberAcid.copy(0.15f) else Color.Transparent)
        .border(1.dp, if (isAdmin) CyberAcid.copy(0.5f) else DimBorder)
        .padding(horizontal = 7.dp, vertical = 3.dp)) {
        Text(role.uppercase(), fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAdmin) CyberAcid else TechSilver)
    }
}

@Composable
private fun StatusPill(status: String) {
    val isBanned = status.equals("banned", true)
    val color    = if (isBanned) AdminRed else Color(0xFF00FF88)
    Box(modifier = Modifier.background(color.copy(0.1f))
        .border(1.dp, color.copy(0.4f)).padding(horizontal = 7.dp, vertical = 3.dp)) {
        Text(status.uppercase(), fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
            fontWeight = FontWeight.Bold, color = color)
    }
}

fun archetypeColor(archetype: String): Color = when (archetype.uppercase()) {
    "GHOST"    -> Color(0xFF9B9BFF)
    "OPERATOR" -> Color(0xFFCCFF00)
    "GLITCH"   -> Color(0xFFFF003C)
    "NOMAD"    -> Color(0xFFFF8C00)
    else       -> Color(0xFF888888)
}
