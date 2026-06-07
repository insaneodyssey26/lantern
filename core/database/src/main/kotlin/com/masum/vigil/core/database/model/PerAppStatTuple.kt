package com.masum.vigil.core.database.model

data class PerAppStatTuple(
    val appPackageName: String,
    val appName: String,
    val appUid: Int,
    val totalQueries: Int,
    val trackerQueries: Int,
)
