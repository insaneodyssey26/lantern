package com.masum.lantern.feature.settings.updater

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.lantern.domain.model.AppUpdate
import com.masum.lantern.domain.model.UpdateDownloadState
import com.masum.lantern.domain.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UpdateCheckStatus {
    data object Idle : UpdateCheckStatus
    data object Checking : UpdateCheckStatus
    data class UpdateAvailable(val update: AppUpdate) : UpdateCheckStatus
    data object UpToDate : UpdateCheckStatus
    data class Error(val message: String) : UpdateCheckStatus
}

data class UpdaterUiState(
    val status: UpdateCheckStatus = UpdateCheckStatus.Idle,
    val downloadState: UpdateDownloadState = UpdateDownloadState.Idle,
    val isSheetVisible: Boolean = false,
)

@HiltViewModel
class UpdaterViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdaterUiState())
    val uiState: StateFlow<UpdaterUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null

    fun checkForUpdates(showSheetOnResult: Boolean = true) {
        if (_uiState.value.status is UpdateCheckStatus.Checking) return

        _uiState.update { it.copy(status = UpdateCheckStatus.Checking) }
        viewModelScope.launch {
            val result = updateRepository.checkForUpdate()
            result.fold(
                onSuccess = { update ->
                    if (update != null) {
                        _uiState.update {
                            it.copy(
                                status = UpdateCheckStatus.UpdateAvailable(update),
                                isSheetVisible = showSheetOnResult
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                status = UpdateCheckStatus.UpToDate,
                                isSheetVisible = showSheetOnResult
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            status = UpdateCheckStatus.Error(error.localizedMessage ?: "Failed to check for updates"),
                            isSheetVisible = showSheetOnResult
                        )
                    }
                }
            )
        }
    }

    fun startDownload(update: AppUpdate) {
        if (downloadJob != null && downloadJob?.isActive == true) return

        downloadJob = viewModelScope.launch {
            updateRepository.downloadUpdate(update.apkDownloadUrl, update.apkFileName)
                .collect { state ->
                    _uiState.update { it.copy(downloadState = state) }
                }
        }
    }

    fun installDownloadedUpdate(filePath: String): Result<Unit> {
        return updateRepository.installUpdate(filePath)
    }

    fun showSheet() {
        _uiState.update { it.copy(isSheetVisible = true) }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(isSheetVisible = false) }
    }

    fun resetState() {
        downloadJob?.cancel()
        downloadJob = null
        _uiState.update {
            UpdaterUiState(
                status = UpdateCheckStatus.Idle,
                downloadState = UpdateDownloadState.Idle,
                isSheetVisible = false
            )
        }
    }
}
