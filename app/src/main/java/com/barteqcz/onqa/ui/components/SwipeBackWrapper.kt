package com.barteqcz.onqa.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.barteqcz.onqa.ui.theme.AnimationSystem

@Composable
fun SwipeBackWrapper(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isDragging) offsetX else 0f,
        animationSpec = AnimationSystem.vividTween(),
        label = "swipeBackOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        // Tylko jeśli zaczynamy blisko lewej krawędzi (np. 40dp)
                        if (offset.x < 40.dp.toPx()) {
                            isDragging = true
                        }
                    },
                    onDragEnd = {
                        if (isDragging) {
                            // Jeśli przeciągnięto więcej niż 30% ekranu, wywołaj onBack
                            if (offsetX > size.width * 0.3f) {
                                onBack()
                            }
                            offsetX = 0f
                            isDragging = false
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        offsetX = 0f
                    }
                ) { change, dragAmount ->
                    if (isDragging) {
                        change.consume()
                        // Blokujemy ruch w lewo (powyżej 0)
                        offsetX = (offsetX + dragAmount).coerceAtLeast(0f)
                    }
                }
            }
            .graphicsLayer {
                translationX = animatedOffsetX
            }
    ) {
        content()
    }
}
