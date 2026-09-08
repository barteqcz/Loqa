package com.barteqcz.onqa.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

object AnimationSystem {
    object Duration {
        const val MEDIUM = 300
    }

    // Natural FastOutSlowInEasing for consistent UI transitions
    fun <T> vividTween(
        duration: Int = Duration.MEDIUM,
        delay: Int = 0,
        easing: Easing = FastOutSlowInEasing,
    ): TweenSpec<T> = tween(
        durationMillis = duration,
        delayMillis = delay,
        easing = easing
    )

    val VividSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val VividSpringIntOffset: SpringSpec<IntOffset> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val VividSpringIntSize: SpringSpec<IntSize> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
