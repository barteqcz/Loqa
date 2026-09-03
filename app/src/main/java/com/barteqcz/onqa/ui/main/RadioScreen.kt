package com.barteqcz.onqa.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barteqcz.onqa.R
import com.barteqcz.onqa.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    viewModel: RadioViewModel,
    onSettingsClick: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    var isSearchFocused by remember { mutableStateOf(value = false) }

    BackHandler(viewState.isSearchActive) {
        if (isSearchFocused) {
            focusManager.clearFocus()
        } else {
            viewModel.setSearchActive(false)
        }
    }

    LaunchedEffect(viewState.isSearchActive) {
        if (viewState.isSearchActive) {
            searchFocusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = viewState.isSearchActive,
                    ) { 
                        focusManager.clearFocus()
                    }
                    .padding(bottom = if (viewState.settings.showLocationHeader) 8.dp else 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedContent(
                        targetState = viewState.isSearchActive,
                        transitionSpec = {
                            if (targetState) {
                                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                            } else {
                                (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = "TopBarSearchTransition",
                    ) { isSearchActive ->
                        if (isSearchActive) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.setSearchActive(false) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = stringResource(R.string.back),
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                TextField(
                                    value = viewState.searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester)
                                        .onFocusChanged { isSearchFocused = it.isFocused },
                                    placeholder = {
                                        Text(
                                            stringResource(R.string.search_stations_hint),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                    trailingIcon = {
                                        if (viewState.searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { viewModel.setSearchQuery("") },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Close,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onBackground,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            Text(
                                stringResource(R.string.app_name),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!viewState.isSearchActive) {
                            IconButton(
                                onClick = { viewModel.setSearchActive(true) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        } else {
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        IconButton(
                            onClick = {
                                if (!viewState.isSearchActive) {
                                    onSettingsClick()
                                }
                            },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = stringResource(R.string.settings_title),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
                
                if (viewState.settings.showLocationHeader) {
                    LocationHeader(viewState.locationInfo)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = viewState.isSearchActive
                ) {
                    focusManager.clearFocus()
                }
        ) {
            AnimatedContent(
                targetState = viewState.uiState,
                transitionSpec = {
                    fadeIn(tween(500)).togetherWith(fadeOut(tween(500)))
                },
                contentKey = { if (!viewState.isNetworkAvailable) "no_internet" else it::class },
                label = "uiStateTransition",
                modifier = Modifier.fillMaxSize()
            ) { state ->
                if (!viewState.isNetworkAvailable) {
                    StatusContainer(
                        message = stringResource(R.string.error_no_internet),
                        isError = true,
                        modifier = Modifier.padding(paddingValues),
                        onRetry = { viewModel.refresh() }
                    )
                } else {
                    when (state) {
                        is RadioUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        is RadioUiState.Success -> {
                            if (state.stations.isEmpty()) {
                                StatusContainer(
                                    message = stringResource(R.string.no_stations_message),
                                    modifier = Modifier.padding(paddingValues)
                                )
                            } else {
                                val listState = rememberLazyListState()
                                val density = LocalDensity.current

                                LaunchedEffect(Unit) {
                                    viewModel.events.collect { event ->
                                        when (event) {
                                            is RadioUiEvent.ScrollToTop -> {
                                                // Auto-scroll disabled per user request
                                            }
                                        }
                                    }
                                }

                                var wasMiniPlayerVisible by remember { mutableStateOf(viewState.selectedUrl != null) }
                                LaunchedEffect(viewState.selectedUrl) {
                                    val selectedUrl = viewState.selectedUrl
                                    if (selectedUrl != null) {
                                        val layoutInfo = listState.layoutInfo
                                        val isLastItemVisible = layoutInfo.visibleItemsInfo.any { it.index == (layoutInfo.totalItemsCount - 1) }

                                        if (isLastItemVisible && !wasMiniPlayerVisible) {
                                            val scrollAmount = with(density) { 100.dp.toPx() }
                                            listState.animateScrollBy(scrollAmount)
                                        }
                                    }
                                    wasMiniPlayerVisible = selectedUrl != null
                                }

                                val bottomNavPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                                val showShadow by remember {
                                    derivedStateOf {
                                        viewState.selectedUrl != null
                                    }
                                }

                                LaunchedEffect(listState.canScrollForward, listState.canScrollBackward) {
                                    viewModel.setScrollable(listState.canScrollForward || listState.canScrollBackward)
                                }

                                Box(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(
                                            top = paddingValues.calculateTopPadding() + 8.dp,
                                            bottom = if (viewState.selectedUrl != null) 116.dp + bottomNavPadding else 16.dp + bottomNavPadding,
                                            start = 20.dp,
                                            end = 20.dp
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        item(key = "scroll_anchor") {
                                            Spacer(modifier = Modifier.height(0.5.dp))
                                        }

                                        items(
                                            items = state.stations,
                                            key = { "${it.streamUrl ?: it.name}|${it.network}" }
                                        ) { station ->
                                            StationCard(
                                                station = station,
                                                isActive = station.matchesUrl(viewState.selectedUrl),
                                                isPlaying = station.matchesUrl(viewState.selectedUrl) && viewState.isPlaying && !viewState.isBuffering,
                                                showHqIcon = !station.streamUrlHq.isNullOrBlank(),
                                                modifier = Modifier.animateItem(
                                                    fadeInSpec = tween(durationMillis = 300),
                                                    fadeOutSpec = tween(durationMillis = 300),
                                                    placementSpec = if (viewState.isSearchActive) null else spring()
                                                ),
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    val url = station.streamUrl ?: station.streamUrlHq
                                                    url?.let { viewModel.toggleStation(it) }
                                                },
                                                onLongClick = { viewModel.toggleFavorite(station) }
                                            )
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = showShadow,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    ) {
                                        val bgColor = MaterialTheme.colorScheme.background
                                        val shadowBrush = remember(bgColor) {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    bgColor.copy(alpha = 0.4f),
                                                    bgColor.copy(alpha = 0.8f),
                                                    bgColor
                                                )
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .background(shadowBrush)
                                        )
                                    }
                                }
                            }
                        }
                        is RadioUiState.Error -> {
                            if (state.isServerError) {
                                ServerNapContainer(
                                    modifier = Modifier.padding(paddingValues),
                                    onRetry = { viewModel.refresh() }
                                )
                            } else {
                                StatusContainer(
                                    message = stringResource(R.string.error_no_internet),
                                    isError = true,
                                    modifier = Modifier.padding(paddingValues),
                                    onRetry = { viewModel.refresh() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

