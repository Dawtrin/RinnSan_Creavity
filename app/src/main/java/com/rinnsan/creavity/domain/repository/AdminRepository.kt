package com.rinnsan.creavity.domain.repository

import android.net.Uri
import com.rinnsan.creavity.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    suspend fun checkIsAdmin(): Boolean
    
    suspend fun getSummary(): AdminSummary
    suspend fun getBrandStats(): List<BrandStat>
    suspend fun getArtifactStats(): List<ArtifactStat>
    suspend fun getDayStats(): List<DayStat>
    suspend fun getSalesDayStats(): List<SalesDayStat>
    
    fun getRecentOrdersStream(): Flow<List<RecentOrder>>
    fun getRecentClicksStream(): Flow<List<RecentClick>>
    fun getTicketsStream(): Flow<List<TicketItem>>
    
    suspend fun getInventory(): List<ArtifactItem>
    suspend fun updateArtifact(docId: String, price: String, stock: Long, commissionRate: Double, isDirectSale: Boolean, internalPrice: Long)
    suspend fun updateSizeStock(docId: String, sizeStock: Map<String, Long>, totalStock: Long)
    suspend fun deleteArtifact(docId: String)
    suspend fun addArtifact(data: Map<String, Any>, imageUri: Uri?): Result<Unit>
    
    suspend fun runNikeSeeder()
    suspend fun runPumaSeeder()
    suspend fun runBalenciagaSeeder()
    suspend fun runRickOwensSeeder()
    suspend fun runRinnsanSeeder()
    
    suspend fun getOrders(): List<AdminOrderItem>
    suspend fun updateOrderStatus(docId: String, newStatus: String)
    suspend fun updateOrderTracking(docId: String, lat: Double, lng: Double)
    
    suspend fun getRegisteredBrands(): List<String>
    suspend fun updateBrandRate(vendor: String, newRate: Double)
    
    suspend fun getUsers(): List<UserItem>
    suspend fun updateUserRole(uid: String, newRole: String)
    suspend fun updateUserStatus(uid: String, newStatus: String)
    
    suspend fun getTickets(): List<TicketItem>
    suspend fun updateTicketStatus(docId: String, newStatus: String)
    suspend fun replyToTicket(docId: String, reply: String)
}
