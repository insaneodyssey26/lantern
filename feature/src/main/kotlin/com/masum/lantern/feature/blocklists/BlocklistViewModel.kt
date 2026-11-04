package com.masum.lantern.feature.blocklists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.repository.BlocklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlocklistViewModel @Inject constructor(
    private val blocklistRepository: BlocklistRepository,
) : ViewModel() {

    private val _isUpdatingAll = MutableStateFlow(false)
    private val _updatingBlocklistId = MutableStateFlow<String?>(null)
    private val _showAddDialog = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<BlocklistUiState> = combine(
        combine(
            blocklistRepository.observeBlocklists(),
            blocklistRepository.observeTotalRuleCount(),
            _isUpdatingAll
        ) { lists, totalRules, updatingAll ->
            Triple(lists, totalRules, updatingAll)
        },
        combine(
            _updatingBlocklistId,
            _showAddDialog,
            _userMessage
        ) { updatingId, showDialog, msg ->
            Triple(updatingId, showDialog, msg)
        }
    ) { (lists, totalRules, updatingAll), (updatingId, showDialog, msg) ->
        BlocklistUiState(
            blocklists = lists,
            totalRules = totalRules,
            isUpdatingAll = updatingAll,
            updatingBlocklistId = updatingId,
            showAddDialog = showDialog,
            userMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BlocklistUiState()
    )

    fun toggleBlocklist(id: String, enabled: Boolean) {
        viewModelScope.launch {
            blocklistRepository.toggleBlocklist(id, enabled)
        }
    }

    fun syncAll() {
        if (_isUpdatingAll.value) return
        viewModelScope.launch {
            _isUpdatingAll.value = true
            val result = blocklistRepository.syncAllBlocklists()
            _isUpdatingAll.value = false
            result.onSuccess { count ->
                _userMessage.value = if (count > 0) "Updated $count blocklists" else "All blocklists up to date"
            }.onFailure {
                _userMessage.value = "Failed to update blocklists: ${it.message}"
            }
        }
    }

    fun syncSingle(id: String) {
        if (_updatingBlocklistId.value != null) return
        viewModelScope.launch {
            _updatingBlocklistId.value = id
            val result = blocklistRepository.syncBlocklist(id)
            _updatingBlocklistId.value = null
            result.onSuccess { modified ->
                _userMessage.value = if (modified) "Blocklist updated" else "Already up to date"
            }.onFailure {
                _userMessage.value = "Failed to sync: ${it.message}"
            }
        }
    }

    fun openAddDialog() {
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
    }

    fun addCustomBlocklist(name: String, url: String) {
        val trimmedName = name.trim()
        val trimmedUrl = url.trim()
        if (trimmedName.isEmpty() || trimmedUrl.isEmpty()) {
            _userMessage.value = "Name and URL cannot be empty"
            return
        }
        viewModelScope.launch {
            _showAddDialog.value = false
            val result = blocklistRepository.addCustomBlocklist(trimmedName, trimmedUrl)
            result.onSuccess {
                _userMessage.value = "Custom blocklist added"
            }.onFailure {
                _userMessage.value = "Failed to load custom list: ${it.message}"
            }
        }
    }

    fun deleteCustomBlocklist(id: String) {
        viewModelScope.launch {
            val result = blocklistRepository.deleteCustomBlocklist(id)
            result.onSuccess {
                _userMessage.value = "Blocklist removed"
            }.onFailure {
                _userMessage.value = "Failed to delete: ${it.message}"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
