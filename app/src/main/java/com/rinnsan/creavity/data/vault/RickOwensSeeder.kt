package com.rinnsan.creavity.data.vault

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object RickOwensSeeder {

    suspend fun seed(db: FirebaseFirestore) {

        val data = listOf(
            // ── GHOST (Timeless, Iconic, Monochromatic) ──────────────────────────────────
            mapOf("id" to "RO-G01", "title" to "RICK OWENS HUN CREWNECK", "vendor" to "RICK OWENS", "category" to "TOP", "price" to "8.000.000", "imageUrl" to "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&q=80&w=600", "affiliateLink" to "https://www.rickowens.eu/products/du01f4267rigp09", "archetype" to "GHOST", "commissionRate" to 0.08, "isDirectSale" to true, "stock" to 10L, "internalPrice" to 8000000.0),
            mapOf("id" to "RO-G02", "title" to "RICK OWENS ROUND NECK", "vendor" to "RICK OWENS", "category" to "TOP", "price" to "9.000.000", "imageUrl" to "https://www.rickowens.eu/cdn/shop/files/RU01F2601_KWP_981_01.jpg?v=1769695235&width=832", "affiliateLink" to "https://www.rickowens.eu/products/ru01f2601kwp981", "archetype" to "GHOST", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "RO-G03", "title" to "RICK OWENS JUMBO SS T", "vendor" to "RICK OWENS", "category" to "TOP", "price" to "10.000.000", "imageUrl" to "https://www.rickowens.eu/cdn/shop/files/RR01F2208_BHEP4_5109_01.jpg?v=1771516848&width=832", "affiliateLink" to "https://www.rickowens.eu/products/rr01f2208bhep45109", "archetype" to "GHOST", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),

            // ── OPERATOR (Tactical, Heavy Utility, Stealth) ──────────────────────────────
            mapOf("id" to "RO-O01", "title" to "RICK OWENS HOLLYWOOD FLIGHT", "vendor" to "RICK OWENS", "category" to "JACKET", "price" to "30.000.000", "imageUrl" to "https://www.rickowens.eu/cdn/shop/files/RU01F2280_BA_35_01.jpg?v=1769695406&width=832", "affiliateLink" to "https://www.rickowens.eu/products/ru01f2280ba35", "archetype" to "OPERATOR", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "RO-O02", "title" to "RICK OWENS BLIXA CARGO BOMBER", "vendor" to "RICK OWENS", "category" to "JACKET", "price" to "45.000.000", "imageUrl" to "https://images.unsplash.com/photo-1559551409-dadc959f76b8?auto=format&fit=crop&q=80&w=600", "affiliateLink" to "https://www.rickowens.eu/products/rl01f2715ccvs09", "archetype" to "OPERATOR", "commissionRate" to 0.08, "isDirectSale" to true, "stock" to 5L, "internalPrice" to 45000000.0),
            mapOf("id" to "RO-O03", "title" to "RICK OWENS JUMBOLACE ARMY BOZO TRACTOR", "vendor" to "RICK OWENS", "category" to "BOOTS", "price" to "40.000.000", "imageUrl" to "https://www.rickowens.eu/cdn/shop/files/RU01F2882_LOOW2_99_01.jpg?v=1769695428&width=832", "affiliateLink" to "https://www.rickowens.eu/products/ru01f2882loow299", "archetype" to "OPERATOR", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),

            // ── GLITCH (Avant-Garde, Experimental, Disruptive) ───────────────────────────
            mapOf("id" to "RO-GL1", "title" to "RICK OWENS DRACUCABAN", "vendor" to "RICK OWENS", "category" to "JACKET", "price" to "35.000.000", "imageUrl" to "https://www.rickowens.eu/cdn/shop/files/RR01F2722_CMA_09_01.jpg?v=1769695453&width=832", "affiliateLink" to "https://www.rickowens.eu/products/rr01f2722cma09", "archetype" to "GLITCH", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "RO-GL2", "title" to "RICK OWENS FORK SHIELD T", "vendor" to "RICK OWENS", "category" to "TOP", "price" to "12.000.000", "imageUrl" to "https://www.rickowens.eu/cdn/shop/files/m_702752_RU01F2242_S_34_01.jpg?v=1774944859&width=832", "affiliateLink" to "https://www.rickowens.eu/products/ru01f2242s34", "archetype" to "GLITCH", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),

            // ── NOMAD (Exploration, Everyday Movement, Drapey) ───────────────────────────
            mapOf("id" to "RO-N01", "title" to "RICK OWENS SL HOODIE", "vendor" to "RICK OWENS", "category" to "HOODIE", "price" to "15.000.000", "imageUrl" to "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&q=80&w=600", "affiliateLink" to "https://www.rickowens.eu/products/ru01f2153jnt09", "archetype" to "NOMAD", "commissionRate" to 0.08, "isDirectSale" to true, "stock" to 15L, "internalPrice" to 15000000.0),
            mapOf("id" to "RO-N02", "title" to "RICK OWENS LACEUP BOGUN", "vendor" to "RICK OWENS", "category" to "BOOTS", "price" to "38.000.000", "imageUrl" to "https://www.rickowens.eu/cdn/shop/files/0297171_laceup-bogun.jpg?v=1771865539&width=832", "affiliateLink" to "https://www.rickowens.eu/products/ru01f2839loo09", "archetype" to "NOMAD", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L)
        )

        for (item in data) {
            val docId = item["id"] as String
            db.collection("artifacts").document(docId).set(item).await()
        }
    }
}
