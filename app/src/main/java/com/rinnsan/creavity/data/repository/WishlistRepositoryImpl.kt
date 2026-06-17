package com.rinnsan.creavity.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rinnsan.creavity.data.vault.AffiliateArtifact
import com.rinnsan.creavity.domain.models.Archetype
import com.rinnsan.creavity.domain.repository.WishlistRepository
import kotlinx.coroutines.tasks.await

class WishlistRepositoryImpl(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : WishlistRepository {

    override suspend fun getWishlistItems(): List<AffiliateArtifact> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val savedIds = userDoc.get("wishlist") as? List<String> ?: emptyList()

            if (savedIds.isEmpty()) {
                return emptyList()
            }

            val result = db.collection("artifacts").get().await()
            val list = mutableListOf<AffiliateArtifact>()
            for (document in result.documents) {
                val id = document.getString("id") ?: document.id

                if (savedIds.contains(id)) {
                    val archetypeStr = document.getString("archetype") ?: "GHOST"
                    val archetypeEnum = try {
                        Archetype.valueOf(archetypeStr.uppercase())
                    } catch (e: Exception) {
                        Archetype.GHOST
                    }

                    list.add(
                        AffiliateArtifact(
                            id = id,
                            title = document.getString("title") ?: "UNKNOWN",
                            category = document.getString("category") ?: "UNCLASSIFIED",
                            price = document.getString("price") ?: "0 VND",
                            imageUrl = document.getString("imageUrl") ?: "",
                            affiliateLink = document.getString("affiliateLink") ?: "",
                            vendor = document.getString("vendor") ?: "RINNSAN",
                            archetype = archetypeEnum
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun removeFromWishlist(artifactId: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val doc = db.collection("users").document(uid).get().await()
            val currentList = doc.get("wishlist") as? MutableList<String> ?: mutableListOf()
            if (currentList.contains(artifactId)) {
                currentList.remove(artifactId)
                db.collection("users").document(uid).update("wishlist", currentList).await()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
