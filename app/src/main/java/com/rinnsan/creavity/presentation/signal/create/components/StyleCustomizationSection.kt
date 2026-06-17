package com.rinnsan.creavity.presentation.signal.create.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.model.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * STYLE CUSTOMIZATION SECTION
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun StyleCustomizationSection(
    style: PostStyle,
    onColorFilterChange: (ColorFilter) -> Unit,
    onTextEffectChange: (TextEffect) -> Unit,
    onLayoutChange: (LayoutType) -> Unit,
    onFontChange: (FontStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // COLOR FILTER
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        StyleOption(
            label = "COLOR FILTER",
            selected = style.colorFilter.name
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorFilter.entries.take(4).forEach { filter ->
                    ColorFilterChip(
                        filter = filter,
                        isSelected = style.colorFilter == filter,
                        onClick = { onColorFilterChange(filter) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // TEXT EFFECT
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        StyleOption(
            label = "TEXT EFFECT",
            selected = style.textEffect.name
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextEffect.entries.take(4).forEach { effect ->
                    TextEffectChip(
                        effect = effect,
                        isSelected = style.textEffect == effect,
                        onClick = { onTextEffectChange(effect) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // LAYOUT
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        StyleOption(
            label = "LAYOUT",
            selected = style.layout.name
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(LayoutType.STANDARD, LayoutType.GRID, LayoutType.FULLBLEED).forEach { layout ->
                        LayoutChip(
                            layout = layout,
                            isSelected = style.layout == layout,
                            onClick = { onLayoutChange(layout) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // FONT
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        StyleOption(
            label = "FONT",
            selected = style.font.name
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FontStyle.entries.forEach { font ->
                    FontChip(
                        font = font,
                        isSelected = style.font == font,
                        onClick = { onFontChange(font) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StyleOption(
    label: String,
    selected: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TechSilver,
                letterSpacing = 1.sp
            )

            Text(
                text = selected.replace("_", " "),
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                color = ElectricBlue
            )
        }

        content()
    }
}

@Composable
fun ColorFilterChip(
    filter: ColorFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (filter) {
        ColorFilter.NONE -> Color.White
        ColorFilter.GRAYSCALE -> Color.Gray
        ColorFilter.NEON -> LaserGreen
        ColorFilter.VINTAGE -> Color(0xFFD4A574)
        ColorFilter.CYBERPUNK -> ElectricBlue
        ColorFilter.MONOCHROME -> Color.Black
    }

    Box(
        modifier = modifier
            .height(60.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) ElectricBlue else color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isSelected) color.copy(alpha = 0.15f) else color.copy(alpha = 0.05f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = filter.name,
            fontFamily = AppFonts.spaceMono,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) ElectricBlue else TechSilver,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TextEffectChip(
    effect: TextEffect,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) NeonPurple else TechSilver.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isSelected) NeonPurple.copy(alpha = 0.15f) else VoidBlack,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = effect.name,
            fontFamily = AppFonts.spaceMono,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NeonPurple else TechSilver,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun LayoutChip(
    layout: LayoutType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) CyberAcid else TechSilver.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isSelected) CyberAcid.copy(alpha = 0.15f) else VoidBlack,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = layout.name,
            fontFamily = AppFonts.spaceMono,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) CyberAcid else TechSilver,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun FontChip(
    font: FontStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fontFamily = when (font) {
        FontStyle.OSWALD -> AppFonts.oswald
        FontStyle.SPACE_MONO -> AppFonts.spaceMono
        FontStyle.INTER -> AppFonts.spaceMono // Fallback
    }

    Box(
        modifier = modifier
            .height(60.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) ElectricBlue else TechSilver.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isSelected) ElectricBlue.copy(alpha = 0.15f) else VoidBlack,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Aa",
            fontFamily = fontFamily,
            fontSize = 24.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) ElectricBlue else TechSilver
        )
    }
}

// Colors
private val ElectricBlue = Color(0xFF00D9FF)
private val NeonPurple = Color(0xFFB366FF)
private val LaserGreen = Color(0xFF39FF14)