package com.rentmanager.app.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rentmanager.app.data.entities.Payment
import com.rentmanager.app.data.entities.Tenant
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.ui.components.*
import com.rentmanager.app.ui.navigation.Screen
import com.rentmanager.app.ui.theme.*
import com.rentmanager.app.ui.utils.UiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(repository: RentRepository, navController: NavController) {
    val scope = rememberCoroutineScope()
    val now = Calendar.getInstance()
    var selectedMonth by remember { mutableStateOf(now.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(now.get(Calendar.YEAR)) }
    var filterMode by remember { mutableStateOf("month") } // "month" or "all"

    val monthPayments by repository.getPaymentsByMonthYear(selectedMonth, selectedYear)
        .collectAsStateWithLifecycle(emptyList())
    val allPayments by repository.allPayments.collectAsStateWithLifecycle(emptyList())
    val activeTenants by repository.allActiveTenants.collectAsStateWithLifecycle(emptyList())

    val displayed = if (filterMode == "month") monthPayments else allPayments

    // Tenant lookup map
    var tenantMap by remember { mutableStateOf<Map<Long, Tenant>>(emptyMap()) }
    LaunchedEffect(activeTenants) {
        tenantMap = activeTenants.associateBy { it.id }
    }

    fun prevMonth() {
        if (selectedMonth == 1) { selectedMonth = 12; selectedYear-- }
        else selectedMonth--
    }

    fun nextMonth() {
        if (selectedMonth == 12) { selectedMonth = 1; selectedYear++ }
        else selectedMonth++
    }

    val totalCollected = displayed.filter { it.status == "PAID" }.sumOf { it.paidAmount }
    val totalDue = displayed.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payments", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditPayment.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Payment", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // ── Filter tabs ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterMode == "month",
                    onClick = { filterMode = "month" },
                    label = { Text("By Month") }
                )
                FilterChip(
                    selected = filterMode == "all",
                    onClick = { filterMode = "all" },
                    label = { Text("All Payments") }
                )
            }

            // ── Month navigator ────────────────────────────────────────────
            if (filterMode == "month") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = ::prevMonth) {
                        Icon(Icons.Default.ChevronLeft, "Previous Month")
                    }
                    Text(
                        UiUtils.formatMonthYear(selectedMonth, selectedYear),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = ::nextMonth) {
                        Icon(Icons.Default.ChevronRight, "Next Month")
                    }
                }
            }

            // ── Summary bar ────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Collected", style = MaterialTheme.typography.labelSmall)
                        Text(UiUtils.formatCurrency(totalCollected),
                            fontWeight = FontWeight.Bold, color = GreenPaid)
                    }
                    VerticalDivider(modifier = Modifier.height(36.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Due", style = MaterialTheme.typography.labelSmall)
                        Text(UiUtils.formatCurrency(totalDue), fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider(modifier = Modifier.height(36.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Outstanding", style = MaterialTheme.typography.labelSmall)
                        Text(
                            UiUtils.formatCurrency(totalDue - totalCollected),
                            fontWeight = FontWeight.Bold,
                            color = if (totalDue - totalCollected > 0) RedOverdue else GreenPaid
                        )
                    }
                }
            }

            if (displayed.isEmpty()) {
                EmptyState(
                    "No payments for this period.",
                    Icons.Default.Payments
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayed, key = { it.id }) { payment ->
                        val tenant = tenantMap[payment.tenantId]
                        PaymentCard(payment, tenant, navController, scope, repository)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentCard(
    payment: Payment,
    tenant: Tenant?,
    navController: NavController,
    scope: kotlinx.coroutines.CoroutineScope,
    repository: RentRepository
) {
    val statusColor = when (payment.status) {
        "PAID" -> GreenPaid
        "OVERDUE" -> RedOverdue
        "PARTIAL" -> OrangeWarning
        else -> BluePending
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        tenant?.let { "${it.firstName} ${it.lastName}" } ?: "Tenant #${payment.tenantId}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        UiUtils.formatMonthYear(payment.month, payment.year),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(UiUtils.formatCurrency(payment.amount), fontWeight = FontWeight.Bold)
                    StatusChip(payment.status, statusColor)
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Due: ${UiUtils.formatDate(payment.dueDate)}",
                    style = MaterialTheme.typography.bodySmall)
                if (payment.paidDate != null)
                    Text("Paid: ${UiUtils.formatDate(payment.paidDate)}",
                        style = MaterialTheme.typography.bodySmall, color = GreenPaid)
            }

            // Quick-pay button for pending/overdue
            if (payment.status == "PENDING" || payment.status == "OVERDUE") {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                repository.updatePayment(
                                    payment.copy(
                                        status = "PAID",
                                        paidDate = System.currentTimeMillis(),
                                        paidAmount = payment.amount
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Mark Paid")
                    }
                    OutlinedButton(
                        onClick = {
                            navController.navigate(
                                Screen.AddEditPayment.createRoute(paymentId = payment.id)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit")
                    }
                }
            } else {
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = {
                    navController.navigate(Screen.AddEditPayment.createRoute(paymentId = payment.id))
                }) {
                    Icon(Icons.Default.Edit, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }
            }
        }
    }
}
