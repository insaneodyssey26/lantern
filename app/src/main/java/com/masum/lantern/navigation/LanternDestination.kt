package com.masum.lantern.navigation

enum class LanternDestination(val route: String, val label: String) {
    Home("home", "Monitor"),
    LiveFeed("live_feed", "Live"),
    PerApp("per_app", "Apps"),
    History("history", "History"),
    Stats("stats", "Stats"),
}
