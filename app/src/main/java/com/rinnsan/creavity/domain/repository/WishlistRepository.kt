package com.rinnsan.creavity.domain.repository

import com.rinnsan.creavity.data.vault.AffiliateArtifact

interface WishlistRepository {
    suspend fun getWishlistItems(): List<AffiliateArtifact>
    suspend fun removeFromWishlist(artifactId: String)
}
