package com.rinnsan.creavity.presentation.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.creavity.domain.model.*
import com.rinnsan.creavity.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminState {
    object Loading : AdminState()
    object Success : AdminState()
    data class Error(val message: String) : AdminState()
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    val state         = MutableStateFlow<AdminState>(AdminState.Loading)
    val isAdmin       = MutableStateFlow(false)
    val summary       = MutableStateFlow(AdminSummary())
    val registeredBrands = MutableStateFlow<List<String>>(emptyList())
    val brandStats    = MutableStateFlow<List<BrandStat>>(emptyList())
    val artifactStats = MutableStateFlow<List<ArtifactStat>>(emptyList())
    val dayStats       = MutableStateFlow<List<DayStat>>(emptyList())
    val recentClicks   = MutableStateFlow<List<RecentClick>>(emptyList())
    val salesDayStats  = MutableStateFlow<List<SalesDayStat>>(emptyList())
    val recentOrders   = MutableStateFlow<List<RecentOrder>>(emptyList())
    val artifacts     = MutableStateFlow<List<ArtifactItem>>(emptyList())
    val orders        = MutableStateFlow<List<AdminOrderItem>>(emptyList())
    val users         = MutableStateFlow<List<UserItem>>(emptyList())
    val tickets       = MutableStateFlow<List<TicketItem>>(emptyList())
    val actionMessage = MutableStateFlow<String?>(null)

    init { checkAdminAndLoad() }

    private fun checkAdminAndLoad() {
        viewModelScope.launch {
            try {
                isAdmin.value = repository.checkIsAdmin()
                if (isAdmin.value) { 
                    loadAllStats()
                    startRealtimeListener()
                    startTicketsListener() 
                } else {
                    state.value = AdminState.Error("ACCESS DENIED")
                }
            } catch (e: Exception) {
                state.value = AdminState.Error(e.message ?: "ERROR")
            }
        }
    }

    fun loadAllStats() {
        viewModelScope.launch {
            state.value = AdminState.Loading
            try {
                loadSummary()
                loadBrandStats()
                loadArtifactStats()
                loadDayStats()
                loadSalesStats()
                loadInventory()
                loadOrders()
                loadUsers()
                loadTickets()
                state.value = AdminState.Success
            } catch (e: Exception) {
                state.value = AdminState.Error(e.message ?: "LOAD ERROR")
            }
        }
    }

    private suspend fun loadSummary() {
        try {
            summary.value = repository.getSummary()
            android.util.Log.d("AdminVM", "Summary loaded: ${summary.value}")
        } catch (e: Exception) {
            android.util.Log.e("AdminVM", "loadSummary FAILED: ${e.message}", e)
        }
    }

    fun loadBrandStats() {
        viewModelScope.launch {
            try {
                registeredBrands.value = repository.getRegisteredBrands()
                brandStats.value = repository.getBrandStats()
            } catch (e: Exception) {
                android.util.Log.e("AdminVM", "loadBrandStats FAILED: ${e.message}", e)
            }
        }
    }

    private suspend fun loadArtifactStats() {
        try {
            artifactStats.value = repository.getArtifactStats()
        } catch (e: Exception) { android.util.Log.e("AdminVM", "loadArtifactStats FAILED: ${e.message}", e) }
    }

    private suspend fun loadDayStats() {
        try {
            dayStats.value = repository.getDayStats()
        } catch (e: Exception) { android.util.Log.e("AdminVM", "loadDayStats FAILED: ${e.message}", e) }
    }

    private suspend fun loadSalesStats() {
        try {
            salesDayStats.value = repository.getSalesDayStats()
        } catch (e: Exception) { android.util.Log.e("AdminVM", "loadSalesStats FAILED: ${e.message}", e) }
        
        // Orders listener
        viewModelScope.launch {
            repository.getRecentOrdersStream().collect { orders ->
                recentOrders.value = orders
            }
        }
    }

    private fun startRealtimeListener() {
        viewModelScope.launch {
            repository.getRecentClicksStream().collect { clicks ->
                recentClicks.value = clicks
            }
        }
    }

    private fun startTicketsListener() {
        viewModelScope.launch {
            repository.getTicketsStream().collect { tix ->
                tickets.value = tix
                val openCount = tix.count { it.status.equals("new", true) }.toLong()
                summary.value = summary.value.copy(openTickets = openCount)
            }
        }
    }

    fun loadInventory() {
        viewModelScope.launch {
            try {
                artifacts.value = repository.getInventory()
            } catch (e: Exception) { actionMessage.value = "// LOAD ERROR" }
        }
    }

    fun updateArtifact(docId: String, price: String, stock: Long, commissionRate: Double, isDirectSale: Boolean, internalPrice: Long) {
        viewModelScope.launch {
            try {
                repository.updateArtifact(docId, price, stock, commissionRate, isDirectSale, internalPrice)
                actionMessage.value = "// ARTIFACT UPDATED"
                loadInventory()
            } catch (e: Exception) { actionMessage.value = "// UPDATE ERROR: ${e.message}" }
        }
    }

    fun updateSizeStock(docId: String, sizeStock: Map<String, Long>, totalStock: Long) {
        viewModelScope.launch {
            try {
                repository.updateSizeStock(docId, sizeStock, totalStock)
                actionMessage.value = "// STOCK UPDATED"
                loadInventory()
            } catch (e: Exception) { actionMessage.value = "// STOCK UPDATE ERROR: ${e.message}" }
        }
    }

    fun deleteArtifact(docId: String) {
        viewModelScope.launch {
            try {
                repository.deleteArtifact(docId)
                actionMessage.value = "// ARTIFACT DELETED"
                loadInventory()
            } catch (e: Exception) { actionMessage.value = "// DELETE ERROR" }
        }
    }

    fun addArtifact(data: Map<String, Any>, imageUri: Uri? = null) {
        viewModelScope.launch {
            try {
                actionMessage.value = "// ADDING ARTIFACT..."
                val result = repository.addArtifact(data, imageUri)
                if (result.isSuccess) {
                    actionMessage.value = "// ARTIFACT ADDED"
                    loadInventory()
                } else {
                    actionMessage.value = "// ADD ERROR: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) { actionMessage.value = "// ADD ERROR: ${e.message}" }
        }
    }

    fun runNikeSeeder() {
        viewModelScope.launch {
            try {
                actionMessage.value = "// SEEDING NIKE DATA..."
                repository.runNikeSeeder()
                actionMessage.value = "// 20 NIKE PRODUCTS ADDED //"
                loadInventory()
                loadBrandStats()
            } catch (e: Exception) { actionMessage.value = "// SEED ERROR: ${e.message}" }
        }
    }

    fun runPumaSeeder() {
        viewModelScope.launch {
            try {
                actionMessage.value = "// SEEDING PUMA DATA..."
                repository.runPumaSeeder()
                actionMessage.value = "// 10 PUMA PRODUCTS ADDED //"
                loadInventory()
                loadBrandStats()
            } catch (e: Exception) { actionMessage.value = "// SEED ERROR: ${e.message}" }
        }
    }

    fun runBalenciagaSeeder() {
        viewModelScope.launch {
            try {
                actionMessage.value = "// SEEDING BALENCIAGA DATA..."
                repository.runBalenciagaSeeder()
                actionMessage.value = "// 9 BALENCIAGA PRODUCTS ADDED //"
                loadInventory()
                loadBrandStats()
            } catch (e: Exception) { actionMessage.value = "// SEED ERROR: ${e.message}" }
        }
    }

    fun runRickOwensSeeder() {
        viewModelScope.launch {
            try {
                actionMessage.value = "// SEEDING RICK OWENS DATA..."
                repository.runRickOwensSeeder()
                actionMessage.value = "// 9 RICK OWENS PRODUCTS ADDED //"
                loadInventory()
                loadBrandStats()
            } catch (e: Exception) { actionMessage.value = "// SEED ERROR: ${e.message}" }
        }
    }

    fun runRinnsanSeeder() {
        viewModelScope.launch {
            try {
                actionMessage.value = "// UPDATING RINNSAN STOCK DATA..."
                repository.runRinnsanSeeder()
                actionMessage.value = "// RINNSAN STOCK UPDATED //"
                loadInventory()
            } catch (e: Exception) { actionMessage.value = "// SEED ERROR: ${e.message}" }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            try {
                orders.value = repository.getOrders()
            } catch (e: Exception) { actionMessage.value = "// ORDER LOAD ERROR" }
        }
    }

    fun updateOrderStatus(docId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(docId, newStatus)
                actionMessage.value = "// ORDER → $newStatus"
                loadOrders(); loadSummary()
            } catch (e: Exception) { actionMessage.value = "// ERROR: ${e.message}" }
        }
    }

    fun updateOrderTracking(docId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                repository.updateOrderTracking(docId, lat, lng)
                actionMessage.value = "// LOCATION UPDATED: $lat, $lng"
                loadOrders()
            } catch (e: Exception) { actionMessage.value = "// ERROR: ${e.message}" }
        }
    }

    fun updateBrandRate(vendor: String, newRate: Double) {
        viewModelScope.launch {
            try {
                repository.updateBrandRate(vendor, newRate)
                actionMessage.value = "// ${vendor} RATE → ${(newRate * 100).toInt()}%"
                loadBrandStats()
            } catch (e: Exception) { actionMessage.value = "// ERROR: ${e.message}" }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                users.value = repository.getUsers()
            } catch (e: Exception) { actionMessage.value = "// USER LOAD ERROR" }
        }
    }

    fun updateUserRole(uid: String, newRole: String) {
        viewModelScope.launch {
            try {
                repository.updateUserRole(uid, newRole)
                actionMessage.value = "// ROLE → $newRole"; loadUsers()
            } catch (e: Exception) { actionMessage.value = "// ERROR: ${e.message}" }
        }
    }

    fun updateUserStatus(uid: String, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateUserStatus(uid, newStatus)
                actionMessage.value = if (newStatus == "banned") "// USER BANNED" else "// USER RESTORED"
                loadUsers()
            } catch (e: Exception) { actionMessage.value = "// ERROR: ${e.message}" }
        }
    }

    fun loadTickets() {
        viewModelScope.launch {
            try {
                tickets.value = repository.getTickets()
            } catch (e: Exception) { actionMessage.value = "// TICKET LOAD ERROR" }
        }
    }

    fun updateTicketStatus(docId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateTicketStatus(docId, newStatus)
                actionMessage.value = "// TICKET → $newStatus"
                loadTickets(); loadSummary()
            } catch (e: Exception) { actionMessage.value = "// ERROR: ${e.message}" }
        }
    }

    fun replyToTicket(docId: String, reply: String) {
        viewModelScope.launch {
            try {
                repository.replyToTicket(docId, reply)
                actionMessage.value = "// TICKET REPLIED & RESOLVED"
                loadTickets(); loadSummary()
            } catch (e: Exception) { actionMessage.value = "// ERROR: ${e.message}" }
        }
    }

    fun clearActionMessage() { actionMessage.value = null }
}
