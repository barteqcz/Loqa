package com.barteqcz.onqa.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.barteqcz.onqa.R
import com.barteqcz.onqa.data.model.RadioStation
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StationTile(
    station: RadioStation,
    isActive: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tileScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (station.isFavorite) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )

    val activeOverlayColor by animateColorAsState(
        targetValue = when {
            isActive && station.isFavorite -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "activeOverlayColor"
    )

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            borderColor
        ),
        tonalElevation = 1.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(activeOverlayColor)
        ) {
            val isSmall = maxWidth < 170.dp
            val isUltraSmall = maxWidth < 130.dp
            val outerPadding = if (isSmall) 4.dp else 12.dp
            val bottomPadding = if (isSmall) 10.dp else 16.dp
            val logoSize = if (isUltraSmall) 36.dp else if (isSmall) 48.dp else 64.dp
            val logoGap = if (isSmall) 4.dp else 12.dp
            val topPadding = if (isSmall) 10.dp else 12.dp
            val nameStyle = if (isUltraSmall) MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp) 
                           else if (isSmall) MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp) 
                           else MaterialTheme.typography.titleMedium
            val infoFontSize = if (isUltraSmall) 7.sp else if (isSmall) 9.sp else 12.sp
            val infoLineHeight = if (isSmall) 11.sp else 16.sp

            Box(modifier = Modifier.fillMaxSize().padding(top = topPadding, start = outerPadding, end = outerPadding, bottom = bottomPadding)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        var isImageLoaded by remember(station.logo) { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .size(logoSize)
                                .clip(RoundedCornerShape(if (isSmall) 8.dp else 12.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isImageLoaded) {
                                Icon(
                                    imageVector = Icons.Rounded.Radio,
                                    contentDescription = null,
                                    modifier = Modifier.size(if (isUltraSmall) 20.dp else if (isSmall) 28.dp else 40.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(station.logo)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                onSuccess = { isImageLoaded = true },
                                onError = { isImageLoaded = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(logoGap))

                        Text(
                            text = station.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = nameStyle,
                            fontWeight = FontWeight.Bold,
                            maxLines = if (isSmall) 1 else 2,
                            textAlign = TextAlign.Center,
                            modifier = if (isSmall) {
                                Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE)
                            } else {
                                Modifier.fillMaxWidth()
                            },
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if ((station.transmitterName != null) || (station.distance != null)) {
                        val infoText = buildString {
                            station.transmitterName?.let { append(it) }
                            station.distance?.let {
                                val dist = it.roundToInt()
                                if (isNotEmpty()) append(" ")
                                if (dist == 0) {
                                    append(stringResource(R.string.less_than_one_km_with_dot))
                                } else {
                                    append(stringResource(R.string.distance_with_dot, dist))
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(if (isSmall) 2.dp else 4.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        ) {
                            Text(
                                text = infoText,
                                color = if (station.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = infoFontSize, lineHeight = infoLineHeight),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = if (isUltraSmall) 0.dp else 1.dp)
                            )
                        }
                    }
                }

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(if (isSmall) (-2).dp else 4.dp)
                            .size(if (isSmall) 12.dp else 16.dp)
                    ) {
                        EqualizerAnimation(
                            color = if (station.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (station.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(if (isSmall) (-2).dp else 4.dp)
                    ) {
                        FavoriteHeart(visible = true, modifier = Modifier.size(if (isSmall) 12.dp else 16.dp))
                    }
                }
            }
        }
    }
}
