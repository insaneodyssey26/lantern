package com.masum.lantern.feature.blocklists

import com.masum.lantern.domain.model.Blocklist
import com.masum.lantern.domain.model.BlocklistCategory

data class BlocklistUiState(
    val blocklists: List<Blocklist> = emptyList(),
    val totalRules: Int = 0,
    val isUpdatingAll: Boolean = false,
    val updatingBlocklistId: String? = null,
    val showAddDialog: Boolean = false,
    val userMessage: String? = null
) {
    val categorized: Map<BlocklistCategory, List<Blocklist>>
        get() = blocklists.groupBy { it.category }
}
