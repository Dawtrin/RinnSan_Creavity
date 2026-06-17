package com.rinnsan.creavity.domain.model

data class OrderItem(
    val artifactId: String = "",
    val title: String = "",
    val vendor: String = "",
    val size: String = "",
    val quantity: Int = 1,
    val price: Long = 0L,
    val subtotal: Long = 0L
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<String> = emptyList(),          // Display strings: "Title [L] × 2"
    val itemDetails: List<OrderItem> = emptyList(),  // Structured data for processing
    val totalAmount: Long = 0L,
    val status: String = "pending", // "pending", "paid", "shipped", "delivered", "cancelled"
    val paymentMethod: String = "COD", // "COD", "Banking"
    val address: Map<String, String> = emptyMap(),
    val shippingFee: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val tracking: Map<String, Double> = emptyMap() // "lat" to 10.123, "lng" to 106.123
)
