package com.rinnsan.creavity.domain.model

data class AdminSummary(
    val totalClicks: Long      = 0L,
    val totalCommission: Long  = 0L,
    val totalUsers: Long       = 0L,
    val totalArtifacts: Long   = 0L,
    val totalOrders: Long      = 0L,
    val pendingOrders: Long    = 0L,
    val openTickets: Long      = 0L,
    val deliveredOrders: Long  = 0L,
    val totalRevenue: Long     = 0L
)

data class BrandStat(
    val vendor: String,
    val name: String,
    val rate: Double,
    val clicks: Long,
    val commission: Long
)

data class ArtifactStat(
    val id: String,
    val title: String,
    val vendor: String,
    val archetype: String,
    val clicks: Long,
    val commission: Long
)

data class DayStat(
    val dateKey: String,
    val clicks: Long,
    val commission: Long
)

data class SalesDayStat(
    val dateKey: String,
    val orderCount: Int,
    val revenue: Long
)

data class RecentOrder(
    val docId: String,
    val userId: String,
    val totalAmount: Long,
    val status: String,
    val paymentMethod: String,
    val timestamp: Long,
    val items: List<String>
)

data class RecentClick(
    val artifactTitle: String,
    val vendor: String,
    val commissionEarned: Long,
    val archetype: String,
    val timestamp: Long
)

data class ArtifactItem(
    val docId: String,
    val id: String,
    val title: String,
    val vendor: String,
    val archetype: String,
    val price: String,
    val imageUrl: String,
    val affiliateLink: String,
    val category: String,
    val commissionRate: Double,
    val isDirectSale: Boolean,
    val stock: Long,
    val internalPrice: Long,
    val sizes: List<String> = emptyList(),
    val sizeStock: Map<String, Long> = emptyMap()
)

data class OrderItemDetail(
    val artifactId: String,
    val title:      String,
    val size:       String,
    val quantity:   Int,
    val subtotal:   Long
)

data class AdminOrderItem(
    val docId: String,
    val userId: String,
    val totalAmount: Long,
    val status: String,
    val paymentMethod: String,
    val timestamp: Long,
    val items: List<String>,
    val itemDetails: List<OrderItemDetail> = emptyList()
)

data class UserItem(
    val uid: String,
    val email: String,
    val username: String,
    val role: String,
    val status: String,
    val archetype: String,
    val wishlistCount: Int
)

data class TicketItem(
    val docId: String,
    val userId: String,
    val userEmail: String,
    val title: String,
    val message: String,
    val status: String,
    val timestamp: Long,
    val adminReply: String? = null
)
