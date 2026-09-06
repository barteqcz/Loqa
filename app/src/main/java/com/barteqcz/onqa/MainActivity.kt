package com.barteqcz.onqa

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barteqcz.onqa.ui.navigation.MapPickerRoute
import com.barteqcz.onqa.ui.navigation.RadioRoute
import com.barteqcz.onqa.ui.navigation.SettingsRoute
import com.barteqcz.onqa.ui.components.MiniPlayer
import com.barteqcz.onqa.ui.onboarding.BackgroundLocationDisclosure
import com.barteqcz.onqa.ui.onboarding.DataDisclaimer
import com.barteqcz.onqa.ui.onboarding.OnboardingScreen
import com.barteqcz.onqa.ui.main.RadioScreen
import com.barteqcz.onqa.ui.main.RadioUiState
import com.barteqcz.onqa.ui.main.RadioViewModel
import com.barteqcz.onqa.ui.settings.MapPickerScreen
import com.barteqcz.onqa.ui.settings.MapViewModel
import com.barteqcz.onqa.ui.settings.SettingsScreen
import com.barteqcz.onqa.ui.theme.OnqaTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: RadioViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val locationGranted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)

        if (locationGranted && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
            showBackgroundLocationDisclosure = true
        } else {
            checkPermissionsAndCompleteOnboarding()
        }
    }

    private var showBackgroundLocationDisclosure by mutableStateOf(value = false)
    private var showDataDisclaimer by mutableStateOf(value = false)

    private val requestBackgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        checkPermissionsAndCompleteOnboarding()
    }

    private fun checkPermissionsAndCompleteOnboarding() {
        if (hasAllPermissions()) {
            showDataDisclaimer = true
        }
    }

    private fun hasAllPermissions(): Boolean {
        val location = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        
        val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true

        return location && background
    }

    private fun launchPermissionRequest() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncLanguage()
        if (!hasAllPermissions()) {
            viewModel.resetOnboarding()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        splashScreen.setKeepOnScreenCondition {
            viewModel.settings.value.isInitialValue
        }

        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        setContent {
            val viewState by viewModel.viewState.collectAsStateWithLifecycle()

            OnqaTheme(
                themeMode = viewState.settings.themeMode,
                dynamicColor = if (!viewState.settings.isOnboardingCompleted) false else viewState.settings.isMaterialYouEnabled,
                accentColor = viewState.settings.accentColor,
                isAmoledMode = viewState.settings.isAmoledModeEnabled,
            ) {
                LaunchedEffect(viewState.settings.isOnboardingCompleted) {
                    if (!viewState.settings.isOnboardingCompleted && hasAllPermissions()) {
                        showDataDisclaimer = true
                    }
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    
                    if (showBackgroundLocationDisclosure) {
                        BackgroundLocationDisclosure(
                            onConfirm = {
                                showBackgroundLocationDisclosure = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                }
                            },
                            onDismiss = {
                                showBackgroundLocationDisclosure = false
                                checkPermissionsAndCompleteOnboarding()
                            }
                        )
                    }

                    val isInitial = viewState.settings.isInitialValue
                    val isOnboardingCompleted = viewState.settings.isOnboardingCompleted
                    
                    if (isInitial && !isOnboardingCompleted) {
                        Box(modifier = Modifier.fillMaxSize())
                    } else if (!isOnboardingCompleted) {
                        if (showDataDisclaimer) {
                            DataDisclaimer(
                                onConfirm = {
                                    viewModel.completeOnboarding()
                                }
                            )
                        } else {
                            OnboardingScreen(onGrantClick = { launchPermissionRequest() })
                        }
                    } else {
                        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        val isMapPickerVisible = currentDestination?.hasRoute<MapPickerRoute>() == true

                        Box(modifier = Modifier.fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = RadioRoute,
                            ) {
                                composable<RadioRoute>(
                                    exitTransition = {
                                        if (targetState.destination.hasRoute<SettingsRoute>()) {
                                            slideOutHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeOut(tween(300))
                                        } else {
                                            slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeOut(tween(300))
                                        }
                                    },
                                    popEnterTransition = {
                                        if (initialState.destination.hasRoute<SettingsRoute>()) {
                                            slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeIn(tween(300))
                                        } else {
                                            slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeIn(tween(300))
                                        }
                                    }
                                ) {
                                    RadioScreen(
                                        viewModel = viewModel,
                                        onSettingsClick = { 
                                            if (navController.currentDestination?.hasRoute<RadioRoute>() == true) {
                                                navController.navigate(SettingsRoute)
                                            }
                                        }
                                    )
                                }
                                composable<SettingsRoute>(
                                    enterTransition = {
                                        slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(tween(300)) + scaleIn(initialScale = 0.92f)
                                    },
                                    exitTransition = {
                                        fadeOut(tween(300)) + scaleOut(targetScale = 0.95f)
                                    },
                                    popEnterTransition = {
                                        fadeIn(tween(300)) + scaleIn(initialScale = 0.95f)
                                    },
                                    popExitTransition = {
                                        slideOutHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut(tween(300)) + scaleOut(targetScale = 0.92f)
                                    }
                                ) {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onBack = { 
                                            if (navController.currentDestination?.hasRoute<SettingsRoute>() == true) {
                                                navController.popBackStack(RadioRoute, inclusive = false)
                                            }
                                        },
                                        onNavigateToMapPicker = {
                                            if (navController.currentDestination?.hasRoute<SettingsRoute>() == true) {
                                                navController.navigate(MapPickerRoute)
                                            }
                                        }
                                    )
                                }
                                composable<MapPickerRoute>(
                                    enterTransition = {
                                        slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(tween(300)) + scaleIn(initialScale = 0.92f)
                                    },
                                    exitTransition = {
                                        fadeOut(tween(300)) + scaleOut(targetScale = 0.95f)
                                    },
                                    popEnterTransition = {
                                        fadeIn(tween(300)) + scaleIn(initialScale = 0.95f)
                                    },
                                    popExitTransition = {
                                        slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut(tween(300)) + scaleOut(targetScale = 0.92f)
                                    }
                                ) {
                                    val mapViewModel: MapViewModel = hiltViewModel()
                                    MapPickerScreen(
                                        radioViewModel = viewModel,
                                        mapViewModel = mapViewModel,
                                        onBack = { 
                                            if (navController.currentDestination?.hasRoute<MapPickerRoute>() == true) {
                                                navController.popBackStack()
                                            }
                                        }
                                    )
                                }
                            }

                            val uiState = viewState.uiState
                            val stations = (uiState as? RadioUiState.Success)?.stations ?: emptyList()

                            AnimatedVisibility(
                                visible = viewState.isMiniPlayerActive && !isMapPickerVisible,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut(),
                                modifier = Modifier.align(Alignment.BottomCenter),
                            ) {
                                viewState.displayStation?.let {
                                    MiniPlayer(
                                        station = it,
                                        stations = stations,
                                        isPlaying = viewState.isPlaying,
                                        isBuffering = viewState.isBuffering,
                                        metadata = viewState.metadata,
                                        showHqIcon = viewState.settings.useHqStream && !it.streamUrlHq.isNullOrBlank(),
                                        isScrollable = viewState.isScrollable,
                                        onToggle = { 
                                            focusManager.clearFocus()
                                            viewModel.toggleStation(viewState.selectedUrl!!) 
                                        },
                                        onNext = { 
                                            focusManager.clearFocus()
                                            viewModel.nextStation() 
                                        },
                                        onPrevious = { 
                                            focusManager.clearFocus()
                                            viewModel.previousStation() 
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
