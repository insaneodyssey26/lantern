package com.masum.vigil.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.vigil.core.ui.components.StatCard
import com.masum.vigil.core.ui.components.VerdictChip
import com.masum.vigil.core.ui.components.VerdictUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stats") }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        label = "Total queries",
                        value = uiState.totalQueries.toString(),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    StatCard(
                        label = "Trackers",
                        value = "${uiState.trackerPercentage}%",
                        supportingText = "${uiState.trackerQueries} domains",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }

            if (uiState.topDomains.isNotEmpty()) {
                item {
                    Text(
                        text = "Top domains",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(uiState.topDomains) { domain ->
                    DomainRow(domain)
                    HorizontalDivider()
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Blocklist: ${uiState.blocklistCount.formatCount()} domains",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DomainRow(domain: DomainStatUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = domain.domain,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${domain.count} queries",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (domain.isTracker) {
            VerdictChip(verdict = VerdictUiState.Tracker)
        }
    }
}

private fun Int.formatCount(): String = when {
    this >= 1_000 -> "${this / 1_000}k"
    else -> toString()
}
