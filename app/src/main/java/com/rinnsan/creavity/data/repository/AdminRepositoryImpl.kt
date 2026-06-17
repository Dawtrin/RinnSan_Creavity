package com.rinnsan.creavity.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.rinnsan.creavity.data.remote.CloudinaryApi
import com.rinnsan.creavity.domain.model.*
import com.rinnsan.creavity.domain.repository.AdminRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class AdminRepositoryImpl(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val cloudinaryApi: CloudinaryApi
) : AdminRepository {

    override suspend fun checkIsAdmin(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val doc = db.collection("users").document(uid).get().await()
            val role = doc.getString("role") ?: "user"
            role.uppercase() == "ADMIN"
        } catch (e: Exception) {
            false
        }
    }

    // Helper: đếm số document an toàn, ưu tiên Aggregate, fallback về get().size()
    private suspend fun safeCount(query: com.google.firebase.firestore.Query): Long {
        return try {
            query.count().get(com.google.firebase.firestore.AggregateSource.SERVER).await().count
        } catch (e: Exception) {
            android.util.Log.w("AdminRepo", "Aggregate count failed, falling back: ${e.message}")
            try {
                query.get().await().size().toLong()
            } catch (e2: Exception) {
                android.util.Log.e("AdminRepo", "Fallback count also failed: ${e2.message}")
                0L
            }
        }
    }

    override suspend fun getSummary(): AdminSummary {
        // Đọc stats summary document (có thể không tồn tại)
        val s = try {
            db.collection("stats").document("summary").get().await()
        } catch (e: Exception) {
            android.util.Log.e("AdminRepo", "stats/summary read failed: ${e.message}")
            null
        }
        
        val totalUsersCount = safeCount(db.collection("users"))
        val totalArtifactsCount = safeCount(db.collection("artifacts"))
        val totalOrdersCount = safeCount(db.collection("orders"))
        val openTixCount = safeCount(db.collection("contacts").whereEqualTo("status", "new"))
        val pendingCount = safeCount(db.collection("orders").whereEqualTo("status", "pending"))
        val deliveredCount = safeCount(db.collection("orders").whereIn("status", listOf("delivered", "paid")))

        // Tính doanh thu - fallback nếu Aggregate sum() không hỗ trợ
        val revenue = try {
            val revenueAggregate = db.collection("orders")
                .whereIn("status", listOf("delivered", "paid"))
                .aggregate(com.google.firebase.firestore.AggregateField.sum("totalAmount"))
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
            (revenueAggregate.get(com.google.firebase.firestore.AggregateField.sum("totalAmount")) as? Number)?.toLong() ?: 0L
        } catch (e: Exception) {
            android.util.Log.w("AdminRepo", "Aggregate sum failed, falling back: ${e.message}")
            try {
                val orders = db.collection("orders")
                    .whereIn("status", listOf("delivered", "paid"))
                    .get().await()
                orders.documents.sumOf { it.getLong("totalAmount") ?: 0L }
            } catch (e2: Exception) {
                android.util.Log.e("AdminRepo", "Revenue fallback also failed: ${e2.message}")
                0L
            }
        }
        
        return AdminSummary(
            totalClicks = s?.getLong("totalClicks") ?: 0L,
            totalCommission = s?.getLong("totalCommission") ?: 0L,
            totalUsers = totalUsersCount,
            totalArtifacts = totalArtifactsCount,
            totalOrders = totalOrdersCount,
            pendingOrders = pendingCount,
            openTickets = openTixCount,
            deliveredOrders = deliveredCount,
            totalRevenue = revenue
        )
    }

    override suspend fun getBrandStats(): List<BrandStat> {
        val statDoc = db.collection("stats").document("by_brand").get().await()
        val brandsSnapshot = db.collection("brands").get().await()

        val rateMap = brandsSnapshot.documents.associate { it.id to (it.getDouble("rate") ?: 0.08) }
        val nameMap = brandsSnapshot.documents.associate { it.id to (it.getString("name") ?: it.id) }
        
        val list = mutableListOf<BrandStat>()
        statDoc.data?.forEach { (vendor, value) ->
            val map = value as? Map<String, Any> ?: return@forEach
            list.add(
                BrandStat(
                    vendor, nameMap[vendor] ?: vendor, rateMap[vendor] ?: 0.08,
                    (map["clicks"] as? Long) ?: 0L, (map["commission"] as? Long) ?: 0L
                )
            )
        }
        return list.sortedByDescending { it.commission }
    }
    
    override suspend fun getRegisteredBrands(): List<String> {
        val brandsSnapshot = db.collection("brands").get().await()
        return brandsSnapshot.documents.map { it.id }.sorted()
    }

    override suspend fun getArtifactStats(): List<ArtifactStat> {
        val doc = db.collection("stats").document("by_artifact").get().await()
        val list = mutableListOf<ArtifactStat>()
        doc.data?.forEach { (id, value) ->
            val map = value as? Map<String, Any> ?: return@forEach
            list.add(
                ArtifactStat(
                    id, (map["title"] as? String) ?: id,
                    (map["vendor"] as? String) ?: "UNKNOWN", (map["archetype"] as? String) ?: "GHOST",
                    (map["clicks"] as? Long) ?: 0L, (map["commission"] as? Long) ?: 0L
                )
            )
        }
        return list.sortedByDescending { it.clicks }.take(10)
    }

    override suspend fun getDayStats(): List<DayStat> {
        val doc = db.collection("stats").document("by_day").get().await()
        val list = mutableListOf<DayStat>()
        doc.data?.forEach { (dateKey, value) ->
            val map = value as? Map<String, Any> ?: return@forEach
            list.add(DayStat(dateKey, (map["clicks"] as? Long) ?: 0L, (map["commission"] as? Long) ?: 0L))
        }
        return list.sortedByDescending { it.dateKey }.take(14).reversed()
    }

    override suspend fun getSalesDayStats(): List<SalesDayStat> {
        val cutoffMs = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000
        
        // Chỉ tải đơn hàng trong 14 ngày gần nhất (thay vì tải toàn bộ)
        val result = db.collection("orders")
            .whereGreaterThan("timestamp", cutoffMs)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().await()

        val dayMap = mutableMapOf<String, Pair<Int, Long>>() 
        
        result.documents.forEach { doc ->
            val ts = doc.getLong("timestamp") ?: 0L
            val cal = Calendar.getInstance().also { it.timeInMillis = ts }
            val dateKey = "%04d-%02d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            val amount = doc.getLong("totalAmount") ?: 0L
            val prev = dayMap[dateKey] ?: (0 to 0L)
            dayMap[dateKey] = (prev.first + 1) to (prev.second + amount)
        }
        return dayMap.entries
            .map { (k, v) -> SalesDayStat(k, v.first, v.second) }
            .sortedBy { it.dateKey }
    }

    override fun getRecentOrdersStream(): Flow<List<RecentOrder>> = callbackFlow {
        val listener = db.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    android.util.Log.e("AdminRepo", "Orders Listen failed.", error)
                    // Do not close with error to prevent app crash if Permission Denied
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snap != null) {
                    val orders = snap.documents.mapNotNull { doc ->
                        RecentOrder(
                            docId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            totalAmount = doc.getLong("totalAmount") ?: 0L,
                            status = doc.getString("status") ?: "pending",
                            paymentMethod = doc.getString("paymentMethod") ?: "COD",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            items = (doc.get("items") as? List<String>) ?: emptyList()
                        )
                    }
                    trySend(orders)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getRecentClicksStream(): Flow<List<RecentClick>> = callbackFlow {
        val listener = db.collection("clicks")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    android.util.Log.e("AdminRepo", "Orders Listen failed.", error)
                    // Do not close with error to prevent app crash if Permission Denied
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snap != null) {
                    val clicks = snap.documents.mapNotNull { doc ->
                        RecentClick(
                            artifactTitle = doc.getString("artifactTitle") ?: "UNKNOWN",
                            vendor = doc.getString("vendor") ?: "UNKNOWN",
                            commissionEarned = doc.getLong("commissionEarned") ?: 0L,
                            archetype = doc.getString("archetype") ?: "GHOST",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    trySend(clicks)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getTicketsStream(): Flow<List<TicketItem>> = callbackFlow {
        val listener = db.collection("contacts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    android.util.Log.e("AdminRepo", "Orders Listen failed.", error)
                    // Do not close with error to prevent app crash if Permission Denied
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snap != null) {
                    val tickets = snap.documents.mapNotNull { doc ->
                        TicketItem(
                            docId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            userEmail = doc.getString("userEmail") ?: "UNKNOWN",
                            title = doc.getString("title") ?: "NO TITLE",
                            message = doc.getString("message") ?: "",
                            status = doc.getString("status") ?: "new",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    trySend(tickets)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getInventory(): List<ArtifactItem> {
        val result = db.collection("artifacts").get().await()
        return result.documents.mapNotNull { doc ->
            val sizesList = (doc.get("sizes") as? List<String>) ?: emptyList()
            val rawSizeStock = (doc.get("sizeStock") as? Map<String, Any>) ?: emptyMap()
            val sizeStockMap = rawSizeStock.mapValues { (_, v) ->
                when (v) {
                    is Long -> v
                    is Double -> v.toLong()
                    else -> 0L
                }
            }
            ArtifactItem(
                docId = doc.id,
                id = doc.getString("id") ?: doc.id,
                title = doc.getString("title") ?: "UNKNOWN",
                vendor = doc.getString("vendor") ?: "UNKNOWN",
                archetype = doc.getString("archetype") ?: "GHOST",
                price = doc.getString("price") ?: "0 VND",
                imageUrl = doc.getString("imageUrl") ?: "",
                affiliateLink = doc.getString("affiliateLink") ?: "",
                category = doc.getString("category") ?: "UNCLASSIFIED",
                commissionRate = doc.getDouble("commissionRate") ?: 0.08,
                isDirectSale = doc.getBoolean("isDirectSale") ?: false,
                stock = doc.getLong("stock") ?: 0L,
                internalPrice = doc.getLong("internalPrice") ?: 0L,
                sizes = sizesList,
                sizeStock = sizeStockMap
            )
        }
    }

    override suspend fun updateArtifact(docId: String, price: String, stock: Long, commissionRate: Double, isDirectSale: Boolean, internalPrice: Long) {
        db.collection("artifacts").document(docId).update(
            mapOf(
                "price" to price, 
                "stock" to stock,
                "commissionRate" to commissionRate,
                "isDirectSale" to isDirectSale,
                "internalPrice" to internalPrice
            )
        ).await()
    }

    override suspend fun updateSizeStock(docId: String, sizeStock: Map<String, Long>, totalStock: Long) {
        db.collection("artifacts").document(docId).update(
            mapOf(
                "sizeStock" to sizeStock,
                "stock" to totalStock
            )
        ).await()
    }

    override suspend fun deleteArtifact(docId: String) {
        val artifactDoc = db.collection("artifacts").document(docId).get().await()
        val artifactId = artifactDoc.getString("id") ?: docId

        // Delete the artifact itself
        db.collection("artifacts").document(docId).delete().await()

        // Clean up from all users' wishlists
        try {
            val usersWithItem = db.collection("users")
                .whereArrayContains("wishlist", artifactId)
                .get().await()
                
            for (userDoc in usersWithItem.documents) {
                db.collection("users").document(userDoc.id).update(
                    "wishlist", FieldValue.arrayRemove(artifactId)
                ).await()
            }
        } catch (e: Exception) {
            android.util.Log.e("AdminRepo", "Failed to clean up orphaned wishlists", e)
        }
    }

    override suspend fun addArtifact(data: Map<String, Any>, imageUri: Uri?): Result<Unit> {
        return try {
            val mutData = data.toMutableMap()
            if (imageUri != null) {
                val res = cloudinaryApi.uploadImage(imageUri)
                if (res.isSuccess) {
                    mutData["imageUrl"] = res.getOrThrow()
                } else {
                    throw Exception(res.exceptionOrNull()?.message ?: "Upload failed")
                }
            }
            db.collection("artifacts").add(mutData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun runNikeSeeder() {
        com.rinnsan.creavity.data.vault.NikeSeeder.seed(db)
    }

    override suspend fun runPumaSeeder() {
        com.rinnsan.creavity.data.vault.PumaSeeder.seed(db)
    }

    override suspend fun runBalenciagaSeeder() {
        com.rinnsan.creavity.data.vault.BalenciagaSeeder.seed(db)
    }

    override suspend fun runRickOwensSeeder() {
        com.rinnsan.creavity.data.vault.RickOwensSeeder.seed(db)
    }

    override suspend fun runRinnsanSeeder() {
        com.rinnsan.creavity.data.vault.RinnsanSeeder.seed(db)
    }

    override suspend fun getOrders(): List<AdminOrderItem> {
        val result = db.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            
        return result.documents.mapNotNull { doc ->
            val rawDetails = (doc.get("itemDetails") as? List<Map<String, Any>>) ?: emptyList()
            val details = rawDetails.map { m ->
                OrderItemDetail(
                    artifactId = m["artifactId"] as? String ?: "",
                    title = m["title"] as? String ?: "",
                    size = m["size"] as? String ?: "",
                    quantity = (m["quantity"] as? Long)?.toInt() ?: 1,
                    subtotal = m["subtotal"] as? Long ?: 0L
                )
            }
            AdminOrderItem(
                docId = doc.id,
                userId = doc.getString("userId") ?: "",
                totalAmount = doc.getLong("totalAmount") ?: 0L,
                status = doc.getString("status") ?: "pending",
                paymentMethod = doc.getString("paymentMethod") ?: "COD",
                timestamp = doc.getLong("timestamp") ?: 0L,
                items = (doc.get("items") as? List<String>) ?: emptyList(),
                itemDetails = details
            )
        }
    }

    override suspend fun updateOrderStatus(docId: String, newStatus: String) {
        val orderRef = db.collection("orders").document(docId)

        // If changing to cancelled, refund stock and sizeStock
        if (newStatus.equals("cancelled", ignoreCase = true)) {
            val orderSnap = orderRef.get().await()
            val currentStatus = orderSnap.getString("status") ?: ""
            
            // Only refund if it wasn't already cancelled
            if (!currentStatus.equals("cancelled", ignoreCase = true)) {
                val rawDetails = (orderSnap.get("itemDetails") as? List<Map<String, Any>>) ?: emptyList()
                for (item in rawDetails) {
                    val artifactId = item["artifactId"] as? String ?: continue
                    val quantity = (item["quantity"] as? Long) ?: 1L
                    val size = item["size"] as? String ?: ""
                    
                    val artifactsQuery = db.collection("artifacts").whereEqualTo("id", artifactId).get().await()
                    for (artifactDoc in artifactsQuery.documents) {
                        val updates = mutableMapOf<String, Any>(
                            "stock" to FieldValue.increment(quantity)
                        )
                        if (size.isNotBlank()) {
                            updates["sizeStock.$size"] = FieldValue.increment(quantity)
                        }
                        db.collection("artifacts").document(artifactDoc.id).update(updates).await()
                    }
                }
            }
        }

        orderRef.update("status", newStatus).await()
    }

    override suspend fun updateOrderTracking(docId: String, lat: Double, lng: Double) {
        val data = mapOf("lat" to lat, "lng" to lng)
        db.collection("orders").document(docId).update("tracking", data).await()
    }

    override suspend fun updateBrandRate(vendor: String, newRate: Double) {
        db.collection("brands").document(vendor).update("rate", newRate).await()
    }

    override suspend fun getUsers(): List<UserItem> {
        val result = db.collection("users").get().await()
        return result.documents.map { doc ->
            val wishlist = (doc.get("wishlist") as? List<*>)?.size ?: 0
            val pf = doc.get("identityProfile")
            val archetype = when (pf) {
                is String -> pf
                is Map<*, *> -> pf["dominantArchetype"] as? String ?: "UNKNOWN"
                else -> "UNKNOWN"
            }
            val username = doc.getString("username") ?: "GUEST"
            UserItem(
                uid = doc.id,
                email = doc.getString("email") ?: "NO_EMAIL",
                username = username,
                role = doc.getString("role") ?: "user",
                status = doc.getString("status") ?: "active",
                archetype = archetype,
                wishlistCount = wishlist
            )
        }
    }

    override suspend fun updateUserRole(uid: String, newRole: String) {
        db.collection("users").document(uid).update("role", newRole).await()
    }

    override suspend fun updateUserStatus(uid: String, newStatus: String) {
        db.collection("users").document(uid).update("status", newStatus).await()
    }

    override suspend fun getTickets(): List<TicketItem> {
        val result = db.collection("contacts")
            .orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            
        return result.documents.mapNotNull { doc ->
            TicketItem(
                docId = doc.id,
                userId = doc.getString("userId") ?: "",
                userEmail = doc.getString("userEmail") ?: "UNKNOWN",
                title = doc.getString("title") ?: "NO TITLE",
                message = doc.getString("message") ?: "",
                status = doc.getString("status") ?: "new",
                timestamp = doc.getLong("timestamp") ?: 0L,
                adminReply = doc.getString("adminReply")
            )
        }
    }

    override suspend fun updateTicketStatus(docId: String, newStatus: String) {
        db.collection("contacts").document(docId).update("status", newStatus).await()
    }

    override suspend fun replyToTicket(docId: String, reply: String) {
        val updates = mapOf(
            "adminReply" to reply,
            "status" to "resolved",
            "adminReplyTimestamp" to System.currentTimeMillis()
        )
        db.collection("contacts").document(docId).update(updates).await()
    }
}
