package com.masum.lantern.updates

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.masum.lantern.domain.repository.BlocklistRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class BlocklistSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BlocklistWorkerEntryPoint {
        fun blocklistRepository(): BlocklistRepository
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                BlocklistWorkerEntryPoint::class.java
            )
            val repository = entryPoint.blocklistRepository()
            repository.syncAllBlocklists()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
