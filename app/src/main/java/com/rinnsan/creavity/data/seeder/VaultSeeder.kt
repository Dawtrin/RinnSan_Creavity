package com.rinnsan.creavity.data.seeder

import com.google.firebase.firestore.FirebaseFirestore
import com.rinnsan.creavity.domain.model.Brand

object VaultSeeder {
    fun seedBrands(firestore: FirebaseFirestore, onComplete: (Boolean) -> Unit) {
        val brands = listOf(
            Brand(id = "NIKE", name = "Nike", rate = 0.08, logoUrl = ""),
            Brand(id = "ADIDAS", name = "Adidas", rate = 0.07, logoUrl = ""),
            Brand(id = "RINNSAN_LAB", name = "RinnSan Lab", rate = 0.12, logoUrl = "")
        )

        val batch = firestore.batch()
        val brandsCollection = firestore.collection("brands")

        brands.forEach { brand ->
            val docRef = brandsCollection.document(brand.id)
            batch.set(docRef, brand)
        }

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun seedTestProduct(firestore: FirebaseFirestore, onComplete: (Boolean) -> Unit) {
        val testProduct = hashMapOf(
            "id" to "RINN-001",
            "title" to "SYS.ADMIN // CORE HOODIE",
            "category" to "MID LAYER // DIRECT",
            "price" to "2.500.000 VND",
            "internalPrice" to 2500000.0,
            "stock" to 100,
            "imageUrl" to "https://images.unsplash.com/photo-1556821840-3a63f15732ce?w=800&q=80",
            "vendor" to "RINNSAN LAB",
            "archetype" to "GHOST",
            "affiliateLink" to "",
            "isDirectSale" to true,
            "commissionRate" to 0.0
        )

        firestore.collection("artifacts").document("RINN-001")
            .set(testProduct)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
