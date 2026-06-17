package com.rinnsan.creavity.core.constants

import com.rinnsan.creavity.domain.model.Artifact

// DỮ LIỆU MẪU (THEO PATTERN LƯỚI GÃY)
// Ánh xạ 1:1 từ file mock_data.dart

object MockData {
    val artifacts = listOf(
        // HÀNG 1: 1 To - 2 Nhỏ
        Artifact(
            id = "1",
            title = "KINETIC BOOT",
            category = "BOOT",
            price = "450.00",
            image = "assets/images/s3.jpg" // Lưu ý: Kotlin cần load ảnh kiểu khác, nhưng cứ giữ string này đã
        ),
        Artifact(
            id = "2",
            title = "ARMOR VEST",
            category = "ACCESSORY",
            price = "320.00",
            image = "assets/images/s4.jpg"
        ),
        Artifact(
            id = "3",
            title = "VISOR X1",
            category = "ACCESSORY",
            price = "150.00",
            image = "assets/images/s5.jpg"
        ),

        // HÀNG 2: Video
        Artifact(
            id = "4",
            title = "RUNWAY_CAM_04",
            category = "VIDEO",
            price = "N/A",
            image = "assets/images/s2.jpg",
            isVideo = true
        ),

        // HÀNG 3: So le
        Artifact(
            id = "5",
            title = "NEON RUNNER",
            category = "SNEAKER",
            price = "380.00",
            image = "assets/images/s6.jpg"
        ),
        Artifact(
            id = "6",
            title = "CYBER PANTS",
            category = "APPAREL",
            price = "280.00",
            image = "assets/images/s7.jpg"
        ),
        Artifact(
            id = "7",
            title = "DELTA JACKET",
            category = "APPAREL",
            price = "550.00",
            image = "assets/images/s8.jpg"
        )
    )
}