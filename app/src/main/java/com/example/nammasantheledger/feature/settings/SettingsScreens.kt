package com.example.nammasantheledger.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nammasantheledger.core.designsystem.theme.Spacing
import com.google.firebase.auth.FirebaseAuth

// ═══════════════════════════════════════════════════════════════════════════
// Settings Screen
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit,
    onSignOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isDailyReminder by viewModel.isDailyReminderEnabled.collectAsStateWithLifecycle()
    val firebaseUser = remember { FirebaseAuth.getInstance().currentUser }
    val isLoggedIn = firebaseUser != null

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Column(
                modifier = Modifier.padding(
                    horizontal = Spacing.md,
                    vertical = Spacing.lg
                )
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Profile section
        item {
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "My Profile",
                    subtitle = "Shop name, owner details",
                    onClick = onNavigateToProfile,
                    showArrow = true
                )
            }
        }

        // Notifications
        item {
            SettingsSection(title = "Notifications") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily Reminder",
                    subtitle = "Remind to record transactions",
                    isChecked = isDailyReminder,
                    onToggle = { viewModel.toggleDailyReminder(it) }
                )
            }
        }

        // Appearance
        item {
            SettingsSection(title = "Appearance") {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Switch to dark theme",
                    isChecked = isDarkMode,
                    onToggle = { viewModel.toggleDarkMode(it) }
                )
            }
        }

        // Security
        item {
            SettingsSection(title = "Security") {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Change PIN",
                    subtitle = "Update your login PIN",
                    onClick = { /* TODO: Show change PIN dialog */ },
                    showArrow = true
                )
            }
        }

        // Cloud & Account
        item {
            SettingsSection(title = "Cloud & Account") {
                if (isLoggedIn) {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Signed in",
                        subtitle = firebaseUser?.email ?: "Account verified",
                        onClick = {},
                        showArrow = false
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                onSignOut()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Sign In",
                        subtitle = "Enable cloud backup & sync",
                        onClick = onSignOut, // Navigate to auth screen
                        showArrow = true
                    )
                }
            }
        }

        // About
        item {
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "2.0.0",
                    onClick = {},
                    showArrow = false
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    subtitle = "Tell vendors about this app",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT,
                                "Check out Namma Santhe Ledger - a free digital khata app for market vendors!")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    },
                    showArrow = true
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = Spacing.xs)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            fontWeight = FontWeight.SemiBold
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showArrow: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showArrow) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Profile Screen
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
    }

    var shopName by remember { mutableStateOf(prefs.getString("shopName", "") ?: "") }
    var ownerName by remember { mutableStateOf(prefs.getString("ownerName", "") ?: "") }
    var ownerPhone by remember { mutableStateOf(prefs.getString("ownerPhone", "") ?: "") }
    var ownerAddress by remember { mutableStateOf(prefs.getString("ownerAddress", "") ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.md)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Shop Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Owner Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = ownerPhone,
                onValueChange = { ownerPhone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = ownerAddress,
                onValueChange = { ownerAddress = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.weight(1f))

            androidx.compose.material3.Button(
                onClick = {
                    prefs.edit()
                        .putString("shopName", shopName.trim())
                        .putString("ownerName", ownerName.trim())
                        .putString("ownerPhone", ownerPhone.trim())
                        .putString("ownerAddress", ownerAddress.trim())
                        .apply()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = shopName.isNotBlank() && ownerName.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Save Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
