package com.rinnsan.creavity.core.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rinnsan.creavity.R

object AppFonts {
    val oswald = FontFamily(
        Font(R.font.oswald_light, FontWeight.Light),        // 300
        Font(R.font.oswald_regular, FontWeight.Normal),     // 400
        Font(R.font.oswald_medium, FontWeight.Medium),      // 500
        Font(R.font.oswald_semibold, FontWeight.SemiBold),  // 600
        Font(R.font.oswald_bold, FontWeight.Bold)           // 700
    )

    val spaceMono = FontFamily(
        Font(R.font.space_mono_regular, FontWeight.Normal),
        Font(R.font.space_mono_bold, FontWeight.Bold)
    )
}
