package com.masum.lantern.core.ui.navigation

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val StandardDecelerate = CubicBezierEasing(0f, 0f, 0f, 1f)

@Composable
fun PredictiveBackContainer(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val progressAnim = remember { Animatable(0f) }
    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    PredictiveBackHandler(enabled = enabled) { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                swipeEdge = backEvent.swipeEdge
                progressAnim.snapTo(backEvent.progress)
            }
            coroutineScope.launch {
                progressAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            onBack()
        } catch (e: CancellationException) {
            coroutineScope.launch {
                progressAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    val currentProgress = progressAnim.value
    val easedProgress = StandardDecelerate.transform(currentProgress)

    val scale = 1f - (easedProgress * 0.10f)
    val cornerRadius = (easedProgress * 28f).dp
    val elevation = (easedProgress * 10f).dp

    val maxTranslationX = 40f
    val translationX = if (swipeEdge == BackEventCompat.EDGE_LEFT) {
        easedProgress * maxTranslationX
    } else {
        -easedProgress * maxTranslationX
    }

    val originX = if (swipeEdge == BackEventCompat.EDGE_LEFT) 0f else 1f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .graphicsLayer {
                transformOrigin = TransformOrigin(originX, 0.5f)
                scaleX = scale
                scaleY = scale
                this.translationX = translationX
                shape = RoundedCornerShape(cornerRadius)
                clip = cornerRadius > 0.dp
                shadowElevation = elevation.toPx()
            }
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        content()
    }
}
