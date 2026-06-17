package com.rinnsan.creavity.core.util

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object AnimationUtil {

    // 1. Hiệu ứng "Magnetic Force" (Lực từ tính)
    // Dùng cho các nút bấm: Nảy nhẹ, phản hồi nhanh nhưng êm
    val MagneticSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy, // Độ nảy trung bình
        stiffness = Spring.StiffnessMediumLow           // Độ cứng vừa phải (mềm mại)
    )

    // 2. Hiệu ứng "Gravity" (Trọng lực)
    // Dùng cho các khối Card hoặc Text trượt vào: Cảm giác nặng, đầm tay
    val GravitySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,     // Không nảy
        stiffness = Spring.StiffnessLow                 // Rất chậm và đầm
    )

    // 3. Hiệu ứng "Glitch" (Nhiễu)
    // Dùng cho chuyển cảnh nhanh, giật cục
    val GlitchSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioHighBouncy,   // Nảy cực mạnh
        stiffness = Spring.StiffnessHigh                // Cực nhanh
    )
}

