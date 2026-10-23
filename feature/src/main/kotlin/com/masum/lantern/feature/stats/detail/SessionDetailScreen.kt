package com.masum.lantern.feature.stats.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.lantern.core.ui.components.AppIconImage
import com.masum.lantern.core.ui.components.EmptyState
import com.masum.lantern.core.ui.components.ExpressiveVerdict
import com.masum.lantern.core.ui.components.ExpressiveVerdictBadge
import com.masum.lantern.core.ui.components.VerdictUiState
import com.masum.lantern.core.ui.haptics.rememberLanternHaptics
import com.masum.lantern.core.ui.navigation.PredictiveBackContainer
import com.masum.lantern.core.ui.theme.VerdictSafe
import com.masum.lantern.core.ui.theme.VerdictTracker
import com.masum.lantern.feature.livefeed.DnsQueryUiModel
import com.masum.lantern.feature.livefeed.QueryDetailSheet
import com.masum.lantern.feature.util.formatDate
import com.masum.lantern.feature.util.formatTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SessionDetailScreen(
    onBackClick: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberLanternHaptics()
    var selectedQueryDetail by remember { mutableStateOf<DnsQueryUiModel?>(null) }
    var selectedAppForInspection by remember { mutableStateOf<SessionAppStatUiModel?>(null) }

    PredictiveBackContainer(onBack = onBackClick) {
        Scaffold(
            topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.isActive) "Active Session" else "Session Details",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (uiState.startedAt.isNotBlank()) {
                            Text(
                                text = uiState.startedAt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptics.tap()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                SessionHeroCard(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalToggleButton(
                        checked = uiState.selectedTab == SessionDetailTab.QUERIES,
                        onCheckedChange = {
                            haptics.tap()
                            viewModel.onTabSelected(SessionDetailTab.QUERIES)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = FilledTonalToggleButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Dns,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Queries (${uiState.queries.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (uiState.selectedTab == SessionDetailTab.QUERIES) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }

                    FilledTonalToggleButton(
                        checked = uiState.selectedTab == SessionDetailTab.APPS,
                        onCheckedChange = {
                            haptics.tap()
                            viewModel.onTabSelected(SessionDetailTab.APPS)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = FilledTonalToggleButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Apps (${uiState.apps.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (uiState.selectedTab == SessionDetailTab.APPS) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.weight(1f),
                    label = "session_tab_content"
                ) { tab ->
                    when (tab) {
                        SessionDetailTab.QUERIES -> {
                            SessionQueriesList(
                                queries = uiState.queries,
                                onQueryClick = { query ->
                                    haptics.tap()
                                    selectedQueryDetail = DnsQueryUiModel(
                                        id = query.id,
                                        domain = query.domain,
                                        appName = query.appName,
                                        packageName = query.packageName,
                                        verdict = if (query.isTracker) VerdictUiState.Tracker else VerdictUiState.Clean,
                                        time = query.formattedTime,
                                        isBlocked = query.isBlocked
                                    )
                                }
                            )
                        }
                        SessionDetailTab.APPS -> {
                            SessionAppsList(
                                apps = uiState.apps,
                                onAppClick = { app ->
                                    haptics.tap()
                                    selectedAppForInspection = app
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    }

    selectedQueryDetail?.let { queryUi ->
        ModalBottomSheet(
            onDismissRequest = { selectedQueryDetail = null },
            sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            QueryDetailSheet(
                query = queryUi,
                onDismiss = { selectedQueryDetail = null }
            )
        }
    }

    selectedAppForInspection?.let { app ->
        SessionAppQueriesBottomSheet(
            app = app,
            viewModel = viewModel,
            onDismiss = { selectedAppForInspection = null },
            onQueryClick = { queryUi ->
                selectedQueryDetail = queryUi
            }
        )
    }
}

@Composable
private fun SessionHeroCard(
    uiState: SessionDetailUiState,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (uiState.isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isActive) VerdictSafe else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = if (uiState.isActive) "Active Protection" else "Completed Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = uiState.duration,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (uiState.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SessionMetricPill(
                    title = "Total Calls",
                    value = uiState.totalQueries.toString(),
                    modifier = Modifier.weight(1f)
                )
                SessionMetricPill(
                    title = "Blocked",
                    value = "${uiState.trackerQueries} (${uiState.trackerPercentage}%)",
                    valueColor = if (uiState.trackerQueries > 0) VerdictTracker else VerdictSafe,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    }
}

@Composable
private fun SessionMetricPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun SessionQueriesList(
    queries: List<SessionQueryUiModel>,
    onQueryClick: (SessionQueryUiModel) -> Unit
) {
    if (queries.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(
                title = "No Queries",
                subtitle = "No DNS lookups were recorded during this session."
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = queries, key = { it.id }) { query ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onQueryClick(query) },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconImage(
                            packageName = query.packageName,
                            appName = query.appName,
                            size = 36.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = query.domain,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${query.appName.ifBlank { query.packageName }} · ${query.formattedTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val verdict = when {
                            query.isTracker && query.isBlocked -> ExpressiveVerdict.BLOCKED
                            query.isTracker && !query.isBlocked -> ExpressiveVerdict.AUDITED
                            else -> ExpressiveVerdict.SAFE
                        }
                        ExpressiveVerdictBadge(verdict = verdict)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionAppsList(
    apps: List<SessionAppStatUiModel>,
    onAppClick: (SessionAppStatUiModel) -> Unit
) {
    if (apps.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(
                title = "No Apps",
                subtitle = "No app traffic was recorded during this session."
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = apps, key = { it.packageName }) { app ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable { onAppClick(app) },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AppIconImage(
                            packageName = app.packageName,
                            appName = app.appName,
                            size = 44.dp
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${app.totalQueries} calls · ${app.trackerQueries} trackers (${app.trackerPercentage}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val verdict = if (app.trackerQueries > 0) ExpressiveVerdict.BLOCKED else ExpressiveVerdict.SAFE
                        ExpressiveVerdictBadge(verdict = verdict)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionAppQueriesBottomSheet(
    app: SessionAppStatUiModel,
    viewModel: SessionDetailViewModel,
    onDismiss: () -> Unit,
    onQueryClick: (DnsQueryUiModel) -> Unit
) {
    val queries by viewModel.getSessionAppQueries(app.packageName).collectAsStateWithLifecycle(initialValue = emptyList())
    val haptics = rememberLanternHaptics()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AppIconImage(
                    packageName = app.packageName,
                    appName = app.appName,
                    size = 46.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val verdict = if (app.trackerQueries > 0) ExpressiveVerdict.BLOCKED else ExpressiveVerdict.SAFE
                ExpressiveVerdictBadge(verdict = verdict)
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (queries.isEmpty()) {
                EmptyState(
                    title = "No Queries",
                    subtitle = "No queries recorded for this app in this session."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = queries, key = { it.id }) { query ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    haptics.tap()
                                    onQueryClick(
                                        DnsQueryUiModel(
                                            id = query.id,
                                            domain = query.domain,
                                            packageName = query.appInfo.packageName,
                                            appName = app.appName,
                                            time = query.timestamp.formatTime(),
                                            verdict = if (query.verdict is com.masum.lantern.domain.model.Verdict.Tracker) VerdictUiState.Tracker else VerdictUiState.Clean,
                                            isBlocked = query.isBlocked
                                        )
                                    )
                                },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = query.domain,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = query.timestamp.formatDate(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                val verdict = when {
                                    query.verdict is com.masum.lantern.domain.model.Verdict.Tracker && query.isBlocked -> ExpressiveVerdict.BLOCKED
                                    query.verdict is com.masum.lantern.domain.model.Verdict.Tracker && !query.isBlocked -> ExpressiveVerdict.AUDITED
                                    query.verdict is com.masum.lantern.domain.model.Verdict.Clean -> ExpressiveVerdict.SAFE
                                    else -> ExpressiveVerdict.BYPASSED
                                }
                                ExpressiveVerdictBadge(verdict = verdict)
                            }
                        }
                    }
                }
            }
        }
    }
}
