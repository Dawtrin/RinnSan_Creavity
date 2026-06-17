package com.rinnsan.creavity.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.creavity.data.vault.AffiliateArtifact
import com.rinnsan.creavity.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    // Danh sách đồ yêu thích để đưa lên UI
    private val _wishlistItems = MutableStateFlow<List<AffiliateArtifact>>(emptyList())
    val wishlistItems: StateFlow<List<AffiliateArtifact>> = _wishlistItems

    init {
        loadWishlist()
    }

    fun loadWishlist() {
        viewModelScope.launch {
            _wishlistItems.value = wishlistRepository.getWishlistItems()
        }
    }

    // Hàm để xóa sản phẩm khỏi Wishlist
    fun removeFromWishlist(artifactId: String) {
        // Optimistic UI update
        _wishlistItems.value = _wishlistItems.value.filter { it.id != artifactId }
        
        viewModelScope.launch {
            wishlistRepository.removeFromWishlist(artifactId)
        }
    }
}