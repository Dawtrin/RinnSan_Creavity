package com.rinnsan.creavity.presentation.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class ContactSubmitState {
    object Idle    : ContactSubmitState()
    object Loading : ContactSubmitState()
    object Success : ContactSubmitState()
    data class Error(val message: String) : ContactSubmitState()
}

data class UserTicket(
    val docId: String,
    val title: String,
    val message: String,
    val status: String,
    val timestamp: Long,
    val adminReply: String? = null
)

@HiltViewModel
class ContactViewModel @Inject constructor() : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _submitState = MutableStateFlow<ContactSubmitState>(ContactSubmitState.Idle)
    val submitState: StateFlow<ContactSubmitState> = _submitState

    private val _myTickets = MutableStateFlow<List<UserTicket>>(emptyList())
    val myTickets: StateFlow<List<UserTicket>> = _myTickets
    
    private var ticketListener: ListenerRegistration? = null

    init {
        loadMyTickets()
    }

    // Lấy thông tin user hiện tại từ FirebaseAuth để hiển thị trên UI
    val currentUserName: String
        get() = auth.currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Anonymous"

    val currentUserEmail: String
        get() = auth.currentUser?.email ?: "—"

    val isCurrentUserVerified: Boolean
        get() = auth.currentUser?.isEmailVerified == true

    // Chỉ cần message, name/email lấy từ auth luôn
    fun submitTicket(message: String) {
        if (message.isBlank()) {
            _submitState.value = ContactSubmitState.Error("MESSAGE CANNOT BE EMPTY")
            return
        }

        val user      = auth.currentUser
        val userName  = user?.displayName?.takeIf { it.isNotBlank() } ?: "Anonymous"
        val userEmail = user?.email ?: "no-email"

        viewModelScope.launch {
            _submitState.value = ContactSubmitState.Loading
            try {
                val ticket = hashMapOf(
                    "title"     to "Message from $userName",
                    "userEmail" to userEmail,
                    "userId"    to (user?.uid ?: "GUEST"),
                    "message"   to message,
                    "status"    to "new",
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("contacts").add(ticket).await()
                _submitState.value = ContactSubmitState.Success
                loadMyTickets() // Reload after success
            } catch (e: Exception) {
                _submitState.value = ContactSubmitState.Error(e.message ?: "TRANSMISSION FAILED")
            }
        }
    }

    fun resetState() {
        _submitState.value = ContactSubmitState.Idle
    }

    fun loadMyTickets() {
        val uid = auth.currentUser?.uid ?: return
        ticketListener?.remove()
        ticketListener = db.collection("contacts")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                
                val list = snapshot?.documents?.mapNotNull { doc ->
                    UserTicket(
                        docId = doc.id,
                        title = doc.getString("title") ?: "NO TITLE",
                        message = doc.getString("message") ?: "",
                        status = doc.getString("status") ?: "new",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        adminReply = doc.getString("adminReply")
                    )
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                
                _myTickets.value = list
            }
    }
    
    override fun onCleared() {
        super.onCleared()
        ticketListener?.remove()
    }
}