package com.masum.lantern.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.masum.lantern.core.ui.theme.VerdictBypassed
import com.masum.lantern.core.ui.theme.VerdictBypassedContainer
import com.masum.lantern.core.ui.theme.VerdictSafe
import com.masum.lantern.core.ui.theme.VerdictSafeContainer
import com.masum.lantern.core.ui.theme.VerdictTracker
import com.masum.lantern.core.ui.theme.VerdictTrackerContainer

enum class ExpressiveVerdict {
    SAFE,
    BLOCKED,
    AUDITED,
    BYPASSED
}

@Composable
fun ExpressiveVerdictBadge(
    verdict: ExpressiveVerdict,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (verdict) {
        ExpressiveVerdict.SAFE -> Triple(VerdictSafeContainer, VerdictSafe, "Safe")
        ExpressiveVerdict.BLOCKED -> Triple(VerdictTrackerContainer, VerdictTracker, "Blocked")
        ExpressiveVerdict.AUDITED -> Triple(VerdictBypassedContainer, VerdictBypassed, "Audited")
        ExpressiveVerdict.BYPASSED -> Triple(VerdictBypassedContainer, VerdictBypassed, "Bypassed")
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
