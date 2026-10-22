package com.masum.lantern.feature.perapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.model.PerAppStat
import com.masum.lantern.domain.usecase.GetPerAppStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.repository.DnsLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PerAppViewModel @Inject constructor(
    getPerAppStats: GetPerAppStatsUseCase,
    private val dnsLogRepository: DnsLogRepository,
) : ViewModel() {

    fun getAppQueries(packageName: String): Flow<List<DnsQuery>> =
        dnsLogRepository.observeAppQueries(packageName)

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
