package com.masum.lantern.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.masum.lantern.core.ui.haptics.rememberLanternHaptics
import com.masum.lantern.feature.control.ControlScreen
import com.masum.lantern.feature.domainrules.DomainRulesScreen
import com.masum.lantern.feature.livefeed.FeedFilter
import com.masum.lantern.feature.livefeed.LiveFeedScreen
import com.masum.lantern.feature.onboarding.OnboardingScreen
import com.masum.lantern.feature.settings.SettingsScreen
import com.masum.lantern.feature.stats.StatsScreen
import com.masum.lantern.feature.stats.detail.SessionDetailScreen
import com.masum.lantern.feature.whitelist.AppWhitelistScreen
import com.masum.lantern.feature.whatsnew.WhatsNewBottomSheet

private val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
private val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private const val NAV_ENTER_DURATION = 450
private const val NAV_EXIT_DURATION = 380
private const val TAB_TRANSITION_DURATION = 320
private const val TAB_SLIDE_FRACTION = 0.15f

private fun isSubScreenRoute(route: String?): Boolean =
    route in listOf("settings", "app_whitelist", "domain_rules", "onboarding", "whats_new") || route?.startsWith("session_detail/") == true

private fun destinationOrder(route: String?): Int = when (route) {
    LanternDestination.Home.route -> 0
    LanternDestination.Traffic.route -> 1
    LanternDestination.Insights.route -> 2
    else -> -1
}

@Composable
fun LanternNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = LanternDestination.Home.route,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val haptics = rememberLanternHaptics()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        var trafficInitialFilter by remember { mutableStateOf(FeedFilter.All) }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            enterTransition = {
                val targetOrder = destinationOrder(targetState.destination.route)
                val initialOrder = destinationOrder(initialState.destination.route)
                when {
                    initialState.destination.route == "onboarding" -> {
                        fadeIn(
                            animationSpec = tween(520, easing = EmphasizedDecelerateEasing)
                        ) + scaleIn(
                            initialScale = 0.92f,
                            animationSpec = tween(520, easing = EmphasizedDecelerateEasing)
                        )
                    }
                    isSubScreenRoute(targetState.destination.route) -> {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(NAV_ENTER_DURATION, easing = EmphasizedDecelerateEasing),
                            initialOffset = { (it * 0.45f).toInt() }
                        ) + scaleIn(
                            initialScale = 0.90f,
                            animationSpec = tween(NAV_ENTER_DURATION, easing = EmphasizedDecelerateEasing)
                        ) + fadeIn(animationSpec = tween(NAV_ENTER_DURATION / 2, easing = EmphasizedDecelerateEasing))
                    }
                    targetOrder != -1 && initialOrder != -1 && targetOrder != initialOrder -> {
                        val slideDirection = if (targetOrder > initialOrder) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        slideIntoContainer(
                            towards = slideDirection,
                            animationSpec = tween(TAB_TRANSITION_DURATION, easing = EmphasizedEasing),
                            initialOffset = { (it * TAB_SLIDE_FRACTION).toInt() }
                        ) + fadeIn(animationSpec = tween(TAB_TRANSITION_DURATION, easing = EmphasizedEasing))
                    }
                    else -> EnterTransition.None
                }
            },
            exitTransition = {
                val targetOrder = destinationOrder(targetState.destination.route)
                val initialOrder = destinationOrder(initialState.destination.route)
                when {
                    initialState.destination.route == "onboarding" -> {
                        fadeOut(
                            animationSpec = tween(380, easing = EmphasizedEasing)
                        ) + scaleOut(
                            targetScale = 1.05f,
                            animationSpec = tween(380, easing = EmphasizedEasing)
                        )
                    }
                    isSubScreenRoute(targetState.destination.route) -> {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(NAV_EXIT_DURATION, easing = EmphasizedEasing),
                            targetOffset = { (it * 0.18f).toInt() }
                        ) + scaleOut(
                            targetScale = 0.94f,
                            animationSpec = tween(NAV_EXIT_DURATION, easing = EmphasizedEasing)
                        ) + fadeOut(animationSpec = tween(NAV_EXIT_DURATION, easing = EmphasizedEasing))
                    }
                    targetOrder != -1 && initialOrder != -1 && targetOrder != initialOrder -> {
                        val slideDirection = if (targetOrder > initialOrder) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        slideOutOfContainer(
                            towards = slideDirection,
                            animationSpec = tween(TAB_TRANSITION_DURATION, easing = EmphasizedEasing),
                            targetOffset = { (it * TAB_SLIDE_FRACTION).toInt() }
                        ) + fadeOut(animationSpec = tween(TAB_TRANSITION_DURATION, easing = EmphasizedEasing))
                    }
                    else -> ExitTransition.None
                }
            },
            popEnterTransition = {
                val targetOrder = destinationOrder(targetState.destination.route)
                val initialOrder = destinationOrder(initialState.destination.route)
                when {
                    isSubScreenRoute(initialState.destination.route) -> {
                        scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(NAV_ENTER_DURATION, easing = EmphasizedDecelerateEasing)
                        ) + fadeIn(animationSpec = tween(NAV_ENTER_DURATION / 2, easing = EmphasizedDecelerateEasing))
                    }
                    targetOrder != -1 && initialOrder != -1 && targetOrder != initialOrder -> {
                        val slideDirection = if (targetOrder > initialOrder) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        slideIntoContainer(
                            towards = slideDirection,
                            animationSpec = tween(TAB_TRANSITION_DURATION, easing = EmphasizedEasing),
                            initialOffset = { (it * TAB_SLIDE_FRACTION).toInt() }
                        ) + fadeIn(animationSpec = tween(TAB_TRANSITION_DURATION, easing = EmphasizedEasing))
                    }
                    else -> EnterTransition.None
                }
            },
            popExitTransition = {
                val targetOrder = destinationOrder(targetState.destination.route)
                val initialOrder = destinationOrder(initialState.destination.route)
                when {
                    isSubScreenRoute(initialState.destination.route) -> {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(NAV_EXIT_DURATION, easing = EmphasizedEasing),
                            targetOffset = { it }
                        ) + fadeOut(animationSpec = tween(NAV_EXIT_DURATION / 2, easing = EmphasizedEasing))
                    }
                    targetOrder != -1 && initialOrder != -1 && targetOrder != initialOrder -> {
                        val slideDirection = if (targetOrder > initialOrder) {
                            AnimatedContentTransitionScope.SlideDirection.Start
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.End
                        }
                        slideOutOfContainer(
                            towards = slideDirection,
                            animationSpec = tween(TAB_TRANSITION_DURATION, easing = EmphasizedEasing),
                            targetOffset = { (it * TAB_SLIDE_FRACTION).toInt() }
                        ) + fadeOut(animationSpec = tween(TAB_TRANSITION_DURATION, easing = EmphasizedEasing))
                    }
                    else -> ExitTransition.None
                }
            },
        ) {
            composable(LanternDestination.Home.route) {
                ControlScreen(
                    onSettingsClick = { navController.navigate("settings") },
                    onNavigateToTraffic = { filterName ->
                        trafficInitialFilter = when (filterName) {
                            "Trackers" -> FeedFilter.Trackers
                            "Clean" -> FeedFilter.Clean
                            else -> FeedFilter.All
                        }
                        navController.navigate(LanternDestination.Traffic.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToInsights = {
                        navController.navigate(LanternDestination.Insights.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToDomainRules = {
                        navController.navigate("domain_rules")
                    }
                )
            }
            composable(LanternDestination.Traffic.route) {
                BackHandler {
                    navController.navigate(LanternDestination.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                LiveFeedScreen(
                    onSettingsClick = { navController.navigate("settings") },
                    initialFilter = trafficInitialFilter
                )
            }
            composable(LanternDestination.Insights.route) {
                BackHandler {
                    navController.navigate(LanternDestination.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                StatsScreen(
                    onSettingsClick = { navController.navigate("settings") },
                    onSessionClick = { sessionId ->
                        navController.navigate("session_detail/$sessionId")
                    }
                )
            }
            composable(
                route = "session_detail/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) {
                SessionDetailScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToWhitelist = { navController.navigate("app_whitelist") },
                    onNavigateToDomainRules = { navController.navigate("domain_rules") },
                    onNavigateToWhatsNew = { navController.navigate("whats_new") }
                )
            }
            composable("app_whitelist") {
                AppWhitelistScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("domain_rules") {
                DomainRulesScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("whats_new") {
                WhatsNewBottomSheet(
                    onDismiss = { navController.popBackStack() }
                )
            }
            composable("onboarding") {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(LanternDestination.Home.route) {
                            popUpTo("onboarding") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        val isSubScreen = isSubScreenRoute(currentDestination?.route)
        AnimatedVisibility(
            visible = !isSubScreen,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            enter = slideInVertically(
                animationSpec = tween(NAV_ENTER_DURATION, easing = EmphasizedDecelerateEasing),
                initialOffsetY = { it * 2 }
            ) + fadeIn(animationSpec = tween(NAV_ENTER_DURATION, easing = EmphasizedDecelerateEasing)),
            exit = slideOutVertically(
                animationSpec = tween(250, easing = EmphasizedEasing),
                targetOffsetY = { it * 2 }
            ) + fadeOut(animationSpec = tween(200))
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
                modifier = Modifier
                    .clip(CircleShape)
                    .height(54.dp)
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
