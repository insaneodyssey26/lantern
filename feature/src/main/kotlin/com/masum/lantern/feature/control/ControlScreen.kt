package com.masum.lantern.feature.control

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FilledTonalToggleButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.masum.lantern.feature.whatsnew.WhatsNewContent
import kotlinx.coroutines.launch
import com.masum.lantern.core.ui.components.LanternHero
import com.masum.lantern.core.ui.components.SpotlightStep
import com.masum.lantern.core.ui.components.SpotlightTourOverlay
import com.masum.lantern.core.ui.haptics.rememberLanternHaptics
import com.masum.lantern.core.ui.theme.LanternAmber
import com.masum.lantern.domain.model.MonitoringState
import com.masum.lantern.feature.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ControlScreen(
    onSettingsClick: () -> Unit,
    onNavigateToTraffic: (String) -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToDomainRules: () -> Unit = {},
    viewModel: ControlViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeSession = uiState.monitoringState as? MonitoringState.Active
    val isActive = activeSession != null
    val haptics = rememberLanternHaptics()
    val scope = rememberCoroutineScope()
    val whatsNewSheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    var shieldRect by remember { mutableStateOf<Rect?>(null) }
    var snapshotRect by remember { mutableStateOf<Rect?>(null) }
    var currentTourStepIndex by remember { mutableIntStateOf(0) }

    val tourSteps = listOf(
        SpotlightStep(
            title = "Power Shield",
            description = "Tap the shield anytime to turn on your on-device firewall and start blocking trackers.",
            targetRect = shieldRect,
            isBottomTarget = false
        ),
        SpotlightStep(
            title = "Security Snapshot",
            description = "Monitor your active DNS upstream, protocol, and threat blocklist count at a glance.",
            targetRect = snapshotRect,
            isBottomTarget = false
        )
    )

    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.onConsentGranted()
        else viewModel.onConsentDismissed()
    }

    LaunchedEffect(uiState.pendingConsentIntent) {
        uiState.pendingConsentIntent?.let { consentLauncher.launch(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lantern",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.75).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = {
                            haptics.tap()
                            onSettingsClick()
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Box(modifier = Modifier.onGloballyPositioned { coordinates ->
                    shieldRect = coordinates.boundsInRoot()
                }) {
                    LanternHero(
                        isActive = isActive,
                        onToggle = { viewModel.onToggleClick() },
                        isBlockingEnabled = uiState.isBlockingEnabled,
                        onPresetSelected = { preset ->
                            when (preset) {
                                "blocking" -> viewModel.setBlockingEnabled(true)
                                "audit" -> viewModel.setBlockingEnabled(false)
                            }
                        }
                    )
                }
            }

            item {
                Box(modifier = Modifier.onGloballyPositioned { coordinates ->
                    snapshotRect = coordinates.boundsInRoot()
                }) {
                    QuickSecuritySnapshot(
                        upstreamDns = uiState.upstreamDns,
                        isBlockingEnabled = uiState.isBlockingEnabled,
                        isActive = isActive,
                        onSelectDns = { dns ->
                            viewModel.setUpstreamDns(dns)
                        },
                        onSelectBlockingEnabled = { enabled ->
                            viewModel.setBlockingEnabled(enabled)
                        }
                    )
                }
            }

            if (isActive) {
                item {
                    ExpressiveLivePulseStrip(
                        startedAt = activeSession.startedAt,
                        totalQueries = uiState.totalQueries
                    )
                }
            }

            if (uiState.isPrivateDnsActive) {
                item {
                    PrivateDnsExpressiveWarning(
                        onClick = { viewModel.refreshPrivateDnsStatus() }
                    )
                }
            }

            item {
                Text(
                    text = "Live Telemetry Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                ExpressiveSegmentedMetrics(
                    totalQueries = uiState.totalQueries,
                    trackerQueries = uiState.trackerQueries,
                    blocklistCount = uiState.blocklistCount,
                    isBlockingEnabled = uiState.isBlockingEnabled,
                    onBlockedClick = {
                        haptics.tap()
                        onNavigateToTraffic("Trackers")
                    },
                    onCleanClick = {
                        haptics.tap()
                        onNavigateToTraffic("Clean")
                    },
                    onTotalAuditedClick = {
                        haptics.tap()
                        onNavigateToInsights()
                    },
                    onRulesetClick = {
                        haptics.tap()
                        onNavigateToDomainRules()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (!uiState.isFeatureGuideCompleted && currentTourStepIndex < tourSteps.size) {
        SpotlightTourOverlay(
            step = tourSteps[currentTourStepIndex],
            stepIndex = currentTourStepIndex,
            totalSteps = tourSteps.size,
            onNext = {
                if (currentTourStepIndex < tourSteps.size - 1) {
                    currentTourStepIndex++
                } else {
                    viewModel.completeFeatureGuide()
                }
            },
            onSkip = {
                viewModel.completeFeatureGuide()
            }
        )
    }

    if (uiState.showWhatsNewSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissWhatsNew() },
            sheetState = whatsNewSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            WhatsNewContent(
                onDismiss = {
                    scope.launch {
                        whatsNewSheetState.hide()
                        viewModel.dismissWhatsNew()
                    }
                },
                versionName = "2.0"
            )
        }
    }
}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveLivePulseStrip(
    startedAt: Long,
    totalQueries: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
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
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "Live Protection Active",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = formatDuration(startedAt, System.currentTimeMillis()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Inspecting on-device traffic",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$totalQueries processed",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ExpressiveSegmentedMetrics(
    totalQueries: Int,
    trackerQueries: Int,
    blocklistCount: Int,
    isBlockingEnabled: Boolean,
    onBlockedClick: () -> Unit = {},
    onCleanClick: () -> Unit = {},
    onTotalAuditedClick: () -> Unit = {},
    onRulesetClick: () -> Unit = {}
) {
    val cleanQueries = (totalQueries - trackerQueries).coerceAtLeast(0)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                title = if (isBlockingEnabled) "Blocked Trackers" else "Trackers Detected",
                count = trackerQueries.toString(),
                label = if (isBlockingEnabled) "Threats silenced" else "Audited passively",
                icon = if (isBlockingEnabled) Icons.Rounded.Block else Icons.Rounded.Shield,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                onClick = onBlockedClick,
                modifier = Modifier.weight(1f)
            )

            MetricTile(
                title = "Clean Requests",
                count = cleanQueries.toString(),
                label = "Resolved safely",
                icon = Icons.Rounded.CheckCircleOutline,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onCleanClick,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                title = "Total Audited",
                count = totalQueries.toString(),
                label = "Network inquiries",
                icon = Icons.Rounded.Speed,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = onTotalAuditedClick,
                modifier = Modifier.weight(1f)
            )

            MetricTile(
                title = "Ruleset Engine",
                count = blocklistCount.formatCount(),
                label = "Trie domain filters",
                icon = Icons.Rounded.Dns,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = onRulesetClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(17.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Text(
                text = count,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QuickSecuritySnapshot(
    upstreamDns: String,
    isBlockingEnabled: Boolean,
    isActive: Boolean,
    onSelectDns: (String) -> Unit = {},
    onSelectBlockingEnabled: (Boolean) -> Unit = {}
) {
    val haptics = rememberLanternHaptics()
    var dnsExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }

    val dnsName = when (upstreamDns) {
        "1.1.1.1" -> "Cloudflare"
        "9.9.9.9" -> "Quad9"
        "8.8.8.8" -> "Google"
        else -> upstreamDns
    }

    val dnsArrowRotation by animateFloatAsState(
        targetValue = if (dnsExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dnsArrowRotation"
    )

    val modeArrowRotation by animateFloatAsState(
        targetValue = if (modeExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "modeArrowRotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Dns,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Upstream DNS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = upstreamDns,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    SplitButtonLayout(
                        leadingButton = {
                            SplitButtonDefaults.TonalLeadingButton(
                                onClick = {
                                    haptics.tap()
                                    dnsExpanded = !dnsExpanded
                                }
                            ) {
                                Text(
                                    text = dnsName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        },
                        trailingButton = {
                            SplitButtonDefaults.TonalTrailingButton(
                                checked = dnsExpanded,
                                onCheckedChange = {
                                    haptics.tap()
                                    dnsExpanded = it
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "Choose DNS Resolver",
                                    modifier = Modifier
                                        .size(SplitButtonDefaults.TrailingIconSize)
                                        .graphicsLayer { rotationZ = dnsArrowRotation }
                                )
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = dnsExpanded,
                        onDismissRequest = { dnsExpanded = false },
                        shape = MaterialTheme.shapes.large,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        val dnsOptions = listOf(
                            Pair("Cloudflare", "1.1.1.1"),
                            Pair("Quad9", "9.9.9.9"),
                            Pair("Google", "8.8.8.8")
                        )

                        dnsOptions.forEach { (name, ip) ->
                            val isSelected = upstreamDns == ip
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "($ip)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                trailingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null,
                                onClick = {
                                    haptics.heavy()
                                    onSelectDns(ip)
                                    dnsExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBlockingEnabled) Icons.Rounded.Shield else Icons.Rounded.Security,
                            contentDescription = null,
                            tint = if (isBlockingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Protection Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBlockingEnabled) "Block trackers" else "Audit queries only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    val modeOptions = listOf(
                        Triple(true, "Block", Icons.Rounded.Shield),
                        Triple(false, "Audit", Icons.Rounded.Security)
                    )
                    modeOptions.forEachIndexed { index, (enabled, label, icon) ->
                        val isSelected = isBlockingEnabled == enabled
                        val shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            else -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        }
                        FilledTonalToggleButton(
                            checked = isSelected,
                            onCheckedChange = {
                                if (it && !isSelected) {
                                    haptics.toggle()
                                    onSelectBlockingEnabled(enabled)
                                }
                            },
                            shapes = shapes,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivateDnsExpressiveWarning(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Android Private DNS Active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Encrypted DoT bypasses local VPN interception. Turn off Private DNS in Android Settings to allow Lantern to audit all app queries.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

private fun Int.formatCount(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}k"
    else -> toString()
}
