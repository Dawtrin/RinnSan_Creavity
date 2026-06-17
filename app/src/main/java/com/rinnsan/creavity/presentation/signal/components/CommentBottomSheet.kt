package com.rinnsan.creavity.presentation.signal.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.util.copy
import coil.compose.AsyncImage
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.model.getFormattedTimestamp
import com.rinnsan.creavity.domain.model.Comment as SignalComment
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════
 * COMMENT BOTTOM SHEET - XEM VÀ THÊM BÌNH LUẬN
 * ═══════════════════════════════════════════════════════════════════
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    postId: String,
    comments: List<SignalComment>,
    currentUserId: String,
    onAddComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VoidBlack,
        contentColor = TeslaWhite,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // HEADER
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BÌNH LUẬN (${comments.size})",
                    fontFamily = AppFonts.oswald,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TeslaWhite,
                    letterSpacing = 1.sp
                )

                IconButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = TechSilver
                    )
                }
            }

            Divider(
                color = GridLineColor.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // COMMENTS LIST
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            if (comments.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = TechSilver.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "Chưa có bình luận",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 14.sp,
                            color = TechSilver.copy(alpha = 0.6f)
                        )

                        Text(
                            text = "Hãy là người đầu tiên bình luận",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp,
                            color = TechSilver.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                // Comments list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(
                        items = comments,
                        key = { it.id }
                    ) { comment ->
                        CommentItem(
                            comment = comment,
                            currentUserId = currentUserId,
                            onDelete = { onDeleteComment(comment.id) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // INPUT BOX
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            Divider(
                color = GridLineColor.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PhantomGrey.copy(alpha = 0.2f))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Input field
                BasicTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp, max = 120.dp)
                        .background(
                            color = PhantomGrey.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    textStyle = TextStyle(
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 14.sp,
                        color = TeslaWhite,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(ElectricBlue),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (commentText.isEmpty()) {
                                Text(
                                    text = "Viết bình luận...",
                                    fontFamily = AppFonts.spaceMono,
                                    fontSize = 14.sp,
                                    color = TechSilver.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Send button
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onAddComment(commentText.trim())
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (commentText.isNotBlank()) ElectricBlue else PhantomGrey,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gửi",
                        tint = if (commentText.isNotBlank()) VoidBlack else TechSilver.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// COMMENT ITEM
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun CommentItem(
    comment: SignalComment,
    currentUserId: String,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        if (comment.userPhotoUrl != null) {
            AsyncImage(
                model = comment.userPhotoUrl,
                contentDescription = comment.username,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, ElectricBlue.copy(alpha = 0.5f), CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PhantomGrey, CircleShape)
                    .border(1.dp, TechSilver.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.username.firstOrNull()?.toString() ?: "?",
                    fontFamily = AppFonts.oswald,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TeslaWhite
                )
            }
        }

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Username + timestamp
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.username,
                    fontFamily = AppFonts.oswald,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TeslaWhite
                )

                Text(
                    text = "•",
                    fontSize = 10.sp,
                    color = TechSilver.copy(alpha = 0.5f)
                )

                Text(
                    text = comment.getFormattedTimestamp(),
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 11.sp,
                    color = TechSilver.copy(alpha = 0.6f)
                )

                if (comment.isEdited) {
                    Text(
                        text = "(đã chỉnh sửa)",
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 10.sp,
                        color = TechSilver.copy(alpha = 0.5f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            // Comment text
            Text(
                text = comment.text,
                fontFamily = AppFonts.spaceMono,
                fontSize = 13.sp,
                color = TeslaWhite.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )
        }

        // More menu (if owner)
        if (comment.userId == currentUserId) {
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = TechSilver,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = GlitchRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Xóa",
                                    fontFamily = AppFonts.spaceMono,
                                    fontSize = 13.sp,
                                    color = GlitchRed
                                )
                            }
                        },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

// Colors