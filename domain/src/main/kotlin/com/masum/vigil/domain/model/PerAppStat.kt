package com.masum.vigil.domain.model

data class PerAppStat(
    val appInfo: AppInfo,
    val totalQueries: Int,
    val trackerQueries: Int,
) {
    val trackerPercentage: Float
        get() = if (totalQueries == 0) 0f else trackerQueries / totalQueries.toFloat()
}
