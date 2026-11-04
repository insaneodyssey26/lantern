package com.masum.lantern.feature.whatsnew

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.lantern.core.ui.haptics.rememberLanternHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

data class WhatsNewFeature(
    val icon: ImageVector,
    val iconContainerColor: Color,
    val iconContentColor: Color,
    val tag: String,
    val title: String,
    val description: String,
)

data class WhatsNewRelease(
    val versionName: String,
    val title: String,
    val isLatest: Boolean = false,
    val features: List<WhatsNewFeature>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        WhatsNewContent(
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            }
        )
    }
}

@Composable
fun WhatsNewContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberLanternHaptics()

    val releases = remember {
        listOf(
            WhatsNewRelease(
                versionName = "2.0-beta2",
                title = "Multi-List Filter Engine & Presets",
                isLatest = true,
                features = listOf(
                    WhatsNewFeature(
                        icon = Icons.Rounded.FilterList,
                        iconContainerColor = Color(0xFFD1C4E9),
                        iconContentColor = Color(0xFF4527A0),
                        tag = "New Engine",
                        title = "Multi-List Protection Feeds",
                        description = "Enable multiple curated blocklists simultaneously: StevenBlack, OISD Small, HaGeZi Threat Intelligence, and AdGuard DNS Filter."
                    ),
                    WhatsNewFeature(
                        icon = Icons.Rounded.Security,
                        iconContainerColor = Color(0xFFFFCDD2),
                        iconContentColor = Color(0xFFB71C1C),
                        tag = "Custom Feeds",
                        title = "Custom Community URLs",
                        description = "Add any custom remote hosts or AdGuard format filter with in-memory dynamic merging and instant live reload."
                    ),
                    WhatsNewFeature(
                        icon = Icons.Rounded.Sync,
                        iconContainerColor = Color(0xFFC8E6C9),
                        iconContentColor = Color(0xFF1B5E20),
                        tag = "Background",
                        title = "Smart Background Sync",
                        description = "Scheduled daily filter updates via WorkManager, running strictly on unmetered Wi-Fi and healthy battery."
                    ),
                    WhatsNewFeature(
                        icon = Icons.Rounded.Tune,
                        iconContainerColor = Color(0xFFE1BEE7),
                        iconContentColor = Color(0xFF4A148C),
                        tag = "Preset",
                        title = "One-Tap Safe Baseline",
                        description = "Apply an optimal privacy-safe filter baseline in one tap with direct source attribution links to list maintainers."
                    )
                )
            ),
            WhatsNewRelease(
                versionName = "2.0",
                title = "Next-Gen Redesign & Architecture",
                isLatest = false,
                features = listOf(
                    WhatsNewFeature(
                        icon = Icons.Rounded.AutoAwesome,
                        iconContainerColor = Color(0xFFFFE082),
                        iconContentColor = Color(0xFF5D4037),
                        tag = "Redesign",
                        title = "Material 3 Expressive System",
                        description = "Modern floating capsule dock navigation, Google Sans Flex variable typography, and seamless edge-to-edge support."
                    ),
                    WhatsNewFeature(
                        icon = Icons.Rounded.Block,
                        iconContainerColor = Color(0xFFFFCCBC),
                        iconContentColor = Color(0xFFBF360C),
                        tag = "Firewall",
                        title = "Active DNS Sinkhole Engine",
                        description = "Sub-millisecond tracker neutralizing with synthetic NXDOMAIN responses and zero-overhead reverse-label suffix trie."
                    ),
                    WhatsNewFeature(
                        icon = Icons.Rounded.Speed,
                        iconContainerColor = Color(0xFFB2DFDB),
                        iconContentColor = Color(0xFF004D40),
                        tag = "Insights",
                        title = "Real-Time Telemetry & Stats",
                        description = "Live query monitoring, per-app breakdown, block rate gauge, and rolling 24-hour visual network analytics."
                    ),
                    WhatsNewFeature(
                        icon = Icons.Rounded.Lock,
                        iconContainerColor = Color(0xFFBBDEFB),
                        iconContentColor = Color(0xFF0D47A1),
                        tag = "Privacy",
                        title = "Encrypted DoH & DoT Upstreams",
                        description = "DNS-over-HTTPS (RFC 8484) and DNS-over-TLS (RFC 7858) protection against ISP surveillance and DNS hijacking."
                    )
                )
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                shape = CircleShape
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Changelog & Highlights",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "What's New in Lantern",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Release history and latest feature additions",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            releases.forEach { release ->
                ReleaseSection(release = release)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                haptics.heavy()
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun ReleaseSection(
    release: WhatsNewRelease,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "v${release.versionName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (release.isLatest) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "LATEST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = release.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        release.features.forEach { feature ->
            WhatsNewCard(feature = feature)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhatsNewCard(
    feature: WhatsNewFeature,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(feature.iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = feature.iconContentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = feature.tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
