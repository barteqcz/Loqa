package com.barteqcz.onqa.data.util

import com.barteqcz.onqa.data.model.RadioStation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

object StationProcessor {

    private val comparator = compareBy<RadioStation> { it.distance ?: Double.MAX_VALUE }
        .thenBy { it.transmitterId ?: Int.MAX_VALUE }
        .thenBy { it.displayOrder ?: Int.MAX_VALUE }

    private val resultComparator = compareByDescending<RadioStation> { it.isFavorite }
        .thenBy { it.distance ?: Double.MAX_VALUE }
        .thenBy { it.transmitterId ?: Int.MAX_VALUE }
        .thenBy { it.displayOrder ?: Int.MAX_VALUE }

    fun groupAndSortStations(
        allStations: List<RadioStation>,
        activeUrl: String?,
        favorites: Set<String>
    ): ImmutableList<RadioStation> {
        if (allStations.isEmpty()) return kotlinx.collections.immutable.persistentListOf()
        
        val normalizedActive = activeUrl?.trimEnd('/')
        return allStations.groupBy { it.name to it.network }
            .asSequence()
            .mapNotNull { (_, networkStations) ->
                var currentInGroup: RadioStation? = null
                var bestRepresentative: RadioStation? = null

                for (station in networkStations) {
                    if (normalizedActive != null && 
                        (station.normalizedStreamUrl == normalizedActive || 
                         station.normalizedStreamUrlHq == normalizedActive)) {
                        currentInGroup = station
                        break
                    }

                    val isZeroCoverage = station.coverageKm != null && station.coverageKm <= 0.0
                    val isExplicitlyOutOfCoverage = station.distance != null && 
                            station.coverageKm != null && station.distance > station.coverageKm
                    
                    if (!isZeroCoverage && !isExplicitlyOutOfCoverage) {
                        if (bestRepresentative == null || comparator.compare(station, bestRepresentative) < 0) {
                            bestRepresentative = station
                        }
                    }
                }

                val representative = currentInGroup ?: bestRepresentative
                representative?.copy(isFavorite = representative.name in favorites)
            }
            .sortedWith(resultComparator)
            .toImmutableList()
    }
}
