package com.masum.vigil.feature.control

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.vigil.core.ui.components.AnimatedCounter
import com.masum.vigil.core.ui.components.MonitoringToggle
import com.masum.vigil.core.ui.components.StatCard
import com.masum.vigil.domain.model.MonitoringState
import com.masum.vigil.feature.util.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(viewModel: ControlViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        topBar = { TopAppBar(title = { Text("Vigil") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (uiState.isPrivateDnsActive) {
                PrivateDnsBanner()
            }

            Spacer(Modifier.weight(1f))

            MonitoringStatusText(uiState.monitoringState)

            Spacer(Modifier.height(8.dp))

            MonitoringToggle(
                isActive = uiState.monitoringState is MonitoringState.Active,
                onClick = viewModel::onToggleClick,
            )

            Spacer(Modifier.height(16.dp))

            if (uiState.monitoringState is MonitoringState.Active) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        label = "Queries",
                        value = uiState.totalQueries.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Trackers",
                        value = uiState.trackerQueries.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (uiState.monitoringState is MonitoringState.Error) {
                Text(
                    text = (uiState.monitoringState as MonitoringState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun MonitoringStatusText(state: MonitoringState) {
    val text = when (state) {
        MonitoringState.Idle -> "Tap to start monitoring"
        is MonitoringState.Active -> "Active since ${state.startedAt.formatTime()}"
        is MonitoringState.Error -> "Error — tap to retry"
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
