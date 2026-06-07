package com.masum.vigil.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun MonitoringToggle(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.errorContainer
                      else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "toggle_container",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onErrorContainer
                      else MaterialTheme.colorScheme.onPrimaryContainer,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "toggle_content",
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.06f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "toggle_scale",
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(148.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ToggleIcon(
                isActive = isActive,
                color = contentColor,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (isActive) "Stop" else "Start",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ToggleIcon(isActive: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (isActive) {
            val inset = size.width * 0.2f
            drawRect(
                color = color,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
            )
        } else {
            val path = Path().apply {
                moveTo(size.width * 0.15f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(size.width * 0.15f, size.height)
                close()
            }
            drawPath(path = path, color = color)
        }
    }
}
