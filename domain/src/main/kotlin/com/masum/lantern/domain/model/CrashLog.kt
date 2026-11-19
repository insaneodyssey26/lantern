package com.masum.lantern.domain.model

data class CrashLog(
    val timestamp: Long,
    val formattedTime: String,
    val appVersion: String,
    val deviceModel: String,
    val androidVersion: String,
    val exceptionClass: String,
    val message: String,
    val stackTrace: String,
)
