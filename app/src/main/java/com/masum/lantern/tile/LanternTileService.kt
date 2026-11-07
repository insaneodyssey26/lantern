package com.masum.lantern.tile

import android.content.Intent
import android.graphics.drawable.Icon
import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.masum.lantern.MainActivity
import com.masum.lantern.R
import com.masum.lantern.domain.model.MonitoringState
import com.masum.lantern.domain.repository.MonitoringRepository
import com.masum.lantern.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LanternTileService : TileService() {

    @Inject lateinit var monitoringRepository: MonitoringRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var observeJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        observeJob?.cancel()
        observeJob = scope.launch {
            monitoringRepository.observeMonitoringState().collect { state ->
                updateTileState(state)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        observeJob?.cancel()
        observeJob = null
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val currentState = monitoringRepository.observeMonitoringState().first()
            if (currentState is MonitoringState.Active) {
                monitoringRepository.stopMonitoring()
            } else {
                val prepareIntent = VpnService.prepare(applicationContext)
                if (prepareIntent != null) {
                    val activityIntent = Intent(applicationContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        startActivityAndCollapse(
                            android.app.PendingIntent.getActivity(
                                applicationContext,
                                0,
                                activityIntent,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        startActivityAndCollapse(activityIntent)
                    }
                } else {
                    monitoringRepository.startMonitoring()
                }
            }
        }
    }

    private fun updateTileState(state: MonitoringState) {
        val tile = qsTile ?: return
        when (state) {
            is MonitoringState.Active -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "Lantern"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = if (state.isPaused) "Paused" else "Protected"
                }
                tile.icon = Icon.createWithResource(this, R.drawable.ic_stat_lantern)
            }
            is MonitoringState.Idle -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "Lantern"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Off"
                }
                tile.icon = Icon.createWithResource(this, R.drawable.ic_stat_lantern)
            }
            is MonitoringState.Error -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "Lantern"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Error"
                }
                tile.icon = Icon.createWithResource(this, R.drawable.ic_stat_lantern)
            }
        }
        tile.updateTile()
    }
}
