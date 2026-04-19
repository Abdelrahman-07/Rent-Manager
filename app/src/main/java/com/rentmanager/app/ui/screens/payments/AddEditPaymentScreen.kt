package com.rentmanager.app.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.rentmanager.app.data.entities.Payment
import com.rentmanager.app.data.entities.PaymentStatus
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.ui.screens.tenants.DatePickerField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPaymentScreen(
    paymentId: Long?,
    preselectedTenantId: Long?,
    repository: RentRepository,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val isEdit = paymentId != null

    val now = Calendar.getInstance()
    var selectedTenantId by remember { mutableStateOf(preselectedTenantId) }
    var amount by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var paidDate by remember { mutableStateOf(0L) }
    var month by remember { mutableStateOf(now.get(Calendar.MONTH) + 1) }
    var year by remember { mutableStateOf(now.get(Calendar.YEAR)) }
    var status by remember { mutableStateOf(PaymentStatus.PENDING.name) }
    var notes by remember { mutableStateOf("") }

    val tenants by repository.allActiveTenants.collectAsStateWithLifecycle(emptyList())
    var tenantExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val selectedTenantName = tenants.find { it.id == selectedTenantId }
        ?.let { "${it.firstName} ${it.lastName}" } ?: "Select Tenant"

    LaunchedEffect(paymentId) {
        if (paymentId != null) {
            repository.getPaymentById(paymentId)?.let { p ->
                selectedTenantId = p.tenantId
                amount = p.amount.toString()
                paidAmount = if (p.paidAmount > 0) p.paidAmount.toString() else ""
                dueDate = p.dueDate
                paidDate = p.paidDate ?: 0L
                month = p.month; year = p.year
                status = p.status; notes = p.notes
            }
        } else if (preselectedTenantId != null) {
            repository.getTenantById(preselectedTenantId)?.let { t ->
                amount = if (t.monthlyRent > 0) t.monthlyRent.toString() else ""
            }
        }
    }

    fun save() {
        val tenantId = selectedTenantId ?: return
        val amountVal = amount.toDoubleOrNull() ?: return

        val tenant = tenants.find { it.id == tenantId }
        val payment = Payment(
            id = paymentId ?: 0,
            tenantId = tenantId,
            apartmentId = tenant?.apartmentId ?: 0L,
            amount = amountVal,
            dueDate = dueDate,
            paidDate = if (paidDate > 0L) paidDate else null,
            paidAmount = paidAmount.toDoubleOrNull() ?: 0.0,
            month = month, year = year,
            status = status, notes = notes.trim()
        )
        scope.launch(Dispatchers.IO) {
            if (isEdit) repository.updatePayment(payment)
            else repository.insertPayment(payment)
        }
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Payment" else "New Payment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = ::save) { Icon(Icons.Default.Save, "Save") }
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Tenant picker ─────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = tenantExpanded,
                onExpandedChange = { tenantExpanded = !tenantExpanded }
            ) {
                OutlinedTextField(
                    value = selectedTenantName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tenant *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tenantExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = tenantExpanded,
                    onDismissRequest = { tenantExpanded = false }
                ) {
                    tenants.forEach { tenant ->
                        DropdownMenuItem(
                            text = { Text("${tenant.firstName} ${tenant.lastName}") },
                            onClick = {
                                selectedTenantId = tenant.id
                                if (amount.isBlank() && tenant.monthlyRent > 0)
                                    amount = tenant.monthlyRent.toString()
                                tenantExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Month/year ────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = month.toString(),
                    onValueChange = { month = it.toIntOrNull()?.coerceIn(1, 12) ?: month },
                    label = { Text("Month") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = year.toString(),
                    onValueChange = { year = it.toIntOrNull() ?: year },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f), singleLine = true
                )
            }

            // ── Amounts ───────────────────────────────────────────────────
            OutlinedTextField(value = amount, onValueChange = { amount = it },
                label = { Text("Amount Due *") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)

            OutlinedTextField(value = paidAmount, onValueChange = { paidAmount = it },
                label = { Text("Amount Paid") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)

            // ── Dates ─────────────────────────────────────────────────────
            DatePickerField("Due Date", dueDate) { dueDate = it }
            DatePickerField("Paid Date (leave empty if unpaid)", paidDate) { paidDate = it }

            // ── Status ────────────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    PaymentStatus.values().forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.name) },
                            onClick = { status = s.name; statusExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(value = notes, onValueChange = { notes = it },
                label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = ::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedTenantId != null && amount.isNotBlank()
            ) {
                Text(if (isEdit) "Save Changes" else "Add Payment")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
