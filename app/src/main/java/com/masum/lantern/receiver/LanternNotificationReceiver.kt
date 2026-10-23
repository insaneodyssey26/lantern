package com.masum.lantern.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.domain.usecase.StopMonitoringUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LanternNotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var stopMonitoringUseCase: StopMonitoringUseCase
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent?.action) {
                    ACTION_STOP_VPN -> {
                        stopMonitoringUseCase()
                    }
                    ACTION_TOGGLE_SHIELD -> {
                        val current = settingsRepository.isBlockingEnabled().first()
                        settingsRepository.setBlockingEnabled(!current)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_STOP_VPN = "com.masum.lantern.action.STOP_VPN"
        const val ACTION_TOGGLE_SHIELD = "com.masum.lantern.action.TOGGLE_SHIELD"
    }
}
