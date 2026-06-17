package com.rinnsan.creavity.presentation.archive

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.data.vault.AffiliateArtifact
import com.rinnsan.creavity.presentation.cart.CartItem
import com.rinnsan.creavity.presentation.cart.CartViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DimBorder    = Color(0xFF2A2A2A)
private val ScanlineGray = Color(0xFF111111)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ROOT — fetch artifact by ID from VaultViewModel
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun ArtifactDetailScreen(
    artifactId: String,
    navController: NavController,
    vaultViewModel: VaultViewModel = hiltViewModel(),
    cartViewModel: CartViewModel   = hiltViewModel()
) {
    val allArtifacts by vaultViewModel.allArtifacts.collectAsState()
    val artifact = allArtifacts.firstOrNull { it.id == artifactId }

    if (artifact == null) {
        Box(modifier = Modifier.fillMaxSize().background(VoidBlack),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = CyberAcid,
                    modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("LOADING ARTIFACT...", fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp, color = TechSilver)
            }
        }
        return
    }

    DetailContent(artifact = artifact, navController = navController, cartViewModel = cartViewModel)
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DETAIL CONTENT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun DetailContent(
    artifact: AffiliateArtifact,
    navController: NavController,
    cartViewModel: CartViewModel
) {
    val uriHandler = LocalUriHandler.current
    val scope      = rememberCoroutineScope()

    val imageList  = if (artifact.images.isNotEmpty()) artifact.images
    else listOfNotNull(artifact.imageUrl.takeIf { it.isNotEmpty() })

    val pagerState   = rememberPagerState(pageCount = { imageList.size.coerceAtLeast(1) })
    var selectedSize by remember { mutableStateOf<String?>(null) }
    var sizeError    by remember { mutableStateOf(false) }
    var addedToCart  by remember { mutableStateOf(false) }

    LaunchedEffect(sizeError) {
        if (sizeError) { delay(2000); sizeError = false }
    }

    // Reset "ADDED" indicator when user changes size selection
    LaunchedEffect(selectedSize) { addedToCart = false }

    Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── IMAGE PAGER ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.52f)
            ) {
                if (imageList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().background(ScanlineGray),
                        contentAlignment = Alignment.Center) {
                        Text("NO IMAGE", fontFamily = AppFonts.spaceMono,
                            fontSize = 11.sp, color = TechSilver)
                    }
                } else {
                    // Main pager
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model              = imageList[page],
                            contentDescription = artifact.title,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    }

                    // Bottom gradient
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.Transparent, 0.65f to Color.Transparent, 1f to VoidBlack
                        )
                    ))

                    // Back button
                    IconButton(
                        onClick  = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.TopStart)
                            .padding(top = 48.dp, start = 16.dp).size(36.dp)
                            .background(VoidBlack.copy(alpha = 0.75f)).border(1.dp, DimBorder)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = TeslaWhite,
                            modifier = Modifier.size(18.dp))
                    }

                    // Image counter
                    if (imageList.size > 1) {
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd)
                                .padding(top = 48.dp, end = 16.dp)
                                .background(VoidBlack.copy(0.75f)).border(1.dp, DimBorder)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${pagerState.currentPage + 1} / ${imageList.size}",
                                fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                fontWeight = FontWeight.Bold, color = TeslaWhite)
                        }
                    }

                    // Thumbnail strip
                    if (imageList.size > 1) {
                        Row(
                            modifier = Modifier.align(Alignment.BottomStart)
                                .padding(start = 16.dp, bottom = 12.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            imageList.forEachIndexed { idx, url ->
                                val active = pagerState.currentPage == idx
                                Box(
                                    modifier = Modifier.size(46.dp)
                                        .border(if (active) 2.dp else 1.dp,
                                            if (active) CyberAcid else DimBorder)
                                        .clickable { scope.launch { pagerState.animateScrollToPage(idx) } }
                                ) {
                                    AsyncImage(model = url, contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                            .alpha(if (active) 1f else 0.55f))
                                }
                            }
                        }
                    }

                    // Dot indicators
                    if (imageList.size > 1) {
                        Row(
                            modifier              = Modifier.align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            repeat(imageList.size) { idx ->
                                val active = pagerState.currentPage == idx
                                Box(modifier = Modifier
                                    .size(if (active) 8.dp else 5.dp)
                                    .background(
                                        if (active) CyberAcid else TeslaWhite.copy(0.35f),
                                        CircleShape
                                    ))
                            }
                        }
                    }
                }
            }

            // ── INFO PANEL ─────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .background(VoidBlack)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Category + Archetype
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(artifact.category, fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = TechSilver, letterSpacing = 1.sp)
                    Box(modifier = Modifier.border(1.dp, DimBorder)
                        .padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text(artifact.archetype.name, fontFamily = AppFonts.spaceMono,
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TechSilver)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(artifact.title, fontFamily = AppFonts.oswald, fontSize = 24.sp,
                    fontWeight = FontWeight.Black, color = TeslaWhite,
                    letterSpacing = (-0.5).sp, lineHeight = 26.sp)

                Spacer(modifier = Modifier.height(4.dp))

                // Vendor + ID
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("RINNSAN LAB", fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                        color = CyberAcid.copy(alpha = 0.8f), letterSpacing = 1.sp)
                    Text("ID: ${artifact.id}", fontFamily = AppFonts.spaceMono,
                        fontSize = 9.sp, color = TechSilver.copy(alpha = 0.6f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price
                Text(artifact.price + " VND", fontFamily = AppFonts.oswald,
                    fontSize = 26.sp, fontWeight = FontWeight.Black, color = CyberAcid,
                    letterSpacing = (-0.5).sp)

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))

                // ── SIZE SELECTOR ──────────────────────────────────
                if (artifact.sizes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("SELECT SIZE", fontFamily = AppFonts.spaceMono,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = if (sizeError) Color(0xFFFF003C) else TeslaWhite,
                            letterSpacing = 2.sp)
                        if (sizeError) {
                            Text("// CHỌN SIZE TRƯỚC",
                                fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                                color = Color(0xFFFF003C))
                        } else selectedSize?.let {
                            Text("$it ✓", fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                fontWeight = FontWeight.Bold, color = CyberAcid)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        artifact.sizes.forEach { size ->
                            val isSel = selectedSize == size
                            Box(
                                modifier = Modifier.size(52.dp)
                                    .background(when {
                                        isSel     -> CyberAcid
                                        sizeError -> Color(0xFFFF003C).copy(0.08f)
                                        else      -> Color.Transparent
                                    })
                                    .border(if (isSel) 2.dp else 1.dp, when {
                                        isSel     -> CyberAcid
                                        sizeError -> Color(0xFFFF003C).copy(0.5f)
                                        else      -> DimBorder
                                    })
                                    .clickable { selectedSize = size; sizeError = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(size, fontFamily = AppFonts.oswald, fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) VoidBlack else TeslaWhite)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
                }

                // ── STOCK STATUS ───────────────────────────────────
                if (artifact.isDirectSale) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val (stockColor, stockLabel) = when {
                        artifact.stock == 0L -> Color(0xFFFF003C) to "OUT OF STOCK"
                        artifact.stock <= 5L -> Color(0xFFFF8C00) to "LOW STOCK — ${artifact.stock} còn lại"
                        else                 -> Color(0xFF00FF88) to "IN STOCK"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(stockColor, CircleShape))
                        Text(stockLabel, fontFamily = AppFonts.spaceMono,
                            fontSize = 10.sp, color = stockColor)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── CTA BUTTON ─────────────────────────────────────
                if (artifact.isDirectSale) {
                    val isOutOfStock = artifact.stock == 0L

                    // ADD TO CART + BUY NOW — side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ADD TO CART
                        Box(
                            modifier = Modifier
                                .weight(1f).height(60.dp)
                                .background(when {
                                    isOutOfStock -> ScanlineGray
                                    addedToCart  -> VoidBlack
                                    else         -> CyberAcid
                                })
                                .border(1.dp, when {
                                    isOutOfStock -> DimBorder
                                    addedToCart  -> CyberAcid
                                    else         -> CyberAcid
                                })
                                .clickable(enabled = !isOutOfStock) {
                                    if (artifact.sizes.isNotEmpty() && selectedSize == null) {
                                        sizeError = true; return@clickable
                                    }
                                    cartViewModel.addToCart(CartItem(
                                        artifactId   = artifact.id,
                                        title        = artifact.title,
                                        vendor       = "RINNSAN LAB",
                                        imageUrl     = artifact.imageUrl,
                                        price        = artifact.internalPrice,
                                        priceDisplay = artifact.price,
                                        archetype    = artifact.archetype.name,
                                        size         = selectedSize ?: ""
                                    ))
                                    addedToCart = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isOutOfStock -> Text("OUT OF STOCK",
                                    fontFamily = AppFonts.oswald, fontSize = 14.sp,
                                    fontWeight = FontWeight.Black, color = TechSilver, letterSpacing = 1.sp)
                                addedToCart  -> Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.CheckCircle, null, tint = CyberAcid,
                                        modifier = Modifier.size(18.dp))
                                    Text("ADDED", fontFamily = AppFonts.oswald, fontSize = 16.sp,
                                        fontWeight = FontWeight.Black, color = CyberAcid)
                                }
                                else -> Text("ADD TO CART",
                                    fontFamily = AppFonts.oswald, fontSize = 16.sp,
                                    fontWeight = FontWeight.Black, color = VoidBlack, letterSpacing = 1.sp)
                            }
                        }

                        // BUY NOW
                        Box(
                            modifier = Modifier
                                .weight(1f).height(60.dp)
                                .background(Color.Transparent)
                                .border(1.dp, if (isOutOfStock) DimBorder else TeslaWhite)
                                .clickable(enabled = !isOutOfStock) {
                                    if (artifact.sizes.isNotEmpty() && selectedSize == null) {
                                        sizeError = true; return@clickable
                                    }
                                    cartViewModel.buyNow(CartItem(
                                        artifactId   = artifact.id,
                                        title        = artifact.title,
                                        vendor       = "RINNSAN LAB",
                                        imageUrl     = artifact.imageUrl,
                                        price        = artifact.internalPrice,
                                        priceDisplay = artifact.price,
                                        archetype    = artifact.archetype.name,
                                        size         = selectedSize ?: ""
                                    ))
                                    navController.navigate(Routes.CART)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = "BUY NOW",
                                fontFamily = AppFonts.oswald,
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Black,
                                color      = if (isOutOfStock) TechSilver else TeslaWhite,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // VIEW CART — appears after adding
                    if (addedToCart) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(48.dp)
                            .border(1.dp, CyberAcid)
                            .clickable { navController.navigate(Routes.CART) },
                            contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.ShoppingCart, null, tint = CyberAcid,
                                    modifier = Modifier.size(16.dp))
                                Text("VIEW CART", fontFamily = AppFonts.spaceMono,
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberAcid)
                            }
                        }
                    }
                } else {
                    // Affiliate
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp)
                        .background(VoidBlack).border(1.dp, CyberAcid)
                        .clickable {
                            if (artifact.affiliateLink.isNotEmpty())
                                uriHandler.openUri(artifact.affiliateLink)
                        }, contentAlignment = Alignment.Center) {
                        Text("[ INITIATE ACQUISITION ]",
                            fontFamily = AppFonts.oswald, fontSize = 18.sp,
                            fontWeight = FontWeight.Black, color = CyberAcid, letterSpacing = 2.sp)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}