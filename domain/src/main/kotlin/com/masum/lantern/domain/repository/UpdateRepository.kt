package com.masum.lantern.domain.repository

import com.masum.lantern.domain.model.AppUpdate
import com.masum.lantern.domain.model.UpdateDownloadState
import kotlinx.coroutines.flow.Flow

interface UpdateRepository {
    suspend fun checkForUpdate(): Result<AppUpdate?>
    fun downloadUpdate(apkUrl: String, fileName: String): Flow<UpdateDownloadState>
    fun installUpdate(filePath: String): Result<Unit>
}
