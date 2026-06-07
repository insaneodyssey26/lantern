package com.masum.vigil.feature.perapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.vigil.domain.model.PerAppStat
import com.masum.vigil.domain.usecase.GetPerAppStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PerAppViewModel @Inject constructor(
    getPerAppStats: GetPerAppStatsUseCase,
) : ViewModel() {

    val uiState: StateFlow<PerAppUiState> = getPerAppStats()
        .map { list -> PerAppUiState(apps = list.map { it.toUiModel() }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PerAppUiState(),
        )

    private fun PerAppStat.toUiModel() = PerAppStatUiModel(
        appName = appInfo.appName,
        packageName = appInfo.packageName,
        totalQueries = totalQueries,
        trackerQueries = trackerQueries,
        trackerPercentage = (trackerPercentage * 100).toInt(),
    )
}
