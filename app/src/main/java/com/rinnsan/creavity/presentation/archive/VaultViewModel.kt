package com.rinnsan.creavity.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rinnsan.creavity.data.vault.AffiliateArtifact
import com.rinnsan.creavity.domain.models.Archetype
import com.rinnsan.creavity.domain.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository
) : ViewModel() {

    // ── States ────────────────────────────────────────────────────
    private val _userArchetype = MutableStateFlow<Archetype>(Archetype.GHOST)
    val userArchetype: StateFlow<Archetype> = _userArchetype

    private val _allArtifacts = MutableStateFlow<List<AffiliateArtifact>>(emptyList())
    val allArtifacts: StateFlow<List<AffiliateArtifact>> = _allArtifacts

    private val _isFiltered = MutableStateFlow(false)
    val isFiltered: StateFlow<Boolean> = _isFiltered

    // Loading state — HomeScreen dùng để show skeleton
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _displayArtifacts = MutableStateFlow<List<AffiliateArtifact>>(emptyList())
    val displayArtifacts: StateFlow<List<AffiliateArtifact>> = _displayArtifacts

    private val _wishlistIds = MutableStateFlow<List<String>>(emptyList())
    val wishlistIds: StateFlow<List<String>> = _wishlistIds

    enum class VaultTab { STORE, AFFILIATE }

    private val _selectedTab = MutableStateFlow(VaultTab.STORE)
    val selectedTab: StateFlow<VaultTab> = _selectedTab

    init {
        listenToArtifacts()
        loadUserProfile()

        viewModelScope.launch {
            combine(_allArtifacts, _isFiltered, _userArchetype, _selectedTab) { all, filtered, archetype, tab ->
                var list = all
                if (filtered) list = list.filter { it.archetype == archetype }
                if (tab == VaultTab.STORE) {
                    list.filter { it.isDirectSale }
                } else {
                    list.filter { !it.isDirectSale }
                }
            }.collect {
                _displayArtifacts.value = it
            }
        }
    }

    private fun listenToArtifacts() {
        _isLoading.value = true
        viewModelScope.launch {
            vaultRepository.getArtifactsStream().collect { list ->
                _allArtifacts.value = list
                _isLoading.value = false
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val profileData = vaultRepository.getUserProfile()
            if (profileData != null) {
                _wishlistIds.value = profileData.first
                _userArchetype.value = profileData.second
            }
        }
    }

    fun setTab(tab: VaultTab)      { _selectedTab.value = tab }
    fun toggleFilter()             { _isFiltered.value = !_isFiltered.value }

    fun recordClick(artifact: AffiliateArtifact) {
        vaultRepository.recordClick(artifact)
    }

    fun toggleWishlistStatus(artifactId: String) {
        viewModelScope.launch {
            _wishlistIds.value = vaultRepository.toggleWishlistStatus(artifactId, _wishlistIds.value)
        }
    }
}