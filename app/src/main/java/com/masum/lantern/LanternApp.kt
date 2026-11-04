package com.masum.lantern

import android.app.Application
import com.masum.lantern.updates.AppUpdateScheduler
import com.masum.lantern.updates.BlocklistSyncScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LanternApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppUpdateScheduler.schedulePeriodicUpdateCheck(this)
        BlocklistSyncScheduler.schedulePeriodicBlocklistSync(this)
    }
}
