package com.masum.lantern.feature.domainrules

data class DomainRulesUiState(
    val allowedDomains: List<String> = emptyList(),
    val blockedDomains: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: DomainRuleTab = DomainRuleTab.ALLOWED,
)

enum class DomainRuleTab {
    ALLOWED,
    BLOCKED
}
