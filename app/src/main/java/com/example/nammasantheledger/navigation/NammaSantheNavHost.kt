package com.example.nammasantheledger.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.nammasantheledger.feature.analytics.AnalyticsScreen
import com.example.nammasantheledger.feature.auth.AuthScreen
import com.example.nammasantheledger.feature.customer.AddCustomerScreen
import com.example.nammasantheledger.feature.customer.CustomerDetailScreen
import com.example.nammasantheledger.feature.customer.CustomersScreen
import com.example.nammasantheledger.feature.customer.EditCustomerScreen
import com.example.nammasantheledger.feature.home.HomeScreen
import com.example.nammasantheledger.feature.settings.ProfileScreen
import com.example.nammasantheledger.feature.settings.SettingsScreen
import com.example.nammasantheledger.feature.transaction.AddTransactionScreen

private const val TRANSITION_DURATION = 350

@Composable
fun NammaSantheNavHost(
    navController: NavHostController,
    startDestination: Screen = Screen.Home,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(TRANSITION_DURATION)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_DURATION)
            )
        },
        exitTransition = {
            fadeOut(tween(TRANSITION_DURATION)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_DURATION)
            )
        },
        popEnterTransition = {
            fadeIn(tween(TRANSITION_DURATION)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_DURATION)
            )
        },
        popExitTransition = {
            fadeOut(tween(TRANSITION_DURATION)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_DURATION)
            )
        }
    ) {
        composable<Screen.Auth> {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Auth) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail(customerId))
                },
                onNavigateToAddTransaction = { customerId ->
                    navController.navigate(Screen.AddTransaction(customerId))
                }
            )
        }

        composable<Screen.Customers> {
            CustomersScreen(
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail(customerId))
                },
                onNavigateToAddCustomer = {
                    navController.navigate(Screen.AddCustomer)
                }
            )
        }

        composable<Screen.CustomerDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.CustomerDetail>()
            CustomerDetailScreen(
                customerId = route.customerId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditCustomer = { customerId ->
                    navController.navigate(Screen.EditCustomer(customerId))
                },
                onNavigateToAddTransaction = { customerId ->
                    navController.navigate(Screen.AddTransaction(customerId))
                }
            )
        }

        composable<Screen.AddCustomer> {
            AddCustomerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.EditCustomer> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.EditCustomer>()
            EditCustomerScreen(
                customerId = route.customerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.AddTransaction> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AddTransaction>()
            AddTransactionScreen(
                preSelectedCustomerId = route.customerId,
                onNavigateBack = { navController.popBackStack() },
                onTransactionAdded = { navController.popBackStack() }
            )
        }

        composable<Screen.Analytics> {
            AnalyticsScreen()
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile)
                },
                onSignOut = {
                    navController.navigate(Screen.Auth) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
