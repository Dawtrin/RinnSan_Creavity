package com.rinnsan.creavity.presentation.admin.tabs

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.presentation.admin.*
import com.rinnsan.creavity.domain.model.*
import kotlinx.coroutines.tasks.await

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// LOCAL MODEL — Brand full data
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
data class BrandDetail(
    val docId: String,       // Firestore document ID (= vendor key, e.g. "NIKE")
    val name: String,
    val rate: Double,        // commission rate 0.0 - 1.0
    val logoUrl: String,
    val websiteUrl: String,
    val description: String,
    // Stats từ stats/by_brand
    val clicks: Long,
    val commission: Long
)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ROOT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun BrandsAdminTab(viewModel: AdminViewModel) {
    val brandStats by viewModel.brandStats.collectAsState()

    // Load full brand details từ Firestore brands/
    var brands       by remember { mutableStateOf<List<BrandDetail>>(emptyList()) }
    var isLoading    by remember { mutableStateOf(true) }
    var showAddDialog  by remember { mutableStateOf(false) }
    var editingBrand   by remember { mutableStateOf<BrandDetail?>(null) }
    var deletingBrand  by remember { mutableStateOf<BrandDetail?>(null) }
    var adjustingBrand by remember { mutableStateOf<BrandDetail?>(null) }

    // Merge Firestore brands/ với stats + auto-discover từ artifacts
    val db = remember { FirebaseFirestore.getInstance() }

    fun loadBrands() {
        isLoading = true
        // Load cả 2 collection song song
        db.collection("brands").get().addOnSuccessListener { brandsResult ->
            db.collection("artifacts").get().addOnSuccessListener { artifactsResult ->

                val statMap = brandStats.associateBy { it.vendor }

                // Vendors đã đăng ký trong brands/
                val registeredBrands = brandsResult.documents.associate { it.id to it }

                // Auto-discover vendors từ artifacts Affiliate (loại bỏ Direct Sale của chính Shop)
                val vendorsFromArtifacts = artifactsResult.documents
                    .filter { it.getBoolean("isDirectSale") != true }
                    .mapNotNull { it.getString("vendor")?.uppercase() }
                    .distinct()

                // Build danh sách cuối — ưu tiên data từ brands/, fallback từ artifacts
                val allVendors = (registeredBrands.keys + vendorsFromArtifacts).distinct()

                brands = allVendors.map { vendorKey ->
                    val doc  = registeredBrands[vendorKey]
                    val stat = statMap[vendorKey]
                    BrandDetail(
                        docId       = vendorKey,
                        name        = doc?.getString("name") ?: vendorKey,
                        rate        = doc?.getDouble("rate") ?: 0.08,
                        logoUrl     = doc?.getString("logoUrl") ?: "",
                        websiteUrl  = doc?.getString("websiteUrl") ?: "",
                        description = doc?.getString("description")
                            ?: if (doc == null) "⚠ Chưa đăng ký trong brands/" else "",
                        clicks      = stat?.clicks ?: 0L,
                        commission  = stat?.commission ?: 0L
                    )
                }.sortedByDescending { it.commission }

                isLoading = false
            }.addOnFailureListener { isLoading = false }
        }.addOnFailureListener { isLoading = false }
    }

    LaunchedEffect(brandStats) { loadBrands() }

    // Totals
    val totalClicks     = brands.sumOf { it.clicks }
    val totalCommission = brands.sumOf { it.commission }
    val maxCommission   = brands.maxOfOrNull { it.commission }?.coerceAtLeast(1L) ?: 1L

    Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
        if (isLoading) {
            AdminLoadingScreen()
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // ── Header ─────────────────────────────────────────
                item {
                    AdminSectionLabel(text = "AFFILIATE BRANDS")
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ── Summary stats ──────────────────────────────────
                item {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("BRANDS",       brands.size.toString(),           TeslaWhite, Modifier.weight(1f))
                        StatCard("TOTAL CLICKS", totalClicks.toString(),           CyberAcid,  Modifier.weight(1f))
                        StatCard("TOTAL EARNED", formatVnd(totalCommission) + "đ", CyberAcid,  Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Performance bar chart ──────────────────────────
                if (brands.isNotEmpty()) {
                    item {
                        BrandPerformanceChart(
                            brands        = brands,
                            maxCommission = maxCommission
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ── Add brand button ───────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("${brands.size} BRAND${if (brands.size != 1) "S" else ""} REGISTERED",
                                fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)
                            
                            Box(
                                modifier = Modifier
                                    .background(CyberAcid)
                                    .clickable { showAddDialog = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Add, null, tint = VoidBlack,
                                        modifier = Modifier.size(14.dp))
                                    Text("ADD BRAND", fontFamily = AppFonts.spaceMono,
                                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VoidBlack)
                                }
                            }
                        }

                        // Developer Data Tools (Seeders)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                                        .clickable { viewModel.runNikeSeeder() }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Text("SEED NIKE", fontFamily = AppFonts.spaceMono,
                                        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberAcid.copy(alpha = 0.8f))
                                }
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                                        .clickable { viewModel.runPumaSeeder() }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Text("SEED PUMA", fontFamily = AppFonts.spaceMono,
                                        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberAcid.copy(alpha = 0.8f))
                                }
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                                        .clickable { viewModel.runBalenciagaSeeder() }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Text("SEED BALENC.", fontFamily = AppFonts.spaceMono,
                                        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberAcid.copy(alpha = 0.8f))
                                }
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                                        .clickable { viewModel.runRickOwensSeeder() }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Text("SEED R. OWENS", fontFamily = AppFonts.spaceMono,
                                        fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberAcid.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ── Brand cards ────────────────────────────────────
                if (brands.isEmpty()) {
                    item { EmptyState("NO BRANDS REGISTERED") }
                } else {
                    items(brands) { brand ->
                        BrandCard(
                            brand         = brand,
                            maxCommission = maxCommission,
                            onAdjustRate  = { adjustingBrand = brand },
                            onEdit        = { editingBrand = brand },
                            onDelete      = { deletingBrand = brand }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // ── Dialogs ────────────────────────────────────────────────
        if (showAddDialog) {
            AddBrandDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { docId, data ->
                    db.collection("brands").document(docId).set(data)
                    showAddDialog = false
                    isLoading = true
                    viewModel.loadBrandStats()
                    loadBrands()
                }
            )
        }

        editingBrand?.let { brand ->
            EditBrandDialog(
                brand     = brand,
                onDismiss = { editingBrand = null },
                onConfirm = { data ->
                    db.collection("brands").document(brand.docId).update(data)
                    editingBrand = null
                    isLoading = true
                    viewModel.loadBrandStats()
                    loadBrands()
                }
            )
        }

        adjustingBrand?.let { brand ->
            RateAdjustDialog(
                brand     = brand,
                onDismiss = { adjustingBrand = null },
                onConfirm = { newRate ->
                    viewModel.updateBrandRate(brand.docId, newRate)
                    adjustingBrand = null
                    loadBrands()
                }
            )
        }

        deletingBrand?.let { brand ->
            DeleteBrandDialog(
                brandName = brand.name,
                onDismiss = { deletingBrand = null },
                onConfirm = {
                    db.collection("brands").document(brand.docId).delete()
                    deletingBrand = null
                    isLoading = true
                    viewModel.loadBrandStats()
                    loadBrands()
                }
            )
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// PERFORMANCE CHART — horizontal bars
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun BrandPerformanceChart(brands: List<BrandDetail>, maxCommission: Long) {
    val barColors = listOf(
        CyberAcid, Color(0xFF00BFFF), Color(0xFFFF8C00),
        Color(0xFFBF00FF), Color(0xFF00FF88), Color(0xFFFF4444)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
            .padding(16.dp)
    ) {
        Text("COMMISSION PERFORMANCE", fontFamily = AppFonts.spaceMono,
            fontSize = 9.sp, color = TechSilver, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(14.dp))

        brands.take(6).forEachIndexed { index, brand ->
            val fraction = (brand.commission.toFloat() / maxCommission).coerceIn(0f, 1f)
            val animFrac by animateFloatAsState(
                targetValue   = fraction,
                animationSpec = tween(700, easing = FastOutSlowInEasing),
                label         = "brand_perf_${brand.docId}"
            )
            val color = barColors[index % barColors.size]

            Column {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(brand.name, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = TeslaWhite,
                        modifier = Modifier.weight(1f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+${formatVnd(brand.commission)}đ",
                        fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = color)
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Bar
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(DimBorder)) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animFrac)
                            .background(
                                Brush.horizontalGradient(0f to color, 1f to color.copy(alpha = 0.4f))
                            )
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text("${brand.clicks} clicks • ${(brand.rate * 100).toInt()}% rate",
                    fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
                    color = TechSilver.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// BRAND CARD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun BrandCard(
    brand: BrandDetail,
    maxCommission: Long,
    onAdjustRate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val fraction = (brand.commission.toFloat() / maxCommission).coerceIn(0f, 1f)
    val animFrac by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = tween(600),
        label         = "card_bar_${brand.docId}"
    )

    // Rate color: green if >=10%, amber if 5-10%, red if <5%
    val rateColor = when {
        brand.rate >= 0.10 -> Color(0xFF00FF88)
        brand.rate >= 0.05 -> Color(0xFFFF8C00)
        else               -> AdminRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
    ) {
        // ── Top commission bar ─────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(DimBorder)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animFrac).background(CyberAcid))
        }

        // ── Main row ───────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand initial circle
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .background(DimBorder)
                    .border(1.dp, CyberAcid.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = brand.name.take(2).uppercase(),
                    fontFamily = AppFonts.oswald,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Black,
                    color      = CyberAcid
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(brand.name, fontFamily = AppFonts.oswald, fontSize = 18.sp,
                        fontWeight = FontWeight.Black, color = TeslaWhite, letterSpacing = (-0.5).sp)
                    // Warning badge nếu chưa đăng ký trong brands/
                    if (brand.description.startsWith("⚠")) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF8C00).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFFF8C00).copy(alpha = 0.5f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("UNREGISTERED", fontFamily = AppFonts.spaceMono,
                                fontSize = 7.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8C00))
                        }
                    }
                }
                Text(brand.docId, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                    color = TechSilver.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Rate badge
                    Box(
                        modifier = Modifier
                            .border(1.dp, rateColor.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("${(brand.rate * 100).toInt()}% RATE",
                            fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, color = rateColor)
                    }
                    Text("${brand.clicks} clicks", fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = TechSilver)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("+${formatVnd(brand.commission)}đ",
                    fontFamily = AppFonts.oswald, fontSize = 16.sp,
                    fontWeight = FontWeight.Black, color = CyberAcid)
                Text("EARNED", fontFamily = AppFonts.spaceMono,
                    fontSize = 8.sp, color = TechSilver.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, tint = TechSilver, modifier = Modifier.size(18.dp)
            )
        }

        // ── Expanded detail ────────────────────────────────────────
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter   = androidx.compose.animation.expandVertically(),
            exit    = androidx.compose.animation.shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VoidBlack)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Description
                if (brand.description.isNotEmpty() && !brand.description.startsWith("⚠")) {
                    Text(brand.description, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                        color = TechSilver, lineHeight = 18.sp)
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
                }

                // Unregistered warning
                if (brand.description.startsWith("⚠")) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0xFFFF8C00).copy(alpha = 0.08f))
                            .border(1.dp, Color(0xFFFF8C00).copy(alpha = 0.3f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("//", fontFamily = AppFonts.spaceMono, fontSize = 12.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFFFF8C00))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("BRAND CHƯA ĐĂNG KÝ", fontFamily = AppFonts.spaceMono,
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8C00))
                            Text("Vendor key \"${brand.docId}\" có trong artifacts nhưng chưa có trong brands/. Bấm ADD để đăng ký.",
                                fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                                color = TechSilver, lineHeight = 16.sp)
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
                }

                // Stats row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    BrandStatChip("CLICKS",     brand.clicks.toString(),             Modifier.weight(1f))
                    BrandStatChip("COMMISSION", "+${formatVnd(brand.commission)}đ",  Modifier.weight(1f))
                    BrandStatChip("RATE",       "${(brand.rate * 100).toInt()}%",    Modifier.weight(1f))
                }

                // Website URL
                if (brand.websiteUrl.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Link, null, tint = TechSilver.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp))
                        Text(brand.websiteUrl, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                            color = TechSilver.copy(alpha = 0.4f), maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                }

                // ── Quick rate adjuster ────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("COMMISSION RATE", fontFamily = AppFonts.spaceMono,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TeslaWhite)

                    Box(
                        modifier = Modifier
                            .border(1.dp, CyberAcid)
                            .clickable { onAdjustRate() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Edit, null, tint = CyberAcid,
                                modifier = Modifier.size(12.dp))
                            Text("ADJUST RATE", fontFamily = AppFonts.spaceMono,
                                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberAcid)
                        }
                    }
                }

                // ── Action row ─────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).border(1.dp, TechSilver.copy(alpha = 0.3f))
                        .clickable { onEdit() }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Edit, null, tint = TechSilver,
                                modifier = Modifier.size(13.dp))
                            Text("EDIT INFO", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                fontWeight = FontWeight.Bold, color = TechSilver)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).border(1.dp, AdminRed.copy(alpha = 0.4f))
                        .background(AdminRed.copy(alpha = 0.05f))
                        .clickable { onDelete() }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Delete, null, tint = AdminRed,
                                modifier = Modifier.size(13.dp))
                            Text("REMOVE", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                fontWeight = FontWeight.Bold, color = AdminRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandStatChip(label: String, value: String, modifier: Modifier) {
    Column(
        modifier            = modifier
            .border(1.dp, DimBorder)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontFamily = AppFonts.oswald, fontSize = 16.sp,
            fontWeight = FontWeight.Black, color = CyberAcid)
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 8.sp,
            color = TechSilver.copy(alpha = 0.6f))
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// RATE ADJUST DIALOG — slider + quick presets
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun RateAdjustDialog(
    brand: BrandDetail,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var ratePercent by remember { mutableFloatStateOf((brand.rate * 100).toFloat()) }
    val presets = listOf(5, 7, 8, 10, 12, 15)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, DimBorder).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text("// ADJUST RATE", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                color = CyberAcid, letterSpacing = 1.sp)
            Text(brand.name, fontFamily = AppFonts.oswald, fontSize = 22.sp,
                fontWeight = FontWeight.Black, color = TeslaWhite)
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))

            // Current rate display
            Box(
                modifier         = Modifier.fillMaxWidth().background(ScanlineGray)
                    .border(1.dp, CyberAcid).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${ratePercent.toInt()}%", fontFamily = AppFonts.oswald, fontSize = 48.sp,
                    fontWeight = FontWeight.Black, color = CyberAcid, letterSpacing = (-1).sp)
            }

            // Slider
            Slider(
                value         = ratePercent,
                onValueChange = { ratePercent = it },
                valueRange    = 1f..30f,
                steps         = 28,
                colors        = SliderDefaults.colors(
                    thumbColor       = CyberAcid,
                    activeTrackColor = CyberAcid,
                    inactiveTrackColor = DimBorder
                )
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1%", fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)
                Text("30%", fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)
            }

            // Quick preset chips
            Text("QUICK PRESETS", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                color = TechSilver, letterSpacing = 1.sp)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { preset ->
                    val selected = ratePercent.toInt() == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (selected) CyberAcid else DimBorder)
                            .background(if (selected) CyberAcid.copy(0.15f) else Color.Transparent)
                            .clickable { ratePercent = preset.toFloat() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$preset%", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) CyberAcid else TechSilver)
                    }
                }
            }

            // Estimate
            val estimatedPerClick = 1_500_000L // ví dụ 1.5M VND avg product price
            val estimatedCommission = (estimatedPerClick * ratePercent / 100).toLong()
            Text(
                "Ước tính mỗi click (avg 1.5M): +${formatVnd(estimatedCommission)}đ",
                fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                color = TechSilver.copy(alpha = 0.6f)
            )

            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                    .clickable { onDismiss() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("CANCEL", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TechSilver)
                }
                Box(modifier = Modifier.weight(1f).background(CyberAcid)
                    .clickable { onConfirm(ratePercent.toDouble() / 100.0) }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("APPLY RATE", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = VoidBlack)
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ADD BRAND DIALOG
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun AddBrandDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Map<String, Any>) -> Unit
) {
    var docId       by remember { mutableStateOf("") }
    var name        by remember { mutableStateOf("") }
    var rate        by remember { mutableStateOf("8") }
    var websiteUrl  by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, DimBorder)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("// ADD BRAND", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                color = CyberAcid, letterSpacing = 1.sp)
            Text("New affiliate partner", fontFamily = AppFonts.oswald, fontSize = 18.sp,
                fontWeight = FontWeight.Black, color = TeslaWhite)
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))

            BrandDialogField("VENDOR KEY (e.g. NIKE)", docId, onValueChange = { docId = it.uppercase() })
            BrandDialogField("DISPLAY NAME (e.g. Nike)", name) { name = it }
            BrandDialogField("COMMISSION RATE (%)", rate, KeyboardType.Number) { rate = it }
            BrandDialogField("WEBSITE URL", websiteUrl) { websiteUrl = it }
            BrandDialogField("DESCRIPTION (optional)", description) { description = it }

            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                    .clickable { onDismiss() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("CANCEL", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TechSilver)
                }
                Box(modifier = Modifier.weight(1f).background(CyberAcid)
                    .clickable {
                        if (docId.isNotBlank() && name.isNotBlank()) {
                            onConfirm(docId, mapOf(
                                "name"        to name,
                                "rate"        to (rate.toDoubleOrNull() ?: 8.0) / 100.0,
                                "websiteUrl"  to websiteUrl,
                                "description" to description,
                                "logoUrl"     to ""
                            ))
                        }
                    }
                    .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("ADD BRAND", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = VoidBlack)
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// EDIT BRAND DIALOG
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun EditBrandDialog(
    brand: BrandDetail,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Any>) -> Unit
) {
    var name        by remember { mutableStateOf(brand.name) }
    var websiteUrl  by remember { mutableStateOf(brand.websiteUrl) }
    var description by remember { mutableStateOf(brand.description) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, DimBorder).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("// EDIT BRAND", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                color = CyberAcid, letterSpacing = 1.sp)
            Text(brand.docId, fontFamily = AppFonts.oswald, fontSize = 18.sp,
                fontWeight = FontWeight.Black, color = TeslaWhite)
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))

            BrandDialogField("DISPLAY NAME", name)        { name = it }
            BrandDialogField("WEBSITE URL", websiteUrl)   { websiteUrl = it }
            BrandDialogField("DESCRIPTION", description)  { description = it }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f).border(1.dp, DimBorder)
                    .clickable { onDismiss() }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("CANCEL", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TechSilver)
                }
                Box(modifier = Modifier.weight(1f).background(CyberAcid)
                    .clickable {
                        onConfirm(mapOf(
                            "name"        to name,
                            "websiteUrl"  to websiteUrl,
                            "description" to description
                        ))
                    }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center) {
                    Text("SAVE", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = VoidBlack)
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DELETE BRAND DIALOG
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun DeleteBrandDialog(
    brandName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().background(VoidBlack)
                .border(1.dp, AdminRed).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("REMOVE BRAND", fontFamily = AppFonts.oswald, fontSize = 20.sp,
                fontWeight = FontWeight.Black, color = AdminRed)
            Text("Xác nhận xóa brand:\n\"$brandName\"",
                fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                color = TechSilver, lineHeight = 18.sp)
            Text("Dữ liệu stats vẫn còn, nhưng brand sẽ không còn trong hệ thống.",
                fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                color = AdminRed.copy(alpha = 0.7f), lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
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
                    Text("REMOVE", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = TeslaWhite)
                }
            }
        }
    }
}

// ── Shared field ───────────────────────────────────────────────────
@Composable
private fun BrandDialogField(
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
