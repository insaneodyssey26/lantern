package com.masum.lantern.domain.model

data class AppInfo(
    val uid: Int,
    val packageName: String,
    val appName: String,
) {
    companion object {
        val UNKNOWN = AppInfo(uid = -1, packageName = "unknown", appName = "Unknown app")
    }
}
