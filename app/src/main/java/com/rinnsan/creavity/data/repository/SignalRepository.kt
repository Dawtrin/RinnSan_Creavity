package com.rinnsan.creavity.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rinnsan.creavity.data.remote.CloudinaryApi
import com.rinnsan.creavity.domain.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// ✅ FIX: XÓA @Singleton và @Inject constructor.
//
// NetworkModule.kt đã có provideSignalRepository() dùng @Provides @Singleton.
// Nếu class vừa có @Inject constructor VÀ NetworkModule vừa có @Provides cho cùng type
// → Hilt gặp DuplicateBindingException → crash toàn bộ app khi build DI graph.
//
// Quy tắc: Chọn MỘT trong hai cách, không dùng cả hai:
//   - Cách A (đang dùng): @Provides trong Module → class không cần @Inject
//   - Cách B: @Inject constructor trong class → không cần @Provides trong Module
class SignalRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val cloudinaryApi: CloudinaryApi
) {

    private val postsCollection = firestore.collection("signals")

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // CREATE POST
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    suspend fun createPost(
        post: SignalPost,
        imageUris: List<Uri>,
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            onProgress(10)
            val uploadResult = cloudinaryApi.uploadImages(imageUris) { index, progress ->
                onProgress(10 + (index * 40 / imageUris.size) + (progress * 40 / 100 / imageUris.size))
            }

            if (uploadResult.isFailure) {
                return Result.failure(uploadResult.exceptionOrNull() ?: Exception("Upload failed"))
            }

            val imageUrls = uploadResult.getOrThrow()
            onProgress(60)

            val postData = post.copy(
                userId = currentUser.uid,
                username = currentUser.displayName ?: "Anonymous",
                userPhotoUrl = currentUser.photoUrl?.toString(),
                content = post.content.copy(images = imageUrls)
            )

            val docRef = postsCollection.add(postData).await()

            try {
                firestore.collection("users").document(currentUser.uid)
                    .set(
                        mapOf("postCount" to FieldValue.increment(1)),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await()
            } catch (e: Exception) { /* Ignore */ }

            onProgress(100)
            Result.success(docRef.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDraft(post: SignalPost): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val draftData = post.copy(
                userId = currentUser.uid,
                username = currentUser.displayName ?: "Anonymous",
                status = PostStatus.DRAFT
            )

            val docRef = postsCollection.add(draftData).await()
            Result.success(docRef.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // READ POSTS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun getFeed(
        limit: Int = 20,
        sortBy: FeedSort = FeedSort.RECENT
    ): Flow<List<SignalPost>> = callbackFlow {

        var query: Query = postsCollection

        query = when (sortBy) {
            FeedSort.RECENT   -> query.orderBy("timestamp", Query.Direction.DESCENDING)
            FeedSort.TRENDING -> query.orderBy("interactions.likes.count", Query.Direction.DESCENDING)
            FeedSort.TOP      -> query.orderBy("interactions.views", Query.Direction.DESCENDING)
        }

        query = query.limit(limit.toLong())

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }

            val posts = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(SignalPost::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(posts)
        }

        awaitClose { listener.remove() }
    }

    suspend fun getPost(postId: String): Result<SignalPost> {
        return try {
            val doc = postsCollection.document(postId).get().await()
            val post = doc.toObject(SignalPost::class.java)?.copy(id = doc.id)
                ?: return Result.failure(Exception("Post not found"))
            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserPosts(userId: String): Flow<List<SignalPost>> = callbackFlow {
        val listener = postsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SignalPost::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun searchByTag(tag: String): Result<List<SignalPost>> {
        return try {
            val snapshot = postsCollection
                .whereArrayContains("content.tags", tag)
                .limit(50)
                .get().await()

            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SignalPost::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.timestamp }
            Result.success(posts)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // UPDATE / DELETE
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    suspend fun updatePost(postId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            postsCollection.document(postId)
                .update(updates + mapOf("isEdited" to true, "editedAt" to FieldValue.serverTimestamp()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val doc = postsCollection.document(postId).get().await()
            val userId = doc.getString("userId")

            postsCollection.document(postId).delete().await()

            if (userId != null) {
                try {
                    firestore.collection("users").document(userId)
                        .set(
                            mapOf("postCount" to FieldValue.increment(-1)),
                            com.google.firebase.firestore.SetOptions.merge()
                        ).await()
                } catch (e: Exception) { /* Ignore */ }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // INTERACTIONS
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    suspend fun toggleLike(postId: String): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val postRef = postsCollection.document(postId)
            val post = postRef.get().await().toObject(SignalPost::class.java)
                ?: return Result.failure(Exception("Post not found"))

            val isLiked = post.interactions.likes.userIds.contains(currentUser.uid)

            if (isLiked) {
                postRef.update(mapOf(
                    "interactions.likes.count" to FieldValue.increment(-1),
                    "interactions.likes.userIds" to FieldValue.arrayRemove(currentUser.uid)
                )).await()
            } else {
                postRef.update(mapOf(
                    "interactions.likes.count" to FieldValue.increment(1),
                    "interactions.likes.userIds" to FieldValue.arrayUnion(currentUser.uid)
                )).await()
            }

            Result.success(!isLiked)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, text: String, replyTo: String? = null): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val comment = Comment(
                postId = postId,
                userId = currentUser.uid,
                username = currentUser.displayName ?: "Anonymous",
                userPhotoUrl = currentUser.photoUrl?.toString(),
                text = text,
                replyTo = replyTo
            )

            val docRef = postsCollection.document(postId)
                .collection("comments").add(comment).await()

            postsCollection.document(postId).update(mapOf(
                "interactions.comments.count" to FieldValue.increment(1),
                "interactions.comments.lastCommentTimestamp" to FieldValue.serverTimestamp()
            )).await()

            Result.success(docRef.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = postsCollection.document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("SignalRepo", "getComments error: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                }
                    ?.filter { !it.isDeleted }
                    ?.sortedBy { it.timestamp?.toDate()?.time ?: Long.MAX_VALUE }
                    ?: emptyList()

                trySend(comments)
            }

        awaitClose { listener.remove() }
    }

    suspend fun deleteComment(postId: String, commentId: String): Result<Unit> {
        return try {
            postsCollection.document(postId)
                .collection("comments").document(commentId)
                .update(mapOf("isDeleted" to true)).await()

            postsCollection.document(postId)
                .update(mapOf("interactions.comments.count" to FieldValue.increment(-1))).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementShare(postId: String): Result<Unit> {
        return try {
            postsCollection.document(postId)
                .update(mapOf("interactions.shares" to FieldValue.increment(1))).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementView(postId: String): Result<Unit> {
        return try {
            postsCollection.document(postId)
                .update(mapOf("interactions.views" to FieldValue.increment(1))).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

enum class FeedSort {
    RECENT,
    TRENDING,
    TOP
}