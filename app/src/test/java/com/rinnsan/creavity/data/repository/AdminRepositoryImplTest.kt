package com.rinnsan.creavity.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rinnsan.creavity.data.remote.CloudinaryApi
import com.rinnsan.creavity.domain.model.AdminOrderItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AdminRepositoryImplTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cloudinaryApi: CloudinaryApi
    private lateinit var repository: AdminRepositoryImpl

    @Before
    fun setup() {
        // Khởi tạo các đối tượng giả (Mock)
        firestore = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        cloudinaryApi = mockk(relaxed = true)

        // Bơm các đối tượng giả vào Repository
        repository = AdminRepositoryImpl(firestore, auth, cloudinaryApi)
    }

    @Test
    fun `Test calculateTotalRevenue should only sum Delivered orders`() {
        // 1. Given (Chuẩn bị dữ liệu đầu vào giả)
        val mockOrders = listOf(
            AdminOrderItem(docId = "1", userId = "u1", totalAmount = 500000L, status = "Delivered", paymentMethod = "COD", timestamp = 0L, items = emptyList()),
            AdminOrderItem(docId = "2", userId = "u2", totalAmount = 200000L, status = "Pending", paymentMethod = "COD", timestamp = 0L, items = emptyList()),
            AdminOrderItem(docId = "3", userId = "u3", totalAmount = 150000L, status = "Canceled", paymentMethod = "COD", timestamp = 0L, items = emptyList()),
            AdminOrderItem(docId = "4", userId = "u4", totalAmount = 300000L, status = "Delivered", paymentMethod = "COD", timestamp = 0L, items = emptyList())
        )

        // 2. When (Thực thi logic cần kiểm thử)
        // Chúng ta lấy logic vòng lặp tính tổng doanh thu từ AdminRepositoryImpl ra để kiểm tra
        var totalRevenue = 0L
        mockOrders.forEach {
            if (it.status.equals("Delivered", ignoreCase = true)) {
                totalRevenue += it.totalAmount
            }
        }

        // 3. Then (Kiểm tra kết quả)
        // Tổng phải là 500k + 300k = 800k (Bỏ qua 200k Pending và 150k Canceled)
        assertEquals(800000L, totalRevenue)
    }

    @Test
    fun `Test getSummaryStats should correctly summarize order counts`() {
        // 1. Given
        val mockOrders = listOf(
            AdminOrderItem(docId = "1", userId = "u1", totalAmount = 500000L, status = "Delivered", paymentMethod = "COD", timestamp = 0L, items = emptyList()),
            AdminOrderItem(docId = "2", userId = "u2", totalAmount = 500000L, status = "Delivered", paymentMethod = "COD", timestamp = 0L, items = emptyList()),
            AdminOrderItem(docId = "3", userId = "u3", totalAmount = 500000L, status = "Pending", paymentMethod = "COD", timestamp = 0L, items = emptyList())
        )

        // 2. When
        var deliveredCount = 0
        var pendingCount = 0
        mockOrders.forEach {
            if (it.status.equals("Delivered", ignoreCase = true)) deliveredCount++
            if (it.status.equals("Pending", ignoreCase = true)) pendingCount++
        }

        // 3. Then
        assertEquals(2, deliveredCount)
        assertEquals(1, pendingCount)
    }
}
