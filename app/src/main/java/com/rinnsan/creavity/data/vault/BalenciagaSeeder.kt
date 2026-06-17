package com.rinnsan.creavity.data.vault

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object BalenciagaSeeder {

    suspend fun seed(db: FirebaseFirestore) {

        val data = listOf(
            // ── GHOST (Timeless, Iconic, Monochromatic) ──────────────────────────────────
            mapOf("id" to "BC-G01", "title" to "BALENCIAGA POLITICAL CAMPAIGN HOODIE", "vendor" to "BALENCIAGA", "category" to "HOODIE", "price" to "26.500.000", "imageUrl" to "https://balenciaga.dam.kering.com/m/2aba7c015b3fa7e4/Large-578135TKVI99084_F.jpg?v=7", "affiliateLink" to "https://www.balenciaga.com/en-us/political-campaign-medium-fit-hoodie--white-578135TKVI99084.html", "archetype" to "GHOST", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "BC-G02", "title" to "BALENCIAGA LE CAGOLE SHOULDER BAG", "vendor" to "BALENCIAGA", "category" to "BAG", "price" to "63.000.000", "imageUrl" to "https://www.balenciaga.com/en-us/le-cagole-shoulder-bag-small-black-6713071VG9Y1000.html", "affiliateLink" to "https://www.balenciaga.com/en-us/le-cagole-shoulder-bag-small-black-6713071VG9Y1000.html", "archetype" to "GHOST", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),

            // ── OPERATOR (Tactical, Heavy Utility, Stealth) ──────────────────────────────
            mapOf("id" to "BC-O01", "title" to "BALENCIAGA HOODED BOMBER BLACK", "vendor" to "BALENCIAGA", "category" to "JACKET", "price" to "72.000.000", "imageUrl" to "https://balenciaga.dam.kering.com/asset/2a5e9e19-fcc6-4430-ac41-550b31f842e7/Large/866531TQM111000_F.jpg?v=2", "affiliateLink" to "https://www.balenciaga.com/en-us/hooded-bomber-black-866531TQM111000.html", "archetype" to "OPERATOR", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "BC-O02", "title" to "BALENCIAGA CHINO PANTS BLACK", "vendor" to "BALENCIAGA", "category" to "PANTS", "price" to "27.500.000", "imageUrl" to "https://balenciaga.dam.kering.com/asset/52cd3cc2-8770-4dae-bfd8-e85e5189c90a/Large/871309TKP071000_F.jpg?v=2", "affiliateLink" to "https://www.balenciaga.com/en-us/chino-pants-black-871309TKP071000.html", "archetype" to "OPERATOR", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),

            // ── GLITCH (Avant-Garde, Experimental, Disruptive) ───────────────────────────
            mapOf("id" to "BC-GL1", "title" to "BALENCIAGA STANDARD BOMBER DIRTY GREY", "vendor" to "BALENCIAGA", "category" to "JACKET", "price" to "70.000.000", "imageUrl" to "https://balenciaga.dam.kering.com/asset/f06f7090-fcf7-47c0-a5da-0b02518ddead/Large/850058TTW661505_F.jpg?v=3", "affiliateLink" to "https://www.balenciaga.com/en-us/standard-bomber-jacket-dirty-grey-light-blue-850058TTW661505.html", "archetype" to "GLITCH", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "BC-GL2", "title" to "BALENCIAGA PAINTER'S BAGGY SWEATPANTS", "vendor" to "BALENCIAGA", "category" to "PANTS", "price" to "33.500.000", "imageUrl" to "https://balenciaga.dam.kering.com/asset/37efe67e-8ab8-40a0-91c4-2c0bb593ae9d/Large/740028TUVJ61000_F.jpg?v=2", "affiliateLink" to "https://www.balenciaga.com/en-us/painter-s-shirt-baggy-sweatpants-black-740028TUVJ61000.html", "archetype" to "GLITCH", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),

            // ── NOMAD (Exploration, Everyday Movement, Utility) ──────────────────────────
            mapOf("id" to "BC-N01", "title" to "BALENCIAGA TRACKSUIT JACKET BLACK", "vendor" to "BALENCIAGA", "category" to "JACKET", "price" to "45.000.000", "imageUrl" to "https://balenciaga.dam.kering.com/asset/d7a71da6-8a82-46eb-8d90-eb76329f2d92/Large/869734TUS026165_F.jpg?v=2", "affiliateLink" to "https://www.balenciaga.com/en-us/tracksuit-jacket-black-burgundy-869734TUS026165.html", "archetype" to "NOMAD", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "BC-N02", "title" to "BALENCIAGA LE CITY BAG SMALL NAVY", "vendor" to "BALENCIAGA", "category" to "BAG", "price" to "52.000.000", "imageUrl" to "https://balenciaga.dam.kering.com/asset/22a29fc9-fb0b-4a30-8dda-981795b29125/Large/8657622ACVW4449_F.jpg?v=1", "affiliateLink" to "https://www.balenciaga.com/en-us/le-city-bag-small-navy-8657622ACVW4449.html", "archetype" to "NOMAD", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L),
            mapOf("id" to "BC-N03", "title" to "BALENCIAGA SKI BACKPACK BLACK", "vendor" to "BALENCIAGA", "category" to "BAG", "price" to "37.500.000", "imageUrl" to "https://www.balenciaga.com/en-us/ski-backpack-black-7708822ABEN1000.html", "affiliateLink" to "https://www.balenciaga.com/en-us/ski-backpack-black-7708822ABEN1000.html", "archetype" to "NOMAD", "commissionRate" to 0.08, "isDirectSale" to false, "stock" to 0L, "internalPrice" to 0L)
        )

        for (item in data) {
            val docId = item["id"] as String
            db.collection("artifacts").document(docId).set(item).await()
        }
    }
}
