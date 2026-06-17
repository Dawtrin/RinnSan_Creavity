package com.rinnsan.creavity.presentation.signal.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.rinnsan.creavity.core.theme.*
import com.rinnsan.creavity.domain.model.*
import com.rinnsan.creavity.presentation.signal.create.components.ImageUploadSection
import com.rinnsan.creavity.presentation.signal.create.components.StyleCustomizationSection
import com.rinnsan.creavity.presentation.signal.create.components.TagInputSection
import com.rinnsan.creavity.presentation.signal.viewmodel.CreatePostViewModel
import com.rinnsan.creavity.presentation.signal.viewmodel.SignalViewModel
import com.rinnsan.creavity.presentation.signal.viewmodel.UserFeedPost
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ═══════════════════════════════════════════════════════════════════
 * CREATE POST SCREEN
 * ═══════════════════════════════════════════════════════════════════
 *
 * Main screen để tạo bài post mới
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: CreatePostViewModel = hiltViewModel(),
    signalViewModel: SignalViewModel  // shared: nhận từ AppNavigation
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    // Publish thành công → lấy username → đẩy bài lên feed → quay lại
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            // Ưu tiên: Firestore username → Auth displayName → email prefix
            val firestoreDoc = firebaseUser?.let {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(it.uid).get().await()
            }
            val username = (
                firestoreDoc?.getString("username")?.takeIf { it.isNotBlank() }
                    ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: firebaseUser?.email?.substringBefore("@")
                    ?: "USER"
            ).uppercase()

            signalViewModel.addLocalPost(
                UserFeedPost(
                    username    = username,
                    description = uiState.description,
                    location    = uiState.location,
                    imageUris   = uiState.selectedImages,
                    tags        = uiState.tags
                )
            )

            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CREATE POST",
                        fontFamily = AppFonts.oswald,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TeslaWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TeslaWhite
                        )
                    }
                },
                actions = {
                    // Save Draft
                    TextButton(
                        onClick = { viewModel.saveDraft() },
                        enabled = uiState.canSaveDraft && !uiState.isLoading
                    ) {
                        Text(
                            text = "DRAFT",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp,
                            color = if (uiState.canSaveDraft) CyberAcid else TechSilver.copy(alpha = 0.3f)
                        )
                    }

                    // Publish
                    TextButton(
                        onClick = { viewModel.publishPost() },
                        enabled = uiState.canPublish && !uiState.isLoading
                    ) {
                        Text(
                            text = "PUBLISH",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.canPublish) ElectricBlue else TechSilver.copy(alpha = 0.3f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VoidBlack,
                    titleContentColor = TeslaWhite
                )
            )
        },
        containerColor = VoidBlack
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // POST TYPE SELECTION
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                SectionLabel("POST TYPE")

                Spacer(modifier = Modifier.height(12.dp))

                PostTypeSelector(
                    selectedType = uiState.selectedType,
                    onTypeSelected = { viewModel.selectPostType(it) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // IMAGE UPLOAD
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                SectionLabel("IMAGES (${uiState.selectedImages.size}/10)")

                Spacer(modifier = Modifier.height(12.dp))

                ImageUploadSection(
                    images = uiState.selectedImages,
                    onAddClick = { imagePickerLauncher.launch("image/*") },
                    onRemove = { viewModel.removeImage(it) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // CONTENT
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                SectionLabel("CONTENT")

                Spacer(modifier = Modifier.height(12.dp))

                // Title (optional)
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = {
                        Text(
                            text = "Title (Optional)",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = AppFonts.oswald,
                        fontSize = 18.sp,
                        color = TeslaWhite
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = TechSilver.copy(alpha = 0.3f),
                        focusedLabelColor = ElectricBlue,
                        unfocusedLabelColor = TechSilver,
                        cursorColor = ElectricBlue
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description (required)
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = {
                        Text(
                            text = "Description *",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 14.sp,
                        color = TeslaWhite
                    ),
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = TechSilver.copy(alpha = 0.3f),
                        focusedLabelColor = ElectricBlue,
                        unfocusedLabelColor = TechSilver,
                        cursorColor = ElectricBlue
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location
                OutlinedTextField(
                    value = uiState.location,
                    onValueChange = { viewModel.updateLocation(it) },
                    label = {
                        Text(
                            text = "Location (Optional)",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 11.sp
                        )
                    },
                    placeholder = {
                        Text(
                            text = "CITY // COUNTRY",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp,
                            color = TechSilver.copy(alpha = 0.3f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = AppFonts.spaceMono,
                        fontSize = 14.sp,
                        color = TeslaWhite
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = TechSilver.copy(alpha = 0.3f),
                        focusedLabelColor = ElectricBlue,
                        unfocusedLabelColor = TechSilver,
                        cursorColor = ElectricBlue
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TechSilver
                        )
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // TAGS
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                SectionLabel("TAGS")

                Spacer(modifier = Modifier.height(12.dp))

                TagInputSection(
                    tags = uiState.tags,
                    onAddTag = { viewModel.addTag(it) },
                    onRemoveTag = { viewModel.removeTag(it) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // STYLE CUSTOMIZATION
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                SectionLabel("STYLE")

                Spacer(modifier = Modifier.height(12.dp))

                StyleCustomizationSection(
                    style = uiState.style,
                    onColorFilterChange = { viewModel.selectColorFilter(it) },
                    onTextEffectChange = { viewModel.selectTextEffect(it) },
                    onLayoutChange = { viewModel.selectLayout(it) },
                    onFontChange = { viewModel.selectFont(it) }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // LOADING OVERLAY
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VoidBlack.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = ElectricBlue,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "UPLOADING ${uiState.uploadProgress}%",
                            fontFamily = AppFonts.spaceMono,
                            fontSize = 12.sp,
                            color = ElectricBlue
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { uiState.uploadProgress / 100f },
                            modifier = Modifier
                                .width(200.dp)
                                .height(4.dp),
                            color = ElectricBlue,
                            trackColor = TechSilver.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // ERROR SNACKBAR
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    scope.launch {
                        // Show snackbar
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// HELPER COMPONENTS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = AppFonts.spaceMono,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = CyberAcid.copy(alpha = 0.8f),
        letterSpacing = 2.sp
    )
}

@Composable
fun PostTypeSelector(
    selectedType: PostType,
    onTypeSelected: (PostType) -> Unit
) {
    val types = listOf(
        PostType.OUTFIT to "OUTFIT",
        PostType.STYLE_GUIDE to "GUIDE",
        PostType.MANIFESTO to "MANIFESTO",
        PostType.FEATURED_MOMENT to "MOMENT"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { (type, label) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .border(
                        width = 2.dp,
                        color = if (type == selectedType) ElectricBlue else TechSilver.copy(alpha = 0.3f)
                    )
                    .background(
                        if (type == selectedType) ElectricBlue.copy(alpha = 0.1f) else VoidBlack
                    )
                    .clickable { onTypeSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontFamily = AppFonts.spaceMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (type == selectedType) ElectricBlue else TechSilver,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// Import Color constants
private val ElectricBlue = Color(0xFF00D9FF)
private val NeonPurple = Color(0xFFB366FF)
private val LaserGreen = Color(0xFF39FF14)