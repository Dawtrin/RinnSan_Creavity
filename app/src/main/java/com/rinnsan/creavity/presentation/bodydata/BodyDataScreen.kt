package com.rinnsan.creavity.presentation.bodydata

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel // <-- Thư viện tiêm ViewModel
import androidx.navigation.NavController
import com.rinnsan.creavity.core.theme.*

private val DimBorder    = Color(0xFF2A2A2A)
private val ScanlineGray = Color(0xFF111111)
private val FieldBg      = Color(0xFF0D0D0D)

// ── Data model ─────────────────────────────────────────────────────
data class BodyMeasurement(
    val key: String,       // internal
    val label: String,     // display
    val unit: String,
    val hint: String
)

private val MEASUREMENTS = listOf(
    BodyMeasurement("height",  "CHIỀU CAO",      "cm",   "e.g. 175"),
    BodyMeasurement("weight",  "CÂN NẶNG",       "kg",   "e.g. 65"),
    BodyMeasurement("chest",   "VÒNG NGỰC",      "cm",   "e.g. 96"),
    BodyMeasurement("waist",   "VÒNG EO",        "cm",   "e.g. 80"),
    BodyMeasurement("hips",    "VÒNG HÔNG",      "cm",   "e.g. 95"),
    BodyMeasurement("shoulder","NGANG VAI",      "cm",   "e.g. 44"),
    BodyMeasurement("sleeve",  "DÀI TAY",        "cm",   "e.g. 62"),
    BodyMeasurement("inseam",  "DÀI TRONG QUẦN", "cm",   "e.g. 76"),
    BodyMeasurement("foot",    "CỠ GIÀY",        "EU",   "e.g. 42")
)

@Composable
fun BodyDataScreen(
    navController: NavController,
    viewModel: BodyDataViewModel = hiltViewModel() // <-- Gắn động cơ ViewModel vào đây
) {
    val values = remember {
        mutableStateMapOf<String, String>().also { map ->
            MEASUREMENTS.forEach { map[it.key] = "" }
        }
    }

    var saved by remember { mutableStateOf(false) }
    var fitLabel by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // ── LẮNG NGHE DỮ LIỆU TỪ ĐÁM MÂY ───────────────────────────────
    val cloudData by viewModel.bodyData.collectAsState()
    val isSavedOnCloud by viewModel.isSaved.collectAsState()

    // Tự động điền dữ liệu vào các ô nếu trên Cloud đã có sẵn
    LaunchedEffect(cloudData) {
        if (cloudData.isNotEmpty()) {
            cloudData.forEach { (key, value) ->
                values[key] = value
            }
        }
    }

    // Đổi màu nút Save khi Firebase báo lưu thành công
    LaunchedEffect(isSavedOnCloud) {
        saved = isSavedOnCloud
    }

    // Hiệu ứng nhấp nháy cho text góc trên
    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue  = 1f,
        targetValue   = 0.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    // Tính fit label dựa trên chiều cao + cân nặng (BMI-style)
    LaunchedEffect(values["height"], values["weight"]) {
        val h = values["height"]?.toFloatOrNull() ?: 0f
        val w = values["weight"]?.toFloatOrNull() ?: 0f
        fitLabel = if (h > 0 && w > 0) {
            val bmi = w / ((h / 100f) * (h / 100f))
            when {
                bmi < 18.5f -> "XS — SLIM BUILD"
                bmi < 23f   -> "S/M — STANDARD FIT"
                bmi < 27f   -> "L — REGULAR BUILD"
                else        -> "XL — OVERSIZED FIT"
            }
        } else ""
    }

    Box(
        modifier = Modifier.fillMaxSize().background(VoidBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── Top bar ────────────────────────────────────────────
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
                        text       = "// BODY_DATA",
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = VoidBlack,
                        modifier   = Modifier.alpha(blinkAlpha)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Header ─────────────────────────────────────────────
            Text(
                text          = "BODY\nDATA",
                fontFamily    = AppFonts.oswald,
                fontSize      = 48.sp,
                fontWeight    = FontWeight.Black,
                color         = TeslaWhite,
                letterSpacing = (-1).sp,
                lineHeight    = 50.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text          = "SỐ ĐO CƠ THỂ // FIT CALIBRATION SYSTEM",
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 10.sp,
                color         = TechSilver,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberAcid.copy(alpha = 0.06f))
                    .border(1.dp, CyberAcid.copy(alpha = 0.25f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text       = "//",
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = CyberAcid
                )
                Text(
                    text       = "Số đo giúp AI Stylist gợi ý size chính xác hơn khi bạn xem sản phẩm.",
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 10.sp,
                    color      = TechSilver,
                    lineHeight = 18.sp,
                    modifier   = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Acid divider
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

            Spacer(modifier = Modifier.height(28.dp))

            // ── Fit label chip (auto computed) ─────────────────────
            if (fitLabel.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ScanlineGray)
                        .border(1.dp, CyberAcid.copy(alpha = 0.4f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text          = "COMPUTED FIT",
                                fontFamily    = AppFonts.spaceMono,
                                fontSize      = 9.sp,
                                color         = TechSilver,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text          = fitLabel,
                                fontFamily    = AppFonts.oswald,
                                fontSize      = 22.sp,
                                fontWeight    = FontWeight.Black,
                                color         = CyberAcid,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text       = "AUTO",
                            fontFamily = AppFonts.spaceMono,
                            fontSize   = 9.sp,
                            color      = CyberAcid.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Measurement fields ─────────────────────────────────
            MEASUREMENTS.forEach { measurement ->
                BodyDataField(
                    measurement = measurement,
                    value       = values[measurement.key] ?: "",
                    onValueChange = {
                        values[measurement.key] = it
                        saved = false // Nếu sửa số liệu thì nút Save trở lại bình thường
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Save button ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(if (saved) CyberAcid.copy(alpha = 0.15f) else CyberAcid)
                    .border(1.dp, CyberAcid)
                    .clickable {
                        // KÍCH HOẠT LỆNH BẮN DỮ LIỆU LÊN ĐÁM MÂY
                        if (!saved) {
                            viewModel.saveDataToCloud(values.toMap())
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text          = if (saved) "// DATA SAVED TO CLOUD //" else "SAVE BODY DATA",
                    fontFamily    = AppFonts.oswald,
                    fontSize      = 18.sp,
                    fontWeight    = FontWeight.Black,
                    color         = if (saved) CyberAcid else VoidBlack,
                    letterSpacing = 2.sp
                )
            }

            if (saved) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text          = "Dữ liệu đã được đồng bộ an toàn lên Cloud Firestore.",
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 10.sp,
                    color         = TechSilver.copy(alpha = 0.6f),
                    textAlign     = TextAlign.Center,
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// ── Single measurement input field ─────────────────────────────────
@Composable
private fun BodyDataField(
    measurement: BodyMeasurement,
    value: String,
    onValueChange: (String) -> Unit
) {
    val isFilled = value.isNotEmpty()

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label block
        Column(modifier = Modifier.width(120.dp)) {
            Text(
                text          = measurement.label,
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Bold,
                color         = if (isFilled) TeslaWhite else TechSilver,
                letterSpacing = 0.5.sp
            )
            Text(
                text       = measurement.unit,
                fontFamily = AppFonts.spaceMono,
                fontSize   = 9.sp,
                color      = CyberAcid.copy(alpha = 0.6f)
            )
        }

        // Input box
        Box(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = if (isFilled) CyberAcid.copy(alpha = 0.5f) else DimBorder
                )
                .background(FieldBg)
        ) {
            TextField(
                value         = value,
                onValueChange = onValueChange,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                placeholder   = {
                    Text(
                        text       = measurement.hint,
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 12.sp,
                        color      = TechSilver.copy(alpha = 0.3f)
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 14.sp,
                    color      = TeslaWhite
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                trailingIcon = {
                    Text(
                        text       = measurement.unit,
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 11.sp,
                        color      = TechSilver.copy(alpha = 0.5f),
                        modifier   = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = FieldBg,
                    unfocusedContainerColor = FieldBg,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = CyberAcid
                )
            )
        }
    }
}