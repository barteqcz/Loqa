package com.barteqcz.onqa.location

import android.location.Address
import com.barteqcz.onqa.data.model.LocationInfo

object AddressRefiner {

    fun refineLocation(addresses: List<Address>?, lat: Double? = null, lon: Double? = null): LocationInfo {
        val firstAddress = addresses?.firstOrNull()
        val baseInfo = if (firstAddress == null) {
            LocationInfo()
        } else {
            val countryNames = addresses.mapNotNull { it.countryName?.lowercase() }.toSet()
            val roadNames = addresses.mapNotNull { it.thoroughfare?.lowercase() }.toSet()

            var city = addresses.firstNotNullOfOrNull { addr ->
                val loc = addr.locality ?: return@firstNotNullOfOrNull null
                val admin = addr.adminArea
                val subAdmin = addr.subAdminArea
                
                if (loc.lowercase() in countryNames || loc.lowercase() in roadNames) return@firstNotNullOfOrNull null
                
                if (loc.equals(admin, ignoreCase = true)) return@firstNotNullOfOrNull loc
                
                if (loc.equals(subAdmin, ignoreCase = true)) {
                    val subLoc = addr.subLocality
                    if (subLoc != null && subLoc.lowercase() !in roadNames) return@firstNotNullOfOrNull subLoc
                    
                    // If no subLocality, we'll keep looking in other address results.
                    return@firstNotNullOfOrNull null
                }
                
                loc
            }

            if (city == null) {
                city = firstAddress.locality ?: firstAddress.subAdminArea ?: firstAddress.adminArea
            }

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
