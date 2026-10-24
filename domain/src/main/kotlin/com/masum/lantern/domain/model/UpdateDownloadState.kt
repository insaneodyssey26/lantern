package com.masum.lantern.domain.model

sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState
    data object Connecting : UpdateDownloadState
    data class Progress(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val percentage: Float,
        val speedBytesPerSec: Long,
    ) : UpdateDownloadState
    data class Completed(val filePath: String) : UpdateDownloadState
    data class Error(val message: String) : UpdateDownloadState
}
