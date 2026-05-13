package com.example.nammasantheledger.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nammasantheledger.core.designsystem.component.EmptyState
import com.example.nammasantheledger.core.designsystem.component.InitialsAvatar
import com.example.nammasantheledger.core.designsystem.component.SectionHeader
import com.example.nammasantheledger.core.designsystem.component.SummaryCard
import com.example.nammasantheledger.core.designsystem.component.TransactionTypeBadge
import com.example.nammasantheledger.core.designsystem.theme.*
import com.example.nammasantheledger.core.util.CurrencyUtil
import com.example.nammasantheledger.core.util.DateTimeUtil
import com.example.nammasantheledger.domain.model.Transaction
import com.example.nammasantheledger.domain.model.TransactionType
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun HomeScreen(
    onNavigateToCustomerDetail: (Long) -> Unit,
    onNavigateToAddTransaction: (Long?) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // ── Header ────────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { -it }
            ) {
                DashboardHeader()
            }
        }

        // ── Daily Summary Banner ──────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                DailySummaryBanner(
                    todayCredit = uiState.todayCredit,
                    totalOutstanding = uiState.totalOutstanding
                )
            }
        }

        // ── Summary Cards Row ─────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        SummaryCard(
                            title = "Total Outstanding",
                            value = CurrencyUtil.formatAmountCompact(uiState.totalOutstanding),
                            subtitle = "across all customers",
                            gradientStart = GradientStart1,
                            gradientEnd = GradientEnd1,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Active Customers",
                            value = uiState.activeCustomers.toString(),
                            subtitle = "with transactions",
                            gradientStart = GradientStart4,
                            gradientEnd = GradientEnd4,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        SummaryCard(
                            title = "Today's Credit",
                            value = CurrencyUtil.formatAmountCompact(uiState.todayCredit),
                            subtitle = "goods given",
                            gradientStart = GradientStart2,
                            gradientEnd = GradientEnd2,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Today's Payments",
                            value = CurrencyUtil.formatAmountCompact(uiState.todayPayment),
                            subtitle = "received",
                            gradientStart = GradientStart3,
                            gradientEnd = GradientEnd3,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ── Recent Transactions ───────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(Spacing.xs))
            SectionHeader(
                title = "Recent Transactions",
                actionText = if (uiState.recentTransactions.isNotEmpty()) "View All" else "",
                onActionClick = { /* Navigate to all transactions */ }
            )
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (uiState.recentTransactions.isEmpty()) {
            item {
                EmptyState(
                    message = "No transactions yet",
                    submessage = "Tap the + button to add your first transaction"
                )
            }
        } else {
            itemsIndexed(
                items = uiState.recentTransactions,
                key = { _, transaction -> transaction.id }
            ) { index, transaction ->
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onNavigateToCustomerDetail(transaction.customerId) }
                    )
                }
            }
        }

        // Bottom spacing for FAB
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun DashboardHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.lg)
    ) {
        Text(
            text = "Namma Santhe",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = DateTimeUtil.formatDateOnly(Date()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DailySummaryBanner(
    todayCredit: Double,
    totalOutstanding: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF9800),
                            Color(0xFFFF5722)
                        )
                    ),
                    shape = MaterialTheme.shapes.large
                )
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = Spacing.sm)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Summary",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Today you sold for ${CurrencyUtil.formatAmountCompact(todayCredit)}; " +
                                "Dues pending ${CurrencyUtil.formatAmountCompact(totalOutstanding)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.xxxs)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            InitialsAvatar(name = transaction.customerName, size = 44)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.customerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                ) {
                    TransactionTypeBadge(isCredit = transaction.type == TransactionType.CREDIT)
                    if (transaction.notes.isNotEmpty()) {
                        Text(
                            text = "• ${transaction.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = DateTimeUtil.formatRelative(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val isCredit = transaction.type == TransactionType.CREDIT
                val amountColor = if (isCredit) CreditRed else PaymentGreen
                val prefix = if (isCredit) "+" else "-"

                Text(
                    text = "$prefix${CurrencyUtil.formatPlain(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                Icon(
                    imageVector = if (isCredit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = if (isCredit) "Credit" else "Payment",
                    tint = amountColor,
                    modifier = Modifier.height(16.dp)
                )
            }
        }
    }
}
