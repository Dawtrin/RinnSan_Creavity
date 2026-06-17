package com.rinnsan.creavity.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 1. Headline Font: Oswald / Druk Wide [cite: 18]
// (Tạm thời dùng Default Bold, sau này bạn copy font vào res/font rồi uncomment dòng dưới)
val DisplayFont = FontFamily.Default
/* val DisplayFont = FontFamily(
    Font(R.font.oswald_bold, FontWeight.Bold),
    Font(R.font.oswald_medium, FontWeight.Medium)
) */

// 2. Tech Font: Space Mono / JetBrains Mono [cite: 21]
val TechFont = FontFamily.Monospace
/* val TechFont = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal)
) */

val RinnSanTypography = Typography(
    // Tiêu đề lớn (Headline) - Viết HOA [cite: 19]
    displayLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        letterSpacing = 2.sp,
        color = TeslaWhite
    ),
    // Thông số kỹ thuật (Specs) - Size nhỏ [cite: 22]
    labelSmall = TextStyle(
        fontFamily = TechFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        color = CyberAcid
    ),
    // Nội dung thường
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        color = TeslaWhite,
        fontSize = 16.sp
    )
)