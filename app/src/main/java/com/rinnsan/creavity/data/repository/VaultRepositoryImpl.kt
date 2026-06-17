package com.rinnsan.creavity.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.rinnsan.creavity.data.tracker.ClickTracker
import com.rinnsan.creavity.data.vault.AffiliateArtifact
import com.rinnsan.creavity.domain.models.Archetype
import com.rinnsan.creavity.domain.repository.VaultRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class VaultRepositoryImpl(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : VaultRepository {

    override fun getArtifactsStream(): Flow<List<AffiliateArtifact>> = callbackFlow {
        val listener = db.collection("artifacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val archetypeStr = doc.getString("archetype") ?: "GHOST"
                    val archetype = try {
                        Archetype.valueOf(archetypeStr.uppercase())
                    } catch (e: Exception) { Archetype.GHOST }

                    @Suppress("UNCHECKED_CAST")
                    val imageList = (doc.get("images") as? List<String>) ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    val sizeList  = (doc.get("sizes")  as? List<String>) ?: emptyList()

                    AffiliateArtifact(
                        id             = doc.getString("id") ?: doc.id,
                        title          = doc.getString("title")        ?: "UNKNOWN ARTIFACT",
                        category       = doc.getString("category")     ?: "UNCLASSIFIED",
                        price          = doc.getString("price")        ?: "0 VND",
                        imageUrl       = doc.getString("imageUrl")     ?: "",
                        affiliateLink  = doc.getString("affiliateLink")?: "",
                        vendor         = doc.getString("vendor")       ?: "RINNSAN_LAB",
                        archetype      = archetype,
                        isDirectSale   = doc.getBoolean("isDirectSale")  ?: false,
                        commissionRate = doc.getDouble("commissionRate")  ?: 0.0,
                        internalPrice  = doc.getLong("internalPrice")
                            ?: doc.getDouble("internalPrice")?.toLong() ?: 0L,
                        stock          = doc.getLong("stock") ?: 0L,
                        images         = imageList,
                        sizes          = sizeList
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getUserProfile(): Pair<List<String>, Archetype>? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val document = db.collection("users").document(uid).get().await()
            if (!document.exists()) return null

            val savedWishlist = document.get("wishlist") as? List<String> ?: emptyList()
            val profileField = document.get("identityProfile")
            val savedArchetype = when {
                profileField is String              -> profileField
                profileField is Map<*, *>           -> profileField["dominantArchetype"] as? String ?: "GHOST"
                document.getString("archetype") != null -> document.getString("archetype") ?: "GHOST"
                else                                -> "GHOST"
            }
            val archetypeEnum = try {
                Archetype.valueOf(savedArchetype.uppercase())
            } catch (e: Exception) { Archetype.GHOST }
            
            Pair(savedWishlist, archetypeEnum)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun toggleWishlistStatus(artifactId: String, currentList: List<String>): List<String> {
        val uid = auth.currentUser?.uid ?: return currentList
        val mutableList = currentList.toMutableList()
        if (mutableList.contains(artifactId)) {
            mutableList.remove(artifactId)
        } else {
            mutableList.add(artifactId)
        }
        try {
            db.collection("users").document(uid)
                .set(hashMapOf("wishlist" to mutableList), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            // Ignore for now
        }
        return mutableList
    }

    override fun recordClick(artifact: AffiliateArtifact) {
        if (!artifact.isDirectSale) {
            ClickTracker.recordClick(artifact)
        }
    }
}
