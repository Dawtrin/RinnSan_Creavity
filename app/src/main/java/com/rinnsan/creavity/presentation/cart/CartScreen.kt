package com.rinnsan.creavity.presentation.cart

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*
import kotlinx.coroutines.delay

private val DimBorder    = Color(0xFF2A2A2A)
private val ScanlineGray = Color(0xFF111111)

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val total      = viewModel.totalAmount

    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue  = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "blink"
    )

    Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
        if (cartItems.isEmpty()) {
            EmptyCartScreen(onBack = { navController.popBackStack() })
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 160.dp)
            ) {
                // ── Header ─────────────────────────────────────────
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
                                Icon(Icons.Default.ArrowBack, null, tint = TeslaWhite,
                                    modifier = Modifier.size(18.dp))
                            }

                            Box(
                                modifier = Modifier.background(CyberAcid)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("${cartItems.size} ITEM${if (cartItems.size != 1) "S" else ""}",
                                    fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold, color = VoidBlack,
                                    modifier = Modifier.alpha(blinkAlpha))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("ACQUISITION\nCARGO", fontFamily = AppFonts.oswald,
                            fontSize = 44.sp, fontWeight = FontWeight.Black,
                            color = TeslaWhite, letterSpacing = (-1).sp, lineHeight = 46.sp)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("REVIEW YOUR ITEMS // CONFIRM BEFORE CHECKOUT",
                            fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                            color = TechSilver, letterSpacing = 0.5.sp)

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(
                            Brush.horizontalGradient(0f to CyberAcid.copy(0.9f), 1f to Color.Transparent)
                        ))

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ── Cart items ─────────────────────────────────────
                items(cartItems, key = { it.cartKey }) { item ->
                    CartItemCard(
                        item     = item,
                        onRemove = { viewModel.removeFromCart(item.cartKey) },
                        onIncrease = { viewModel.updateQuantity(item.cartKey, +1) },
                        onDecrease = { viewModel.updateQuantity(item.cartKey, -1) }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // ── Order summary ──────────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OrderSummaryCard(items = cartItems, total = total)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ── Sticky bottom bar ──────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.2f to VoidBlack
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Total display
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("TOTAL", fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp, color = TechSilver, letterSpacing = 2.sp)
                        Text(formatPrice(total) + " VND",
                            fontFamily = AppFonts.oswald, fontSize = 24.sp,
                            fontWeight = FontWeight.Black, color = CyberAcid)
                    }

                    // Checkout button
                    val canCheckout = cartItems.isNotEmpty()
                    Box(
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                            .background(if (canCheckout) CyberAcid else TechSilver.copy(alpha = 0.3f))
                            .clickable(enabled = canCheckout) { navController.navigate(Routes.CHECKOUT) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("PROCEED TO CHECKOUT",
                                fontFamily = AppFonts.oswald, fontSize = 18.sp,
                                fontWeight = FontWeight.Black, color = VoidBlack,
                                letterSpacing = 1.sp)
                            Icon(Icons.Default.ArrowForward, null, tint = VoidBlack,
                                modifier = Modifier.size(20.dp))
                        }
                    }

                    // Continue shopping
                    Box(
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                            .border(1.dp, DimBorder)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CONTINUE BROWSING",
                            fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = TechSilver)
                    }
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// CART ITEM CARD
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun CartItemCard(
    item: CartItem,
    onRemove: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image
        Box(modifier = Modifier.size(80.dp).border(1.dp, DimBorder)) {
            AsyncImage(model = item.imageUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.archetype, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                color = CyberAcid.copy(alpha = 0.7f))
            Text(item.title, fontFamily = AppFonts.oswald, fontSize = 16.sp,
                fontWeight = FontWeight.Black, color = TeslaWhite,
                maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
            if (item.size.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .background(CyberAcid.copy(alpha = 0.12f))
                        .border(1.dp, CyberAcid.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text          = "SIZE: ${item.size}",
                        fontFamily    = AppFonts.spaceMono,
                        fontSize      = 9.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = CyberAcid,
                        letterSpacing = 1.sp
                    )
                }
            }
            Text(item.vendor, fontFamily = AppFonts.spaceMono, fontSize = 9.sp, color = TechSilver)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Price
                Text(formatPrice(item.price * item.quantity) + "đ",
                    fontFamily = AppFonts.spaceMono, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, color = CyberAcid)

                // Quantity controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuantityButton("-") { onDecrease() }
                    Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                        Text("${item.quantity}", fontFamily = AppFonts.oswald, fontSize = 16.sp,
                            fontWeight = FontWeight.Black, color = TeslaWhite)
                    }
                    QuantityButton("+") { onIncrease() }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Remove button
        IconButton(
            onClick  = onRemove,
            modifier = Modifier.size(32.dp).border(1.dp, Color(0xFFFF003C).copy(alpha = 0.3f))
        ) {
            Icon(Icons.Default.Delete, null, tint = Color(0xFFFF003C).copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun QuantityButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(28.dp).border(1.dp, DimBorder).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 14.sp,
            fontWeight = FontWeight.Bold, color = TeslaWhite)
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ORDER SUMMARY
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun OrderSummaryCard(items: List<CartItem>, total: Long) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(1.dp, DimBorder)
            .background(ScanlineGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text("ORDER SUMMARY", fontFamily = AppFonts.spaceMono,
            fontSize = 9.sp, color = TechSilver, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(12.dp))

        items.forEach { item ->
            Row(
                modifier              = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildString {
                        append(item.title)
                        if (item.size.isNotBlank()) append(" [${item.size}]")
                        append(" × ${item.quantity}")
                    },
                    fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                    color = TechSilver, modifier = Modifier.weight(1f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatPrice(item.price * item.quantity) + "đ",
                    fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                    color = TeslaWhite)
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder)
            .padding(vertical = 8.dp))
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Shipping", fontFamily = AppFonts.spaceMono, fontSize = 10.sp, color = TechSilver)
            Text("TBD", fontFamily = AppFonts.spaceMono, fontSize = 10.sp, color = TechSilver)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CyberAcid.copy(alpha = 0.3f)))
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TOTAL", fontFamily = AppFonts.oswald, fontSize = 16.sp,
                fontWeight = FontWeight.Black, color = TeslaWhite)
            Text(formatPrice(total) + "đ", fontFamily = AppFonts.oswald, fontSize = 16.sp,
                fontWeight = FontWeight.Black, color = CyberAcid)
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// EMPTY CART
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun EmptyCartScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(40.dp)
        ) {
            Text("CARGO\nEMPTY", fontFamily = AppFonts.oswald, fontSize = 48.sp,
                fontWeight = FontWeight.Black, color = TeslaWhite.copy(alpha = 0.1f),
                textAlign = TextAlign.Center, letterSpacing = (-1).sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chưa có sản phẩm nào trong giỏ hàng.",
                fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                color = TechSilver, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.border(1.dp, CyberAcid).clickable { onBack() }
                .padding(horizontal = 24.dp, vertical = 12.dp)) {
                Text("[ BACK TO VAULT ]", fontFamily = AppFonts.spaceMono,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberAcid)
            }
        }
    }
}

fun formatPrice(amount: Long): String {
    return String.format("%,d", amount).replace(",", ".")
}