package com.barteqcz.onqa.player

import com.barteqcz.onqa.data.model.RadioStation
import com.barteqcz.onqa.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationCoverageManager @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    sealed interface CoverageResult {
        data class SwitchTo(
            val name: String,
            val url: String,
            val logo: String?,
            val network: String?,
            val forceReload: Boolean = false,
        ) : CoverageResult
        data object StopPlayback : CoverageResult
        data object KeepCurrent : CoverageResult
    }

    suspend fun checkCoverage(
        stations: List<RadioStation>,
        currentUrl: String?,
        currentName: String?,
        currentNetwork: String?,
        isPlayerActive: Boolean,
    ): CoverageResult {
        if (currentUrl == null || currentName == null) return CoverageResult.KeepCurrent
        
        val normalizedCurrent = currentUrl.trimEnd('/')
        val settings = settingsRepository.settingsFlow.first()
        val updatedStation = stations.find { it.name == currentName }
        
        if (updatedStation != null) {
            val targetUrl = if (settings.useHqStream && !updatedStation.streamUrlHq.isNullOrBlank()) {
                updatedStation.streamUrlHq
            } else {
                updatedStation.streamUrl
            }
            
            if (targetUrl != null && targetUrl.trimEnd('/') != normalizedCurrent) {
                return CoverageResult.SwitchTo(
                    name = updatedStation.name,
                    url = targetUrl,
                    logo = updatedStation.logo,
                    network = updatedStation.network,
                    forceReload = true
                )
            }
        }

        val playingStation = updatedStation ?: stations.find { 
            it.streamUrl?.trimEnd('/') == normalizedCurrent || it.streamUrlHq?.trimEnd('/') == normalizedCurrent 
        }
        
        val isOutOfCoverage = playingStation == null || 
                             (playingStation.coverageKm != null && playingStation.coverageKm <= 0.0) ||
                             (playingStation.distance != null && playingStation.coverageKm != null && playingStation.distance > playingStation.coverageKm)
        
        if (isOutOfCoverage && isPlayerActive) {
            val favorites = settings.favoriteStations

            val nextStation = if (currentNetwork != null) {
                stations.asSequence()
                    .filter { it.network == currentNetwork }
                    .sortedWith(
                        compareBy<RadioStation> {
                            val inCoverage = it.distance != null && it.coverageKm != null && it.distance <= it.coverageKm
                            val notZeroCoverage = it.coverageKm == null || it.coverageKm > 0.0
                            !(inCoverage && notZeroCoverage)
                        }.thenBy { it.distance ?: Double.MAX_VALUE }
                    )
                    .firstOrNull()
            } else null
            
            val fallbackStation = nextStation ?: stations.asSequence()
                .sortedWith(
                    compareByDescending<RadioStation> { it.name in favorites }
                        .thenBy {
                            val inCoverage = it.distance != null && it.coverageKm != null && it.distance <= it.coverageKm
                            val notZeroCoverage = it.coverageKm == null || it.coverageKm > 0.0
                            !(inCoverage && notZeroCoverage)
                        }
                        .thenBy { it.distance ?: Double.MAX_VALUE }
                )
                .firstOrNull()

            if ((fallbackStation != null) && (fallbackStation.streamUrl != currentUrl)) {
                val url = if (settings.useHqStream && !fallbackStation.streamUrlHq.isNullOrBlank()) {
                    fallbackStation.streamUrlHq
                } else {
                    fallbackStation.streamUrl
                }
                if (url != null) {
                    return CoverageResult.SwitchTo(
                        name = fallbackStation.name,
                        url = url,
                        logo = fallbackStation.logo,
                        network = fallbackStation.network
                    )
                }
            }
            return CoverageResult.StopPlayback
        }
        
        return CoverageResult.KeepCurrent
    }
}
