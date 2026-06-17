package com.rinnsan.creavity.presentation.brand

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rinnsan.creavity.core.router.Routes
import com.rinnsan.creavity.core.theme.*
import kotlinx.coroutines.delay

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// LOCAL TOKENS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
private val DimBorder    = Color(0xFF2A2A2A)
private val ScanlineGray = Color(0xFF111111)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// DATA
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

data class ArchetypeCard(
    val code: String,
    val name: String,
    val subtitle: String,
    val manifesto: String,
    val traits: List<String>,
    val accentColor: Color
)

data class BrandEntry(
    val name: String,
    val origin: String,
    val descriptor: String
)

private val ARCHETYPES = listOf(
    ArchetypeCard(
        code      = "01",
        name      = "GHOST",
        subtitle  = "LOW VISIBILITY // SHADOW PROTOCOL",
        manifesto = "Không được nhìn thấy là quyền lực tối thượng. Ghost không trốn tránh — Ghost chọn khi nào xuất hiện. Mỗi lớp vải là một lớp giáp vô hình. Mỗi màu tối là một tuyên ngôn im lặng. Trong thành phố đầy ánh đèn neon, trở thành bóng tối là sự phản kháng tinh tế nhất.",
        traits    = listOf("ALL-BLACK PALETTE", "MINIMAL SILHOUETTE", "FACE COVERAGE", "ZERO NOISE"),
        accentColor = Color(0xFFCCFF00)
    ),
    ArchetypeCard(
        code      = "02",
        name      = "OPERATOR",
        subtitle  = "TACTICAL BUILD // FIELD COMMAND",
        manifesto = "Operator không mặc để trông đẹp. Operator mặc để sẵn sàng. Mọi túi đều có lý do tồn tại. Mọi dây đai đều phục vụ một chức năng. Đây là bộ giáp của người không bao giờ bị bắt gặp không chuẩn bị — trong văn phòng, trên phố, hay trong bóng tối đô thị.",
        traits    = listOf("MODULAR VESTS", "CARGO UTILITY", "STRUCTURED LAYERS", "COMBAT FOOTWEAR"),
        accentColor = Color(0xFFCCFF00)
    ),
    ArchetypeCard(
        code      = "03",
        name      = "GLITCH",
        subtitle  = "CHAOS LAYER // SIGNAL DISTORTION",
        manifesto = "Glitch là lỗi hệ thống được nâng lên thành nghệ thuật. Khi mọi người mặc đồng phục của sự hoàn hảo, Glitch mặc sự hỗn loạn có kiểm soát. Asymmetry không phải lỗi — đó là chữ ký. Màu sắc xung đột không phải sai — đó là phản biện. Glitch refuse to render correctly.",
        traits    = listOf("ASYMMETRIC CUTS", "DISTRESSED FABRIC", "CLASHING LAYERS", "ANTI-UNIFORM"),
        accentColor = Color(0xFFFF003C)
    ),
    ArchetypeCard(
        code      = "04",
        name      = "NOMAD",
        subtitle  = "ALL-TERRAIN // TRANSIT OPTIMIZED",
        manifesto = "Nomad không có địa chỉ cố định — chỉ có tọa độ tiếp theo. Mỗi chiếc túi là một căn nhà thu nhỏ. Mỗi lớp jacket là một biên giới di động. Nomad hiểu rằng tự do không đến từ việc ở một nơi — mà từ việc luôn sẵn sàng rời đi.",
        traits    = listOf("MULTI-POCKET SYSTEM", "WEATHER RESISTANCE", "PACKABLE LAYERS", "EDC CARRY"),
        accentColor = Color(0xFFCCFF00)
    )
)

private val BRANDS = listOf(
    BrandEntry("ACRONYM",      "MUNICH, DE",   "Pinnacle techwear. Modular systems. No compromises."),
    BrandEntry("Y-3",          "TOKYO / PARIS","Yohji Yamamoto × Adidas. Sport meets avant-garde."),
    BrandEntry("STONE ISLAND", "RAVARINO, IT", "Material obsession. Dyeing-after-construction."),
    BrandEntry("NIKE ACG",     "PORTLAND, US", "All Conditions Gear. Urban wilderness ready."),
    BrandEntry("JULIUS",       "TOKYO, JP",    "Dark romanticism. Distorted silhouettes."),
    BrandEntry("GUERRILLA-GROUP", "TAIPEI, TW","Underground techwear. Raw and functional."),
    BrandEntry("RICK OWENS",   "PARIS, FR",    "Brutalist luxury. The original darkwear god."),
    BrandEntry("VEILANCE",     "VANCOUVER, CA","Arc'teryx's urban stealth line. Weather + style."),
)

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ROOT SCREEN
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
fun BrandScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

            // 1. HEADER
            item { BrandHeroHeader(onBackClick = { navController.popBackStack() }) }

            // 2. TICKER
            item { BrandTicker() }

            // 3. MANIFESTO BLOCK
            item {
                Spacer(modifier = Modifier.height(48.dp))
                ManifestoBlock()
            }

            // 4. DIVIDER LABEL
            item {
                Spacer(modifier = Modifier.height(48.dp))
                SectionDivider(label = "IDENTITY SYSTEM")
                Spacer(modifier = Modifier.height(32.dp))
            }

            // 5. APP INTRO (what is RinnSan)
            item { AppIntroBlock() }

            // 6. DIVIDER LABEL
            item {
                Spacer(modifier = Modifier.height(48.dp))
                SectionDivider(label = "ARCHETYPE INDEX")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "BỐN NGUYÊN MẪU. BỐN CON ĐƯỜNG. MỘT HỆ THỐNG.",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = TechSilver,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 7. ARCHETYPE CARDS
            items(ARCHETYPES) { archetype ->
                ArchetypeCardItem(card = archetype, onExplore = {
                    navController.navigate(Routes.STYLIST)
                })
                Spacer(modifier = Modifier.height(2.dp))
            }

            // 8. DIVIDER LABEL
            item {
                Spacer(modifier = Modifier.height(48.dp))
                SectionDivider(label = "SIGNAL BRANDS")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "NHỮNG THƯƠNG HIỆU ĐỊNH HÌNH NGÔN NGỮ THỜI TRANG NÀY.",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 10.sp,
                    color = TechSilver,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 9. BRAND LIST
            items(BRANDS) { brand ->
                BrandRow(entry = brand)
            }

            // 10. CLOSING STATEMENT
            item {
                Spacer(modifier = Modifier.height(48.dp))
                ClosingStatement()
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 1. HERO HEADER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun BrandHeroHeader(onBackClick: () -> Unit) {
    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue  = 1f,
        targetValue   = 0.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(VoidBlack)
    ) {
        // Big ghost text
        Text(
            text = "ORIGIN",
            fontFamily    = AppFonts.oswald,
            fontSize      = 120.sp,
            fontWeight    = FontWeight.Black,
            color         = TeslaWhite.copy(alpha = 0.03f),
            modifier      = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-8).dp, y = 20.dp),
            letterSpacing = (-4).sp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, DimBorder)
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
                        text       = "// THE ORIGIN",
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color      = VoidBlack,
                        modifier   = Modifier.alpha(blinkAlpha)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text          = "THE ORIGIN",
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 11.sp,
                color         = CyberAcid,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text          = "RINNSAN\nCREAVITY",
                fontFamily    = AppFonts.oswald,
                fontSize      = 52.sp,
                fontWeight    = FontWeight.Black,
                color         = TeslaWhite,
                letterSpacing = (-1).sp,
                lineHeight    = 54.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text       = "IDENTITY-DRIVEN FASHION INTELLIGENCE // DA NANG → TOKYO",
                fontFamily = AppFonts.spaceMono,
                fontSize   = 10.sp,
                color      = TechSilver,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Acid divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f   to CyberAcid.copy(alpha = 0.9f),
                            0.5f to CyberAcid.copy(alpha = 0.3f),
                            1f   to Color.Transparent
                        )
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 2. TICKER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun BrandTicker() {
    val scrollState = rememberScrollState()
    val message = "RINNSAN_CREAVITY // TECHWEAR // DARKWEAR // AFFILIATE NAVIGATOR // " +
            "DA NANG ORIGIN // TOKYO SPIRIT // IDENTITY FIRST // "

    LaunchedEffect(Unit) {
        delay(300)
        while (true) {
            val target = scrollState.maxValue
            scrollState.animateScrollTo(
                value         = target,
                animationSpec = tween(
                    durationMillis = ((target / 30f) * 1000).toInt().coerceAtLeast(8000),
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
            .border(width = 1.dp, color = CyberAcid.copy(alpha = 0.2f)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState, enabled = false)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(50) {
                Text(
                    text          = message,
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 10.sp,
                    color         = CyberAcid.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 3. MANIFESTO BLOCK
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun ManifestoBlock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Opening quote marker
        Text(
            text       = "//",
            fontFamily = AppFonts.spaceMono,
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold,
            color      = CyberAcid
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Thời trang không phải là thứ bạn mặc.\nĐó là ngôn ngữ bạn nói.",
            fontFamily    = AppFonts.oswald,
            fontSize      = 34.sp,
            fontWeight    = FontWeight.Black,
            color         = TeslaWhite,
            letterSpacing = (-0.5).sp,
            lineHeight    = 38.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Trong một thế giới mà mọi người đều mặc những gì được quảng cáo, " +
                    "RinnSan tồn tại để hỏi một câu khác: BẠN LÀ AI?\n\n" +
                    "Chúng tôi không bán quần áo. Chúng tôi giải mã danh tính của bạn " +
                    "và tìm ra những artifacts phù hợp — từ hàng ngàn sản phẩm Techwear, " +
                    "Darkwear, và Streetwear được lựa chọn kỹ lưỡng trên toàn cầu.\n\n" +
                    "Mỗi người dùng là một archetype. Mỗi archetype là một tuyên ngôn. " +
                    "Mỗi tuyên ngôn xứng đáng được mặc ra đường.",
            fontFamily = AppFonts.spaceMono,
            fontSize   = 12.sp,
            color      = TechSilver,
            lineHeight = 22.sp
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 4. APP INTRO BLOCK
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun AppIntroBlock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // 3 stat chips
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            StatBlock(
                value = "04",
                label = "ARCHETYPES",
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(DimBorder)
                    .align(Alignment.CenterVertically)
            )
            StatBlock(
                value = "AI",
                label = "STYLIST ENGINE",
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(DimBorder)
                    .align(Alignment.CenterVertically)
            )
            StatBlock(
                value = "∞",
                label = "AFFILIATE LINKS",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DimBorder)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // How it works
        Text(
            text          = "HOW IT WORKS",
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 10.sp,
            color         = CyberAcid,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        listOf(
            "01" to "Làm bài Identity Test — một chuỗi câu hỏi được thiết kế để phân tích phong cách, thói quen và thẩm mỹ cá nhân của bạn.",
            "02" to "Hệ thống xác định Archetype của bạn — GHOST, OPERATOR, GLITCH, hay NOMAD — và tính toán chỉ số Confidence.",
            "03" to "AI Stylist phân tích profile và đề xuất outfit, xu hướng phù hợp với ngôn ngữ thời trang của bạn.",
            "04" to "Vault mở ra — kho lưu trữ sản phẩm được lọc theo Archetype, với link affiliate trực tiếp đến Shopee và các platform khác."
        ).forEach { (num, desc) ->
            HowItWorksRow(number = num, description = desc)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier          = modifier
            .background(ScanlineGray)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text       = value,
            fontFamily = AppFonts.oswald,
            fontSize   = 28.sp,
            fontWeight = FontWeight.Black,
            color      = CyberAcid
        )
        Text(
            text          = label,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 9.sp,
            color         = TechSilver,
            letterSpacing = 0.5.sp,
            textAlign     = TextAlign.Center,
            modifier      = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun HowItWorksRow(number: String, description: String) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text       = number,
            fontFamily = AppFonts.spaceMono,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = CyberAcid,
            modifier   = Modifier.width(36.dp)
        )
        Text(
            text       = description,
            fontFamily = AppFonts.spaceMono,
            fontSize   = 11.sp,
            color      = TechSilver,
            lineHeight = 20.sp,
            modifier   = Modifier.weight(1f)
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 5. SECTION DIVIDER
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun SectionDivider(label: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(CyberAcid)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text          = label,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            color         = CyberAcid,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        0f to CyberAcid.copy(alpha = 0.4f),
                        1f to Color.Transparent
                    )
                )
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 6. ARCHETYPE CARD ITEM
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun ArchetypeCardItem(card: ArchetypeCard, onExplore: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(500)) + expandVertically(tween(500))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VoidBlack)
                .border(width = 1.dp, color = DimBorder)
        ) {
            // Top accent line — full width, archetype color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        if (card.name == "GLITCH") Color(0xFFFF003C)
                        else CyberAcid
                    )
            )

            Column(modifier = Modifier.padding(24.dp)) {
                // Code + Name row
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text       = card.code,
                        fontFamily = AppFonts.spaceMono,
                        fontSize   = 13.sp,
                        color      = if (card.name == "GLITCH") Color(0xFFFF003C) else CyberAcid,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(bottom = 6.dp, end = 16.dp)
                    )
                    Text(
                        text          = card.name,
                        fontFamily    = AppFonts.oswald,
                        fontSize      = 48.sp,
                        fontWeight    = FontWeight.Black,
                        color         = TeslaWhite,
                        letterSpacing = (-1).sp,
                        lineHeight    = 50.sp,
                        modifier      = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text          = card.subtitle,
                    fontFamily    = AppFonts.spaceMono,
                    fontSize      = 10.sp,
                    color         = TechSilver,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DimBorder)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Manifesto
                Text(
                    text       = card.manifesto,
                    fontFamily = AppFonts.spaceMono,
                    fontSize   = 11.sp,
                    color      = TechSilver,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Traits
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    card.traits.take(2).forEach { trait ->
                        TraitTag(text = trait, isGlitch = card.name == "GLITCH")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    card.traits.drop(2).forEach { trait ->
                        TraitTag(text = trait, isGlitch = card.name == "GLITCH")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = if (card.name == "GLITCH") Color(0xFFFF003C) else CyberAcid
                        )
                        .clickable { onExplore() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text          = "DISCOVER YOUR ARCHETYPE",
                            fontFamily    = AppFonts.oswald,
                            fontSize      = 14.sp,
                            fontWeight    = FontWeight.Bold,
                            color         = if (card.name == "GLITCH") Color(0xFFFF003C) else CyberAcid,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector        = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint               = if (card.name == "GLITCH") Color(0xFFFF003C) else CyberAcid,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TraitTag(text: String, isGlitch: Boolean) {
    Box(
        modifier = Modifier
            .background(ScanlineGray)
            .border(
                width = 1.dp,
                color = if (isGlitch) Color(0xFFFF003C).copy(alpha = 0.4f)
                else CyberAcid.copy(alpha = 0.3f)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text          = text,
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            color         = if (isGlitch) Color(0xFFFF003C).copy(alpha = 0.8f)
            else CyberAcid.copy(alpha = 0.8f),
            letterSpacing = 0.5.sp
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 7. BRAND ROW
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun BrandRow(entry: BrandEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = DimBorder)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text          = entry.name,
                fontFamily    = AppFonts.oswald,
                fontSize      = 22.sp,
                fontWeight    = FontWeight.Black,
                color         = TeslaWhite,
                letterSpacing = (-0.5).sp
            )
            Text(
                text          = entry.origin,
                fontFamily    = AppFonts.spaceMono,
                fontSize      = 9.sp,
                color         = CyberAcid.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text       = entry.descriptor,
            fontFamily = AppFonts.spaceMono,
            fontSize   = 10.sp,
            color      = TechSilver,
            lineHeight = 18.sp
        )
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 8. CLOSING STATEMENT
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@Composable
private fun ClosingStatement() {
    Column(
        modifier          = Modifier
            .fillMaxWidth()
            .background(ScanlineGray)
            .border(width = 1.dp, color = DimBorder)
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text          = "//",
            fontFamily    = AppFonts.spaceMono,
            fontSize      = 24.sp,
            fontWeight    = FontWeight.Bold,
            color         = CyberAcid,
            textAlign     = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text          = "PHONG CÁCH KHÔNG PHẢI LÀ\nTHỨ BẠN CHỌN.\nĐÓ LÀ THỨ BẠN KHÁM PHÁ.",
            fontFamily    = AppFonts.oswald,
            fontSize      = 28.sp,
            fontWeight    = FontWeight.Black,
            color         = TeslaWhite,
            letterSpacing = (-0.5).sp,
            lineHeight    = 34.sp,
            textAlign     = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text      = "RINNSAN_CREAVITY // DA NANG // 2026",
            fontFamily = AppFonts.spaceMono,
            fontSize   = 10.sp,
            color      = TechSilver.copy(alpha = 0.5f),
            letterSpacing = 2.sp,
            textAlign  = TextAlign.Center
        )
    }
}