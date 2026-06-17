package com.rinnsan.creavity.presentation.signal.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.creavity.data.repository.FeedSort
import com.rinnsan.creavity.data.repository.SignalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.rinnsan.creavity.domain.model.Comment as SignalComment
import com.rinnsan.creavity.domain.model.SignalPost
import com.rinnsan.creavity.domain.model.isLikedBy

/**
 * Model đơn giản đại diện cho bài post vừa được user tạo.
 * Hiển thị ngay trên feed (optimistic) trước khi Firestore sync.
 */
data class UserFeedPost(
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val description: String,
    val location: String,
    val imageUris: List<Uri>,   // URI ảnh từ bộ nhớ máy
    val tags: List<String>,
    val timestamp: String = "JUST NOW"
)

/**
 * ═══════════════════════════════════════════════════════════════════
 * SIGNAL VIEWMODEL - FEED MANAGEMENT
 * ═══════════════════════════════════════════════════════════════════
 */

@HiltViewModel
class SignalViewModel @Inject constructor(
    private val repository: SignalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignalUiState())
    val uiState: StateFlow<SignalUiState> = _uiState.asStateFlow()

    // Current user ID (from Firebase Auth)
    private val currentUserId: String
        get() = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        loadFeed()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // FEED LOADING
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun loadFeed(sort: FeedSort = FeedSort.RECENT) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                currentSort = sort
            )

            repository.getFeed(limit = 50, sortBy = sort)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
                .collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        posts = posts,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun refresh() {
        loadFeed(_uiState.value.currentSort)
    }

    fun changeSortOrder(sort: FeedSort) {
        loadFeed(sort)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // INTERACTIONS - LIKE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            // Optimistic update
            val updatedPosts = _uiState.value.posts.map { post ->
                if (post.id == postId) {
                    val isCurrentlyLiked = post.isLikedBy(currentUserId)
                    val newUserIds = if (isCurrentlyLiked) {
                        post.interactions.likes.userIds - currentUserId
                    } else {
                        post.interactions.likes.userIds + currentUserId
                    }
                    val newCount = if (isCurrentlyLiked) {
                        post.interactions.likes.count - 1
                    } else {
                        post.interactions.likes.count + 1
                    }

                    post.copy(
                        interactions = post.interactions.copy(
                            likes = post.interactions.likes.copy(
                                count = newCount,
                                userIds = newUserIds
                            )
                        )
                    )
                } else {
                    post
                }
            }

            _uiState.value = _uiState.value.copy(posts = updatedPosts)

            // Actual backend update
            repository.toggleLike(postId)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // INTERACTIONS - COMMENT
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    // Job riêng để cancel khi đổi post
    private var commentsJob: kotlinx.coroutines.Job? = null

    fun loadComments(postId: String) {
        // Cancel listener cũ, tránh nhiều collector chạy song song
        commentsJob?.cancel()
        // Xóa comments cũ ngay để UI không hiện dữ liệu của post trước
        _uiState.value = _uiState.value.copy(currentPostComments = emptyList())

        commentsJob = viewModelScope.launch {
            repository.getComments(postId)
                .catch { /* Handle error */ }
                .collect { comments ->
                    _uiState.value = _uiState.value.copy(
                        currentPostComments = comments
                    )
                }
        }
    }

    fun addComment(postId: String, text: String, replyTo: String? = null) {
        viewModelScope.launch {
            val result = repository.addComment(postId, text, replyTo)

            if (result.isSuccess) {
                // Firestore realtime listener trong commentsJob tự nhận update,
                // không cần gọi loadComments lại (tránh tạo listener trùng lặp)
                val updatedPosts = _uiState.value.posts.map { post ->
                    if (post.id == postId) {
                        post.copy(
                            interactions = post.interactions.copy(
                                comments = post.interactions.comments.copy(
                                    count = post.interactions.comments.count + 1
                                )
                            )
                        )
                    } else {
                        post
                    }
                }
                _uiState.value = _uiState.value.copy(posts = updatedPosts)
            }
        }
    }

    fun clearComments() {
        commentsJob?.cancel()
        commentsJob = null
        _uiState.value = _uiState.value.copy(currentPostComments = emptyList())
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(postId, commentId)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // INTERACTIONS - SHARE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun incrementShare(postId: String) {
        viewModelScope.launch {
            repository.incrementShare(postId)

            // Optimistic update
            val updatedPosts = _uiState.value.posts.map { post ->
                if (post.id == postId) {
                    post.copy(
                        interactions = post.interactions.copy(
                            shares = post.interactions.shares + 1
                        )
                    )
                } else {
                    post
                }
            }
            _uiState.value = _uiState.value.copy(posts = updatedPosts)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // POST MANAGEMENT
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val result = repository.deletePost(postId)

            if (result.isSuccess) {
                // Remove from UI
                val updatedPosts = _uiState.value.posts.filter { it.id != postId }
                _uiState.value = _uiState.value.copy(posts = updatedPosts)
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SEARCH & FILTER
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun searchByTag(tag: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                searchQuery = tag
            )

            val result = repository.searchByTag(tag)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    posts = result.getOrNull() ?: emptyList(),
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = null)
        loadFeed()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // LOCAL USER POSTS — Optimistic update, hiện ngay trên feed
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private val _userCreatedPosts = MutableStateFlow<List<UserFeedPost>>(emptyList())
    val userCreatedPosts: StateFlow<List<UserFeedPost>> = _userCreatedPosts.asStateFlow()

    /** Thêm bài post mới vào ĐẦU feed ngay lập tức (không cần đợi Firestore) */
    fun addLocalPost(post: UserFeedPost) {
        _userCreatedPosts.value = listOf(post) + _userCreatedPosts.value
    }
}

/**
 * UI State
 */
data class SignalUiState(
    val posts: List<SignalPost> = emptyList(),
    val currentPostComments: List<SignalComment> = emptyList(),  // ← FIXED: Dùng SignalComment thay vì Comment
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSort: FeedSort = FeedSort.RECENT,
    val searchQuery: String? = null
)