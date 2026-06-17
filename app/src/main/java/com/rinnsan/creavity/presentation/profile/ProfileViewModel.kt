package com.rinnsan.creavity.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ── DỮ LIỆU HIỂN THỊ ──
    private val _agentId      = MutableStateFlow("NULL")
    val agentId: StateFlow<String> = _agentId

    private val _email        = MutableStateFlow("UNKNOWN_AGENT")
    val email: StateFlow<String> = _email

    private val _username     = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _isVerified   = MutableStateFlow(false)
    val isVerified: StateFlow<Boolean> = _isVerified

    private val _enrolledDate = MutableStateFlow("UNKNOWN")
    val enrolledDate: StateFlow<String> = _enrolledDate

    private val _role         = MutableStateFlow("FETCHING...")
    val role: StateFlow<String> = _role

    private val _archetype    = MutableStateFlow("")
    val archetype: StateFlow<String> = _archetype

    private val _postCount    = MutableStateFlow(0)
    val postCount: StateFlow<Int> = _postCount

    private val _wishlistCount = MutableStateFlow(0)
    val wishlistCount: StateFlow<Int> = _wishlistCount

    // ── UI STATE ──
    private val _isSavingUsername = MutableStateFlow(false)
    val isSavingUsername: StateFlow<Boolean> = _isSavingUsername

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess
    
    private var userListener: ListenerRegistration? = null

    init { loadProfileData() }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // LOAD
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun loadProfileData() {
        val user = auth.currentUser ?: return

        // Dữ liệu từ Firebase Auth
        _agentId.value   = user.uid.take(12).uppercase()
        _email.value     = user.email ?: "UNKNOWN"
        _isVerified.value = user.isEmailVerified
        // Ưu tiên displayName từ Auth làm username mặc định
        _username.value  = user.displayName ?: ""

        val createdAt = user.metadata?.creationTimestamp ?: 0L
        if (createdAt > 0L) {
            _enrolledDate.value = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(Date(createdAt))
        }

        // Dữ liệu nâng cao từ Firestore
        userListener?.remove()
        userListener = db.collection("users").document(user.uid)
            .addSnapshotListener { doc, e ->
                if (e != null || doc == null || !doc.exists()) {
                    if (e != null || doc == null) _role.value = "AGENT"
                    return@addSnapshotListener
                }
                
                _role.value      = (doc.getString("role") ?: "AGENT").uppercase()
                _archetype.value = (doc.getString("archetype") ?: "").uppercase()
                _postCount.value = (doc.getLong("postCount") ?: 0L).toInt()

                // Ưu tiên username từ Firestore hơn Auth displayName
                val fsUsername = doc.getString("username") ?: ""
                if (fsUsername.isNotBlank()) _username.value = fsUsername

                // Đếm wishlist
                @Suppress("UNCHECKED_CAST")
                val wishlist = doc.get("wishlist") as? List<String> ?: emptyList()
                _wishlistCount.value = wishlist.size
            }
    }
    
    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // UPDATE USERNAME
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun saveUsername(newName: String) {
        val trimmed = newName.trim().uppercase()
        if (trimmed.isBlank() || trimmed == _username.value) return
        val user = auth.currentUser ?: return

        viewModelScope.launch {
            _isSavingUsername.value = true
            try {
                // 1. Cập nhật Firebase Auth displayName
                val profileUpdates = userProfileChangeRequest { displayName = trimmed }
                user.updateProfile(profileUpdates).await()

                // 2. Lưu vào Firestore users/{uid}.username
                db.collection("users").document(user.uid)
                    .set(mapOf("username" to trimmed), SetOptions.merge())
                    .await()

                _username.value = trimmed
                _saveSuccess.value = true
            } catch (e: Exception) {
                // Giữ giá trị cũ nếu lỗi
            } finally {
                _isSavingUsername.value = false
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}