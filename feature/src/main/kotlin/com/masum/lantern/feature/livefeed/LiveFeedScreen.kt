package com.masum.lantern.feature.livefeed

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.lantern.core.ui.components.EmptyState
import com.masum.lantern.core.ui.components.ExpressiveVerdict
import com.masum.lantern.core.ui.components.ExpressiveVerdictBadge
import com.masum.lantern.core.ui.components.VerdictUiState
import com.masum.lantern.core.ui.theme.VerdictSafe
import com.masum.lantern.core.ui.theme.VerdictTracker
import kotlinx.coroutines.launch

enum class FeedFilter { All, Trackers, Clean }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveFeedScreen(
    onSettingsClick: () -> Unit,
    viewModel: LiveFeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FeedFilter.All) }
    var selectedQuery by remember { mutableStateOf<DnsQueryUiModel?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val filteredQueries = remember(uiState.queries, searchQuery, selectedFilter) {
        uiState.queries.filter { query ->
            val matchesSearch = query.domain.contains(searchQuery, ignoreCase = true) ||
                    query.appName.contains(searchQuery, ignoreCase = true) ||
                    query.packageName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                FeedFilter.All -> true
                FeedFilter.Trackers -> query.verdict == VerdictUiState.Tracker
                FeedFilter.Clean -> query.verdict == VerdictUiState.Clean
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traffic Stream", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by domain or app name…") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ElevatedFilterChip(
                            selected = selectedFilter == FeedFilter.All,
                            onClick = { selectedFilter = FeedFilter.All },
                            label = { Text("All (${uiState.queries.size})") },
                            shape = CircleShape,
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    item {
                        val trackerCount = uiState.queries.count { it.verdict == VerdictUiState.Tracker }
                        ElevatedFilterChip(
                            selected = selectedFilter == FeedFilter.Trackers,
                            onClick = { selectedFilter = FeedFilter.Trackers },
                            label = { Text("Trackers ($trackerCount)") },
                            shape = CircleShape,
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(VerdictTracker)
                                )
                            },
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    item {
                        val cleanCount = uiState.queries.count { it.verdict == VerdictUiState.Clean }
                        ElevatedFilterChip(
                            selected = selectedFilter == FeedFilter.Clean,
                            onClick = { selectedFilter = FeedFilter.Clean },
                            label = { Text("Clean ($cleanCount)") },
                            shape = CircleShape,
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(VerdictSafe)
                                )
                            },
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredQueries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = if (searchQuery.isNotEmpty()) "No matching queries" else "No DNS traffic captured yet",
                        subtitle = if (searchQuery.isNotEmpty()) "Try adjusting your search query or filter" else "Traffic will stream here in real-time as device apps make network calls"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredQueries, key = { it.id }) { query ->
                        QueryCard(
                            query = query,
                            onClick = { selectedQuery = query }
                        )
                    }
                }
            }
        }
    }

    selectedQuery?.let { query ->
        ModalBottomSheet(
            onDismissRequest = { selectedQuery = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            QueryDetailSheet(
                query = query,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        selectedQuery = null
                    }
                }
            )
        }
    }
}

@Composable
private fun QueryCard(
    query: DnsQueryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = query.domain,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = query.appName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = query.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            val verdict = when {
                query.verdict == VerdictUiState.Tracker && query.isBlocked -> ExpressiveVerdict.BLOCKED
                query.verdict == VerdictUiState.Tracker && !query.isBlocked -> ExpressiveVerdict.AUDITED
                query.verdict == VerdictUiState.Clean -> ExpressiveVerdict.SAFE
                else -> ExpressiveVerdict.BYPASSED
            }
            ExpressiveVerdictBadge(verdict = verdict)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryDetailSheet(
    query: DnsQueryUiModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = query.appName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = query.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val verdict = when {
                query.verdict == VerdictUiState.Tracker && query.isBlocked -> ExpressiveVerdict.BLOCKED
                query.verdict == VerdictUiState.Tracker && !query.isBlocked -> ExpressiveVerdict.AUDITED
                query.verdict == VerdictUiState.Clean -> ExpressiveVerdict.SAFE
                else -> ExpressiveVerdict.BYPASSED
            }
            ExpressiveVerdictBadge(verdict = verdict)
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Queried Domain",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = query.domain,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Timestamp",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = query.time,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Action / Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when {
                                query.isBlocked -> "Blocked (Synthetic NXDOMAIN)"
                                query.verdict == VerdictUiState.Tracker -> "Audited (Forwarded)"
                                else -> "Clean (Resolved Safely)"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (query.verdict == VerdictUiState.Tracker) VerdictTracker else VerdictSafe
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://who.is/whois/${query.domain}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("WHOIS")
            }

            FilledTonalButton(
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("domain", query.domain)
                    clipboard.setPrimaryClip(clip)
                    onDismiss()
                },
                modifier = Modifier.weight(1f),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Domain")
            }
        }
    }
}
