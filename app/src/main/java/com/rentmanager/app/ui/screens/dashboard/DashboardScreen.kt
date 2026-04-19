package com.rentmanager.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.rentmanager.app.data.entities.Tenant
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.ui.components.*
import com.rentmanager.app.ui.navigation.Screen
import com.rentmanager.app.ui.theme.*
import com.rentmanager.app.ui.utils.UiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(repository: RentRepository, navController: NavController) {
    val scope = rememberCoroutineScope()

    val totalApartments by repository.totalApartmentCount.collectAsStateWithLifecycle(0)
    val occupiedApartments by repository.occupiedApartmentCount.collectAsStateWithLifecycle(0)
    val activeTenants by repository.activeTenantCount.collectAsStateWithLifecycle(0)
    val pendingPayments by repository.pendingAndOverduePayments.collectAsStateWithLifecycle(emptyList())

    var contractAlerts by remember { mutableStateOf<List<Tenant>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val in30Days = now + 30L * 24 * 60 * 60 * 1000
            contractAlerts = repository.getTenantsWithContractEndingSoon(now, in30Days)
        }
    }

    val now = Calendar.getInstance()
    val currentMonth = now.get(Calendar.MONTH) + 1
    val currentYear = now.get(Calendar.YEAR)
    val unpaidThisMonth by repository.getUnpaidCountForMonth(currentMonth, currentYear)
        .collectAsStateWithLifecycle(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rent Manager", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Stats grid ─────────────────────────────────────────────────
            item {
                SectionHeader("Overview")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Flats",
                        value = "$totalApartments",
                        icon = Icons.Default.Apartment,
                        containerColor = Primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Occupied",
                        value = "$occupiedApartments",
                        icon = Icons.Default.MeetingRoom,
                        containerColor = Secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Tenants",
                        value = "$activeTenants",
                        icon = Icons.Default.People,
                        containerColor = Color(0xFF6A1B9A),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Unpaid",
                        value = "$unpaidThisMonth",
                        icon = Icons.Default.Warning,
                        containerColor = if (unpaidThisMonth > 0) RedOverdue else GreenPaid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Quick nav ──────────────────────────────────────────────────
            item {
                SectionHeader("Quick Access")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Apartments.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Apartment, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Apartments")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Tenants.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.People, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tenants")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Payments.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payments, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Payments")
                    }

                }

            }

            // ── Contract alerts ────────────────────────────────────────────
            if (contractAlerts.isNotEmpty()) {
                item { SectionHeader("⚠️ Contracts Expiring Soon") }
                items(contractAlerts) { tenant ->
                    val (label, status) = UiUtils.contractStatusLabel(tenant.contractEndDate)
                    val chipColor = when (status) {
                        UiUtils.ContractStatus.CRITICAL, UiUtils.ContractStatus.EXPIRED -> RedOverdue
                        UiUtils.ContractStatus.WARNING -> OrangeWarning
                        else -> GreenPaid
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = chipColor.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TenantAvatar(tenant.photoUri, size = 40)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "${tenant.firstName} ${tenant.lastName}",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            StatusChip(label = label, color = chipColor)
                        }
                    }
                }
            }

            // ── Pending payments ───────────────────────────────────────────
            if (pendingPayments.isNotEmpty()) {
                item { SectionHeader("💰 Pending / Overdue Payments") }
                items(pendingPayments.take(5)) { payment ->
                    val isOverdue = payment.status == "OVERDUE"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOverdue) RedOverdue.copy(0.07f) else BluePending.copy(0.07f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    UiUtils.formatMonthYear(payment.month, payment.year),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Due: ${UiUtils.formatDate(payment.dueDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    UiUtils.formatCurrency(payment.amount),
                                    fontWeight = FontWeight.Bold
                                )
                                StatusChip(
                                    label = payment.status,
                                    color = if (isOverdue) RedOverdue else BluePending
                                )
                            }
                        }
                    }
                }
                if (pendingPayments.size > 5) {
                    item {
                        TextButton(
                            onClick = { navController.navigate(Screen.Payments.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View all ${pendingPayments.size} pending payments →")
                        }
                    }
                }
            }
        }
    }
}
