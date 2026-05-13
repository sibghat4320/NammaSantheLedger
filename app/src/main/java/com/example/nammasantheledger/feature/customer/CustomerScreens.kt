package com.example.nammasantheledger.feature.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nammasantheledger.core.designsystem.component.EmptyState
import com.example.nammasantheledger.core.designsystem.component.InitialsAvatar
import com.example.nammasantheledger.core.designsystem.component.SummaryCard
import com.example.nammasantheledger.core.designsystem.component.TransactionTypeBadge
import com.example.nammasantheledger.core.designsystem.theme.*
import com.example.nammasantheledger.core.util.CurrencyUtil
import com.example.nammasantheledger.core.util.DateTimeUtil
import com.example.nammasantheledger.domain.model.Customer
import com.example.nammasantheledger.domain.model.TransactionType

// ═══════════════════════════════════════════════════════════════════════════
// Customers List Screen
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun CustomersScreen(
    onNavigateToCustomerDetail: (Long) -> Unit,
    onNavigateToAddCustomer: () -> Unit,
    viewModel: CustomersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustomer,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Customer",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    placeholder = { Text("Search by name or phone...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
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
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.customers.isEmpty()) {
                item {
                    EmptyState(
                        message = if (uiState.searchQuery.isNotEmpty())
                            "No customers found for \"${uiState.searchQuery}\""
                        else
                            "No customers yet",
                        submessage = "Tap + to add your first customer"
                    )
                }
            } else {
                items(
                    items = uiState.customers,
                    key = { it.id }
                ) { customer ->
                    CustomerItem(
                        customer = customer,
                        onClick = { onNavigateToCustomerDetail(customer.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CustomerItem(
    customer: Customer,
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
            InitialsAvatar(name = customer.name, size = 48)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (customer.phoneNumber.isNotEmpty()) {
                    Text(
                        text = customer.phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val balanceColor = if (customer.outstandingBalance > 0) CreditRed else PaymentGreen
                Text(
                    text = CurrencyUtil.formatPlain(customer.outstandingBalance),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
                Text(
                    text = if (customer.outstandingBalance > 0) "Due" else "Clear",
                    style = MaterialTheme.typography.bodySmall,
                    color = balanceColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Customer Detail Screen
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun CustomerDetailScreen(
    customerId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditCustomer: (Long) -> Unit,
    onNavigateToAddTransaction: (Long) -> Unit,
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.customer?.name ?: "Customer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEditCustomer(customerId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddTransaction(customerId) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Balance card
                item {
                    val balance = uiState.balance
                    SummaryCard(
                        title = "Outstanding Balance",
                        value = CurrencyUtil.formatPlain(balance),
                        subtitle = if (balance > 0) "Amount due from customer"
                                   else "All dues cleared ✓",
                        gradientStart = if (balance > 0) GradientStart2 else GradientStart3,
                        gradientEnd = if (balance > 0) GradientEnd2 else GradientEnd3,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }

                // Customer info
                item {
                    uiState.customer?.let { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(Spacing.md)) {
                                if (customer.phoneNumber.isNotEmpty()) {
                                    DetailRow("Phone", customer.phoneNumber)
                                }
                                if (customer.address.isNotEmpty()) {
                                    DetailRow("Address", customer.address)
                                }
                                if (customer.notes.isNotEmpty()) {
                                    DetailRow("Notes", customer.notes)
                                }
                                DetailRow("Customer since",
                                    DateTimeUtil.formatDateOnly(java.util.Date(customer.createdAt)))
                            }
                        }
                    }
                }

                // Send Reminder buttons - shown only when customer has dues and phone
                item {
                    uiState.customer?.let { customer ->
                        if (uiState.balance > 0 && customer.phoneNumber.isNotEmpty()) {
                            val context = LocalContext.current
                            val formattedAmount = CurrencyUtil.formatPlain(uiState.balance)
                            val message = "Hi ${customer.name}, " +
                                    "this is a friendly reminder that you have a pending due of $formattedAmount " +
                                    "at Namma Santhe. Please clear it at your earliest convenience. Thank you! \uD83D\uDE4F"

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                Text(
                                    text = "Send Reminder via",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = Spacing.xxs)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                ) {
                                    // SMS Button
                                    Button(
                                        onClick = {
                                            val smsUri = Uri.parse("smsto:${customer.phoneNumber}")
                                            val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                                                putExtra("sms_body", message)
                                            }
                                            context.startActivity(smsIntent)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(TouchTarget.minimum),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1976D2),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.xxs))
                                        Text("SMS", style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold)
                                    }

                                    // Telegram Button
                                    Button(
                                        onClick = {
                                            val phone = customer.phoneNumber
                                                .replace("[^0-9]".toRegex(), "")
                                                .let { if (it.length == 10) "91$it" else it }
                                            try {
                                                val tgIntent = Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("tg://msg?to=$phone&text=${Uri.encode(message)}")
                                                )
                                                context.startActivity(tgIntent)
                                            } catch (_: Exception) {
                                                // Fallback to Telegram web
                                                val webIntent = Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("https://t.me/+$phone?text=${Uri.encode(message)}")
                                                )
                                                context.startActivity(webIntent)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(TouchTarget.minimum),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF0088CC),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("✈", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(Spacing.xxs))
                                        Text("Telegram", style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold)
                                    }

                                    // WhatsApp Button
                                    Button(
                                        onClick = {
                                            com.example.nammasantheledger.feature.reminders
                                                .WhatsAppReminderHelper.sendWhatsAppReminder(
                                                    context, customer.phoneNumber, message
                                                )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(TouchTarget.minimum),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF25D366),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("💬", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(Spacing.xxs))
                                        Text("WhatsApp", style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Transaction history header
                item {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }

                if (uiState.transactions.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No transactions yet",
                            submessage = "Tap + to record a transaction"
                        )
                    }
                } else {
                    items(
                        items = uiState.transactions,
                        key = { it.id }
                    ) { transaction ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.md, vertical = Spacing.xxxs),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                    ) {
                                        TransactionTypeBadge(
                                            isCredit = transaction.type == TransactionType.CREDIT
                                        )
                                        if (transaction.notes.isNotEmpty()) {
                                            Text(
                                                text = transaction.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = DateTimeUtil.formatRelative(transaction.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }

                                val isCredit = transaction.type == TransactionType.CREDIT
                                Text(
                                    text = "${if (isCredit) "+" else "-"}${CurrencyUtil.formatPlain(transaction.amount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCredit) CreditRed else PaymentGreen
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Add Customer Screen
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AddCustomerScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddCustomerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    CustomerFormContent(
        title = "Add Customer",
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onPhoneChanged = viewModel::onPhoneChanged,
        onAddressChanged = viewModel::onAddressChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onSave = viewModel::saveCustomer,
        onNavigateBack = onNavigateBack,
        saveButtonText = "Add Customer"
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// Edit Customer Screen
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun EditCustomerScreen(
    customerId: Long,
    onNavigateBack: () -> Unit,
    viewModel: EditCustomerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    CustomerFormContent(
        title = "Edit Customer",
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onPhoneChanged = viewModel::onPhoneChanged,
        onAddressChanged = viewModel::onAddressChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onSave = viewModel::updateCustomer,
        onNavigateBack = onNavigateBack,
        saveButtonText = "Save Changes"
    )
}

@Composable
private fun CustomerFormContent(
    title: String,
    uiState: CustomerFormUiState,
    onNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    saveButtonText: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                value = uiState.name,
                onValueChange = onNameChanged,
                label = { Text("Customer Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null,
                supportingText = uiState.error?.let { { Text(it) } },
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = onPhoneChanged,
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = uiState.address,
                onValueChange = onAddressChanged,
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChanged,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.weight(1f))

            androidx.compose.material3.Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TouchTarget.comfortable),
                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = saveButtonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
