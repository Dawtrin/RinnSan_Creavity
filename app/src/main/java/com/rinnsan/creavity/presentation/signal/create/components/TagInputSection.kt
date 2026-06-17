package com.rinnsan.creavity.presentation.signal.create.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.rinnsan.creavity.core.theme.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * TAG INPUT SECTION
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun TagInputSection(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tagInput by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        // Tag input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                value = tagInput,
                onValueChange = { tagInput = it.uppercase() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(
                        width = 1.dp,
                        color = TechSilver.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = PhantomGrey.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                textStyle = TextStyle(
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 14.sp,
                    color = TeslaWhite,
                    letterSpacing = 1.sp
                ),
                cursorBrush = SolidColor(ElectricBlue),
                decorationBox = { innerTextField ->
                    if (tagInput.isEmpty()) {
                        Text(
                            text = "Add tag (e.g., TECHWEAR)",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp,
                            color = TechSilver.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )

            // Add button
            IconButton(
                onClick = {
                    if (tagInput.isNotBlank()) {
                        onAddTag(tagInput)
                        tagInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 2.dp,
                        color = if (tagInput.isNotBlank()) ElectricBlue else TechSilver.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = if (tagInput.isNotBlank()) ElectricBlue.copy(alpha = 0.1f) else VoidBlack,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Tag",
                    tint = if (tagInput.isNotBlank()) ElectricBlue else TechSilver.copy(alpha = 0.5f)
                )
            }
        }

        // Tag chips
        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onRemove = { onRemoveTag(tag) }
                    )
                }
            }
        }

        // Suggested tags
        if (tags.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Suggested:",
                fontFamily = AppFonts.spaceMono,
                fontSize = 10.sp,
                color = TechSilver.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                listOf("TECHWEAR", "STREETWEAR", "MINIMAL", "VINTAGE", "URBAN", "NIGHT").forEach { suggested ->
                    SuggestedTagChip(
                        tag = suggested,
                        onAdd = { onAddTag(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun TagChip(
    tag: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .border(
                width = 1.dp,
                color = CyberAcid,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = CyberAcid.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "#$tag",
            fontFamily = AppFonts.spaceMono,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberAcid,
            letterSpacing = 0.5.sp
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = CyberAcid,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SuggestedTagChip(
    tag: String,
    onAdd: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .border(
                width = 1.dp,
                color = TechSilver.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(
                color = PhantomGrey.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onAdd(tag) }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$tag",
            fontFamily = AppFonts.spaceMono,
            fontSize = 10.sp,
            color = TechSilver,
            letterSpacing = 0.5.sp
        )
    }
}

// Colors
private val ElectricBlue = Color(0xFF00D9FF)