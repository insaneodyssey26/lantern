package com.masum.lantern.feature.util

import com.masum.lantern.core.ui.components.VerdictUiState
import com.masum.lantern.domain.model.Verdict
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Verdict.toUiState(): VerdictUiState = when (this) {
    is Verdict.Tracker -> VerdictUiState.Tracker
    Verdict.Clean -> VerdictUiState.Clean
    Verdict.Unknown -> VerdictUiState.Unknown
}

fun Long.formatTime(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))

fun Long.formatDate(): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(this))

fun formatDuration(startMs: Long, endMs: Long): String {
    val duration = endMs - startMs
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
    val hours = TimeUnit.MILLISECONDS.toHours(duration)
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}
