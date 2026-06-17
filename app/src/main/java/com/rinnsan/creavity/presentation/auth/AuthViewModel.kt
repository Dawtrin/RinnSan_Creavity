package com.rinnsan.creavity.presentation.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// STATE
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// VIEWMODEL
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Khởi tạo "Cổng an ninh" (Auth) và "Nhà kho" (Firestore)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ── LOGIN ─────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("EMAIL VÀ ACCESS CODE KHÔNG ĐƯỢC ĐỂ TRỐNG.")
            return
        }

        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = task.exception?.localizedMessage?.uppercase() ?: "LỖI ĐĂNG NHẬP KHÔNG XÁC ĐỊNH."
                    _authState.value = AuthState.Error("// $errorMsg")
                }
            }
    }

    // ── GOOGLE SIGN-IN ────────────────────────────────────────────
    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser ?: return@addOnCompleteListener
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true

                    if (isNewUser) {
                        // Người dùng mới — tạo hồ sơ Firestore
                        val userProfile = hashMapOf(
                            "email" to (user.email ?: ""),
                            "displayName" to (user.displayName ?: ""),
                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                            "createdAt" to System.currentTimeMillis(),
                            "role" to "agent",
                            "provider" to "google",
                            "identityProfile" to null
                        )
                        db.collection("users").document(user.uid)
                            .set(userProfile)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Success
                            }
                            .addOnFailureListener { e ->
                                val errorMsg = e.localizedMessage?.uppercase() ?: "LỖI TẠO HỒ SƠ."
                                _authState.value = AuthState.Error("// $errorMsg")
                            }
                    } else {
                        // Người dùng cũ — vào thẳng
                        _authState.value = AuthState.Success
                    }
                } else {
                    val errorMsg = task.exception?.localizedMessage?.uppercase()
                        ?: "LỖI ĐĂNG NHẬP GOOGLE KHÔNG XÁC ĐỊNH."
                    _authState.value = AuthState.Error("// $errorMsg")
                }
            }
    }

    // ── FACEBOOK SIGN-IN ──────────────────────────────────────────
    fun signInWithFacebook(accessToken: com.facebook.AccessToken) {
        _authState.value = AuthState.Loading

        val credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser ?: return@addOnCompleteListener
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true

                    if (isNewUser) {
                        // Người dùng mới — tạo hồ sơ Firestore
                        val userProfile = hashMapOf(
                            "email" to (user.email ?: ""),
                            "displayName" to (user.displayName ?: ""),
                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                            "createdAt" to System.currentTimeMillis(),
                            "role" to "agent",
                            "provider" to "facebook",
                            "identityProfile" to null
                        )
                        db.collection("users").document(user.uid)
                            .set(userProfile)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Success
                            }
                            .addOnFailureListener { e ->
                                val errorMsg = e.localizedMessage?.uppercase() ?: "LỖI TẠO HỒ SƠ."
                                _authState.value = AuthState.Error("// $errorMsg")
                            }
                    } else {
                        // Người dùng cũ — vào thẳng
                        _authState.value = AuthState.Success
                    }
                } else {
                    val errorMsg = task.exception?.localizedMessage?.uppercase()
                        ?: "LỖI ĐĂNG NHẬP FACEBOOK KHÔNG XÁC ĐỊNH."
                    _authState.value = AuthState.Error("// $errorMsg")
                }
            }
    }

    // ── REGISTER ──────────────────────────────────────────────────
    fun register(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("VUI LÒNG ĐIỀN ĐẦY ĐỦ THÔNG TIN.")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("ACCESS CODE KHÔNG KHỚP. THỬ LẠI.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("ACCESS CODE TỐI THIỂU 6 KÝ TỰ.")
            return
        }

        _authState.value = AuthState.Loading

        // Bước 1: Tạo tài khoản trên hệ thống Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Lấy ra mã UID (định danh duy nhất) của tài khoản vừa tạo
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Bước 2: Đóng gói thông tin hồ sơ
                    val userProfile = hashMapOf(
                        "email" to email,
                        "createdAt" to System.currentTimeMillis(),
                        "role" to "agent", // Phân quyền cơ bản
                        "identityProfile" to null // Chỗ trống để sau này lưu kết quả quét thẻ phong cách
                    )

                    // Bước 3: Lưu hồ sơ vào Firestore trong collection "users"
                    db.collection("users").document(userId)
                        .set(userProfile)
                        .addOnSuccessListener {
                            // Cả Auth và Database đều thành công -> Cho phép vào hệ thống
                            _authState.value = AuthState.Success
                        }
                        .addOnFailureListener { e ->
                            // Xử lý lỗi nếu việc lưu Database thất bại
                            val errorMsg = e.localizedMessage?.uppercase() ?: "LỖI TẠO HỒ SƠ DATABASE."
                            _authState.value = AuthState.Error("// $errorMsg")
                        }
                } else {
                    val errorMsg = task.exception?.localizedMessage?.uppercase() ?: "LỖI KHỞI TẠO IDENTITY KHÔNG XÁC ĐỊNH."
                    _authState.value = AuthState.Error("// $errorMsg")
                }
            }
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────
    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("NHẬP EMAIL ĐỂ TIẾP TỤC.")
            return
        }

        _authState.value = AuthState.Loading

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = task.exception?.localizedMessage?.uppercase() ?: "LỖI GỬI SIGNAL KHÔNG XÁC ĐỊNH."
                    _authState.value = AuthState.Error("// $errorMsg")
                }
            }
    }

    // ── LOGOUT ───────────────────────────────────────────────────
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }
}