package com.example.nammasantheledger.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization.
 * Each sealed class/object represents a destination in the nav graph.
 */

@Serializable
sealed class Screen {
    @Serializable
    data object Auth : Screen()

    @Serializable
    data object Home : Screen()

    @Serializable
    data object Customers : Screen()

    @Serializable
    data class CustomerDetail(val customerId: Long) : Screen()

    @Serializable
    data object AddCustomer : Screen()

    @Serializable
    data class EditCustomer(val customerId: Long) : Screen()

    @Serializable
    data class AddTransaction(val customerId: Long? = null) : Screen()

    @Serializable
    data object Analytics : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object Reminders : Screen()
}

/**
 * Bottom navigation items.
 */
enum class BottomNavItem(
    val route: Screen,
    val label: String,
    val iconName: String
) {
    HOME(Screen.Home, "Home", "home"),
    CUSTOMERS(Screen.Customers, "Customers", "people"),
    ADD(Screen.AddTransaction(), "Add", "add_circle"),
    ANALYTICS(Screen.Analytics, "Analytics", "analytics"),
    SETTINGS(Screen.Settings, "Settings", "settings")
}
