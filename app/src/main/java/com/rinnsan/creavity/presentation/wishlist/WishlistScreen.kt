package com.rinnsan.creavity.presentation.wishlist

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay

private val DimBorder    = Color(0xFF2A2A2A)
private val ScanlineGray = Color(0xFF111111)

@Composable
fun WishlistScreen(
    navController: NavController,
    viewModel: WishlistViewModel = hiltViewModel() // <-- Gọi ViewModel vào đây
) {
    val uriHandler = LocalUriHandler.current

    // Lấy dữ liệu thật từ ViewModel (Firebase)
    val items by viewModel.wishlistItems.collectAsState()

    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue  = 1f,
        targetValue   = 0.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(VoidBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── Header ─────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(52.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick  = { navController.popBackStack() },
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
                                text       = "★ ${items.size} SAVED",
                                fontFamily = AppFonts.spaceMono,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color      = VoidBlack,
                                modifier   = Modifier.alpha(blinkAlpha)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text          = "WISHLIST",
                        fontFamily    = AppFonts.oswald,
                        fontSize      = 48.sp,
                        fontWeight    = FontWeight.Black,
                        color         = TeslaWhite,
                        letterSpacing = (-1).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text          = "SAVED ARTIFACTS // PENDING ACQUISITION",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 10.sp,
                        color         = TechSilver,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    0f to CyberAcid.copy(alpha = 0.9f),
                                    1f to Color.Transparent
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // ── Ticker ─────────────────────────────────────────────
            item { WishlistTicker() }

            // ── Empty state ────────────────────────────────────────
            if (items.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text          = "★",
                                fontFamily    = AppFonts.oswald,
                                fontSize      = 48.sp,
                                color         = TechSilver.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text          = "WISHLIST_EMPTY",
                                fontFamily    = AppFonts.oswald,
                                fontSize      = 24.sp,
                                fontWeight    = FontWeight.Black,
                                color         = TeslaWhite,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text       = "BẠN CHƯA LƯU ARTIFACT NÀO VÀO HỒ SƠ.",
                                fontFamily = AppFonts.spaceMono,
                                fontSize   = 11.sp,
                                color      = TechSilver,
                                textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(
                                modifier = Modifier
                                    .border(1.dp, CyberAcid)
                                    .clickable { navController.navigate(Routes.ARTIFACT_ARCHIVE) }
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text          = "[ ENTER THE VAULT ]",
                                    fontFamily    = AppFonts.spaceMono,
                                    fontSize      = 12.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = CyberAcid
                                )
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(items) { index, artifact ->
                    WishlistItem(
                        artifact = artifact,
                        index    = index,
                        onAcquire = {
                            if(artifact.affiliateLink.isNotEmpty()){
                                uriHandler.openUri(artifact.affiliateLink)
                            } else {
                                navController.navigate("${Routes.ARTIFACT_DETAIL}/${artifact.id}")
                            }
                        },
                        onRemove = { viewModel.removeFromWishlist(artifact.id) }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

// ── Wishlist item — compact horizontal card ───────────────────────
@Composable
private fun WishlistItem(
    artifact: AffiliateArtifact,
    index: Int,
    onAcquire: () -> Unit,
    onRemove: () -> Unit // Thêm hàm xóa
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(350),
        label         = "item_$index"
    )
    val offsetX by animateFloatAsState(
        targetValue   = if (visible) 0f else (-24f),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label         = "item_x_$index"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .graphicsLayer { translationX = offsetX }
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier.width(90.dp).height(110.dp)
        ) {
            AsyncImage(
                model              = artifact.imageUrl,
                contentDescription = artifact.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            // ID tag
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(CyberAcid)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text       = artifact.id,
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color      = VoidBlack
                )
            }
        }

        // Info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text          = artifact.category,
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 9.sp,
                color         = TechSilver,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text          = artifact.title,
                fontFamily    = AppFonts.oswald,
                fontSize      = 18.sp,
                fontWeight    = FontWeight.Black,
                color         = TeslaWhite,
                letterSpacing = (-0.5).sp,
                lineHeight    = 20.sp
            )
            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = artifact.price,
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = CyberAcid
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Nút xóa (Remove)
                    Box(
                        modifier = Modifier
                            .border(1.dp, DimBorder)
                            .clickable { onRemove() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text          = "[ X ]",
                            fontFamily    = AppFonts.spaceMono,
                            fontSize      = 9.sp,
                            color         = TechSilver,
                        )
                    }

                    // Nút mua (Acquire)
                    val isAffiliate = artifact.affiliateLink.isNotEmpty()
                    val btnText = if (isAffiliate) "EXT LINK" else "VIEW ITEM"
                    
                    Box(
                        modifier = Modifier
                            .border(1.dp, CyberAcid)
                            .clickable { onAcquire() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text          = btnText,
                            fontFamily    = AppFonts.spaceMono,
                            fontSize      = 9.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = CyberAcid,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Ticker ─────────────────────────────────────────────────────────
@Composable
private fun WishlistTicker() {
    val scrollState = rememberScrollState()
    val message = "RINNSAN_WISHLIST // SAVED ARTIFACTS // PENDING ACQUISITION // "
    LaunchedEffect(Unit) {
        delay(300)
        while (true) {
            val target = scrollState.maxValue
            scrollState.animateScrollTo(target, tween(
                ((target / 25f) * 1000).toInt().coerceAtLeast(5000), easing = LinearEasing
            ))
            scrollState.scrollTo(0)
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(32.dp)
            .background(Color(0xFF0A0A0A))
            .border(1.dp, CyberAcid.copy(alpha = 0.15f)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.horizontalScroll(scrollState, enabled = false)
                .padding(horizontal = 16.dp)
        ) {
            repeat(50) {
                Text(
                    text          = message,
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 9.sp,
                    color         = CyberAcid.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}