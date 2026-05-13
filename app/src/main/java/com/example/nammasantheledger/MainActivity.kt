package com.example.nammasantheledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nammasantheledger.core.designsystem.theme.NammaSantheTheme
import com.example.nammasantheledger.data.local.UserPreferencesManager
import com.example.nammasantheledger.navigation.NammaSantheNavHost
import com.example.nammasantheledger.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by userPreferencesManager.isDarkMode
                .collectAsStateWithLifecycle(initialValue = false)

            // Determine start destination based on auth state
            val isLoggedIn = firebaseAuth.currentUser != null
            val startDestination: Screen = if (isLoggedIn) Screen.Home else Screen.Auth

            NammaSantheTheme(darkTheme = isDarkMode) {
                NammaSantheMainContent(startDestination = startDestination)
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun NammaSantheMainContent(startDestination: Screen = Screen.Home) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val bottomNavItems = remember {
        listOf(
            BottomNavItem("Home", Screen.Home, Icons.Filled.Home, Icons.Outlined.Home),
            BottomNavItem("Customers", Screen.Customers, Icons.Filled.People, Icons.Outlined.People),
            BottomNavItem("Add", Screen.AddTransaction(), Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
            BottomNavItem("Analytics", Screen.Analytics, Icons.Filled.Analytics, Icons.Outlined.Analytics),
            BottomNavItem("Settings", Screen.Settings, Icons.Filled.Settings, Icons.Outlined.Settings),
        )
    }

    // Determine if bottom bar should be visible (hidden on Auth screen)
    val showBottomBar = navBackStackEntry?.destination?.let { dest ->
        // Hide on Auth screen
        if (dest.hasRoute(Screen.Auth::class)) return@let false
        bottomNavItems.any { item ->
            dest.hasRoute(item.route::class)
        }
    } ?: (startDestination !is Screen.Auth)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = navBackStackEntry?.destination?.hasRoute(item.route::class) == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            NammaSantheNavHost(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}