package com.masum.lantern.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.masum.lantern.domain.repository.SettingsRepository
import com.masum.lantern.domain.usecase.PauseMonitoringUseCase
import com.masum.lantern.domain.usecase.ResumeMonitoringUseCase
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
    @Inject lateinit var pauseMonitoringUseCase: PauseMonitoringUseCase
    @Inject lateinit var resumeMonitoringUseCase: ResumeMonitoringUseCase
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val stopAction = "${context.packageName}.action.STOP_VPN"
        val toggleAction = "${context.packageName}.action.TOGGLE_SHIELD"
        val pauseAction = "${context.packageName}.action.PAUSE_VPN"
        val resumeAction = "${context.packageName}.action.RESUME_VPN"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent?.action) {
                    stopAction -> {
                        stopMonitoringUseCase()
                    }
                    toggleAction -> {
                        val current = settingsRepository.isBlockingEnabled().first()
                        settingsRepository.setBlockingEnabled(!current)
                    }
                    pauseAction -> {
                        val duration = intent.getLongExtra("duration_ms", 15 * 60 * 1000L)
                        pauseMonitoringUseCase(duration)
                    }
                    resumeAction -> {
                        resumeMonitoringUseCase()
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
        const val ACTION_PAUSE_VPN = "com.masum.lantern.action.PAUSE_VPN"
        const val ACTION_RESUME_VPN = "com.masum.lantern.action.RESUME_VPN"
    }
}
