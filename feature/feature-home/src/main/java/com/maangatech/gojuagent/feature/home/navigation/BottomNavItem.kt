package com.maangatech.gojuagent.feature.home.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.graphics.vector.ImageVector

/** The four destinations reachable from the bottom bar — everything else is pushed on top. */
enum class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    DASHBOARD("dashboard", "Home", Icons.Filled.Home),
    CUSTOMERS("customers", "Customers", Icons.Filled.People),
    HISTORY("history", "History", Icons.Filled.History),
    SYNC("sync", "Sync", Icons.Filled.Sync),
}
