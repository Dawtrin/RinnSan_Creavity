package com.rinnsan.creavity.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Chỉ dùng Dark Theme (Neo-Noir) [cite: 7]
private val DarkColorScheme = darkColorScheme(
    primary = CyberAcid,
    onPrimary = VoidBlack,
    background = VoidBlack,
    onBackground = TeslaWhite,
    surface = PhantomGrey,
    onSurface = TeslaWhite,
    error = GlitchRed
)

@Composable
fun RinnSanCreavityTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Thanh trạng thái đen tuyệt đối
            window.statusBarColor = VoidBlack.toArgb()
            // Icon trên status bar màu sáng
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = RinnSanTypography,
        content = content
    )
}