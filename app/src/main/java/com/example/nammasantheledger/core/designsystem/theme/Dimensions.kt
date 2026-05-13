package com.example.nammasantheledger.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing tokens for consistent layout throughout the app.
 * Uses a 4dp base grid system.
 */
object Spacing {
    val xxxs = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    val xxxxl = 48.dp
    val xxxxxl = 64.dp
}

/**
 * Elevation tokens following Material 3 elevation system.
 */
object Elevation {
    val none = 0.dp
    val xs = 1.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 12.dp
}

/**
 * Icon size tokens for consistent iconography.
 */
object IconSize {
    val sm = 16.dp
    val md = 20.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val avatar = 40.dp
    val avatarLg = 56.dp
}

/**
 * Touch target sizes for accessibility compliance.
 * Minimum 48dp as per Material Design guidelines.
 */
object TouchTarget {
    val minimum = 48.dp
    val comfortable = 56.dp
    val large = 64.dp
    val keypad = 72.dp
}
