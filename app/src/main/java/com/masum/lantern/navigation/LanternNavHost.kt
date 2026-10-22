package com.masum.lantern.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.navigation.NavBackStackEntry
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.masum.lantern.feature.control.ControlScreen
import com.masum.lantern.feature.history.HistoryScreen
import com.masum.lantern.feature.livefeed.LiveFeedScreen
import com.masum.lantern.feature.perapp.PerAppScreen
import com.masum.lantern.feature.settings.SettingsScreen
import com.masum.lantern.feature.stats.StatsScreen

private val NavEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
private const val NAV_DURATION = 280
private const val SLIDE_FRACTION = 0.08f

private fun tabEnter(toRight: Boolean): EnterTransition {
    val sign = if (toRight) 1 else -1
    return fadeIn(tween(NAV_DURATION, easing = NavEasing)) +
        slideIn(tween(NAV_DURATION, easing = NavEasing)) { fullSize ->
            IntOffset(x = (fullSize.width * SLIDE_FRACTION * sign).toInt(), y = 0)
        }
}

private fun tabExit(toRight: Boolean): ExitTransition {
    val sign = if (toRight) -1 else 1
    return fadeOut(tween(NAV_DURATION, easing = NavEasing)) +
        slideOut(tween(NAV_DURATION, easing = NavEasing)) { fullSize ->
            IntOffset(x = (fullSize.width * SLIDE_FRACTION * sign).toInt(), y = 0)
        }
}

private fun settingsEnter(): EnterTransition =
    fadeIn(tween(NAV_DURATION, easing = NavEasing)) +
        slideIn(tween(NAV_DURATION, easing = NavEasing)) { fullSize ->
            IntOffset(x = (fullSize.width * 0.25f).toInt(), y = 0)
        }

private fun settingsExit(): ExitTransition =
    fadeOut(tween(NAV_DURATION, easing = NavEasing)) +
        slideOut(tween(NAV_DURATION, easing = NavEasing)) { fullSize ->
            IntOffset(x = (fullSize.width * 0.25f).toInt(), y = 0)
        }

private fun settingsPopEnter(): EnterTransition =
    fadeIn(tween(NAV_DURATION, easing = NavEasing)) +
        slideIn(tween(NAV_DURATION, easing = NavEasing)) { fullSize ->
            IntOffset(x = -(fullSize.width * 0.08f).toInt(), y = 0)
        }

private fun settingsPopExit(): ExitTransition =
    fadeOut(tween(NAV_DURATION, easing = NavEasing)) +
        slideOut(tween(NAV_DURATION, easing = NavEasing)) { fullSize ->
            IntOffset(x = (fullSize.width * 0.12f).toInt(), y = 0)
        }

private val tabRoutes = LanternDestination.entries.map { it.route }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isGoingRight(): Boolean {
    val fromIndex = tabRoutes.indexOf(initialState.destination.route)
    val toIndex = tabRoutes.indexOf(targetState.destination.route)
    return toIndex > fromIndex
}

@Composable
fun LanternNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                LanternDestination.entries.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentDestination?.route == "settings") {
                                navController.popBackStack()
                            }
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
            startDestination = LanternDestination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            enterTransition = {
                when {
                    targetState.destination.route == "settings" -> settingsEnter()
                    else -> tabEnter(toRight = isGoingRight())
                }
            },
            exitTransition = {
                when {
                    targetState.destination.route == "settings" -> fadeOut(tween(NAV_DURATION, easing = NavEasing))
                    else -> tabExit(toRight = isGoingRight())
                }
            },
            popEnterTransition = {
                when {
                    initialState.destination.route == "settings" -> settingsPopEnter()
                    else -> tabEnter(toRight = !isGoingRight())
                }
            },
            popExitTransition = {
                when {
                    initialState.destination.route == "settings" -> settingsPopExit()
                    else -> tabExit(toRight = !isGoingRight())
                }
            },
        ) {
            composable(LanternDestination.Home.route) {
                ControlScreen(
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable(LanternDestination.LiveFeed.route) {
                LiveFeedScreen(
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable(LanternDestination.PerApp.route) {
                PerAppScreen(
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable(LanternDestination.History.route) {
                HistoryScreen(
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable(LanternDestination.Stats.route) {
                StatsScreen(
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun NavIcon(destination: LanternDestination, selected: Boolean) {
    val color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx())
        when (destination) {
            LanternDestination.Home -> {
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
            LanternDestination.LiveFeed -> {
                drawCircle(color, radius = w * 0.08f, center = Offset(w * 0.2f, h * 0.5f))
                listOf(0.35f, 0.5f, 0.65f).forEach { y ->
                    drawLine(color, Offset(w * 0.32f, h * y), Offset(w * 0.82f, h * y), strokeWidth = 2.dp.toPx())
                }
            }
            LanternDestination.PerApp -> {
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
            LanternDestination.History -> {
                drawCircle(color, radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.22f), strokeWidth = 2.dp.toPx())
                drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.7f, h * 0.5f), strokeWidth = 2.dp.toPx())
            }
            LanternDestination.Stats -> {
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
