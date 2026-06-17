package com.rinnsan.creavity.presentation.archive

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.data.vault.AffiliateArtifact
import com.rinnsan.creavity.domain.models.Archetype
import com.rinnsan.creavity.presentation.cart.CartViewModel
import com.rinnsan.creavity.presentation.cart.CartItem
import com.rinnsan.creavity.core.router.Routes
import com.google.firebase.firestore.FirebaseFirestore
import com.rinnsan.creavity.data.vault.RinnsanSeeder
import kotlinx.coroutines.delay

private val ScanlineGray = Color(0xFF111111)
private val DimBorder    = Color(0xFF2A2A2A)

@Composable
fun ArtifactArchiveScreen(
    navController: NavController,
    viewModel: VaultViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val arsenal by viewModel.displayArtifacts.collectAsState()
    val userArchetype by viewModel.userArchetype.collectAsState()
    val isFiltered by viewModel.isFiltered.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    // 🔥 Lấy danh sách ID đã lưu từ ViewModel
    val wishlistIds by viewModel.wishlistIds.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                ArchiveDossierHeader(
                    archetype   = userArchetype,
                    isFiltered  = isFiltered,
                    itemCount   = arsenal.size,
                    selectedTab = selectedTab,
                    onToggleFilter = { viewModel.toggleFilter() },
                    onTabSelected = { viewModel.setTab(it) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            item {
                ArchiveStatusTicker(archetype = userArchetype, isFiltered = isFiltered)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (arsenal.isEmpty()) {
                item { EmptyVaultMessage() }
            } else {
                itemsIndexed(
                    items = arsenal,
                    key   = { _, artifact -> artifact.id } // ← key để LazyColumn giữ vị trí scroll
                ) { index, artifact ->
                    ArtifactCard(
                        artifact = artifact,
                        index = index,
                        isSaved = wishlistIds.contains(artifact.id),
                        onToggleSave = { viewModel.toggleWishlistStatus(artifact.id) },
                        onAcquireClick = { viewModel.recordClick(artifact) },
                        onCardClick = {
                            navController.navigate("${Routes.ARTIFACT_DETAIL}/${artifact.id}")
                        },
                        onBuyNowClick = {
                            if (artifact.sizes.isNotEmpty()) {
                                // Có size → vào Detail để chọn size trước
                                navController.navigate("${Routes.ARTIFACT_DETAIL}/${artifact.id}")
                            } else {
                                // Không có size → mua thẳng
                                cartViewModel.buyNow(
                                    CartItem(
                                        artifactId   = artifact.id,
                                        title        = artifact.title,
                                        vendor       = artifact.vendor,
                                        imageUrl     = artifact.imageUrl,
                                        price        = artifact.internalPrice,
                                        priceDisplay = artifact.price,
                                        archetype    = artifact.archetype.name
                                    )
                                )
                                navController.navigate(Routes.CART)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun ArchiveDossierHeader(
    archetype: Archetype,
    isFiltered: Boolean,
    itemCount: Int,
    selectedTab: VaultViewModel.VaultTab,
    onToggleFilter: () -> Unit,
    onTabSelected: (VaultViewModel.VaultTab) -> Unit,
    onBackClick: () -> Unit
) {
    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue  = 1f,
        targetValue   = 0.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    val titleText = if (isFiltered) archetypeLabel(archetype) else "GLOBAL\nARSENAL"
    val subtitleText = if (isFiltered) archetypeSubtitle(archetype) else "ALL CLASSIFIED ASSETS // UNRESTRICTED ACCESS"
    val statusText = if (isFiltered) "PERSONALIZED" else "GLOBAL VIEW"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VoidBlack)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(
                onClick  = onBackClick,
                modifier = Modifier.size(36.dp).border(1.dp, DimBorder)
            ) {
                Icon(
                    imageVector        = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint               = TeslaWhite,
                    modifier           = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .background(CyberAcid)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text         = "// CLASSIFIED",
                    fontFamily   = AppFonts.spaceMono,
                    fontSize     = 11.sp,
                    fontWeight   = FontWeight.Bold,
                    color        = VoidBlack,
                    modifier     = Modifier.alpha(blinkAlpha)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text          = "ARTIFACT ARCHIVE",
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 11.sp,
            color         = CyberAcid,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text          = titleText,
            fontFamily    = AppFonts.oswald,
            fontSize      = 42.sp,
            fontWeight    = FontWeight.Black,
            color         = TeslaWhite,
            letterSpacing = (-1).sp,
            lineHeight    = 44.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text       = subtitleText,
            fontFamily = AppFonts.spaceMono,
            fontSize   = 11.sp,
            color      = TechSilver
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isFiltered) CyberAcid else DimBorder)
                .background(if (isFiltered) CyberAcid.copy(alpha = 0.1f) else ScanlineGray)
                .clickable { onToggleFilter() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = if (isFiltered) "// SHOW GLOBAL ARSENAL" else "// FILTER BY MY ARCHETYPE",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = if (isFiltered) CyberAcid else TechSilver,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            MetaChip(label = "ASSETS",     value = "$itemCount ITEMS")
            MetaChip(label = "STATUS",     value = statusText)
            MetaChip(label = "SOURCE",     value = "FIRESTORE")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        0f   to CyberAcid.copy(alpha = 0.9f),
                        0.6f to CyberAcid.copy(alpha = 0.2f),
                        1f   to Color.Transparent
                    )
                )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── ĐÂY LÀ TAB LỰA CHỌN: STORE vs AFFILIATE ──
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TabSelector(
                title = "RINNSAN STORE",
                isSelected = selectedTab == VaultViewModel.VaultTab.STORE,
                onClick = { onTabSelected(VaultViewModel.VaultTab.STORE) },
                modifier = Modifier.weight(1f)
            )
            TabSelector(
                title = "AFFILIATE NETWORK",
                isSelected = selectedTab == VaultViewModel.VaultTab.AFFILIATE,
                onClick = { onTabSelected(VaultViewModel.VaultTab.AFFILIATE) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun TabSelector(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (isSelected) CyberAcid else ScanlineGray
    val txtColor = if (isSelected) VoidBlack else TechSilver
    val borderColor = if (isSelected) CyberAcid else DimBorder

    Box(
        modifier = modifier
            .height(44.dp)
            .background(bgColor)
            .border(1.dp, borderColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = "[ $title ]",
            fontFamily = AppFonts.spaceMono,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            color      = txtColor
        )
    }
}

@Composable
private fun MetaChip(label: String, value: String) {
    Column {
        Text(
            text          = label,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 9.sp,
            color         = TechSilver,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text       = value,
            fontFamily = AppFonts.spaceMono,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = TeslaWhite
        )
    }
}

@Composable
private fun ArchiveStatusTicker(archetype: Archetype, isFiltered: Boolean) {
    val scrollState = rememberScrollState()
    val messageText = if (isFiltered) {
        "RINNSAN_VAULT // ARCHETYPE: ${archetype.name} // DOSSIER EYES ONLY // "
    } else {
        "RINNSAN_VAULT // GLOBAL INVENTORY // ALL ACCESS GRANTED // "
    }

    val message = messageText + "AFFILIATE LINKS ACTIVE // ACQUIRE WITH CAUTION // "

    LaunchedEffect(Unit) {
        delay(400)
        while (true) {
            val target = scrollState.maxValue
            scrollState.animateScrollTo(
                value         = target,
                animationSpec = tween(
                    durationMillis = ((target / 30f) * 1000).toInt().coerceAtLeast(6000),
                    easing         = LinearEasing
                )
            )
            scrollState.scrollTo(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color(0xFF0A0A0A))
            .border(width = 1.dp, color = CyberAcid.copy(alpha = 0.25f)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier          = Modifier
                .horizontalScroll(scrollState, enabled = false)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(60) {
                Text(
                    text          = message,
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 10.sp,
                    color         = CyberAcid.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ArtifactCard(
    artifact: AffiliateArtifact,
    index: Int,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onAcquireClick: () -> Unit,
    onCardClick: () -> Unit = {},
    onBuyNowClick: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val context = androidx.compose.ui.platform.LocalContext.current

    // Không dùng LaunchedEffect + delay vì LazyColumn recycle item khi scroll lên
    // → LaunchedEffect(Unit) chạy lại → visible = false → layout thay đổi → scroll reset
    // Thay bằng remember(artifact.id) để chỉ animate lần đầu tiên mỗi item xuất hiện
    var visible by remember(artifact.id) { mutableStateOf(true) }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(300))
        // Bỏ slideInVertically vì nó thay đổi kích thước item → gây scroll jump
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VoidBlack)
                .border(width = 1.dp, color = DimBorder)
                .clickable { onCardClick() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
            ) {
                AsyncImage(
                    model              = artifact.imageUrl,
                    contentDescription = artifact.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f    to Color.Transparent,
                                0.55f to Color.Transparent,
                                1f    to VoidBlack.copy(alpha = 0.9f)
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .background(CyberAcid)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text       = "ID: ${artifact.id}",
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = VoidBlack
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .background(VoidBlack.copy(alpha = 0.85f))
                        .border(1.dp, DimBorder)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text       = artifact.archetype.name,
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TechSilver
                    )
                }

                Text(
                    text       = String.format("%02d", index + 1),
                    fontFamily = AppFonts.oswald,
                    fontSize   = 72.sp,
                    fontWeight = FontWeight.Black,
                    color      = TeslaWhite.copy(alpha = 0.04f),
                    modifier   = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScanlineGray)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text          = artifact.category,
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 10.sp,
                        color         = TechSilver,
                        letterSpacing = 0.5.sp,
                        modifier      = Modifier.weight(1f),
                        maxLines      = 1,
                        overflow      = TextOverflow.Ellipsis
                    )
                    Text(
                        text          = artifact.vendor,
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 10.sp,
                        color         = CyberAcid.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text          = artifact.title,
                    fontFamily    = AppFonts.oswald,
                    fontSize      = 28.sp,
                    fontWeight    = FontWeight.Black,
                    color         = TeslaWhite,
                    letterSpacing = (-0.5).sp,
                    lineHeight    = 30.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(DimBorder)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text       = artifact.price,
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TeslaWhite
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 🔥 NÚT BẤM (Nút Save 1 phần, Nút Mua 1.5 phần)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WishlistToggleButton(
                        isSaved = isSaved,
                        onClick = onToggleSave,
                        modifier = Modifier.weight(1f)
                    )

                    if (artifact.isDirectSale) {
                        BuyNowButton(
                            onClick = {
                                onAcquireClick()
                                onBuyNowClick()
                            },
                            modifier = Modifier.weight(1.5f)
                        )
                    } else {
                        AcquisitionButton(
                            isDirectSale = false,
                            onClick = {
                                onAcquireClick() // Record affiliate click / direct intent to DB
                                if (artifact.affiliateLink.isNotEmpty()) {
                                    uriHandler.openUri(artifact.affiliateLink)
                                }
                            },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            }

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .background(VoidBlack)
                    .border(width = 1.dp, color = DimBorder)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "RINNSAN_VAULT // ${artifact.id}",
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 9.sp,
                    color      = TechSilver.copy(alpha = 0.5f)
                )
                Text(
                    text       = "LINK: ACTIVE",
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 9.sp,
                    color      = CyberAcid.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ── NÚT LƯU VÀO WISHLIST ──────────────────────────────────────────
@Composable
private fun WishlistToggleButton(
    isSaved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor   = if (isSaved) CyberAcid.copy(alpha = 0.15f) else VoidBlack
    val textColor = if (isSaved) CyberAcid else TechSilver
    val borderColor = if (isSaved) CyberAcid else DimBorder

    Box(
        modifier = modifier
            .height(52.dp)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = if (isSaved) "★ SAVED" else "☆ SAVE",
            fontFamily = AppFonts.spaceMono,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            color      = textColor,
            letterSpacing = 1.sp
        )
    }
}

// ── NÚT MUA HÀNG ──────────────────────────────────────────────────
@Composable
private fun AcquisitionButton(isDirectSale: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }

    val bgColor   = if (pressed) CyberAcid else VoidBlack
    val textColor = if (pressed) VoidBlack  else CyberAcid
    val buttonText = if (isDirectSale) "BUY NOW" else "ACQUIRE"

    Box(
        modifier = modifier
            .height(52.dp)
            .background(bgColor)
            .border(width = 1.dp, color = CyberAcid)
            .clickable {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text       = "[",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text          = buttonText,
                fontFamily    = AppFonts.oswald,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                color         = textColor,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text       = "]",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
        }
    }
}

@Composable
private fun EmptyVaultMessage() {
    Box(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text          = "VAULT_EMPTY",
                fontFamily    = AppFonts.oswald,
                fontSize      = 28.sp,
                fontWeight    = FontWeight.Black,
                color         = TeslaWhite,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = "CHƯA CÓ SẢN PHẨM NÀO KHỚP VỚI PHONG CÁCH NÀY.",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 11.sp,
                color      = TechSilver,
                textAlign  = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun archetypeLabel(archetype: Archetype): String = when (archetype) {
    Archetype.GHOST    -> "GHOST\nARSENAL"
    Archetype.OPERATOR -> "OPERATOR\nARSENAL"
    Archetype.GLITCH   -> "GLITCH\nARSENAL"
    Archetype.NOMAD    -> "NOMAD\nARSENAL"
}

private fun archetypeSubtitle(archetype: Archetype): String = when (archetype) {
    Archetype.GHOST    -> "LOW VISIBILITY // SILENT PROFILE // SHADOW SPEC"
    Archetype.OPERATOR -> "TACTICAL BUILD // FIELD READY // COMMAND TIER"
    Archetype.GLITCH   -> "CHAOS LAYER // DISTORTION SPEC // SIGNAL BREAK"
    Archetype.NOMAD    -> "ALL-TERRAIN // MOBILE UNIT // TRANSIT OPTIMIZED"
}

@Composable
fun BuyNowButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val bgColor     = if (pressed) CyberAcid else VoidBlack
    val textColor   = if (pressed) VoidBlack else CyberAcid
    val borderColor = CyberAcid

    Box(
        modifier = modifier
            .height(52.dp)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor)
            .clickable {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "[",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "BUY NOW",
                fontFamily    = AppFonts.oswald,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Bold,
                color         = textColor,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "]",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = textColor
            )
        }
    }
}