package com.barteqcz.onqa.data.repository

import android.content.Context
import android.location.Location
import com.barteqcz.onqa.R
import com.barteqcz.onqa.data.model.LocationInfo
import com.barteqcz.onqa.data.model.RadioStation
import com.barteqcz.onqa.data.remote.LocationRequest
import com.barteqcz.onqa.data.remote.RadioApiService
import com.barteqcz.onqa.data.util.NetworkResult
import com.barteqcz.onqa.di.ApplicationScope
import com.barteqcz.onqa.location.LocationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioRepository @Inject constructor(
    private val apiService: RadioApiService,
    private val locationManager: LocationManager,
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val scope: CoroutineScope,
) {
    private val _stations = MutableStateFlow<NetworkResult<List<RadioStation>>>(NetworkResult.Loading)
    val stations: StateFlow<NetworkResult<List<RadioStation>>> = _stations.asStateFlow()

    val currentLocation: StateFlow<Location?> = locationManager.currentLocation
    val locationInfo: StateFlow<LocationInfo> = locationManager.locationInfo

    private val isFetching = AtomicBoolean(false)
    private var observationJob: Job? = null

    init {
        scope.launch {
            // No initial fetch here. We wait for startLocationTracking() 
            // to be called from ViewModel, which will trigger updateNearbyStations via observationJob.
        }
    }

    fun startLocationTracking() {
        Timber.i("Starting radio station location tracking...")
        locationManager.startTracking()
        
        if (observationJob != null) {
            Timber.d("Observation job already running.")
            return
        }
        observationJob = scope.launch {
            locationManager.currentLocation
                .filterNotNull()
                .distinctUntilChanged { old, new ->
                    (old.latitude == new.latitude) && (old.longitude == new.longitude)
                }
                .collectLatest { location ->
                    updateNearbyStations(location)
                }
        }
    }

    fun stopLocationTracking() {
        locationManager.stopTracking()
        observationJob?.cancel()
        observationJob = null
    }

    suspend fun updateNearbyStations(location: Location) = safeApiCall("updateNearbyStations") {
        val request = LocationRequest(location.latitude, location.longitude)
        apiService.getNearbyStations(request)
    }

    private suspend fun safeApiCall(
        actionName: String,
        call: suspend () -> List<RadioStation>,
    ) {
        if (isFetching.getAndSet(true)) {
            Timber.d("Already fetching stations ($actionName), skipping.")
            return
        }

        try {
            val result = call()
            _stations.value = NetworkResult.Success(result)
            Timber.i("Successfully completed $actionName with ${result.size} stations.")
        } catch (e: IOException) {
            Timber.e(e, "IO error in $actionName")
            val isServerError = e !is UnknownHostException
            val message = e.message ?: context.getString(R.string.error_io)
            _stations.value = NetworkResult.Error(message, e, isServerError = isServerError)
        } catch (e: HttpException) {
            Timber.e(e, "HTTP error in $actionName (code: ${e.code()})")
            _stations.value = NetworkResult.Error(context.getString(R.string.error_server_with_code, e.code()), e, isServerError = true)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error in $actionName")
            val message = e.message ?: context.getString(R.string.error_unknown)
            _stations.value = NetworkResult.Error(message, e, isServerError = true)
        } finally {
            isFetching.set(false)
        }
    }
}
