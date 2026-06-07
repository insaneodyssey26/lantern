package com.masum.vigil.navigation

/** Navigation destinations for Vigil. Expanded as feature screens land. */
enum class VigilDestination(val route: String) {
    Home("home"),
    LiveFeed("live_feed"),
    PerApp("per_app"),
    History("history"),
    Stats("stats"),
}
