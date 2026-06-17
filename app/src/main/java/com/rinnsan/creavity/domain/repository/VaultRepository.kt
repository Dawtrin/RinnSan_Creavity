package com.rinnsan.creavity.domain.repository

import com.rinnsan.creavity.data.vault.AffiliateArtifact
import com.rinnsan.creavity.domain.models.Archetype
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun getArtifactsStream(): Flow<List<AffiliateArtifact>>
    suspend fun getUserProfile(): Pair<List<String>, Archetype>?
    suspend fun toggleWishlistStatus(artifactId: String, currentList: List<String>): List<String>
    fun recordClick(artifact: AffiliateArtifact)
}
