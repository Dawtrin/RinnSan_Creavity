package com.rinnsan.creavity.core.util

// Sealed class quản lý 3 trạng thái của dữ liệu:
// 1. Success: Có dữ liệu trả về (data != null)
// 2. Error: Có lỗi xảy ra (kèm message thông báo)
// 3. Loading: Đang tải (có thể kèm data cũ nếu muốn cache)

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(val isLoading: Boolean = true) : Resource<T>(null)
}