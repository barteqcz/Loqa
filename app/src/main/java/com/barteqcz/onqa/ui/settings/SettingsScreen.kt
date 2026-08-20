package com.barteqcz.onqa.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barteqcz.onqa.R
import com.barteqcz.onqa.data.model.AppLanguage
import com.barteqcz.onqa.data.model.ThemeMode
import com.barteqcz.onqa.ui.main.RadioViewModel
import com.barteqcz.onqa.ui.theme.OnqaBlue
import com.barteqcz.onqa.ui.theme.OnqaCyan
import com.barteqcz.onqa.ui.theme.OnqaGreen
import com.barteqcz.onqa.ui.theme.OnqaOrange
import com.barteqcz.onqa.ui.theme.OnqaPurple
import com.barteqcz.onqa.ui.theme.applyLightVariant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: RadioViewModel,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val settings = viewState.settings
    val selectedUrl = viewState.selectedUrl

    val focusManager = LocalFocusManager.current

    val isLightMode = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    val scrollState = rememberScrollState()

    val showShadow by remember {
        derivedStateOf {
            selectedUrl != null
        }
    }

    LaunchedEffect(scrollState.canScrollForward, scrollState.canScrollBackward) {
        viewModel.setScrollable(scrollState.canScrollForward || scrollState.canScrollBackward)
    }

    val accentColors = listOf(
        OnqaGreen,
        OnqaPurple,
        OnqaCyan,
        OnqaOrange,
        OnqaBlue,
    )

    val displayAccentColors = accentColors.map { color ->
        val target = if (isLightMode) color.applyLightVariant() else color
        animateColorAsState(target, tween(500), label = "paletteColor").value
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) {
            focusManager.clearFocus()
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding(),
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = padding.calculateTopPadding() + 16.dp,
                        bottom = if (selectedUrl != null) 120.dp + padding.calculateBottomPadding() else 16.dp + padding.calculateBottomPadding(),
                        start = 24.dp,
                        end = 24.dp
                    )
                    .animateContentSize(),
            ) {
                SettingCategory(title = stringResource(R.string.category_general))

                LanguageSettings(
                    currentLanguage = settings.language
                ) { viewModel.updateLanguage(it) }

                Spacer(modifier = Modifier.height(48.dp))

                SettingCategory(title = stringResource(R.string.category_ui_elements))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.show_location_header_title), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.show_location_header_desc), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = settings.showLocationHeader,
                        onCheckedChange = { viewModel.updateShowLocationHeader(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                SettingCategory(title = stringResource(R.string.category_audio))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.hq_stream_title), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.hq_stream_desc), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = settings.useHqStream,
                        onCheckedChange = { viewModel.updateUseHqStream(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
                
                SettingCategory(title = stringResource(R.string.category_appearance))

                Text(stringResource(R.string.theme_mode_title), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.theme_mode_desc), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(16.dp))

                ThemeSwitcher(
                    currentMode = settings.themeMode
                ) { viewModel.updateThemeMode(it) }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.amoled_mode_title), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.amoled_mode_desc), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = settings.isAmoledModeEnabled,
                        onCheckedChange = { viewModel.updateAmoledMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.material_you_title), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.material_you_desc), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = settings.isMaterialYouEnabled,
                            onCheckedChange = { viewModel.updateMaterialYou(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    }

                    AnimatedVisibility(
                        visible = !settings.isMaterialYouEnabled,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(animationSpec = tween(300))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                displayAccentColors.forEachIndexed { index, color ->
                                    val baseColor = accentColors[index]
                                    val isSelected = (!settings.isMaterialYouEnabled) && 
                                                     (settings.accentColor.toArgb() == baseColor.toArgb())
                                    
                                    val scale by animateFloatAsState(if (isSelected) 1.15f else 1f, label = "scale")
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .graphicsLayer { 
                                                scaleX = scale
                                                scaleY = scale 
                                            }
                                            .clip(CircleShape)
                                            .background(color)
                                            .clickable { viewModel.updateAccentColor(baseColor) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Rounded.Check,
                                                contentDescription = null, 
                                                tint = if (isLightMode) Color.White else Color.Black.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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

@Composable
private fun LanguageSettings(
    currentLanguage: AppLanguage,
    onLanguageSelect: (AppLanguage) -> Unit
) {
    val isSystem = currentLanguage == AppLanguage.SYSTEM
    val focusManager = LocalFocusManager.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.language_system),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.language_system_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = isSystem,
                onCheckedChange = {
                    focusManager.clearFocus()
                    if (it) onLanguageSelect(AppLanguage.SYSTEM)
                    else onLanguageSelect(AppLanguage.ENGLISH)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            )
        }

        AnimatedVisibility(
            visible = !isSystem,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                LanguageDropdown(
                    currentLanguage = currentLanguage,
                    onLanguageSelect = {
                        focusManager.clearFocus()
                        onLanguageSelect(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageDropdown(
    currentLanguage: AppLanguage,
    onLanguageSelect: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(value = false) }
    val focusManager = LocalFocusManager.current
    
    val label = stringResource(currentLanguage.labelRes)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Surface(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Rounded.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Chevron icon
                val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "rotation")
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { 
                expanded = false 
                focusManager.clearFocus()
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AppLanguage.entries.filter { it != AppLanguage.SYSTEM }.forEach { language ->
                val itemLabel = stringResource(language.labelRes)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (currentLanguage == language) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentLanguage == language) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onLanguageSelect(language)
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeSwitcher(
    currentMode: ThemeMode,
    onModeSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeMode.entries.forEach { mode ->
            ThemeOption(
                mode = mode,
                isSelected = currentMode == mode,
                onClick = { onModeSelect(mode) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (mode) {
        ThemeMode.SYSTEM -> Icons.Rounded.Contrast
        ThemeMode.LIGHT -> Icons.Rounded.LightMode
        ThemeMode.DARK -> Icons.Rounded.DarkMode
    }
    val label = when (mode) {
        ThemeMode.SYSTEM -> stringResource(R.string.theme_mode_system)
        ThemeMode.LIGHT -> stringResource(R.string.theme_mode_light)
        ThemeMode.DARK -> stringResource(R.string.theme_mode_dark)
    }

    SelectableOption(
        icon = icon,
        label = label,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun SelectableOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightMode = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val activeColor = MaterialTheme.colorScheme.primary
    
    val containerColor = if (isSelected) {
        activeColor.copy(alpha = if (isLightMode) 0.12f else 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    
    val contentColor = if (isSelected) {
        activeColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) activeColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SettingCategory(title: String) {
    Text(
        text = title.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}
