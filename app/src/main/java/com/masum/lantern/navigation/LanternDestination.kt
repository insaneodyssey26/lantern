package com.masum.lantern.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.ui.graphics.vector.ImageVector

enum class LanternDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home(
        route = "home",
        label = "Home",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    Traffic(
        route = "traffic",
        label = "Traffic",
        selectedIcon = Icons.Rounded.SwapVert,
        unselectedIcon = Icons.Outlined.SwapVert
    ),
    Insights(
        route = "insights",
        label = "Insights",
        selectedIcon = Icons.Rounded.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    ),
}
