package com.rinnsan.creavity.presentation.signal.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.creavity.data.repository.SignalRepository
import com.rinnsan.creavity.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════════════
 * CREATE POST VIEWMODEL
 * ═══════════════════════════════════════════════════════════════════
 */

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val repository: SignalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // POST TYPE SELECTION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun selectPostType(type: PostType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // CONTENT EDITING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }

    fun addTag(tag: String) {
        if (tag.isNotBlank() && !_uiState.value.tags.contains(tag)) {
            _uiState.value = _uiState.value.copy(
                tags = _uiState.value.tags + tag.uppercase().trim()
            )
        }
    }

    fun removeTag(tag: String) {
        _uiState.value = _uiState.value.copy(
            tags = _uiState.value.tags - tag
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // IMAGE MANAGEMENT
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun addImages(uris: List<Uri>) {
        val currentImages = _uiState.value.selectedImages
        val newImages = (currentImages + uris).take(10) // Max 10 images
        _uiState.value = _uiState.value.copy(selectedImages = newImages)
    }

    fun removeImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImages = _uiState.value.selectedImages - uri
        )
    }

    fun reorderImages(from: Int, to: Int) {
        val images = _uiState.value.selectedImages.toMutableList()
        val item = images.removeAt(from)
        images.add(to, item)
        _uiState.value = _uiState.value.copy(selectedImages = images)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // STYLE CUSTOMIZATION
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun selectColorFilter(filter: ColorFilter) {
        _uiState.value = _uiState.value.copy(
            style = _uiState.value.style.copy(colorFilter = filter)
        )
    }

    fun selectTextEffect(effect: TextEffect) {
        _uiState.value = _uiState.value.copy(
            style = _uiState.value.style.copy(textEffect = effect)
        )
    }

    fun selectLayout(layout: LayoutType) {
        _uiState.value = _uiState.value.copy(
            style = _uiState.value.style.copy(layout = layout)
        )
    }

    fun selectFont(font: FontStyle) {
        _uiState.value = _uiState.value.copy(
            style = _uiState.value.style.copy(font = font)
        )
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // PUBLISH / DRAFT
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun publishPost() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val post = SignalPost(
                type = _uiState.value.selectedType,
                content = PostContent(
                    title = _uiState.value.title.takeIf { it.isNotBlank() },
                    description = _uiState.value.description,
                    tags = _uiState.value.tags,
                    location = _uiState.value.location.takeIf { it.isNotBlank() }
                ),
                style = _uiState.value.style,
                status = PostStatus.PUBLISHED
            )

            val result = repository.createPost(
                post = post,
                imageUris = _uiState.value.selectedImages
            ) { progress ->
                _uiState.value = _uiState.value.copy(uploadProgress = progress)
            }

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    createdPostId = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Upload failed"
                )
            }
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val post = SignalPost(
                type = _uiState.value.selectedType,
                content = PostContent(
                    title = _uiState.value.title.takeIf { it.isNotBlank() },
                    description = _uiState.value.description,
                    tags = _uiState.value.tags,
                    location = _uiState.value.location.takeIf { it.isNotBlank() }
                ),
                style = _uiState.value.style,
                status = PostStatus.DRAFT
            )

            val result = repository.saveDraft(post)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isDraftSaved = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Save failed"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = CreatePostUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI State
 */
data class CreatePostUiState(
    // Post type
    val selectedType: PostType = PostType.OUTFIT,

    // Content
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val tags: List<String> = emptyList(),

    // Images
    val selectedImages: List<Uri> = emptyList(),

    // Style
    val style: PostStyle = PostStyle(),

    // Upload state
    val isLoading: Boolean = false,
    val uploadProgress: Int = 0,
    val isSuccess: Boolean = false,
    val isDraftSaved: Boolean = false,
    val error: String? = null,
    val createdPostId: String? = null
) {
    val canPublish: Boolean
        get() = description.isNotBlank() && selectedImages.isNotEmpty()

    val canSaveDraft: Boolean
        get() = description.isNotBlank() || selectedImages.isNotEmpty()
}