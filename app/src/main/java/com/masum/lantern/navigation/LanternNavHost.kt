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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.masum.lantern.core.ui.haptics.rememberLanternHaptics
import com.masum.lantern.feature.control.ControlScreen
import com.masum.lantern.feature.livefeed.LiveFeedScreen
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
    val haptics = rememberLanternHaptics()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                LanternDestination.entries.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                haptics.tap()
                            }
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
                        icon = {
                            Icon(
                                imageVector = when (destination) {
                                    LanternDestination.Shield -> Icons.Rounded.Shield
                                    LanternDestination.Traffic -> Icons.Rounded.AltRoute
                                    LanternDestination.Insights -> Icons.Rounded.Insights
                                },
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LanternDestination.Shield.route,
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
            composable(LanternDestination.Shield.route) {
                ControlScreen(
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable(LanternDestination.Traffic.route) {
                LiveFeedScreen(
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable(LanternDestination.Insights.route) {
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
