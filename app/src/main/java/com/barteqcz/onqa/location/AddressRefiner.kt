package com.barteqcz.onqa.location

import android.location.Address
import com.barteqcz.onqa.data.model.LocationInfo

object AddressRefiner {

    fun refineLocation(addresses: List<Address>?, lat: Double? = null, lon: Double? = null): LocationInfo {
        val firstAddress = addresses?.firstOrNull()
        val baseInfo = if (firstAddress == null) {
            LocationInfo()
        } else {
            // Collect structural identifiers from all results to use for non-hardcoded filtering
            val adminNames = addresses.flatMap { 
                listOfNotNull(it.subAdminArea, it.adminArea, it.countryName) 
            }.map { it.lowercase() }.toSet()
            
            val roadNames = addresses.mapNotNull { it.thoroughfare?.lowercase() }.toSet()

            // Heuristic: Find a name that isn't a broad administrative region or a specific street.
            // Priority: Locality (City) -> SubLocality (Neighborhood/Town) -> Feature (Village/Building)
            var city = addresses.firstNotNullOfOrNull { addr ->
                addr.locality?.takeIf { it.lowercase() !in adminNames && it.lowercase() !in roadNames }
            } ?: addresses.firstNotNullOfOrNull { addr ->
                addr.subLocality?.takeIf { it.lowercase() !in adminNames && it.lowercase() !in roadNames }
            } ?: addresses.firstNotNullOfOrNull { addr ->
                addr.featureName?.takeIf { 
                    !it.matches(Regex("\\d+.*")) && it.lowercase() !in adminNames && it.lowercase() !in roadNames 
                }
            }

            // Fallback: If everything was filtered out, just use the first available locality or admin area
            if (city == null) {
                city = firstAddress.locality ?: firstAddress.subAdminArea ?: firstAddress.adminArea
            }

            // Cut anything after the first comma
            val finalCity = city?.substringBefore(",")?.trim()
            
            LocationInfo(
                city = finalCity,
                country = firstAddress.countryName,
                countryCode = firstAddress.countryCode
            )
        }

        return applySpecialRegionOverrides(baseInfo, lat, lon, addresses)
    }

    private fun applySpecialRegionOverrides(
        info: LocationInfo,
        lat: Double?,
        lon: Double?,
        addresses: List<Address>?
    ): LocationInfo {
        val area = addresses?.firstNotNullOfOrNull { it.adminArea } ?: ""
        val country = addresses?.firstNotNullOfOrNull { it.countryName } ?: ""
        val locality = addresses?.firstNotNullOfOrNull { it.locality } ?: ""
        val currentCode = info.countryCode

        val mentionsCrimea = listOf(area, country, locality).any {
            it.contains("Crimea", ignoreCase = true)
        }

        if (mentionsCrimea) {
            return info.copy(countryCode = "UA")
        }

        if (lat != null && lon != null && lat in 44.38..46.1 && lon in 32.4..36.6) {
            if (currentCode == null || currentCode == "RU") {
                if (info.city != null || info.country != null || currentCode == "RU") {
                    return info.copy(countryCode = "UA")
                }
            }
        }

        val mentionsKosovo = listOf(area, country, locality).any {
            it.contains("Kosovo", ignoreCase = true)
        }

        if (mentionsKosovo || currentCode == "XK") {
            return info.copy(countryCode = "XK")
        }

        if (currentCode == null && lat != null && lon != null && lat in 41.85..43.25 && lon in 20.0..21.7) {
            if (info.city != null || info.country != null) {
                return info.copy(countryCode = "XK")
            }
        }

        return info
    }
}
