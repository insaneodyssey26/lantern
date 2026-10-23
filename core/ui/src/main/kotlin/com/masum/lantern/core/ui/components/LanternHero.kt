package com.masum.lantern.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.lantern.core.ui.haptics.rememberLanternHaptics
import kotlin.math.roundToInt

@Composable
fun LanternHero(
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isBlockingEnabled: Boolean = true,
    onPresetSelected: (String) -> Unit = {}
) {
    val haptics = rememberLanternHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "heroScale"
    )

    val cardBg by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.surfaceContainerHigh
        else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "heroCardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            HeroGuardianVisualizer(
                isActive = isActive,
                isBlockingEnabled = isBlockingEnabled,
                onClick = {
                    haptics.heavy()
                    onToggle()
                }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isActive) {
                        if (isBlockingEnabled) "Active Shield Protected" else "Audit Telemetry Active"
                    } else "Protection Offline",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isActive) {
                        if (isBlockingEnabled) "Filtering ads, trackers, and telemetry on-device"
                        else "Auditing outbound DNS without blocking queries"
                    } else "Tap the power button to start protection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroGuardianVisualizer(
    isActive: Boolean,
    isBlockingEnabled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breatheTransition")

    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.25f else 0f,
        targetValue = if (isActive) 0.08f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheAlpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    val coreBgColor by animateColorAsState(
        targetValue = if (isActive) primaryColor else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "coreBgColor"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isActive) onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "iconTint"
    )

    Box(
        modifier = Modifier
            .size(164.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Canvas(modifier = Modifier.size(164.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val haloRadius = (size.minDimension / 2f) * breatheScale

                drawCircle(
                    color = primaryColor.copy(alpha = breatheAlpha),
                    radius = haloRadius,
                    center = center
                )

                drawCircle(
                    color = primaryColor.copy(alpha = 0.22f),
                    radius = size.minDimension / 2.3f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        } else {
            Canvas(modifier = Modifier.size(136.dp)) {
                drawCircle(
                    color = outlineVariant.copy(alpha = 0.5f),
                    radius = size.minDimension / 2f,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) {
                        Brush.radialGradient(
                            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.9f))
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isActive) {
                    if (isBlockingEnabled) Icons.Rounded.Shield else Icons.Rounded.Security
                } else Icons.Rounded.PowerSettingsNew,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(52.dp)
            )
        }
    }
}

@Composable
private fun MasterActionButton(
    isActive: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceContainerHighest
            else MaterialTheme.colorScheme.primary,
            contentColor = if (isActive) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isActive) "Disconnect Shield" else "Turn On Protection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProtectionModePill(
    isBlockingEnabled: Boolean,
    onModeSelected: (String) -> Unit
) {
    val selectedIndex = if (isBlockingEnabled) 0 else 1

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(4.dp)
    ) {
        val segmentWidth = maxWidth / 2f

        val indicatorOffset by animateFloatAsState(
            targetValue = if (selectedIndex == 0) 0f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "indicatorOffset"
        )

        Box(
            modifier = Modifier
                .offset { IntOffset((indicatorOffset * segmentWidth.toPx()).roundToInt(), 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeSegmentButton(
                title = "Block Trackers",
                icon = Icons.Rounded.Shield,
                isSelected = isBlockingEnabled,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected("blocking") }
            )
            ModeSegmentButton(
                title = "Audit Only",
                icon = Icons.Rounded.Security,
                isSelected = !isBlockingEnabled,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected("audit") }
            )
        }
    }
}

@Composable
private fun ModeSegmentButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "segmentColor"
    )

    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor
        )
    }
}
