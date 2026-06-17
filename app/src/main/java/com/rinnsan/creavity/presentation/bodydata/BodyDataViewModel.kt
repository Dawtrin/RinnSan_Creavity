package com.rinnsan.creavity.presentation.bodydata

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BodyDataViewModel @Inject constructor() : ViewModel() {

    // Khởi tạo kết nối với Cổng an ninh và Nhà kho
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Biến chứa dữ liệu tải từ Cloud về để đưa lên giao diện
    private val _bodyData = MutableStateFlow<Map<String, String>>(emptyMap())
    val bodyData: StateFlow<Map<String, String>> = _bodyData

    // Trạng thái kiểm tra xem đã lưu thành công chưa
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    init {
        // Tự động tải dữ liệu ngay khi màn hình vừa mở lên
        loadDataFromCloud()
    }

    // ── LẤY DỮ LIỆU TỪ FIRESTORE VỀ ─────────────────────────────────
    private fun loadDataFromCloud() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Lấy ra ngăn "bodyData" (nếu đã từng lưu trước đó)
                    val data = document.get("bodyData") as? Map<String, String>
                    if (data != null) {
                        _bodyData.value = data
                    }
                }
            }
    }

    // ── ĐẨY DỮ LIỆU LÊN FIRESTORE ───────────────────────────────────
    fun saveDataToCloud(measurements: Map<String, String>) {
        val uid = auth.currentUser?.uid ?: return

        // Đóng gói dữ liệu vào một ngăn tên là "bodyData"
        val dataToSave = hashMapOf(
            "bodyData" to measurements
        )

        // Dùng SetOptions.merge() để chỉ cập nhật/thêm mục bodyData
        // mà không làm mất các thông tin cũ (như email, role...)
        db.collection("users").document(uid)
            .set(dataToSave, SetOptions.merge())
            .addOnSuccessListener {
                _isSaved.value = true // Kích hoạt hiệu ứng "DATA SAVED" trên màn hình
            }
    }
}