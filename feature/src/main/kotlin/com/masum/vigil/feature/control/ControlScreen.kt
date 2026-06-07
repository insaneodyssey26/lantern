package com.masum.vigil.feature.control

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.vigil.core.ui.components.AnimatedCounter
import com.masum.vigil.core.ui.components.MonitoringToggle
import com.masum.vigil.domain.model.MonitoringState
import com.masum.vigil.feature.util.formatDuration
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(viewModel: ControlViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isActive = uiState.monitoringState is MonitoringState.Active

    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.onConsentGranted()
        else viewModel.onConsentDismissed()
    }

    LaunchedEffect(uiState.pendingConsentIntent) {
        uiState.pendingConsentIntent?.let { consentLauncher.launch(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Vigil",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.isPrivateDnsActive) {
                Spacer(Modifier.height(4.dp))
                PrivateDnsBanner()
            }

            Spacer(Modifier.weight(1f))

            Crossfade(targetState = isActive, label = "monitor_state") { active ->
                if (active) {
                    ActiveContent(uiState)
                } else {
                    IdleContent()
                }
            }

            Spacer(Modifier.height(36.dp))

            MonitoringToggle(isActive = isActive, onClick = viewModel::onToggleClick)

            if (uiState.monitoringState is MonitoringState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = (uiState.monitoringState as MonitoringState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.weight(1f))

            if (!isActive && uiState.blocklistCount > 0) {
                BlocklistBadge(uiState.blocklistCount)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun IdleContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        ShieldIcon(modifier = Modifier.size(84.dp))
        Spacer(Modifier.height(20.dp))
        Text(
            "Privacy at the network layer",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Every DNS query your apps make, classified in real time — without leaving your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(36.dp))
        Text(
            "Tap start to begin",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActiveContent(uiState: ControlUiState) {
    val startedAt = (uiState.monitoringState as? MonitoringState.Active)?.startedAt
    var elapsed by remember { mutableStateOf("") }

    LaunchedEffect(startedAt) {
        if (startedAt != null) {
            while (true) {
                elapsed = formatDuration(startedAt, System.currentTimeMillis())
                delay(1000)
            }
        }
    }

    val trackerFraction = if (uiState.totalQueries > 0)
        uiState.trackerQueries.toFloat() / uiState.totalQueries else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = trackerFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tracker_fraction",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (elapsed.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = "Active · $elapsed",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(Modifier.height(32.dp))
        }

        PulsingCounter(count = uiState.totalQueries)

        Spacer(Modifier.height(32.dp))

        TrackerRatioBar(
            fraction = animatedFraction,
            trackerCount = uiState.trackerQueries,
            totalCount = uiState.totalQueries,
        )
    }
}

@Composable
private fun PulsingCounter(count: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "p1",
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = LinearEasing),
            RepeatMode.Restart,
            StartOffset(1000),
        ),
        label = "p2",
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(220.dp)) {
            listOf(pulse1, pulse2).forEach { frac ->
                drawCircle(
                    color = primaryColor,
                    radius = size.minDimension / 2f * frac,
                    alpha = (1f - frac) * 0.18f,
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedCounter(
                count = count,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "queries captured",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TrackerRatioBar(fraction: Float, trackerCount: Int, totalCount: Int) {
    val errorColor = MaterialTheme.colorScheme.error
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Tracker ratio",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (fraction > 0f) errorColor else primaryColor,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(if (fraction > 0f) errorColor else primaryColor),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "$trackerCount trackers · $totalCount total",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ShieldIcon(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val shield = Path().apply {
            moveTo(w * 0.5f, h * 0.03f)
            lineTo(w * 0.93f, h * 0.24f)
            lineTo(w * 0.93f, h * 0.57f)
            cubicTo(w * 0.93f, h * 0.80f, w * 0.5f, h * 0.97f, w * 0.5f, h * 0.97f)
            cubicTo(w * 0.5f, h * 0.97f, w * 0.07f, h * 0.80f, w * 0.07f, h * 0.57f)
            lineTo(w * 0.07f, h * 0.24f)
            close()
        }
        drawPath(shield, primary)
        val check = Path().apply {
            moveTo(w * 0.30f, h * 0.53f)
            lineTo(w * 0.44f, h * 0.67f)
            lineTo(w * 0.70f, h * 0.38f)
        }
        drawPath(check, onPrimary, style = Stroke(width = w * 0.08f))
    }
}

@Composable
private fun BlocklistBadge(count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = "${count.formatK()} domains in blocklist",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrivateDnsBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Private DNS active", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Encrypted DNS (DoT/DoH) is configured. Vigil captures Do53 queries only — some traffic may not appear.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun Int.formatK(): String = if (this >= 1_000) "${this / 1_000}k" else toString()
