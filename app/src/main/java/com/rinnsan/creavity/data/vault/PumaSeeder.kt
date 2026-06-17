package com.rinnsan.creavity.data.vault

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object PumaSeeder {
    suspend fun seed(db: FirebaseFirestore) {
        db.collection("brands").document("PUMA").set(mapOf("name" to "PUMA", "rate" to 0.07)).await()

        val data = listOf(
            // GHOST
            mapOf("id" to "PM-G01", "title" to "PUMA SUEDE CLASSIC UNISEX", "vendor" to "PUMA", "category" to "SNEAKERS", "price" to "2.300.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/399781/01/sv01/fnd/VNM/fmt/png/Suede-Classic-Sneakers-Unisex", "affiliateLink" to "https://vn.puma.com/vn/en/pd/suede-classic-sneakers-unisex/399781.html?dwvar_399781_color=01", "archetype" to "GHOST", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "PM-G02", "title" to "PUMA T7 BIG CAT TRACK JACKET", "vendor" to "PUMA", "category" to "JACKET", "price" to "2.000.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/634400/01/mod01/fnd/VNM/fmt/png/T7-Big-Cat-Track-Jacket-Men", "affiliateLink" to "https://vn.puma.com/vn/en/pd/t7-big-cat-track-jacket-men/634400.html?dwvar_634400_color=01", "archetype" to "GHOST", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            
            // OPERATOR
            mapOf("id" to "PM-O01", "title" to "PUMA MB.03 TOXIC BASKETBALL", "vendor" to "PUMA", "category" to "SNEAKER", "price" to "3.800.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/311327/01/sv01/fnd/VNM/fmt/png/PUMA-HOOPS-x-TMNT-MB.03-Lo-Krang-Basketball-Shoes-Unisex", "affiliateLink" to "https://vn.puma.com/vn/en/pd/puma-hoops-x-tmnt-mb.03-lo-krang-basketball-shoes-unisex/311327.html?dwvar_311327_color=01", "archetype" to "OPERATOR", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "PM-O02", "title" to "PUMA VELOCITY NITRO 4 RUNNING", "vendor" to "PUMA", "category" to "SNEAKER", "price" to "3.350.000 ", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_600,h_600/global/377748/01/sv01/fnd/SEA/fmt/png/Deviate-NITRO-2-Men's-Running-Shoes", "affiliateLink" to "https://vn.puma.com/vn/en/search?q=NITRO", "archetype" to "OPERATOR", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),

            // GLITCH
            mapOf("id" to "PM-GL1", "title" to "PUMATECH WINDBREAKER MEN", "vendor" to "PUMA", "category" to "JACKET", "price" to "2.100.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_600,h_600/global/692157/81/mod01/fnd/SEA/fmt/png/PUMATECH-Windbreaker-Men", "affiliateLink" to "https://vn.puma.com/vn/en/pd/pumatech-windbreaker-men/692157.html?dwvar_692157_color=81", "archetype" to "GLITCH", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "PM-GL2", "title" to "PUMATECH TRACK PANTS MEN", "vendor" to "PUMA", "category" to "PANTS", "price" to "1.700.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_600,h_600/global/636036/01/mod01/fnd/SEA/fmt/png/PUMATECH-Track-Pants-Men", "affiliateLink" to "https://vn.puma.com/vn/en/pd/pumatech-track-pants-men/636036.html?dwvar_636036_color=01", "archetype" to "GLITCH", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "PM-GL3", "title" to "CHORE JACKET MEN", "vendor" to "PUMA", "category" to "JACKET", "price" to "1.800.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_600,h_600/global/635021/61/mod01/fnd/SEA/fmt/png/Chore-Jacket-Men", "affiliateLink" to "https://vn.puma.com/vn/en/pd/chore-jacket-men/635021.html?dwvar_635021_color=61", "archetype" to "GLITCH", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),

            // NOMAD
            mapOf("id" to "PM-N01", "title" to "RACE AHEAD BOMBER JACKET UNISEX", "vendor" to "PUMA", "category" to "JACKET", "price" to "2.500.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_600,h_600/global/636031/01/mod01/fnd/SEA/fmt/png/Race-Ahead-Bomber-Jacket-Unisex", "affiliateLink" to "https://vn.puma.com/vn/en/pd/race-ahead-bomber-jacket-unisex/636031.html?dwvar_636031_color=01", "archetype" to "NOMAD", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "PM-N02", "title" to "PUMA x POKÉMON RELAXED GRAPHIC HOODIE", "vendor" to "PUMA", "category" to "HOODIE", "price" to "1.500.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_600,h_600/global/634915/76/mod01/fnd/SEA/fmt/png/PUMA-x-POKEMON-Relaxed-Graphic-Hoodie", "affiliateLink" to "https://vn.puma.com/vn/en/pd/puma-x-pok%C3%A9mon-relaxed-graphic-hoodie-men/634915.html?dwvar_634915_color=76", "archetype" to "NOMAD", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "PM-N03", "title" to "SCUDERIA FERRARI HP REPLICA TEE", "vendor" to "PUMA", "category" to "TOP", "price" to "1.200.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_600,h_600/global/713827/01/mod01/fnd/SEA/fmt/png/Scuderia-Ferrari-HP-Replica-Tee", "affiliateLink" to "https://vn.puma.com/vn/en/pd/scuderia-ferrari-hp-replica-drivers-tee-unisex/713827.html?dwvar_713827_color=01", "archetype" to "NOMAD", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "PM-N04", "title" to "A\$AP ROCKY x PUMA MONOGRAM DENIM PANTS", "vendor" to "PUMA", "category" to "PANTS", "price" to "3.500.000", "imageUrl" to "https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_600,h_600/global/633428/01/mod01/fnd/SEA/fmt/png/ASAP-ROCKY-x-PUMA-Denim-Pants", "affiliateLink" to "https://vn.puma.com/vn/en/pd/a%24ap-rocky-x-puma-laser-monogram-denim-pants-men/633428.html?dwvar_633428_color=01", "archetype" to "NOMAD", "commissionRate" to 0.07, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L)
        )

        for (item in data) {
            val docId = item["id"] as String
            db.collection("artifacts").document(docId).set(item).await()
        }
    }
}
