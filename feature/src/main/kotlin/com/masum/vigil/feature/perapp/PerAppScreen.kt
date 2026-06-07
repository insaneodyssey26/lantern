package com.masum.vigil.feature.perapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.vigil.core.ui.components.AppRow
import com.masum.vigil.core.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerAppScreen(viewModel: PerAppViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Per App") }) },
    ) { innerPadding ->
        if (uiState.apps.isEmpty()) {
            EmptyState(
                title = "No data yet",
                subtitle = "Start monitoring to see which apps make DNS queries",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(uiState.apps, key = { it.packageName }) { app ->
                    AppRow(
                        appName = app.appName,
                        queryCount = app.totalQueries,
                        trackerCount = app.trackerQueries,
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
