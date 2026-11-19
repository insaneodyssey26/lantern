package com.masum.lantern.feature.settings.about

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Shop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.lantern.core.ui.R as UiR
import com.masum.lantern.core.ui.haptics.rememberLanternHaptics
import com.masum.lantern.core.ui.navigation.PredictiveBackContainer
import com.masum.lantern.feature.settings.SettingsViewModel
import com.masum.lantern.feature.settings.crash.CrashLogBottomSheet
import com.masum.lantern.feature.settings.legal.LegalDocumentBottomSheet
import com.masum.lantern.feature.settings.legal.LegalDocumentType
import com.masum.lantern.feature.settings.updater.UpdateBottomSheet
import com.masum.lantern.feature.settings.updater.UpdateCheckStatus
import com.masum.lantern.feature.settings.updater.UpdaterViewModel
import com.masum.lantern.feature.whatsnew.WhatsNewBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onCheckPlayUpdates: ((onResult: (String) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    updaterViewModel: UpdaterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updaterUiState by updaterViewModel.uiState.collectAsStateWithLifecycle()
    val isCheckingUpdates = updaterUiState.status is UpdateCheckStatus.Checking
    val haptics = rememberLanternHaptics()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var showWhatsNewSheet by remember { mutableStateOf(false) }
    var showCrashLogSheet by remember { mutableStateOf(false) }
    var selectedLegalDoc by remember { mutableStateOf<LegalDocumentType?>(null) }

    val whatsNewSheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val crashLogSheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val legalSheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    val updateSpinTransition = rememberInfiniteTransition(label = "github_update_spin")
    val updateSpinRotation by updateSpinTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "github_spin_rotation"
    )

    val currentVersion = remember(context) {
        runCatching {
            val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            "v" + (pInfo.versionName ?: "2.0")
        }.getOrDefault("v2.0-beta4")
    }

    PredictiveBackContainer(onBack = onBackClick) {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "About Lantern",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Version, updates, diagnostics & legal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = {
                                haptics.tap()
                                onBackClick()
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(paddingValues),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = UiR.drawable.ic_lantern_logo),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Lantern",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Private On-Device DNS Firewall",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = currentVersion,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Updates & Releases",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AboutRowItem(
                                icon = Icons.Rounded.Shop,
                                title = "Check Google Play Updates",
                                subtitle = "Fastest updates for Play Store installs",
                                onClick = {
                                    haptics.tap()
                                    if (onCheckPlayUpdates != null) {
                                        onCheckPlayUpdates {}
                                    } else {
                                        val playIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=${context.packageName}")
                                        ).apply {
                                            setPackage("com.android.vending")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        if (playIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(playIntent)
                                        } else {
                                            val webIntent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(webIntent)
                                        }
                                    }
                                }
                            )

                            AboutRowItem(
                                icon = Icons.Rounded.Refresh,
                                iconModifier = if (isCheckingUpdates) Modifier.rotate(updateSpinRotation) else Modifier,
                                title = if (isCheckingUpdates) "Checking GitHub..." else "Check GitHub Releases",
                                subtitle = "Direct APK releases and changelogs",
                                onClick = {
                                    haptics.tap()
                                    updaterViewModel.checkForUpdates(showSheetOnResult = true)
                                }
                            )

                            AboutRowItem(
                                icon = Icons.Rounded.NewReleases,
                                title = "What's New in this Build",
                                subtitle = "View recent highlights and changelog",
                                onClick = {
                                    haptics.tap()
                                    showWhatsNewSheet = true
                                }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Diagnostics & Support",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AboutRowItem(
                                icon = Icons.Rounded.BugReport,
                                title = "Crash Diagnostics",
                                subtitle = if (uiState.crashLog != null) "Unsent crash report available" else "No crash recorded",
                                hasBadge = uiState.crashLog != null,
                                onClick = {
                                    haptics.tap()
                                    showCrashLogSheet = true
                                }
                            )

                            AboutRowItem(
                                icon = Icons.Rounded.Email,
                                title = "Email Developer",
                                subtitle = "skmasumali.dev@gmail.com",
                                onClick = {
                                    haptics.tap()
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:skmasumali.dev@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Lantern Feedback / Bug Report ($currentVersion)")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    runCatching { context.startActivity(emailIntent) }
                                }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Legal & Open Source",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                item {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AboutRowItem(
                                icon = Icons.Rounded.Shield,
                                title = "Privacy Policy",
                                subtitle = "Zero data collection, 100% on-device",
                                onClick = {
                                    haptics.tap()
                                    selectedLegalDoc = LegalDocumentType.PRIVACY_POLICY
                                }
                            )

                            AboutRowItem(
                                icon = Icons.Rounded.Gavel,
                                title = "Terms & Conditions",
                                subtitle = "GNU General Public License v3.0 terms",
                                onClick = {
                                    haptics.tap()
                                    selectedLegalDoc = LegalDocumentType.TERMS_AND_CONDITIONS
                                }
                            )

                            AboutRowItem(
                                icon = Icons.Rounded.Code,
                                title = "Source Code on GitHub",
                                subtitle = "github.com/insaneodyssey26/lantern",
                                trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                                onClick = {
                                    haptics.tap()
                                    val ghIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/insaneodyssey26/lantern")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    runCatching { context.startActivity(ghIntent) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWhatsNewSheet) {
        WhatsNewBottomSheet(
            onDismiss = {
                scope.launch {
                    whatsNewSheetState.hide()
                    showWhatsNewSheet = false
                }
            },
            sheetState = whatsNewSheetState
        )
    }

    if (showCrashLogSheet) {
        CrashLogBottomSheet(
            crashLog = uiState.crashLog,
            onDismiss = {
                scope.launch {
                    crashLogSheetState.hide()
                    showCrashLogSheet = false
                }
            },
            onClear = {
                viewModel.clearCrashLog()
            },
            sheetState = crashLogSheetState
        )
    }

    selectedLegalDoc?.let { docType ->
        LegalDocumentBottomSheet(
            type = docType,
            onDismiss = {
                scope.launch {
                    legalSheetState.hide()
                    selectedLegalDoc = null
                }
            },
            sheetState = legalSheetState
        )
    }

    if (updaterUiState.isSheetVisible) {
        UpdateBottomSheet(
            uiState = updaterUiState,
            onDismiss = { updaterViewModel.dismissSheet() },
            onStartDownload = { update -> updaterViewModel.startDownload(update) },
            onInstallUpdate = { filePath -> updaterViewModel.installDownloadedUpdate(filePath) },
            onRetryCheck = {
                updaterViewModel.checkForUpdates(showSheetOnResult = true)
            }
        )
    }
}

@Composable
private fun AboutRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    hasBadge: Boolean = false,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = iconModifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (hasBadge) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = "1",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
