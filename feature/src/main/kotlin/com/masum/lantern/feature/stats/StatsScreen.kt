package com.masum.lantern.feature.stats

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.lantern.core.ui.haptics.rememberLanternHaptics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.lantern.core.ui.components.AppIconImage
import com.masum.lantern.core.ui.components.EmptyState
import com.masum.lantern.core.ui.components.ExpressiveVerdict
import com.masum.lantern.core.ui.components.ExpressiveVerdictBadge
import com.masum.lantern.core.ui.components.VerdictUiState
import com.masum.lantern.core.ui.theme.VerdictBypassed
import com.masum.lantern.core.ui.theme.VerdictSafe
import com.masum.lantern.core.ui.theme.VerdictSafeContainer
import com.masum.lantern.core.ui.theme.VerdictTracker
import com.masum.lantern.core.ui.theme.VerdictTrackerContainer
import com.masum.lantern.domain.model.DnsQuery
import com.masum.lantern.domain.model.Verdict
import com.masum.lantern.feature.livefeed.DnsQueryUiModel
import com.masum.lantern.feature.livefeed.QueryDetailSheet
import com.masum.lantern.feature.util.formatDate
import com.masum.lantern.feature.util.formatTime
import com.masum.lantern.feature.util.toUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    onSettingsClick: () -> Unit,
    onSessionClick: (Long) -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    var selectedAppForInspection by remember { mutableStateOf<AppStatUiModel?>(null) }
    val haptics = rememberLanternHaptics()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (pagerState.currentPage == 2 && uiState.sessions.isNotEmpty()) {
                        IconButton(onClick = {
                            haptics.heavy()
                            viewModel.onClearHistory()
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Clear Session History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                val tabs = listOf(
                    Triple("Overview", Icons.Rounded.PieChart, ""),
                    Triple("Apps", Icons.Rounded.Layers, if (uiState.apps.isNotEmpty()) " (${uiState.apps.size})" else ""),
                    Triple("Sessions", Icons.Rounded.QueryStats, if (uiState.sessions.isNotEmpty()) " (${uiState.sessions.size})" else "")
                )
                tabs.forEachIndexed { index, (label, icon, badge) ->
                    val isSelected = pagerState.currentPage == index
                    val shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        tabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }
                    FilledTonalToggleButton(
                        checked = isSelected,
                        onCheckedChange = {
                            if (it && !isSelected) {
                                haptics.toggle()
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shapes = shapes,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        colors = FilledTonalToggleButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$label$badge",
                            maxLines = 1,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> OverviewTabContent(uiState = uiState)
                    1 -> AppsTabContent(
                        apps = uiState.apps,
                        onAppClick = { selectedAppForInspection = it }
                    )
                    2 -> SessionsTabContent(
                        sessions = uiState.sessions,
                        onSessionClick = onSessionClick
                    )
                }
            }
        }
    }

    selectedAppForInspection?.let { app ->
        AppQueriesBottomSheet(
            app = app,
            viewModel = viewModel,
            onDismiss = { selectedAppForInspection = null }
        )
    }
}

@Composable
private fun OverviewTabContent(uiState: StatsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            PixelTelemetryRadialGauge(
                trackerPercentage = uiState.trackerPercentage,
                totalQueries = uiState.totalQueries,
                trackerQueries = uiState.trackerQueries,
                blockedQueries = uiState.blockedQueries,
                cleanQueries = uiState.cleanQueries,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = uiState.totalQueries.formatCount(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Total Queries",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        val countText = when {
                            uiState.blockedQueries > 0 && uiState.auditedQueries == 0 -> uiState.blockedQueries.formatCount()
                            uiState.blockedQueries == 0 && uiState.auditedQueries > 0 -> uiState.auditedQueries.formatCount()
                            uiState.blockedQueries > 0 && uiState.auditedQueries > 0 -> "${uiState.blockedQueries.formatCount()}/${uiState.auditedQueries.formatCount()}"
                            else -> "0"
                        }
                        val labelText = when {
                            uiState.blockedQueries > 0 && uiState.auditedQueries == 0 -> "Threats Blocked"
                            uiState.blockedQueries == 0 && uiState.auditedQueries > 0 -> "Trackers Audited"
                            uiState.blockedQueries > 0 && uiState.auditedQueries > 0 -> "Blocked / Audited"
                            else -> "Threats Detected"
                        }
                        Text(
                            text = countText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = VerdictTracker
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelSmall,
                            color = VerdictTracker.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = uiState.cleanQueries.formatCount(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = VerdictSafe
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Allowed Safe",
                            style = MaterialTheme.typography.labelSmall,
                            color = VerdictSafe.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (uiState.topDomains.isNotEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Top Targeted Domains",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${uiState.topDomains.size} analyzed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val maxCount = uiState.topDomains.maxOf { it.count }
                        uiState.topDomains.forEachIndexed { index, domain ->
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f),
                                thickness = 1.dp
                            )
                            ExpressiveDomainBar(
                                domain = domain,
                                maxCount = maxCount,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppsTabContent(
    apps: List<AppStatUiModel>,
    onAppClick: (AppStatUiModel) -> Unit
) {
    if (apps.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(
                title = "No Apps Captured",
                subtitle = "Active background and foreground network calls will appear here."
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 96.dp),
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
                            size = 46.dp
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
                            val detailText = when {
                                app.blockedQueries > 0 && app.auditedQueries == 0 ->
                                    "${app.totalQueries} calls · ${app.blockedQueries} blocked (${app.trackerPercentage}%)"
                                app.blockedQueries == 0 && app.auditedQueries > 0 ->
                                    "${app.totalQueries} calls · ${app.auditedQueries} audited (${app.trackerPercentage}%)"
                                app.blockedQueries > 0 && app.auditedQueries > 0 ->
                                    "${app.totalQueries} calls · ${app.blockedQueries} blk, ${app.auditedQueries} aud"
                                else ->
                                    "${app.totalQueries} calls · 0 trackers"
                            }
                            Text(
                                text = detailText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val verdict = when {
                            app.blockedQueries > 0 -> ExpressiveVerdict.BLOCKED
                            app.trackerQueries > 0 -> ExpressiveVerdict.AUDITED
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
private fun SessionsTabContent(
    sessions: List<SessionUiModel>,
    onSessionClick: (Long) -> Unit
) {
    val haptics = rememberLanternHaptics()
    if (sessions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(
                title = "No Past Sessions",
                subtitle = "Completed protection sessions will be recorded here."
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = sessions, key = { it.sessionId }) { session ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable {
                            haptics.tap()
                            onSessionClick(session.sessionId)
                        },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (session.isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (session.isActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(VerdictSafe)
                                    )
                                }
                                Text(
                                    text = session.startedAt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = session.duration,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (session.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${session.totalQueries} Queries Recorded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val trackerSummaryText = when {
                                session.blockedQueries > 0 && session.auditedQueries == 0 ->
                                    "${session.blockedQueries} Blocked (${session.trackerPercentage}%)"
                                session.blockedQueries == 0 && session.auditedQueries > 0 ->
                                    "${session.auditedQueries} Audited (${session.trackerPercentage}%)"
                                session.blockedQueries > 0 && session.auditedQueries > 0 ->
                                    "${session.blockedQueries} Blk · ${session.auditedQueries} Aud"
                                else -> "100% Clean"
                            }
                            Text(
                                text = trackerSummaryText,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (session.trackerQueries > 0) VerdictTracker else VerdictSafe,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppQueriesBottomSheet(
    app: AppStatUiModel,
    viewModel: StatsViewModel,
    onDismiss: () -> Unit
) {
    val queries by viewModel.getAppQueries(app.packageName).collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedQueryDetail by remember { mutableStateOf<DnsQueryUiModel?>(null) }

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
                    subtitle = "No queries recorded yet for this app."
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
                                    selectedQueryDetail = DnsQueryUiModel(
                                        id = query.id,
                                        domain = query.domain,
                                        packageName = query.appInfo.packageName,
                                        appName = app.appName,
                                        time = query.timestamp.formatTime(),
                                        verdict = query.verdict.toUiState(),
                                        isBlocked = query.isBlocked
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
                                    query.verdict is Verdict.Tracker && query.isBlocked -> ExpressiveVerdict.BLOCKED
                                    query.verdict is Verdict.Tracker && !query.isBlocked -> ExpressiveVerdict.AUDITED
                                    query.verdict is Verdict.Clean -> ExpressiveVerdict.SAFE
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
}

@Composable
private fun PixelTelemetryRadialGauge(
    trackerPercentage: Int,
    totalQueries: Int,
    trackerQueries: Int,
    blockedQueries: Int,
    cleanQueries: Int,
    modifier: Modifier = Modifier
) {
    val animatedPct by animateFloatAsState(
        targetValue = trackerPercentage.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "radial_pct"
    )
    val fraction = (animatedPct / 100f).coerceIn(0f, 1f)

    val primary = MaterialTheme.colorScheme.primary
    val error = VerdictTracker
    val trackBg = MaterialTheme.colorScheme.surfaceContainerHighest

    val totalArc = 200f
    val startAngle = 170f
    val auditedQueries = (trackerQueries - blockedQueries).coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val strokeWidth = 18.dp.toPx()
            val availableWidth = size.width - strokeWidth * 2
            val availableHeight = size.height * 1.6f
            val r = minOf(availableWidth / 2f, availableHeight / 2f)
            val centerOffset = Offset(size.width / 2f, size.height * 0.62f)
            val tl = Offset(centerOffset.x - r, centerOffset.y - r)
            val arcSize = Size(r * 2, r * 2)
            val strokeStyle = Stroke(strokeWidth, cap = StrokeCap.Round)

            drawArc(
                color = trackBg,
                startAngle = startAngle,
                sweepAngle = totalArc,
                useCenter = false,
                topLeft = tl,
                size = arcSize,
                style = strokeStyle
            )

            val sweepAngle = fraction * totalArc
            if (sweepAngle > 0f) {
                drawArc(
                    color = error,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = tl,
                    size = arcSize,
                    style = strokeStyle
                )
            } else if (totalQueries > 0) {
                drawArc(
                    color = primary,
                    startAngle = startAngle,
                    sweepAngle = totalArc,
                    useCenter = false,
                    topLeft = tl,
                    size = arcSize,
                    style = strokeStyle
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 18.dp)
        ) {
            AnimatedContent(
                targetState = trackerPercentage,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                },
                label = "pct_text"
            ) { pct ->
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 68.sp,
                        letterSpacing = (-1.5).sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = if (pct > 0) error else primary
                )
            }
            Text(
                text = if (trackerPercentage > 0) "Telemetry Filtered" else if (totalQueries > 0) "100% Clean Network" else "Awaiting Traffic",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (trackerPercentage > 0) error else primary)
                )
                val statusBadgeText = when {
                    blockedQueries > 0 && auditedQueries == 0 ->
                        "${blockedQueries.formatCount()} blocked · ${cleanQueries.formatCount()} clean"
                    blockedQueries == 0 && auditedQueries > 0 ->
                        "${auditedQueries.formatCount()} audited · ${cleanQueries.formatCount()} clean"
                    blockedQueries > 0 && auditedQueries > 0 ->
                        "${blockedQueries.formatCount()} blk · ${auditedQueries.formatCount()} aud · ${cleanQueries.formatCount()} clean"
                    else ->
                        "0 threats · ${cleanQueries.formatCount()} clean"
                }
                Text(
                    text = statusBadgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveDomainBar(
    domain: DomainStatUiModel,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    val fraction = if (maxCount > 0) (domain.count.toFloat() / maxCount).coerceIn(0f, 1f) else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bar_frac"
    )
    val barColor = if (domain.isTracker) VerdictTracker else MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = domain.domain,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${domain.count} queries",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                val domainVerdict = when {
                    domain.isTracker && domain.isBlocked -> ExpressiveVerdict.BLOCKED
                    domain.isTracker -> ExpressiveVerdict.AUDITED
                    else -> ExpressiveVerdict.SAFE
                }
                ExpressiveVerdictBadge(
                    verdict = domainVerdict
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

private fun Int.formatCount(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}k"
    else -> toString()
}
