package com.masum.lantern.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
import com.masum.lantern.feature.whitelist.AppWhitelistScreen

private val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
private const val NAV_DURATION = 320
private const val SLIDE_FRACTION = 0.06f

private fun settingsEnter(): EnterTransition =
    fadeIn(tween(NAV_DURATION, easing = EmphasizedEasing)) +
        slideIn(tween(NAV_DURATION, easing = EmphasizedEasing)) { fullSize ->
            IntOffset(x = (fullSize.width * 0.25f).toInt(), y = 0)
        }

private fun settingsExit(): ExitTransition =
    fadeOut(tween(NAV_DURATION, easing = EmphasizedEasing)) +
        slideOut(tween(NAV_DURATION, easing = EmphasizedEasing)) { fullSize ->
            IntOffset(x = (fullSize.width * 0.25f).toInt(), y = 0)
        }

private fun settingsPopEnter(): EnterTransition =
    fadeIn(tween(NAV_DURATION, easing = EmphasizedEasing)) +
        slideIn(tween(NAV_DURATION, easing = EmphasizedEasing)) { fullSize ->
            IntOffset(x = -(fullSize.width * 0.08f).toInt(), y = 0)
        }

private fun settingsPopExit(): ExitTransition =
    fadeOut(tween(NAV_DURATION, easing = EmphasizedEasing)) +
        slideOut(tween(NAV_DURATION, easing = EmphasizedEasing)) { fullSize ->
            IntOffset(x = (fullSize.width * 0.12f).toInt(), y = 0)
        }

@Composable
fun LanternNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val haptics = rememberLanternHaptics()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = LanternDestination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            enterTransition = {
                when (targetState.destination.route) {
                    "settings", "app_whitelist" -> settingsEnter()
                    else -> EnterTransition.None
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    "settings", "app_whitelist" -> fadeOut(tween(NAV_DURATION, easing = EmphasizedEasing))
                    else -> ExitTransition.None
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    "settings", "app_whitelist" -> settingsPopEnter()
                    else -> EnterTransition.None
                }
            },
            popExitTransition = {
                when (initialState.destination.route) {
                    "settings", "app_whitelist" -> settingsPopExit()
                    else -> ExitTransition.None
                }
            },
        ) {
            composable(LanternDestination.Home.route) {
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
                    onBackClick = { navController.popBackStack() },
                    onNavigateToWhitelist = { navController.navigate("app_whitelist") }
                )
            }
            composable("app_whitelist") {
                AppWhitelistScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        val isSubScreen = currentDestination?.route == "settings" || currentDestination?.route == "app_whitelist"
        if (!isSubScreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
                    ),
                    modifier = Modifier.height(54.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LanternDestination.entries.forEach { destination ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == destination.route
                            } == true

                            val targetPillColor = MaterialTheme.colorScheme.primaryContainer
                            val pillBg by animateColorAsState(
                                targetValue = if (selected) targetPillColor else targetPillColor.copy(alpha = 0f),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "dockPillBg"
                            )

                            val contentColor by animateColorAsState(
                                targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "dockPillContent"
                            )

                            val pillHorizontalPadding by animateDpAsState(
                                targetValue = if (selected) 16.dp else 12.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "dockPillPadding"
                            )

                            Row(
                                modifier = Modifier
                                    .height(44.dp)
                                    .clip(CircleShape)
                                    .background(pillBg)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (!selected) {
                                            haptics.tap()
                                        }
                                        navController.navigate(destination.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(horizontal = pillHorizontalPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label,
                                    tint = contentColor,
                                    modifier = Modifier.size(20.dp)
                                )

                                AnimatedVisibility(
                                    visible = selected,
                                    enter = fadeIn(
                                        animationSpec = tween(180, delayMillis = 60, easing = EmphasizedEasing)
                                    ) + expandHorizontally(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    ),
                                    exit = fadeOut(
                                        animationSpec = tween(100, easing = EmphasizedEasing)
                                    ) + shrinkHorizontally(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = destination.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = contentColor,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
