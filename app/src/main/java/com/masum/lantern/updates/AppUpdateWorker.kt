package com.masum.lantern.updates

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.masum.lantern.domain.repository.UpdateRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AppUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun updateRepository(): UpdateRepository
    }

    override suspend fun doWork(): Result {
        val isFromPlayStore = PlayUpdateManager.isInstalledFromPlayStore(applicationContext)

        if (isFromPlayStore) {
            val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
            val appUpdateInfo: AppUpdateInfo? = suspendCancellableCoroutine { cont ->
                appUpdateManager.appUpdateInfo
                    .addOnSuccessListener { info ->
                        if (cont.isActive) cont.resume(info)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            }

            if (appUpdateInfo != null && appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val availableCode = appUpdateInfo.availableVersionCode()
                val currentCode = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionCode
                    }
                } catch (_: Exception) {
                    0
                }

                if (availableCode > currentCode) {
                    UpdateNotificationManager.showUpdateAvailableNotification(applicationContext)
                }
            }
        } else {
            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
            val repo = entryPoint.updateRepository()
            val result = repo.checkForUpdate().getOrNull()
            if (result != null) {
                UpdateNotificationManager.showUpdateAvailableNotification(applicationContext, result.versionName)
            }
        }

        return Result.success()
    }
}
