package com.example.nammasantheledger.feature.transaction

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nammasantheledger.core.designsystem.component.InitialsAvatar
import com.example.nammasantheledger.core.designsystem.theme.*
import com.example.nammasantheledger.domain.model.TransactionType

@Composable
fun AddTransactionScreen(
    preSelectedCustomerId: Long?,
    onNavigateBack: () -> Unit,
    onTransactionAdded: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current

    LaunchedEffect(preSelectedCustomerId) {
        viewModel.setPreSelectedCustomer(preSelectedCustomerId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            val result = snackbarHostState.showSnackbar(
                message = "✅ Transaction saved!",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoTransaction()
            } else {
                onTransactionAdded()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
          // ── Scrollable top section ─────────────────────────────
          Column(
              modifier = Modifier
                  .weight(1f)
                  .verticalScroll(rememberScrollState())
          ) {
            // ── Customer Selection ────────────────────────────────────
            if (uiState.selectedCustomer == null) {
                Text(
                    text = "Select Customer",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    items(
                        items = uiState.customers,
                        key = { it.id }
                    ) { customer ->
                        Card(
                            modifier = Modifier
                                .clickable { viewModel.selectCustomer(customer) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                InitialsAvatar(name = customer.name, size = 32)
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                // Selected customer chip
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                        .clickable {
                            // Deselect if no preselection
                            if (preSelectedCustomerId == null) {
                                viewModel.selectCustomer(uiState.selectedCustomer!!)
                            }
                        },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        InitialsAvatar(name = uiState.selectedCustomer!!.name, size = 40)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.selectedCustomer!!.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tap to change",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ── Transaction Type Toggle ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    selected = uiState.transactionType == TransactionType.CREDIT,
                    onClick = { viewModel.setTransactionType(TransactionType.CREDIT) },
                    label = {
                        Text(
                            "Credit (Udari)",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    modifier = Modifier.weight(1f).height(TouchTarget.minimum),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CreditRedLight,
                        selectedLabelColor = CreditRed
                    )
                )
                FilterChip(
                    selected = uiState.transactionType == TransactionType.PAYMENT,
                    onClick = { viewModel.setTransactionType(TransactionType.PAYMENT) },
                    label = {
                        Text(
                            "Payment Received",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    modifier = Modifier.weight(1f).height(TouchTarget.minimum),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PaymentGreenLight,
                        selectedLabelColor = PaymentGreen
                    )
                )
            }

            // ── Amount Display ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.transactionType == TransactionType.CREDIT)
                            "Credit Amount" else "Payment Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedContent(
                        targetState = uiState.amountText.ifEmpty { "0" },
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith
                                    (fadeOut() + scaleOut(targetScale = 1.2f))
                        },
                        label = "amount"
                    ) { amount ->
                        Text(
                            text = "₹$amount",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = if (amount.length > 7) 36.sp else 45.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.transactionType == TransactionType.CREDIT)
                                CreditRed else PaymentGreen
                        )
                    }
                }
            }

            // ── Quick Amount Buttons ─────────────────────────────────
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                listOf(50, 100, 200, 500, 1000, 2000).forEach { amount ->
                    AssistChip(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.setQuickAmount(amount)
                        },
                        label = {
                            Text(
                                "₹$amount",
                                fontWeight = FontWeight.Medium
                            )
                        },
                        modifier = Modifier.height(40.dp)
                    )
                }
            }

            // ── Notes Field ──────────────────────────────────────────
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChanged,
                placeholder = { Text("Add a note (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
          } // end scrollable top section

            // ── Custom Numeric Keypad ────────────────────────────────
            NumericKeypad(
                onKeyPress = { key ->
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    viewModel.onAmountKeyPress(key)
                },
                onConfirm = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.confirmTransaction()
                },
                isConfirmEnabled = uiState.selectedCustomer != null &&
                        uiState.amountText.isNotEmpty() &&
                        (uiState.amountText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.xs)
            )

            // Error display
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.xxs),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NumericKeypad(
    onKeyPress: (String) -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        key = key,
                        onClick = { onKeyPress(key) },
                        modifier = Modifier
                            .weight(1f)
                            .height(TouchTarget.comfortable)
                    )
                }
            }
        }

        // Confirm button
        val confirmColor by animateColorAsState(
            targetValue = if (isConfirmEnabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            label = "confirm_color"
        )

        ElevatedButton(
            onClick = onConfirm,
            enabled = isConfirmEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = confirmColor,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = "Confirm Transaction",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "keypad_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
