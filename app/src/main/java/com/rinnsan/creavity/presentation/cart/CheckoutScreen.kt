package com.rinnsan.creavity.presentation.cart

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*

private val DimBorder    = Color(0xFF2A2A2A)
private val ScanlineGray = Color(0xFF111111)
private val FieldBg      = Color(0xFF0D0D0D)

@Composable
fun CheckoutScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartItems     by viewModel.cartItems.collectAsState()
    val address       by viewModel.address.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val shippingFee   by viewModel.shippingFee.collectAsState()
    val isCalculatingLocation by viewModel.isCalculatingLocation.collectAsState()
    val focusManager  = LocalFocusManager.current

    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                          permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                viewModel.fetchLocationAndCalculateFee {
                    android.widget.Toast.makeText(context, "Không thể lấy vị trí. Vui lòng bật GPS và thử lại.", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(context, "Vui lòng cấp quyền vị trí để tính phí ship", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Local editable state
    var fullName  by remember { mutableStateOf(address.fullName) }
    var phone     by remember { mutableStateOf(address.phone) }
    var street    by remember { mutableStateOf(address.street) }
    var district  by remember { mutableStateOf(address.district) }
    var city      by remember { mutableStateOf(address.city) }

    LaunchedEffect(address) {
        fullName = address.fullName
        phone    = address.phone
        street   = address.street
        district = address.district
        city     = address.city
    }

    val total = viewModel.totalAmount

    // Navigate on success
    LaunchedEffect(checkoutState) {
        if (checkoutState is CheckoutState.Success) {
            val orderId = (checkoutState as CheckoutState.Success).orderId
            navController.navigate("${Routes.ORDER_SUCCESS}/$orderId") {
                popUpTo(Routes.CART) { inclusive = true }
            }
            viewModel.resetCheckoutState()
        }
    }

    val isLoading = checkoutState is CheckoutState.Loading

    Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── Top bar ────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() },
                    modifier = Modifier.size(36.dp).border(1.dp, DimBorder)) {
                    Icon(Icons.Default.ArrowBack, null, tint = TeslaWhite,
                        modifier = Modifier.size(18.dp))
                }
                Box(modifier = Modifier.background(CyberAcid)
                    .padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("STEP 2 OF 2", fontFamily = AppFonts.spaceMono,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VoidBlack)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("CHECKOUT", fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                color = CyberAcid, letterSpacing = 3.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("CONFIRM\nORDER", fontFamily = AppFonts.oswald, fontSize = 44.sp,
                fontWeight = FontWeight.Black, color = TeslaWhite,
                letterSpacing = (-1).sp, lineHeight = 46.sp)

            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(
                Brush.horizontalGradient(0f to CyberAcid.copy(0.9f), 1f to Color.Transparent)))
            Spacer(modifier = Modifier.height(28.dp))

            // ── Progress steps ─────────────────────────────────────
            CheckoutProgressBar(currentStep = 2)
            Spacer(modifier = Modifier.height(28.dp))

            // ── Shipping Address ───────────────────────────────────
            SectionHeader(
                icon  = Icons.Default.LocationOn,
                label = "SHIPPING ADDRESS"
            )
            Spacer(modifier = Modifier.height(14.dp))

            CheckoutField(label = "HỌ VÀ TÊN *", value = fullName,
                placeholder = "Nguyễn Văn A",
                onValueChange = { fullName = it },
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) })

            Spacer(modifier = Modifier.height(10.dp))

            CheckoutField(label = "SỐ ĐIỆN THOẠI *", value = phone,
                placeholder = "0912 345 678",
                keyboardType = KeyboardType.Phone,
                onValueChange = { phone = it },
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) })

            Spacer(modifier = Modifier.height(16.dp))

            // ── Auto-fill Location Button ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberAcid.copy(0.4f))
                    .background(CyberAcid.copy(0.05f))
                    .clickable {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCalculatingLocation) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = CyberAcid, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Text("ĐANG TÌM VỊ TRÍ...", fontFamily = AppFonts.spaceMono, fontSize = 10.sp, color = CyberAcid)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.MyLocation, "Get Location", tint = CyberAcid, modifier = Modifier.size(16.dp))
                        Text("TỰ ĐỘNG ĐIỀN ĐỊA CHỈ BẰNG GPS", fontFamily = AppFonts.spaceMono, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberAcid)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CheckoutField(label = "ĐỊA CHỈ CỤ THỂ *", value = street,
                placeholder = "123 Nguyễn Huệ, Phường Bến Nghé",
                onValueChange = { street = it },
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) })

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CheckoutField(label = "QUẬN/HUYỆN", value = district,
                    placeholder = "Quận 1",
                    onValueChange = { district = it },
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                    modifier = Modifier.weight(1f))
                CheckoutField(label = "TỈNH/TP *", value = city,
                    placeholder = "TP. Đà Nẵng",
                    onValueChange = { 
                        city = it
                        viewModel.calculateShippingFeeFromCity(it)
                    },
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() },
                    modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Payment method ─────────────────────────────────────
            SectionHeader(
                icon  = Icons.Default.Payment,
                label = "PAYMENT METHOD"
            )
            Spacer(modifier = Modifier.height(14.dp))

            PaymentOption(
                selected      = paymentMethod == "COD",
                method        = "COD",
                title         = "THANH TOÁN KHI NHẬN HÀNG",
                subtitle      = "Cash On Delivery — An toàn, không cần thẻ",
                icon          = Icons.Default.LocalShipping,
                color         = Color(0xFFFF8C00),
                onSelect      = { viewModel.setPaymentMethod("COD") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            PaymentOption(
                selected      = paymentMethod == "Banking",
                method        = "Banking",
                title         = "CHUYỂN KHOẢN NGÂN HÀNG",
                subtitle      = "Banking Transfer — Xác nhận sau khi chuyển khoản",
                icon          = Icons.Default.AccountBalance,
                color         = Color(0xFF00BFFF),
                onSelect      = { viewModel.setPaymentMethod("Banking") }
            )

            // Banking info box
            if (paymentMethod == "Banking") {
                Spacer(modifier = Modifier.height(10.dp))
                BankingInfoBox(total = total, phone = phone)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Order summary ──────────────────────────────────────
            SectionHeader(icon = Icons.Default.ShoppingBag, label = "ORDER SUMMARY")
            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.fillMaxWidth().border(1.dp, DimBorder)
                .background(ScanlineGray).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cartItems.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        val sizeLabel = if (item.size.isNotBlank()) " [${item.size}]" else ""
                        Text("${item.title}$sizeLabel × ${item.quantity}",
                            fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                            color = TechSilver, modifier = Modifier.weight(1f),
                            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(formatPrice(item.price * item.quantity) + "đ",
                            fontFamily = AppFonts.spaceMono, fontSize = 10.sp, color = TeslaWhite)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SHIPPING FEE", fontFamily = AppFonts.spaceMono, fontSize = 10.sp, color = TechSilver)
                    Text(if (shippingFee > 0) "${formatPrice(shippingFee)}đ" else "Tính theo vị trí", fontFamily = AppFonts.spaceMono, fontSize = 10.sp, color = TeslaWhite)
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DimBorder))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL", fontFamily = AppFonts.oswald, fontSize = 18.sp,
                        fontWeight = FontWeight.Black, color = TeslaWhite)
                    Text(formatPrice(total) + "đ", fontFamily = AppFonts.oswald, fontSize = 18.sp,
                        fontWeight = FontWeight.Black, color = CyberAcid)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Error message ──────────────────────────────────────
            if (checkoutState is CheckoutState.Error) {
                Row(modifier = Modifier.fillMaxWidth()
                    .background(Color(0xFFFF003C).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFFFF003C).copy(alpha = 0.4f))
                    .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("//", fontFamily = AppFonts.spaceMono, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFFFF003C))
                    Text((checkoutState as CheckoutState.Error).message,
                        fontFamily = AppFonts.spaceMono, fontSize = 11.sp,
                        color = Color(0xFFFF003C), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Place order button ─────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp)
                    .background(if (isLoading) CyberAcid.copy(alpha = 0.6f) else CyberAcid)
                    .clickable(enabled = !isLoading) {
                        viewModel.updateAddress(address.copy(
                            fullName = fullName,
                            phone = phone,
                            street = street,
                            district = district,
                            city = city
                        ))
                        viewModel.placeOrder()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = VoidBlack,
                            modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("PROCESSING ORDER...", fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VoidBlack)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("[ PLACE ORDER ]", fontFamily = AppFonts.oswald, fontSize = 20.sp,
                            fontWeight = FontWeight.Black, color = VoidBlack, letterSpacing = 1.sp)
                        Icon(Icons.Default.ArrowForward, null, tint = VoidBlack,
                            modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COMPONENTS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun CheckoutProgressBar(currentStep: Int) {
    val steps = listOf("REVIEW CART", "CONFIRM ORDER")
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, label ->
            val isDone = idx + 1 <= currentStep
            val isCurr = idx + 1 == currentStep
            Column(
                modifier            = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(if (isCurr) 32.dp else 24.dp)
                    .background(if (isDone) CyberAcid.copy(0.15f) else DimBorder)
                    .border(if (isCurr) 2.dp else 1.dp, if (isDone) CyberAcid else DimBorder),
                    contentAlignment = Alignment.Center) {
                    Text("${idx+1}", fontFamily = AppFonts.oswald,
                        fontSize = if (isCurr) 14.sp else 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDone) CyberAcid else TechSilver.copy(0.3f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, fontFamily = AppFonts.spaceMono, fontSize = 7.sp,
                    color = if (isDone) CyberAcid else TechSilver.copy(0.3f))
            }
            if (idx < steps.size - 1) {
                Box(modifier = Modifier.weight(0.5f).height(1.dp)
                    .background(if (currentStep > idx + 1) CyberAcid else DimBorder))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(4.dp).background(CyberAcid))
        Icon(icon, null, tint = CyberAcid, modifier = Modifier.size(14.dp))
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = CyberAcid, letterSpacing = 2.sp)
        Box(modifier = Modifier.weight(1f).height(1.dp).background(
            Brush.horizontalGradient(0f to CyberAcid.copy(0.3f), 1f to Color.Transparent)))
    }
}

@Composable
private fun CheckoutField(
    label: String, value: String, placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = TechSilver, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(5.dp))
        Box(modifier = Modifier.fillMaxWidth().border(
            width = 1.dp,
            color = if (value.isNotEmpty()) CyberAcid.copy(0.4f) else DimBorder
        ).background(FieldBg)) {
            TextField(
                value = value, onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text(placeholder, fontFamily = AppFonts.spaceMono,
                    fontSize = 11.sp, color = TechSilver.copy(0.3f)) },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = AppFonts.spaceMono, fontSize = 13.sp, color = TeslaWhite),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onNext = { onImeAction() }, onDone = { onImeAction() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FieldBg, unfocusedContainerColor = FieldBg,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = CyberAcid)
            )
        }
    }
}

@Composable
private fun PaymentOption(
    selected: Boolean, method: String, title: String, subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color, onSelect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .border(width = if (selected) 1.dp else 1.dp,
                color = if (selected) color else DimBorder)
            .background(if (selected) color.copy(0.06f) else ScanlineGray)
            .clickable { onSelect() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Radio indicator
        Box(modifier = Modifier.size(20.dp).border(2.dp,
            if (selected) color else DimBorder),
            contentAlignment = Alignment.Center) {
            if (selected) Box(modifier = Modifier.size(10.dp).background(color))
        }

        // Icon
        Box(modifier = Modifier.size(36.dp).background(color.copy(0.1f))
            .border(1.dp, color.copy(0.3f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, color = if (selected) TeslaWhite else TechSilver)
            Text(subtitle, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
                color = TechSilver.copy(0.6f))
        }
    }
}

@Composable
private fun BankingInfoBox(total: Long = 0L, phone: String = "") {
    // MB Bank — VietQR generate động theo số tiền + nội dung
    val bankId      = "MB"                           // Mã MB Bank trên VietQR
    val accountNo   = "0396704484"                   // ← Thay STK MB thật vào đây
    val accountName = "RINNSAN CREAVITY"
    val addInfo     = "RINNSAN ${phone.ifBlank { "KHACHHANG" }}".take(25)
    val qrUrl = "https://img.vietqr.io/image/$bankId-$accountNo-compact2.png" +
            "?amount=$total" +
            "&addInfo=${java.net.URLEncoder.encode(addInfo, "UTF-8")}" +
            "&accountName=${java.net.URLEncoder.encode(accountName, "UTF-8")}"

    val bankColor = Color(0xFF9B59B6) // Tím — màu đặc trưng MB Bank

    Column(
        modifier = Modifier.fillMaxWidth()
            .background(bankColor.copy(alpha = 0.06f))
            .border(1.dp, bankColor.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text          = "// CHUYỂN KHOẢN MB BANK",
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 9.sp,
            color         = bankColor,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment     = Alignment.Top
        ) {
            // QR Code
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color.White)
                        .border(2.dp, bankColor.copy(alpha = 0.6f))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model              = qrUrl,
                        contentDescription = "QR MB Bank",
                        contentScale       = ContentScale.Fit,
                        modifier           = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text          = "QUÉT ĐỂ THANH TOÁN",
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 8.sp,
                    color         = bankColor,
                    letterSpacing = 0.5.sp
                )
            }

            // Thông tin tài khoản
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BankRow(label = "NGÂN HÀNG",     value = "MB Bank")
                BankRow(label = "SỐ TÀI KHOẢN",  value = accountNo)
                BankRow(label = "CHỦ TÀI KHOẢN", value = accountName)
                BankRow(label = "SỐ TIỀN",        value = "${formatPrice(total)}đ")
                BankRow(label = "NỘI DUNG",       value = addInfo)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(bankColor.copy(alpha = 0.2f)))
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text       = "* QR đã điền sẵn số tiền và nội dung. Xác nhận sau khi admin kiểm tra.",
            fontFamily = AppFonts.spaceMono,
            fontSize   = 9.sp,
            color      = TechSilver.copy(alpha = 0.5f),
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun BankRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontFamily = AppFonts.spaceMono, fontSize = 9.sp,
            color = TechSilver.copy(0.6f))
        Text(value, fontFamily = AppFonts.spaceMono, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = TeslaWhite)
    }
}