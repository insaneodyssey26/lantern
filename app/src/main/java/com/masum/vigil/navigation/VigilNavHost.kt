package com.masum.vigil.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.masum.vigil.feature.control.ControlScreen
import com.masum.vigil.feature.history.HistoryScreen
import com.masum.vigil.feature.livefeed.LiveFeedScreen
import com.masum.vigil.feature.perapp.PerAppScreen
import com.masum.vigil.feature.stats.StatsScreen

@Composable
fun VigilNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                VigilDestination.entries.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { NavIcon(destination = destination, selected = selected) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = VigilDestination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable(VigilDestination.Home.route) {
                ControlScreen()
            }
            composable(VigilDestination.LiveFeed.route) {
                LiveFeedScreen()
            }
            composable(VigilDestination.PerApp.route) {
                PerAppScreen()
            }
            composable(VigilDestination.History.route) {
                HistoryScreen()
            }
            composable(VigilDestination.Stats.route) {
                StatsScreen()
            }
        }
    }
}

@Composable
private fun NavIcon(destination: VigilDestination, selected: Boolean) {
    val color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx())
        when (destination) {
            VigilDestination.Home -> {
                val shield = Path().apply {
                    moveTo(w * 0.5f, h * 0.05f)
                    lineTo(w * 0.92f, h * 0.28f)
                    lineTo(w * 0.92f, h * 0.6f)
                    cubicTo(w * 0.92f, h * 0.82f, w * 0.5f, h * 0.96f, w * 0.5f, h * 0.96f)
                    cubicTo(w * 0.5f, h * 0.96f, w * 0.08f, h * 0.82f, w * 0.08f, h * 0.6f)
                    lineTo(w * 0.08f, h * 0.28f)
                    close()
                }
                if (selected) drawPath(shield, color) else drawPath(shield, color, style = stroke)
            }
            VigilDestination.LiveFeed -> {
                drawCircle(color, radius = w * 0.08f, center = Offset(w * 0.2f, h * 0.5f))
                listOf(0.35f, 0.5f, 0.65f).forEach { y ->
                    drawLine(color, Offset(w * 0.32f, h * y), Offset(w * 0.82f, h * y), strokeWidth = 2.dp.toPx())
                }
            }
            VigilDestination.PerApp -> {
                val gap = w * 0.12f
                val cellSize = (w - gap * 3) / 2
                listOf(gap to gap, gap * 2 + cellSize to gap, gap to gap * 2 + cellSize, gap * 2 + cellSize to gap * 2 + cellSize).forEach { (x, y) ->
                    if (selected) {
                        drawRoundRect(color, topLeft = Offset(x, y), size = Size(cellSize, cellSize), cornerRadius = CornerRadius(2.dp.toPx()))
                    } else {
                        drawRoundRect(color, topLeft = Offset(x, y), size = Size(cellSize, cellSize), cornerRadius = CornerRadius(2.dp.toPx()), style = stroke)
                    }
                }
            }
            VigilDestination.History -> {
                drawCircle(color, radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.22f), strokeWidth = 2.dp.toPx())
                drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.7f, h * 0.5f), strokeWidth = 2.dp.toPx())
            }
            VigilDestination.Stats -> {
                val barWidth = w * 0.18f
                val bars = listOf(0.15f to 0.6f, 0.4f to 0.35f, 0.65f to 0.75f)
                bars.forEach { (xRatio, heightRatio) ->
                    val barH = h * heightRatio
                    val topY = h - barH - h * 0.08f
                    if (selected) {
                        drawRoundRect(color, topLeft = Offset(w * xRatio, topY), size = Size(barWidth, barH), cornerRadius = CornerRadius(2.dp.toPx()))
                    } else {
                        drawRoundRect(color, topLeft = Offset(w * xRatio, topY), size = Size(barWidth, barH), cornerRadius = CornerRadius(2.dp.toPx()), style = stroke)
                    }
                }
            }
        }
    }
}
