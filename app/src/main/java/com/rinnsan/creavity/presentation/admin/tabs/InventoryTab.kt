package com.rinnsan.creavity.presentation.admin.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.presentation.admin.*
import com.rinnsan.creavity.domain.model.*

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ROOT — 2 sub-tabs
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun InventoryTab(viewModel: AdminViewModel) {
    val artifacts by viewModel.artifacts.collectAsState()

    var subTab by remember { mutableIntStateOf(0) }

    val affiliateItems  = artifacts.filter { !it.isDirectSale }
    val directSaleItems = artifacts.filter {  it.isDirectSale }

    Column(modifier = Modifier.fillMaxSize().background(VoidBlack)) {

        // ── Sub-tab bar ────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            AdminSectionLabel(text = "INVENTORY MANAGEMENT")
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // AFFILIATE sub-tab
                SubTabButton(
                    label    = "AFFILIATE",
                    count    = affiliateItems.size,
                    selected = subTab == 0,
                    color    = CyberAcid,
                    modifier = Modifier.weight(1f),
                    onClick  = { subTab = 0 }
                )
                // DIRECT SALE sub-tab
                SubTabButton(
                    label    = "DIRECT SALE",
                    count    = directSaleItems.size,
                    selected = subTab == 1,
                    color    = Color(0xFFFF8C00),
                    modifier = Modifier.weight(1f),
                    onClick  = { subTab = 1 }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-tab description
            Text(
                text = if (subTab == 0)
                    "Sản phẩm dẫn link ngoài • Commission rate • Click tracking"
                else
                    "Sản phẩm bán trong app • Tồn kho • Giá nội bộ",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 9.sp,
                color      = TechSilver.copy(alpha = 0.6f)
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))

        // ── Content ────────────────────────────────────────────────
        if (subTab == 0) {
            AffiliateInventory(
                items     = affiliateItems,
                viewModel = viewModel
            )
        } else {
            DirectSaleInventory(
                items     = directSaleItems,
                viewModel = viewModel
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SUB-TAB 1 — AFFILIATE
// Quản lý sản phẩm link ngoài: commission rate, click stats, link
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun AffiliateInventory(
    items: List<ArtifactItem>,
    viewModel: AdminViewModel
) {
    val artifactStats by viewModel.artifactStats.collectAsState()
    val registeredBrands by viewModel.registeredBrands.collectAsState()
    val statMap = artifactStats.associateBy { it.id }
    val brandNames = registeredBrands

    var searchQuery     by remember { mutableStateOf("") }
    var showAddDialog   by remember { mutableStateOf(false) }
    var editingItem     by remember { mutableStateOf<ArtifactItem?>(null) }
    var deletingItem    by remember { mutableStateOf<ArtifactItem?>(null) }

    val filtered = items.filter {
        searchQuery.isEmpty() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.vendor.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // ── Affiliate stats summary ────────────────────────────
            item {
                val totalClicks      = artifactStats.sumOf { it.clicks }
                val totalCommission  = artifactStats.sumOf { it.commission }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("AFFILIATE\nPRODUCTS", items.size.toString(), CyberAcid, Modifier.weight(1f))
                    StatCard("TOTAL\nCLICKS",      totalClicks.toString(), CyberAcid, Modifier.weight(1f))
                    StatCard("TOTAL\nEARNED",      formatVnd(totalCommission) + "đ", CyberAcid, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Search + Add ───────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    InventorySearchBar(
                        query         = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier      = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(CyberAcid)
                            .clickable { showAddDialog = true }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = VoidBlack,
                            modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${filtered.size} ITEMS", fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp, color = TechSilver)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── List ───────────────────────────────────────────────
            if (filtered.isEmpty()) {
                item { EmptyState("NO AFFILIATE PRODUCTS") }
            } else {
                items(filtered) { item ->
                    val stat = statMap[item.id]
                    AffiliateItemCard(
                        item    = item,
                        clicks  = stat?.clicks ?: 0L,
                        earned  = stat?.commission ?: 0L,
                        onEdit   = { editingItem = item },
                        onDelete = { deletingItem = item }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Dialogs
        if (showAddDialog) {
            AddAffiliateDialog(
                brands = brandNames,
                onDismiss = { showAddDialog = false },
                onConfirm = { data -> viewModel.addArtifact(data); showAddDialog = false }
            )
        }
        editingItem?.let { item ->
            EditAffiliateDialog(
                item      = item,
                onDismiss = { editingItem = null },
                onConfirm = { price, rate ->
                    viewModel.updateArtifact(item.docId, price, 0L, rate, false, 0L)
                    editingItem = null
                }
            )
        }
        deletingItem?.let { item ->
            DeleteConfirmDialog(title = item.title,
                onDismiss = { deletingItem = null },
                onConfirm = { viewModel.deleteArtifact(item.docId); deletingItem = null })
        }
    }
}

@Composable
private fun AffiliateItemCard(
    item: ArtifactItem,
    clicks: Long,
    earned: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(modifier = Modifier.size(56.dp).border(1.dp, DimBorder)) {
                AsyncImage(model = item.imageUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.id, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = CyberAcid.copy(alpha = 0.7f))
                    Text(item.archetype, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = TechSilver.copy(alpha = 0.6f))
                }
                Text(item.title, fontFamily = AppFonts.oswald, fontSize = 14.sp,
                    fontWeight = FontWeight.Black, color = TeslaWhite,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Commission rate
                    Text("${(item.commissionRate * 100).toInt()}% RATE",
                        fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, color = CyberAcid)
                    // Click stats
                    Text("$clicks CLICKS", fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = TechSilver)
                    // Earned
                    if (earned > 0) {
                        Text("+${formatVnd(earned)}đ", fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp, color = CyberAcid.copy(alpha = 0.8f))
                    }
                }
            }

            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, tint = TechSilver, modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(
                modifier = Modifier.fillMaxWidth().background(VoidBlack).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Vendor + Price
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    InfoChip("VENDOR", item.vendor)
                    InfoChip("PRICE",  item.price)
                }
                // Affiliate link
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Link, null, tint = TechSilver.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp))
                    Text(item.affiliateLink, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                        color = TechSilver.copy(alpha = 0.4f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
                // Actions
                ItemActionRow(onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SUB-TAB 2 — DIRECT SALE
// Quản lý sản phẩm bán trong app: stock, internal price, status
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun DirectSaleInventory(
    items: List<ArtifactItem>,
    viewModel: AdminViewModel
) {
    var searchQuery  by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem  by remember { mutableStateOf<ArtifactItem?>(null) }
    var deletingItem by remember { mutableStateOf<ArtifactItem?>(null) }

    val filtered = items.filter {
        searchQuery.isEmpty() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.vendor.contains(searchQuery, ignoreCase = true)
    }

    // Stock summary
    val inStock  = items.count { it.stock > 5 }
    val lowStock = items.count { it.stock in 1..5 }
    val outStock = items.count { it.stock == 0L }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // ── Stock summary ──────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("IN\nSTOCK",  inStock.toString(),  Color(0xFF00FF88), Modifier.weight(1f))
                    StatCard("LOW\nSTOCK", lowStock.toString(), Color(0xFFFF8C00), Modifier.weight(1f))
                    StatCard("OUT OF\nSTOCK", outStock.toString(), AdminRed,      Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Search + Add ───────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    InventorySearchBar(
                        query         = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier      = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF8C00))
                            .clickable { showAddDialog = true }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = VoidBlack,
                            modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${filtered.size} ITEMS", fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp, color = TechSilver)
                Spacer(modifier = Modifier.height(8.dp))

                // Nút RESEED RINNSAN — update sizeStock cho toàn bộ sản phẩm cũ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFF8C00).copy(alpha = 0.5f))
                        .background(Color(0xFFFF8C00).copy(alpha = 0.06f))
                        .clickable { viewModel.runRinnsanSeeder() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Refresh, null, tint = Color(0xFFFF8C00),
                            modifier = Modifier.size(13.dp))
                        Text("RESEED RINNSAN — UPDATE SIZESTOCK",
                            fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFFFF8C00))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── List ───────────────────────────────────────────────
            if (filtered.isEmpty()) {
                item { EmptyState("NO DIRECT SALE PRODUCTS") }
            } else {
                items(filtered) { item ->
                    DirectSaleItemCard(
                        item     = item,
                        onEdit   = { editingItem = item },
                        onDelete = { deletingItem = item },
                        onAdjustStock = { delta, size ->
                            if (size.isBlank()) {
                                // Không có size → adjust tổng
                                val newStock = (item.stock + delta).coerceAtLeast(0L)
                                viewModel.updateArtifact(item.docId, item.price,
                                    newStock, item.commissionRate, true, item.internalPrice)
                            } else {
                                // Có size → adjust sizeStock map rồi tính lại tổng
                                val newSizeStock = item.sizeStock.toMutableMap()
                                val cur = newSizeStock[size] ?: 0L
                                newSizeStock[size] = (cur + delta).coerceAtLeast(0L)
                                val newTotal = newSizeStock.values.sum()
                                viewModel.updateSizeStock(item.docId, newSizeStock, newTotal)
                            }
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Dialogs
        if (showAddDialog) {
            AddDirectSaleDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { data, uri -> viewModel.addArtifact(data, uri); showAddDialog = false }
            )
        }
        editingItem?.let { item ->
            EditDirectSaleDialog(
                item      = item,
                onDismiss = { editingItem = null },
                onConfirm = { price, stock, internalPrice, editedSizeStock ->
                    // FIX: Nếu có sizes → phải gọi updateSizeStock để lưu sizeStock map
                    // (updateArtifact chỉ ghi tổng stock, không ghi sizeStock per-size)
                    if (item.sizes.isNotEmpty() && editedSizeStock != null) {
                        viewModel.updateSizeStock(item.docId, editedSizeStock, stock)
                    }
                    // Luôn update price + internalPrice
                    viewModel.updateArtifact(item.docId, price, stock,
                        item.commissionRate, true, internalPrice)
                    editingItem = null
                }
            )
        }
        deletingItem?.let { item ->
            DeleteConfirmDialog(title = item.title,
                onDismiss = { deletingItem = null },
                onConfirm = { viewModel.deleteArtifact(item.docId); deletingItem = null })
        }
    }
}

@Composable
private fun DirectSaleItemCard(
    item: ArtifactItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: (delta: Long, size: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Stock status
    val (stockColor, stockLabel) = when {
        item.stock == 0L   -> AdminRed           to "OUT OF STOCK"
        item.stock <= 5L   -> Color(0xFFFF8C00)  to "LOW STOCK"
        else               -> Color(0xFF00FF88)  to "IN STOCK"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(modifier = Modifier.size(56.dp).border(1.dp, DimBorder)) {
                AsyncImage(model = item.imageUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                // Direct badge
                Box(modifier = Modifier.align(Alignment.TopStart)
                    .background(Color(0xFFFF8C00))
                    .padding(horizontal = 3.dp, vertical = 1.dp)) {
                    Text("DIRECT", fontFamily = AppFonts.spaceMono,
                        fontSize = 6.sp, fontWeight = FontWeight.Bold, color = VoidBlack)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontFamily = AppFonts.oswald, fontSize = 14.sp,
                    fontWeight = FontWeight.Black, color = TeslaWhite,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.vendor, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                    color = TechSilver)

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Stock badge
                    Box(modifier = Modifier
                        .border(1.dp, stockColor.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("${item.stock} • $stockLabel",
                            fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, color = stockColor)
                    }
                    // Price
                    if (item.internalPrice > 0) {
                        Text(formatVnd(item.internalPrice) + "đ",
                            fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                            color = Color(0xFFFF8C00))
                    }
                }
            }

            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, tint = TechSilver, modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(
                modifier = Modifier.fillMaxWidth().background(VoidBlack).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (item.sizes.isNotEmpty()) {
                    // ── Stock per size breakdown ───────────────────
                    Text("STOCK BY SIZE", fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp)

                    item.sizes.forEach { size ->
                        val sizeQty = item.sizeStock[size] ?: 0L
                        val sizeColor = when {
                            sizeQty == 0L -> AdminRed
                            sizeQty <= 3L -> Color(0xFFFF8C00)
                            else          -> Color(0xFF00FF88)
                        }
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier
                                .border(1.dp, sizeColor.copy(alpha = 0.4f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)) {
                                Text(size, fontFamily = AppFonts.oswald, fontSize = 15.sp,
                                    fontWeight = FontWeight.Black, color = sizeColor)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StockButton("-1", Color(0xFFFF8C00)) { onAdjustStock(-1L, size) }
                                Box(modifier = Modifier
                                    .border(1.dp, DimBorder)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)) {
                                    Text("$sizeQty", fontFamily = AppFonts.oswald, fontSize = 16.sp,
                                        fontWeight = FontWeight.Black, color = sizeColor)
                                }
                                StockButton("+1", Color(0xFF00CC66)) { onAdjustStock(+1L, size) }
                                Spacer(modifier = Modifier.width(4.dp))
                                StockButton("+5", Color(0xFF00FF88)) { onAdjustStock(+5L, size) }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                            color = TechSilver)
                        Text("${item.stock}", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = stockColor)
                    }
                } else {
                    // ── No size — adjust tổng như cũ ──────────────
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("ADJUST STOCK", fontFamily = AppFonts.spaceMono,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TeslaWhite)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StockButton("-5", Color(0xFFFF4444)) { onAdjustStock(-5L, "") }
                            StockButton("-1", Color(0xFFFF8C00)) { onAdjustStock(-1L, "") }
                            Box(modifier = Modifier
                                .border(1.dp, DimBorder)
                                .padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text("${item.stock}", fontFamily = AppFonts.oswald, fontSize = 16.sp,
                                    fontWeight = FontWeight.Black, color = stockColor)
                            }
                            StockButton("+1", Color(0xFF00CC66)) { onAdjustStock(+1L, "") }
                            StockButton("+5", Color(0xFF00FF88)) { onAdjustStock(+5L, "") }
                        }
                    }
                }

                // Info row
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    InfoChip("CATEGORY",     item.category)
                    InfoChip("ARCHETYPE",    item.archetype)
                    InfoChip("LISTED PRICE", item.price)
                }

                ItemActionRow(onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun StockButton(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.3f))
            .background(color.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = color)
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DIALOGS — AFFILIATE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun AddAffiliateDialog(brands: List<String>, onDismiss: () -> Unit, onConfirm: (Map<String, Any>) -> Unit) {
    var id            by remember { mutableStateOf("") }
    var title         by remember { mutableStateOf("") }
    var vendor        by remember { mutableStateOf(brands.firstOrNull() ?: "") }
    var expanded      by remember { mutableStateOf(false) }
    var archetype     by remember { mutableStateOf("GHOST") }
    var category      by remember { mutableStateOf("") }
    var price         by remember { mutableStateOf("") }
    var imageUrl      by remember { mutableStateOf("") }
    var affiliateLink by remember { mutableStateOf("") }
    var commRate      by remember { mutableStateOf("8") }

    AdminDialog("ADD AFFILIATE PRODUCT", "New link-out product", onDismiss, onConfirm = {
        onConfirm(buildMap {
            put("id",            id.uppercase());       put("title",         title.uppercase())
            put("vendor",        vendor.uppercase());   put("archetype",     archetype)
            put("category",      category.uppercase()); put("price",         price)
            put("imageUrl",      imageUrl);             put("affiliateLink", affiliateLink)
            put("commissionRate",(commRate.toDoubleOrNull() ?: 8.0) / 100.0)
            put("isDirectSale",  false);                put("stock",         0L)
            put("internalPrice", 0L)
        })
    }) {
        ArchetypeChips(selected = archetype, onSelect = { archetype = it })
        DialogField("ARTIFACT ID (e.g. GH-010)", id)    { id = it }
        DialogField("TITLE", title)                      { title = it }

        Column {
            Text("VENDOR", fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder).background(Color(0xFF0D0D0D))) {
                OutlinedTextField(
                    value = vendor,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberAcid,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, null, tint = TechSilver)
                        }
                    }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    brands.forEach { b ->
                        DropdownMenuItem(text = { Text(b) }, onClick = { vendor = b; expanded = false })
                    }
                }
            }
        }

        DialogField("CATEGORY", category)                { category = it }
        DialogField("PRICE (e.g. 1.200.000 VND)", price) { price = it }
        DialogField("IMAGE URL", imageUrl)               { imageUrl = it }
        DialogField("AFFILIATE LINK (Shopee/Nike...)", affiliateLink) { affiliateLink = it }
        DialogField("COMMISSION RATE (%)", commRate, KeyboardType.Number) { commRate = it }
    }
}

@Composable
private fun EditAffiliateDialog(
    item: ArtifactItem,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var price by remember { mutableStateOf(item.price) }
    var rate  by remember { mutableStateOf((item.commissionRate * 100).toInt().toString()) }

    AdminDialog("EDIT AFFILIATE", item.title, onDismiss,
        onConfirm = { onConfirm(price, (rate.toDoubleOrNull() ?: 8.0) / 100.0) }) {
        DialogField("PRICE (VND)", price)                          { price = it }
        DialogField("COMMISSION RATE (%)", rate, KeyboardType.Number) { rate = it }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DIALOGS — DIRECT SALE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun AddDirectSaleDialog(onDismiss: () -> Unit, onConfirm: (Map<String, Any>, Uri?) -> Unit) {
    var id            by remember { mutableStateOf("") }
    var title         by remember { mutableStateOf("") }
    val vendor        = "RINNSAN LAB"
    var archetype     by remember { mutableStateOf("GHOST") }
    var category      by remember { mutableStateOf("") }
    var price         by remember { mutableStateOf("") }
    var imageUri      by remember { mutableStateOf<Uri?>(null) }
    var internalPrice by remember { mutableStateOf("0") }

    val availableSizes = listOf("S", "M", "L", "XL", "XXL")
    var selectedSizes  by remember { mutableStateOf(setOf<String>()) }
    var stockPerSize   by remember { mutableStateOf(mapOf<String, String>()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    AdminDialog("ADD DIRECT PRODUCT", "New in-app sale product", onDismiss, onConfirm = {
        val sizeStockMap = selectedSizes.associateWith { s ->
            stockPerSize[s]?.toLongOrNull() ?: 0L
        }
        val totalStock = if (sizeStockMap.isNotEmpty()) sizeStockMap.values.sum() else 0L
        onConfirm(buildMap {
            put("id",            id.uppercase());       put("title",        title.uppercase())
            put("vendor",        vendor.uppercase());   put("archetype",    archetype)
            put("category",      category.uppercase()); put("price",        price)
            put("imageUrl",      "");                   put("affiliateLink","")
            put("commissionRate",0.0);                  put("isDirectSale", true)
            put("stock",         totalStock)
            put("internalPrice", internalPrice.toLongOrNull() ?: 0L)
            put("sizes",         selectedSizes.toList())
            put("sizeStock",     sizeStockMap)
        }, imageUri)
    }) {
        ArchetypeChips(selected = archetype, onSelect = { archetype = it })
        DialogField("ARTIFACT ID (e.g. DS-001)", id)  { id = it }
        DialogField("TITLE", title)                    { title = it }

        Column {
            Text("VENDOR (LOCKED)", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                color = TechSilver, letterSpacing = 1.sp)
            Text(vendor, fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite,
                modifier = Modifier.padding(vertical = 8.dp))
        }

        DialogField("CATEGORY", category)                         { category = it }
        DialogField("DISPLAY PRICE (e.g. 1.200.000 VND)", price)  { price = it }

        Column {
            Text("IMAGE", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                color = TechSilver, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder)
                .clickable { launcher.launch("image/*") }.padding(12.dp),
                contentAlignment = Alignment.Center) {
                Text(if (imageUri != null) "IMAGE SELECTED (WILL UPLOAD)" else "SELECT FROM ALBUM",
                    fontFamily = AppFonts.spaceMono, fontSize = 11.sp, color = CyberAcid)
            }
        }

        // ── SIZES + STOCK PER SIZE ────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("SIZES & STOCK PER SIZE", fontFamily = AppFonts.spaceMono,
                fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp)
            Text("Bật size → nhập tồn kho cho từng size",
                fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                color = TechSilver.copy(alpha = 0.5f))

            // Toggle chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                availableSizes.forEach { size ->
                    val sel = selectedSizes.contains(size)
                    Box(modifier = Modifier
                        .border(1.dp, if (sel) CyberAcid else DimBorder)
                        .background(if (sel) CyberAcid.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable {
                            selectedSizes = if (sel) selectedSizes - size else selectedSizes + size
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(size, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sel) CyberAcid else TechSilver)
                    }
                }
            }

            // Stock input per selected size
            availableSizes.filter { selectedSizes.contains(it) }.forEach { size ->
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier
                        .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(size, fontFamily = AppFonts.oswald, fontSize = 16.sp,
                            fontWeight = FontWeight.Black, color = CyberAcid)
                    }
                    Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                        .background(Color(0xFF0D0D0D))) {
                        TextField(
                            value = stockPerSize[size] ?: "",
                            onValueChange = { v ->
                                stockPerSize = stockPerSize.toMutableMap().apply { put(size, v) }
                            },
                            modifier      = Modifier.fillMaxWidth(),
                            singleLine    = true,
                            placeholder   = {
                                Text("Số lượng", fontFamily = AppFonts.spaceMono,
                                    fontSize = 11.sp, color = TechSilver.copy(alpha = 0.4f))
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor   = Color(0xFF0D0D0D),
                                unfocusedContainerColor = Color(0xFF0D0D0D),
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor             = CyberAcid)
                        )
                    }
                }
            }

            if (selectedSizes.isNotEmpty()) {
                val total = selectedSizes.sumOf { stockPerSize[it]?.toLongOrNull() ?: 0L }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL STOCK", fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = TechSilver)
                    Text("$total", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = CyberAcid)
                }
            }
        }

        DialogField("INTERNAL PRICE (VND)", internalPrice, KeyboardType.Number) { internalPrice = it }
    }
}

@Composable
private fun EditDirectSaleDialog(
    item: ArtifactItem,
    onDismiss: () -> Unit,
    // FIX: Mở rộng callback để truyền sizeStock ra ngoài
    onConfirm: (price: String, stock: Long, internalPrice: Long, sizeStock: Map<String, Long>?) -> Unit
) {
    var price         by remember { mutableStateOf(item.price) }
    var internalPrice by remember { mutableStateOf(item.internalPrice.toString()) }
    // sizeStock editable — khởi tạo từ item hiện tại
    var editSizeStock by remember {
        mutableStateOf(item.sizeStock.mapValues { it.value.toString() })
    }

    val computedTotal = if (item.sizes.isNotEmpty()) {
        item.sizes.sumOf { editSizeStock[it]?.toLongOrNull() ?: 0L }
    } else null  // null = dùng field tổng thủ công

    var stockOverride by remember { mutableStateOf(item.stock.toString()) }

    AdminDialog("EDIT DIRECT SALE", item.title, onDismiss, onConfirm = {
        val finalStock = computedTotal ?: stockOverride.toLongOrNull() ?: 0L
        // FIX: Truyền editSizeStock (Long map) khi có sizes, null khi không có
        val sizeStockLong: Map<String, Long>? = if (item.sizes.isNotEmpty()) {
            item.sizes.associateWith { editSizeStock[it]?.toLongOrNull() ?: 0L }
        } else null
        onConfirm(price, finalStock, internalPrice.toLongOrNull() ?: 0L, sizeStockLong)
    }) {
        DialogField("DISPLAY PRICE", price) { price = it }
        DialogField("INTERNAL PRICE (VND)", internalPrice, KeyboardType.Number) { internalPrice = it }

        if (item.sizes.isNotEmpty()) {
            // ── Stock per size editor ─────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("STOCK BY SIZE", fontFamily = AppFonts.spaceMono,
                    fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp)

                item.sizes.forEach { size ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier
                            .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(size, fontFamily = AppFonts.oswald, fontSize = 16.sp,
                                fontWeight = FontWeight.Black, color = CyberAcid)
                        }
                        Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                            .background(Color(0xFF0D0D0D))) {
                            TextField(
                                value = editSizeStock[size] ?: "0",
                                onValueChange = { v ->
                                    editSizeStock = editSizeStock.toMutableMap().apply { put(size, v) }
                                },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor   = Color(0xFF0D0D0D),
                                    unfocusedContainerColor = Color(0xFF0D0D0D),
                                    focusedIndicatorColor   = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor             = CyberAcid)
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL", fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = TechSilver)
                    Text("${computedTotal ?: 0L}", fontFamily = AppFonts.spaceMono,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberAcid)
                }
            }
        } else {
            // Không có size → chỉnh tổng thủ công
            DialogField("STOCK", stockOverride, KeyboardType.Number) { stockOverride = it }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SHARED COMPONENTS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun SubTabButton(
    label: String, count: Int, selected: Boolean,
    color: Color, modifier: Modifier, onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .border(1.dp, if (selected) color else DimBorder)
            .background(if (selected) color.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = if (selected) color else TechSilver,
            letterSpacing = 1.sp)
        Text("$count items", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = if (selected) color.copy(alpha = 0.7f) else TechSilver.copy(alpha = 0.4f))
    }
}

@Composable
private fun InventorySearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier) {
    Box(modifier = modifier.border(1.dp, DimBorder).background(ScanlineGray)) {
        TextField(value = query, onValueChange = onQueryChange,
            modifier    = Modifier.fillMaxWidth(), singleLine = true,
            placeholder = {
                Text("SEARCH...", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                    color = TechSilver.copy(alpha = 0.4f))
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TechSilver,
                modifier = Modifier.size(16.dp)) },
            trailingIcon = if (query.isNotEmpty()) {{
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, null, tint = TechSilver, modifier = Modifier.size(14.dp))
                }
            }} else null,
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = ScanlineGray, unfocusedContainerColor = ScanlineGray,
                focusedIndicatorColor   = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                cursorColor             = CyberAcid
            )
        )
    }
}

@Composable
private fun ItemActionRow(onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f).border(1.dp, CyberAcid)
            .clickable { onEdit() }.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Edit, null, tint = CyberAcid, modifier = Modifier.size(13.dp))
                Text("EDIT", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, color = CyberAcid)
            }
        }
        Box(modifier = Modifier.weight(1f).border(1.dp, AdminRed.copy(alpha = 0.5f))
            .background(AdminRed.copy(alpha = 0.05f)).clickable { onDelete() }.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Delete, null, tint = AdminRed, modifier = Modifier.size(13.dp))
                Text("DELETE", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, color = AdminRed)
            }
        }
    }
}

@Composable
private fun ArchetypeChips(selected: String, onSelect: (String) -> Unit) {
    val archetypes = listOf("GHOST","OPERATOR","GLITCH","NOMAD")
    Column {
        Text("ARCHETYPE", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = TechSilver, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            archetypes.forEach { type ->
                val sel = selected == type
                Box(modifier = Modifier
                    .border(1.dp, if (sel) CyberAcid else DimBorder)
                    .background(if (sel) CyberAcid.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onSelect(type) }
                    .padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text(type, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                        color = if (sel) CyberAcid else TechSilver)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
            color = TechSilver.copy(alpha = 0.6f), letterSpacing = 1.sp)
        Text(value, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = TeslaWhite,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DeleteConfirmDialog(title: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().background(VoidBlack)
            .border(1.dp, AdminRed).padding(24.dp)) {
            Text("DELETE ARTIFACT", fontFamily = AppFonts.oswald, fontSize = 20.sp,
                fontWeight = FontWeight.Black, color = AdminRed)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Xác nhận xóa:\n\"$title\"", fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp, color = TechSilver, lineHeight = 18.sp)
            Text("Không thể hoàn tác.", fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp, color = AdminRed.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                    .clickable { onDismiss() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("CANCEL", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TechSilver)
                }
                Box(modifier = Modifier.weight(1f).background(AdminRed)
                    .clickable { onConfirm() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("DELETE", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TeslaWhite)
                }
            }
        }
    }
}

@Composable
private fun AdminDialog(
    title: String, subtitle: String,
    onDismiss: () -> Unit, onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, DimBorder)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("// $title", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                color = CyberAcid, letterSpacing = 1.sp)
            Text(subtitle, fontFamily = AppFonts.oswald, fontSize = 18.sp,
                fontWeight = FontWeight.Black, color = TeslaWhite)
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
            content()
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                    .clickable { onDismiss() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("CANCEL", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TechSilver)
                }
                Box(modifier = Modifier.weight(1f).background(CyberAcid)
                    .clickable { onConfirm() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("CONFIRM", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = VoidBlack)
                }
            }
        }
    }
}

@Composable
private fun DialogField(
    label: String, value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = TechSilver, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder)
            .background(Color(0xFF0D0D0D))) {
            TextField(value = value, onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = AppFonts.spaceMono, fontSize = 12.sp, color = TeslaWhite),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color(0xFF0D0D0D),
                    unfocusedContainerColor = Color(0xFF0D0D0D),
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = CyberAcid))
        }
    }
}
