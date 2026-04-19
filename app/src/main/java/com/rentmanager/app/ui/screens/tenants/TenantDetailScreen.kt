package com.rentmanager.app.ui.screens.tenants

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rentmanager.app.data.entities.Apartment
import com.rentmanager.app.data.entities.Tenant
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.ui.components.*
import com.rentmanager.app.ui.navigation.Screen
import com.rentmanager.app.ui.theme.*
import com.rentmanager.app.ui.utils.UiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDetailScreen(
    tenantId: Long,
    repository: RentRepository,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var tenant by remember { mutableStateOf<Tenant?>(null) }
    var apartment by remember { mutableStateOf<Apartment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }

    val payments by repository.getPaymentsByTenant(tenantId)
        .collectAsStateWithLifecycle(emptyList())
    val totalPaid by repository.getTotalPaidByTenant(tenantId)
        .collectAsStateWithLifecycle(0.0)

    LaunchedEffect(tenantId) {
        tenant = repository.getTenantById(tenantId)
        tenant?.apartmentId?.let { apartment = repository.getApartmentById(it) }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete Tenant",
            message = "Permanently delete ${tenant?.firstName}? All payment records will be lost.",
            onConfirm = {
                tenant?.let { t ->
                    scope.launch(Dispatchers.IO) { repository.deleteTenant(t) }
                }
                navController.popBackStack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Move Out Tenant") },
            text = { Text("Mark ${tenant?.firstName} as moved out? This will free their apartment.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        repository.deactivateTenant(tenantId)
                        tenant?.apartmentId?.let { aptId ->
                            repository.getApartmentById(aptId)?.let { apt ->
                                repository.updateApartment(apt.copy(isOccupied = false))
                            }
                        }
                    }
                    showDeactivateDialog = false
                    navController.popBackStack()
                }) { Text("Move Out", color = OrangeWarning) }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tenant?.let { "${it.firstName} ${it.lastName}" } ?: "Tenant",
                    fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (tenant?.isActive == true) {
                        IconButton(onClick = { showDeactivateDialog = true }) {
                            Icon(Icons.Default.ExitToApp, "Move Out")
                        }
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.AddEditTenant.createRoute(tenantId))
                    }) { Icon(Icons.Default.Edit, "Edit") }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        tenant?.let { t ->
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Profile header ─────────────────────────────────────────
                item {
                    Card(elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TenantAvatar(t.photoUri, size = 90)
                            Spacer(Modifier.height(8.dp))
                            Text("${t.firstName} ${t.lastName}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold)
                            if (!t.isActive)
                                StatusChip("Inactive", Color(0xFF9E9E9E))
                            apartment?.let {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Apartment, null,
                                        Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.width(4.dp))
                                    Text(it.name, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }

                // ── Contact info ───────────────────────────────────────────
                item {
                    Card(elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Contact Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            InfoRow("Phone", t.phone)
                            InfoRow("Email", t.email)
                            InfoRow("National ID", t.nationalId)
                            InfoRow("Nationality", t.nationality)
                            if (t.emergencyContactName.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text("Emergency Contact",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline)
                                InfoRow("Name", t.emergencyContactName)
                                InfoRow("Phone", t.emergencyContactPhone)
                            }
                        }
                    }
                }

                // ── Contract info ──────────────────────────────────────────
                item {
                    val (contractLabel, contractStatus) = UiUtils.contractStatusLabel(t.contractEndDate)
                    val chipColor = when (contractStatus) {
                        UiUtils.ContractStatus.CRITICAL, UiUtils.ContractStatus.EXPIRED -> RedOverdue
                        UiUtils.ContractStatus.WARNING -> OrangeWarning
                        else -> GreenPaid
                    }
                    Card(elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Contract & Financials",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                if (t.contractEndDate > 0)
                                    StatusChip(contractLabel, chipColor)
                            }
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            InfoRow("Move-in Date", UiUtils.formatDate(t.moveInDate))
                            InfoRow("Contract Start", UiUtils.formatDate(t.contractStartDate))
                            InfoRow("Contract End", UiUtils.formatDate(t.contractEndDate))
                            InfoRow("Rent Due Day", "Day ${t.rentDueDay} of each month")
                            InfoRow("Monthly Rent", UiUtils.formatCurrency(t.monthlyRent))
                            InfoRow("Security Deposit",
                                "${UiUtils.formatCurrency(t.securityDeposit)} " +
                                        if (t.depositPaid) "(Paid)" else "(Unpaid)")
                            InfoRow("Total Paid", UiUtils.formatCurrency(totalPaid ?: 0.0))
                        }
                    }
                }

                // ── ID Photos ──────────────────────────────────────────────
                if (t.idPhotoFrontUri.isNotBlank() || t.idPhotoBackUri.isNotBlank()) {
                    item {
                        Card(elevation = CardDefaults.cardElevation(2.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("ID Documents",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (t.idPhotoFrontUri.isNotBlank()) {
                                        item { IdPhotoCard("Front", t.idPhotoFrontUri) }
                                    }
                                    if (t.idPhotoBackUri.isNotBlank()) {
                                        item { IdPhotoCard("Back", t.idPhotoBackUri) }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Notes ──────────────────────────────────────────────────
                if (t.notes.isNotBlank()) {
                    item {
                        Card(elevation = CardDefaults.cardElevation(2.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Notes", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                Text(t.notes, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // ── Payment history ────────────────────────────────────────
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payment History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        TextButton(onClick = {
                            navController.navigate(
                                Screen.AddEditPayment.createRoute(tenantId = tenantId)
                            )
                        }) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Payment")
                        }
                    }
                }

                if (payments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No payments recorded", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    items(payments) { payment ->
                        val statusColor = when (payment.status) {
                            "PAID" -> GreenPaid
                            "OVERDUE" -> RedOverdue
                            "PARTIAL" -> OrangeWarning
                            else -> BluePending
                        }
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = statusColor.copy(0.06f)
                            )
                        ) {
                            Row(
                                Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(UiUtils.formatMonthYear(payment.month, payment.year),
                                        fontWeight = FontWeight.Medium)
                                    Text("Due: ${UiUtils.formatDate(payment.dueDate)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline)
                                    if (payment.paidDate != null)
                                        Text("Paid: ${UiUtils.formatDate(payment.paidDate)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GreenPaid)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(UiUtils.formatCurrency(payment.amount),
                                        fontWeight = FontWeight.Bold)
                                    StatusChip(payment.status, statusColor)
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun IdPhotoCard(label: String, uri: String) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(uri).crossfade(true).build(),
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 160.dp, height = 100.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline)
    }
}
