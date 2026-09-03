package com.barteqcz.onqa.update

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface UpdateDownloadStatus {
    data object Idle : UpdateDownloadStatus
    data class Downloading(val progress: Int) : UpdateDownloadStatus
    data object Completed : UpdateDownloadStatus
    data class Error(val message: String) : UpdateDownloadStatus
}

@Singleton
class UpdateManager @Inject constructor() {
    private val _downloadStatus = MutableStateFlow<UpdateDownloadStatus>(UpdateDownloadStatus.Idle)
    val downloadStatus = _downloadStatus.asStateFlow()

    fun updateProgress(progress: Int) {
        _downloadStatus.value = UpdateDownloadStatus.Downloading(progress)
    }

    fun setCompleted() {
        _downloadStatus.value = UpdateDownloadStatus.Completed
    }

    fun setError(message: String) {
        _downloadStatus.value = UpdateDownloadStatus.Error(message)
    }

    fun reset() {
        _downloadStatus.value = UpdateDownloadStatus.Idle
    }
}
