package com.rinnsan.creavity.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.rinnsan.creavity.core.location.LocationService

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// MODELS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
data class CartItem(
    val artifactId: String,
    val title: String,
    val vendor: String,
    val imageUrl: String,
    val price: Long,
    val priceDisplay: String,
    val quantity: Int = 1,
    val archetype: String = "",
    val size: String = ""         // size đã chọn: "S" | "M" | "L" | "XL"
) {
    /** Unique key = artifactId + size, so same product in different sizes are separate cart entries */
    val cartKey: String get() = if (size.isNotBlank()) "${artifactId}_$size" else artifactId
}

data class ShippingAddress(
    val fullName: String = "",
    val phone: String    = "",
    val street: String   = "",
    val district: String = "",
    val city: String     = "",
    val lat: Double?     = null,
    val lng: Double?     = null,
    val countryCode: String = ""
) {
    fun isValid() = fullName.isNotBlank() && phone.isNotBlank() &&
            street.isNotBlank() && city.isNotBlank()

    fun toDisplay() = "$street, $district, $city"
    fun toMap()     = mapOf(
        "fullName" to fullName, "phone" to phone,
        "street"   to street,  "district" to district, "city" to city,
        "lat"      to (lat?.toString() ?: ""),
        "lng"      to (lng?.toString() ?: ""),
        "countryCode" to countryCode
    )
}

sealed class CheckoutState {
    object Idle    : CheckoutState()
    object Loading : CheckoutState()
    data class Success(val orderId: String) : CheckoutState()
    data class Error(val message: String)   : CheckoutState()
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// VIEWMODEL
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@HiltViewModel
class CartViewModel @Inject constructor(
    private val locationService: LocationService
) : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cartItems     = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _address       = MutableStateFlow(ShippingAddress())
    val address: StateFlow<ShippingAddress> = _address

    private val _paymentMethod = MutableStateFlow("COD") // "COD" | "Banking"
    val paymentMethod: StateFlow<String> = _paymentMethod

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState

    private val _shippingFee = MutableStateFlow<Long>(0L)
    val shippingFee: StateFlow<Long> = _shippingFee

    private val _isCalculatingLocation = MutableStateFlow(false)
    val isCalculatingLocation: StateFlow<Boolean> = _isCalculatingLocation

    // Total tính từ internalPrice + shippingFee
    val totalAmount: Long get() = _cartItems.value.sumOf { it.price * it.quantity } + _shippingFee.value

    // Cart item count để hiển thị badge
    val cartCount: Int get() = _cartItems.value.sumOf { it.quantity }

    // ── Cart operations ─────────────────────────────────────────────
    fun addToCart(item: CartItem) {
        val current = _cartItems.value.toMutableList()
        // Match by artifactId AND size — so same product in different sizes are separate entries
        val existing = current.indexOfFirst { it.cartKey == item.cartKey }
        if (existing >= 0) {
            current[existing] = current[existing].copy(quantity = current[existing].quantity + 1)
        } else {
            current.add(item)
        }
        _cartItems.value = current
    }

    fun removeFromCart(cartKey: String) {
        _cartItems.value = _cartItems.value.filter { it.cartKey != cartKey }
    }

    fun updateQuantity(cartKey: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val idx     = current.indexOfFirst { it.cartKey == cartKey }
        if (idx < 0) return
        val newQty  = (current[idx].quantity + delta).coerceAtLeast(1)
        current[idx] = current[idx].copy(quantity = newQty)
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // ── Buy Now shortcut — add 1 item then go to checkout ──────────
    fun buyNow(item: CartItem) {
        _cartItems.value = listOf(item)
    }

    // ── Address & Payment ──────────────────────────────────────────
    fun updateAddress(address: ShippingAddress) {
        _address.value = address
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    // ── Location & Shipping ─────────────────────────────────────────
    fun calculateShippingFeeFromCity(cityName: String) {
        // Nếu người dùng gõ tay đổi tên Tỉnh/TP khác với kết quả GPS thì hủy tọa độ cũ
        if (_address.value.city != cityName && _address.value.lat != null) {
            _address.value = _address.value.copy(lat = null, lng = null)
        }

        // Chỉ tự tính giá thủ công nếu không có tọa độ
        if (_address.value.lat == null) {
            val fee = if (cityName.contains("Đà Nẵng", ignoreCase = true) || cityName.contains("Da Nang", ignoreCase = true)) {
                15_000L // Mặc định 15k nếu gõ tay Đà Nẵng
            } else if (cityName.isBlank()) {
                0L
            } else {
                30_000L // Tỉnh khác
            }
            _shippingFee.value = fee
        }
    }
    fun fetchLocationAndCalculateFee(onFailure: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isCalculatingLocation.value = true
            val info = locationService.getCurrentShippingInfo()
            if (info != null) {
                _shippingFee.value = info.shippingFee
                val currentAddress = _address.value
                // Tự động điền street nếu đang trống, gán district/city
                _address.value = currentAddress.copy(
                    street = if (currentAddress.street.isBlank()) info.addressText else currentAddress.street,
                    district = if (currentAddress.district.isBlank()) info.district else currentAddress.district,
                    city = info.city,
                    lat = info.latitude,
                    lng = info.longitude,
                    countryCode = info.countryCode
                )
            } else {
                onFailure?.invoke()
            }
            _isCalculatingLocation.value = false
        }
    }

    // ── Place Order ────────────────────────────────────────────────
    fun placeOrder() {
        val uid = auth.currentUser?.uid ?: run {
            _checkoutState.value = CheckoutState.Error("CHƯA ĐĂNG NHẬP")
            return
        }
        if (!_address.value.isValid()) {
            _checkoutState.value = CheckoutState.Error("VUI LÒNG ĐIỀN ĐẦY ĐỦ ĐỊA CHỈ")
            return
        }
        if (_cartItems.value.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("GIỎ HÀNG TRỐNG")
            return
        }

        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading
            try {
                val items = _cartItems.value

                // PRE-CHECK: Validate Stock before placing order
                for (item in items) {
                    val artifactDocs = db.collection("artifacts").whereEqualTo("id", item.artifactId).get().await()
                    val doc = artifactDocs.documents.firstOrNull()
                    if (doc == null) {
                        _checkoutState.value = CheckoutState.Error("Artifact ${item.title} no longer exists.")
                        return@launch
                    }
                    
                    // Check total stock
                    val currentStock = doc.getLong("stock") ?: 0L
                    if (currentStock < item.quantity) {
                        _checkoutState.value = CheckoutState.Error("Artifact ${item.title} exceeds available total stock ($currentStock).")
                        return@launch
                    }
                    
                    // Check size stock if applicable
                    if (item.size.isNotBlank()) {
                        val sizeStockMap = doc.get("sizeStock") as? Map<String, Long> ?: emptyMap()
                        val currentSizeStock = sizeStockMap[item.size] ?: 0L
                        if (currentSizeStock < item.quantity) {
                            _checkoutState.value = CheckoutState.Error("Artifact ${item.title} size [${item.size}] exceeds available stock ($currentSizeStock).")
                            return@launch
                        }
                    }
                }

                // Build order document
                val orderData = hashMapOf(
                    "userId"        to uid,
                    // items lưu dạng "Tên sản phẩm [Size] × số lượng" để MyOrders hiển thị đúng
                    "items"         to items.map { item ->
                        val sizeSuffix = if (item.size.isNotBlank()) " [${item.size}]" else ""
                        if (item.quantity > 1) "${item.title}$sizeSuffix × ${item.quantity}"
                        else "${item.title}$sizeSuffix"
                    },
                    "itemDetails"   to items.map { item ->
                        mapOf(
                            "artifactId"  to item.artifactId,
                            "title"       to item.title,
                            "vendor"      to item.vendor,
                            "price"       to item.price,
                            "quantity"    to item.quantity,
                            "size"        to item.size,
                            "subtotal"    to item.price * item.quantity
                        )
                    },
                    "shippingFee"   to _shippingFee.value,
                    "totalAmount"   to totalAmount,
                    "status"        to "pending",
                    "paymentMethod" to _paymentMethod.value,
                    "address"       to _address.value.toMap(),
                    "timestamp"     to System.currentTimeMillis()
                )

                // Write order to Firestore
                val orderRef = db.collection("orders").add(orderData).await()

                // Deduct stock for each item (total + per-size)
                items.forEach { item ->
                    db.collection("artifacts")
                        .whereEqualTo("id", item.artifactId)
                        .get().await()
                        .documents.firstOrNull()?.let { doc ->
                            val updates = mutableMapOf<String, Any>(
                                "stock" to FieldValue.increment(-item.quantity.toLong())
                            )
                            // Also deduct sizeStock for the specific size
                            if (item.size.isNotBlank()) {
                                updates["sizeStock.${item.size}"] =
                                    FieldValue.increment(-item.quantity.toLong())
                            }
                            db.collection("artifacts").document(doc.id).update(updates)
                        }
                }

                // Update user order count
                db.collection("users").document(uid)
                    .update("orderCount", FieldValue.increment(1))
                    .addOnFailureListener {
                        db.collection("users").document(uid)
                            .update("orderCount", 1)
                    }

                _checkoutState.value = CheckoutState.Success(orderRef.id)
                _cartItems.value = emptyList() // Clear cart after success
                _address.value = ShippingAddress() // Reset address
                _shippingFee.value = 0L // Reset fee
                _isCalculatingLocation.value = false

            } catch (e: Exception) {
                _checkoutState.value = CheckoutState.Error(
                    e.message?.uppercase() ?: "LỖI KHÔNG XÁC ĐỊNH"
                )
            }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}