package com.masum.lantern.feature.settings.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.masum.lantern.core.ui.haptics.rememberLanternHaptics

enum class LegalDocumentType {
    PRIVACY_POLICY,
    TERMS_AND_CONDITIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentBottomSheet(
    type: LegalDocumentType,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
) {
    val haptics = rememberLanternHaptics()
    val isPrivacy = type == LegalDocumentType.PRIVACY_POLICY

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPrivacy) Icons.Rounded.Shield else Icons.Rounded.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPrivacy) "Privacy Policy" else "Terms & Conditions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isPrivacy) "Zero data collection, 100% on-device" else "Open source license & terms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isPrivacy) {
                        LegalSection(
                            title = "Zero Data Collection",
                            body = "Lantern does not collect, transmit, store, or sell any personal data. There are no user accounts, analytics SDKs, trackers, or telemetry beacons anywhere in the application."
                        )
                        LegalSection(
                            title = "Local-Only Operation",
                            body = "Lantern uses Android's VpnService API exclusively to create an internal on-device DNS sinkhole. Your network traffic does not pass through any remote proxy server. All traffic filtering and query inspection happen completely on your phone."
                        )
                        LegalSection(
                            title = "DNS Queries & Upstreams",
                            body = "When DNS queries are forwarded, they are sent directly to the DNS provider you choose in Settings (Cloudflare, Quad9, Google, or AdGuard) over UDP, DoH, or DoT. Lantern never intermediates or inspects traffic on any remote server."
                        )
                        LegalSection(
                            title = "On-Device Storage",
                            body = "All recorded DNS logs, custom domain rules, and app bypass preferences are stored locally in an encrypted SQLite database on your device. You can clear all logs at any time in Settings."
                        )
                    } else {
                        LegalSection(
                            title = "Open Source License",
                            body = "Lantern is open source software released under the GNU General Public License v3.0 (GPLv3). You are free to inspect, modify, and distribute this software according to the terms of the license."
                        )
                        LegalSection(
                            title = "Disclaimer of Warranty",
                            body = "Lantern is provided on an 'as-is' and 'as-available' basis without warranties of any kind, either express or implied, including fitness for a particular purpose or non-infringement."
                        )
                        LegalSection(
                            title = "Personal Responsibility",
                            body = "Filtering DNS traffic may occasionally cause certain third-party websites or services that rely heavily on tracking scripts to function differently. You can bypass any app or allow specific domains at any time."
                        )
                        LegalSection(
                            title = "Acceptable Use",
                            body = "You agree to use Lantern in compliance with all applicable local, national, and international laws and regulations."
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FilledTonalButton(
                onClick = {
                    haptics.tap()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Got it")
            }
        }
    }
}

@Composable
private fun LegalSection(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
