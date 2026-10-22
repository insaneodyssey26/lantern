package com.masum.lantern.feature.domainrules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DomainRulesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedTab = MutableStateFlow(DomainRuleTab.ALLOWED)

    val uiState: StateFlow<DomainRulesUiState> = combine(
        settingsRepository.getUserAllowedDomains(),
        settingsRepository.getUserBlockedDomains(),
        searchQuery,
        selectedTab
    ) { allowed, blocked, query, tab ->
        val queryClean = query.trim().lowercase()
        val filteredAllowed = allowed.toList().sorted().filter {
            queryClean.isEmpty() || it.contains(queryClean)
        }
        val filteredBlocked = blocked.toList().sorted().filter {
            queryClean.isEmpty() || it.contains(queryClean)
        }
        DomainRulesUiState(
            allowedDomains = filteredAllowed,
            blockedDomains = filteredBlocked,
            searchQuery = query,
            selectedTab = tab,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DomainRulesUiState()
    )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSelectedTab(tab: DomainRuleTab) {
        selectedTab.value = tab
    }

    fun addAllowedDomain(domain: String) {
        val clean = domain.trim().lowercase()
        if (clean.isNotBlank()) {
            viewModelScope.launch {
                settingsRepository.setUserDomainAllowed(clean, true)
            }
        }
    }

    fun removeAllowedDomain(domain: String) {
        viewModelScope.launch {
            settingsRepository.setUserDomainAllowed(domain, false)
        }
    }

    fun addBlockedDomain(domain: String) {
        val clean = domain.trim().lowercase()
        if (clean.isNotBlank()) {
            viewModelScope.launch {
                settingsRepository.setUserDomainBlocked(clean, true)
            }
        }
    }

    fun removeBlockedDomain(domain: String) {
        viewModelScope.launch {
            settingsRepository.setUserDomainBlocked(domain, false)
        }
    }
}
