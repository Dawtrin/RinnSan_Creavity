package com.rinnsan.creavity.domain.model

data class Artifact(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val price: String = "", // String vì trong dart bạn để "$450.00"
    val image: String = "",
    val isVideo: Boolean = false, // Mặc định là false
    // New fields for Vault E-commerce & Affiliate Hybrid
    val commissionRate: Double = 0.0,
    val isDirectSale: Boolean = false,
    val internalPrice: Double? = null,
    val stock: Int? = null
)