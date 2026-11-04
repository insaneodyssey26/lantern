package com.masum.lantern.domain.model

enum class BlocklistCategory(val displayName: String) {
    ADS_AND_TRACKING("Ads & Tracking"),
    MALWARE("Malware & Security"),
    TELEMETRY("Telemetry & Diagnostics"),
    CUSTOM("Custom Feeds")
}

data class Blocklist(
    val id: String,
    val name: String,
    val description: String,
    val category: BlocklistCategory,
    val url: String,
    val isEnabled: Boolean,
    val isCustom: Boolean,
    val ruleCount: Int,
    val lastUpdated: Long,
    val etag: String? = null,
    val sourceUrl: String? = null
)
