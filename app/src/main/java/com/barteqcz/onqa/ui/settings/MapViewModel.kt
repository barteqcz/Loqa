package com.barteqcz.onqa.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barteqcz.onqa.data.util.NetworkResult
import com.barteqcz.onqa.location.AddressRefiner
import com.barteqcz.onqa.location.LocationRepository
import com.barteqcz.onqa.ui.main.RadioViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import timber.log.Timber
import javax.inject.Inject

data class MapPickerState(
    val selectedLocation: GeoPoint? = null,
    val isGeocoding: Boolean = false,
    val city: String? = null,
    val countryCode: String? = null,
    val error: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MapPickerState())
    val state: StateFlow<MapPickerState> = _state.asStateFlow()

    fun onLocationSelected(point: GeoPoint) {
        _state.value = _state.value.copy(
            selectedLocation = point,
            isGeocoding = true,
            error = null
        )
        
        viewModelScope.launch {
            val androidLoc = android.location.Location("map").apply {
                latitude = point.latitude
                longitude = point.longitude
            }
            
            val result = locationRepository.getAddressesFromLocation(androidLoc, 10)
            
            when (result) {
                is NetworkResult.Success -> {
                    val refined = AddressRefiner.refineLocation(
                        result.data, 
                        point.latitude, 
                        point.longitude
                    )
                    _state.value = _state.value.copy(
                        city = refined.city,
                        countryCode = refined.countryCode,
                        isGeocoding = false
                    )
                }
                is NetworkResult.Error -> {
                    Timber.w("Failed to geocode manual location: ${result.message}")
                    _state.value = _state.value.copy(
                        isGeocoding = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }
    
    fun confirmLocation(radioViewModel: RadioViewModel, onConfirmed: () -> Unit) {
        val currentState = _state.value
        val loc = currentState.selectedLocation ?: return
        
        radioViewModel.updateManualLocation(
            city = currentState.city,
            code = currentState.countryCode,
            latitude = loc.latitude,
            longitude = loc.longitude
        )
        onConfirmed()
    }
}
