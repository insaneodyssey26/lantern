package com.masum.lantern.core.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun AppRow(
    appName: String,
    queryCount: Int,
    trackerCount: Int,
    modifier: Modifier = Modifier,
    packageName: String? = null,
    icon: Painter? = null,
    onClick: (() -> Unit)? = null,
) {
    val trackerFraction = if (queryCount > 0) trackerCount.toFloat() / queryCount else 0f
    val context = LocalContext.current
    val resolvedIcon = remember(packageName, icon) {
        if (icon != null) {
            icon
        } else if (packageName != null) {
            try {
                val drawable = context.packageManager.getApplicationIcon(packageName)
                DrawablePainter(drawable)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .let {
                if (onClick != null) {
                    it.clickable(onClick = onClick)
                } else {
                    it
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(appName = appName, icon = resolvedIcon)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$queryCount total · $trackerCount trackers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (queryCount > 0) {
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(trackerFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (trackerFraction > 0) MaterialTheme.colorScheme.error 
                                    else MaterialTheme.colorScheme.primary
                                )
                        )
                    }
                }
            }
            if (trackerCount > 0) {
                VerdictChip(verdict = VerdictUiState.Tracker)
            }
        }
    }
}

@Composable
private fun AppIcon(appName: String, icon: Painter?) {
    val shape = RoundedCornerShape(12.dp)

    val avatarGradient = remember(appName) {
        val hash = abs(appName.hashCode())
        val hue1 = (hash % 360f)
        val hue2 = ((hash + 60) % 360f)
        val color1 = Color.hsv(hue1, 0.65f, 0.9f)
        val color2 = Color.hsv(hue2, 0.7f, 0.8f)
        Brush.linearGradient(colors = listOf(color1, color2))
    }

    if (icon != null) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(shape),
        )
    } else {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(shape)
                .background(avatarGradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = appName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

class DrawablePainter(private val drawable: Drawable) : Painter() {
    override val intrinsicSize: Size
        get() = Size(
            if (drawable.intrinsicWidth >= 0) drawable.intrinsicWidth.toFloat() else 0f,
            if (drawable.intrinsicHeight >= 0) drawable.intrinsicHeight.toFloat() else 0f
        )

    override fun DrawScope.onDraw() {
        drawIntoCanvas { canvas ->
            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            drawable.draw(canvas.nativeCanvas)
        }
    }
}
