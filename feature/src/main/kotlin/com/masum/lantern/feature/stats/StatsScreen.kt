package com.masum.lantern.feature.stats

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onSettingsClick: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Analytics", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            TrackerDonutChart(
                trackerPercentage = uiState.trackerPercentage,
                modifier = Modifier.size(220.dp),
            )

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Total Logs",
                    value = uiState.totalQueries.formatCount(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Trackers",
                    value = uiState.trackerQueries.formatCount(),
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.error,
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Safe Logs",
                    value = (uiState.totalQueries - uiState.trackerQueries).formatCount(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.topDomains.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Top Domains Requested",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val maxCount = uiState.topDomains.maxOf { it.count }
                        uiState.topDomains.forEach { domain ->
                            DomainBar(domain = domain, maxCount = maxCount)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Blocklist Engine · ${uiState.blocklistCount.formatCount()} active domains",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TrackerDonutChart(trackerPercentage: Int, modifier: Modifier = Modifier) {
    val animatedPct by animateFloatAsState(
        targetValue = trackerPercentage.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "donut_pct",
    )
    val trackerFraction = (animatedPct / 100f).coerceIn(0f, 1f)
    val cleanFraction = 1f - trackerFraction

    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            val r = (size.minDimension - strokeWidth * 2) / 2f
            val tl = Offset((size.width - r * 2) / 2f, (size.height - r * 2) / 2f)
            val arcSize = Size(r * 2, r * 2)
            val strokeStyle = Stroke(strokeWidth, cap = StrokeCap.Round)

            drawArc(surfaceVariant, 0f, 360f, false, tl, arcSize, style = Stroke(strokeWidth / 2f))

            if (trackerFraction > 0f) {
                drawArc(error, -90f, trackerFraction * 360f, false, tl, arcSize, style = strokeStyle)
            }
            if (cleanFraction > 0f) {
                val cleanColor = if (trackerFraction > 0f) tertiary else primary
                drawArc(cleanColor, -90f + trackerFraction * 360f, cleanFraction * 360f, false, tl, arcSize, style = strokeStyle)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$trackerPercentage%",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = if (trackerPercentage > 0) error else primary,
            )
            Text(
                text = if (trackerPercentage > 0) "trackers" else "clean",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp, topEnd = 6.dp, bottomStart = 6.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DomainBar(domain: DomainStatUiModel, maxCount: Int) {
    val fraction = if (maxCount > 0) domain.count.toFloat() / maxCount else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bar_frac",
    )
    val barColor = if (domain.isTracker) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.primary
    val barBg = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = domain.domain,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = domain.count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(barBg),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (domain.isTracker) {
                                listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                            } else {
                                listOf(Color(0xFF00E5FF), Color(0xFF00838F))
                            }
                        )
                    ),
            )
        }
    }
}

private fun Int.formatCount(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}k"
    else -> toString()
}
