package com.rinnsan.creavity.presentation.signal.create.components

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.theme.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * IMAGE UPLOAD SECTION
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun ImageUploadSection(
    images: List<Uri>,
    onAddClick: () -> Unit,
    onRemove: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add button
        item {
            AddImageButton(onClick = onAddClick)
        }

        // Image items
        itemsIndexed(images) { index, uri ->
            ImageItem(
                uri = uri,
                index = index,
                onRemove = { onRemove(uri) }
            )
        }
    }
}

@Composable
fun AddImageButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(ElectricBlue, NeonPurple)
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = ElectricBlue.copy(alpha = 0.05f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Image",
                tint = ElectricBlue,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = "ADD",
                fontFamily = AppFonts.spaceMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ImageItem(
    uri: Uri,
    index: Int,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.size(120.dp)
    ) {
        // Image
        AsyncImage(
            model = uri,
            contentDescription = "Image $index",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .border(
                    width = 1.dp,
                    color = TechSilver.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentScale = ContentScale.Crop
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp)
                .background(
                    color = GlitchRed.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = TeslaWhite,
                modifier = Modifier.size(16.dp)
            )
        }

        // Index indicator
        if (index == 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .background(
                        color = ElectricBlue,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "COVER",
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = VoidBlack,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// Colors
private val ElectricBlue = Color(0xFF00D9FF)
private val NeonPurple = Color(0xFFB366FF)