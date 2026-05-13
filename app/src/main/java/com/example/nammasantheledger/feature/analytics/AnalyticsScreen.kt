package com.example.nammasantheledger.feature.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nammasantheledger.core.designsystem.component.SummaryCard
import com.example.nammasantheledger.core.designsystem.theme.*
import com.example.nammasantheledger.core.util.CurrencyUtil
import kotlinx.coroutines.delay

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item { Spacer(modifier = Modifier.height(Spacing.sm)) }

        // Header
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { -it }
            ) {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overview card
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                SummaryCard(
                    title = "Total Outstanding",
                    value = CurrencyUtil.formatPlain(uiState.totalOutstanding),
                    subtitle = "across all customers",
                    gradientStart = GradientStart1,
                    gradientEnd = GradientEnd1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Weekly summary
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                SummarySection(
                    title = "This Week",
                    credit = uiState.weeklyCredit,
                    payment = uiState.weeklyPayment
                )
            }
        }

        // Monthly summary
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                SummarySection(
                    title = "This Month",
                    credit = uiState.monthlyCredit,
                    payment = uiState.monthlyPayment
                )
            }
        }

        // Repayment chart
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically { it }
            ) {
                RepaymentChart(
                    credit = uiState.monthlyCredit,
                    payment = uiState.monthlyPayment
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SummarySection(
    title: String,
    credit: Double,
    payment: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Credit Given",
                    value = CurrencyUtil.formatPlain(credit),
                    color = CreditRed
                )
                StatItem(
                    label = "Payments Received",
                    value = CurrencyUtil.formatPlain(payment),
                    color = PaymentGreen
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))

            // Simple bar chart
            val total = credit + payment
            if (total > 0) {
                val creditRatio = (credit / total).toFloat()
                val animatedRatio = remember { Animatable(0f) }
                LaunchedEffect(creditRatio) {
                    animatedRatio.animateTo(creditRatio, animationSpec = tween(800))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(MaterialTheme.shapes.small)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(animatedRatio.value.coerceAtLeast(0.01f))
                            .height(12.dp)
                            .background(CreditRed)
                    )
                    Box(
                        modifier = Modifier
                            .weight((1f - animatedRatio.value).coerceAtLeast(0.01f))
                            .height(12.dp)
                            .background(PaymentGreen)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CreditRed)
                        )
                        Text("Credit", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PaymentGreen)
                        )
                        Text("Payment", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun RepaymentChart(
    credit: Double,
    payment: Double
) {
    val repaymentRate = if (credit > 0) (payment / credit * 100).coerceAtMost(100.0) else 0.0
    val animatedAngle = remember { Animatable(0f) }

    LaunchedEffect(repaymentRate) {
        animatedAngle.animateTo(
            (repaymentRate / 100 * 360).toFloat(),
            animationSpec = tween(1200)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Monthly Repayment Rate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 20.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = Offset(
                        (size.width - radius * 2) / 2,
                        (size.height - radius * 2) / 2
                    )

                    // Background arc
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    drawArc(
                        color = if (repaymentRate >= 50) PaymentGreen else CreditRed,
                        startAngle = -90f,
                        sweepAngle = animatedAngle.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.0f", repaymentRate)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (repaymentRate >= 50) PaymentGreen else CreditRed
                    )
                    Text(
                        text = "collected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
