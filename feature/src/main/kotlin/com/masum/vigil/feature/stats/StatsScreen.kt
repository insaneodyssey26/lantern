package com.masum.vigil.feature.stats

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stats") }) },
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

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill(
                    label = "Total",
                    value = uiState.totalQueries.formatCount(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                StatPill(
                    label = "Trackers",
                    value = uiState.trackerQueries.formatCount(),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
                StatPill(
                    label = "Clean",
                    value = (uiState.totalQueries - uiState.trackerQueries).formatCount(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            if (uiState.topDomains.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Top domains", style = MaterialTheme.typography.titleSmall)
                }

                Spacer(Modifier.height(16.dp))

                val maxCount = uiState.topDomains.maxOf { it.count }
                uiState.topDomains.forEachIndexed { i, domain ->
                    DomainBar(domain = domain, maxCount = maxCount)
                    if (i < uiState.topDomains.lastIndex) Spacer(Modifier.height(14.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Blocklist · ${uiState.blocklistCount.formatCount()} domains",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
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
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 30.dp.toPx()
            val r = (size.minDimension - strokeWidth) / 2f
            val tl = Offset((size.width - r * 2) / 2f, (size.height - r * 2) / 2f)
            val arcSize = Size(r * 2, r * 2)
            val strokeStyle = Stroke(strokeWidth, cap = StrokeCap.Butt)

            drawArc(surfaceVariant, 0f, 360f, false, tl, arcSize, style = strokeStyle)

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
                style = MaterialTheme.typography.displaySmall,
                color = if (trackerPercentage > 0) error else primary,
            )
            Text(
                text = if (trackerPercentage > 0) "trackers" else "all clean",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(shape = RoundedCornerShape(12.dp), color = containerColor) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = contentColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor)
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
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = domain.count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(barBg),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor),
            )
        }
    }
}

private fun Int.formatCount(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}k"
    else -> toString()
}
