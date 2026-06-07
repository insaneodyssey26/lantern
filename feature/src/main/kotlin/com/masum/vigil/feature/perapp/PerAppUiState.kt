package com.masum.vigil.feature.perapp

data class PerAppUiState(
    val apps: List<PerAppStatUiModel> = emptyList(),
)

data class PerAppStatUiModel(
    val appName: String,
    val packageName: String,
    val totalQueries: Int,
    val trackerQueries: Int,
    val trackerPercentage: Int,
)
