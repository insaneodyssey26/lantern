package com.masum.lantern.updates

import android.app.Activity
import android.content.Context
import android.os.Build
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

object PlayUpdateManager {
    private const val UPDATE_REQUEST_CODE = 1001

    fun isInstalledFromPlayStore(context: Context): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val sourceInfo = context.packageManager.getInstallSourceInfo(context.packageName)
                sourceInfo.installingPackageName == "com.android.vending"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName) == "com.android.vending"
            }
        }.getOrDefault(false)
    }

    fun checkForPlayUpdates(activity: Activity, onUpdateDownloaded: (() -> Unit)? = null) {
        if (!isInstalledFromPlayStore(activity)) {
            return
        }

        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        var listener: InstallStateUpdatedListener? = null
        listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                onUpdateDownloaded?.invoke()
                listener?.let { appUpdateManager.unregisterListener(it) }
            }
        }
        appUpdateManager.registerListener(listener)

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                onUpdateDownloaded?.invoke()
                appUpdateManager.unregisterListener(listener)
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                runCatching {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        com.google.android.play.core.appupdate.AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                        UPDATE_REQUEST_CODE
                    )
                }
            } else {
                appUpdateManager.unregisterListener(listener)
            }
        }.addOnFailureListener {
            appUpdateManager.unregisterListener(listener)
        }
    }

    fun completeUpdate(activity: Activity) {
        AppUpdateManagerFactory.create(activity).completeUpdate()
    }
}
