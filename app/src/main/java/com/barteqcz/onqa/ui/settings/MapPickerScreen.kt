package com.barteqcz.onqa.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barteqcz.onqa.R
import com.barteqcz.onqa.ui.main.RadioViewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    radioViewModel: RadioViewModel,
    mapViewModel: MapViewModel,
    onBack: () -> Unit
) {
    val state by mapViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            
            // Limit scaling to avoid excessive blurriness while keeping text readable
            val scale = (density / 1.5f).coerceAtLeast(1.0f)
            setTilesScaleFactor(scale)
            
            controller.setZoom(12.0)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }
            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    val settings by radioViewModel.settings.collectAsStateWithLifecycle()
    val isLightMode = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val pinColor = if (isLightMode) Color.Black.toArgb() else Color(0xFF1A1A1A).toArgb()

    val initialLocation = remember {
        val lat = settings.manualLatitude ?: settings.lastLatitude ?: 52.0
        val lon = settings.manualLongitude ?: settings.lastLongitude ?: 20.0
        GeoPoint(lat, lon)
    }

    LaunchedEffect(initialLocation) {
        mapView.controller.setCenter(initialLocation)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_picker_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            mapViewModel.confirmLocation(radioViewModel, onBack)
                        },
                        enabled = state.selectedLocation != null && !state.isGeocoding,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = { 
                    mapView.apply {
                        val marker = Marker(this)
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.infoWindow = null
                        
                        val pinDrawable = ContextCompat.getDrawable(context, R.drawable.ic_location_pin)?.apply {
                            setTint(pinColor)
                        }
                        marker.icon = pinDrawable
                        
                        state.selectedLocation?.let {
                            marker.position = it
                            overlays.add(marker)
                        }

                        val eventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                marker.position = p
                                if (!overlays.contains(marker)) {
                                    overlays.add(marker)
                                }
                                invalidate()
                                mapViewModel.onLocationSelected(p)
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint): Boolean {
                                return false
                            }
                        }
                        
                        overlays.add(MapEventsOverlay(eventsReceiver))
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            if (state.isGeocoding) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                state.city?.let { city ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = city,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                state.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
