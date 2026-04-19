package com.rentmanager.app.ui.screens.tenants

import androidx.compose.foundation.clickable
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
import com.rentmanager.app.data.entities.Tenant
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.ui.components.*
import com.rentmanager.app.ui.navigation.Screen
import com.rentmanager.app.ui.theme.*
import com.rentmanager.app.ui.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantsScreen(repository: RentRepository, navController: NavController) {
    val tenants by repository.allActiveTenants.collectAsStateWithLifecycle(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var showInactive by remember { mutableStateOf(false) }
    val allTenants by repository.allTenants.collectAsStateWithLifecycle(emptyList())

    val source = if (showInactive) allTenants else tenants
    val filtered = source.filter {
        searchQuery.isBlank() ||
                "${it.firstName} ${it.lastName}".contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.nationalId.contains(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenants", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    FilterChip(
                        selected = showInactive,
                        onClick = { showInactive = !showInactive },
                        label = { Text("All", color = Color.White, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(0.2f),
                            selectedContainerColor = Color.White.copy(0.4f)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
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
                onClick = { navController.navigate(Screen.AddEditTenant.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, "Add Tenant", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search tenants…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            if (filtered.isEmpty()) {
                EmptyState("No tenants found.", Icons.Default.People)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { tenant ->
                        TenantCard(tenant) {
                            navController.navigate(Screen.TenantDetail.createRoute(tenant.id))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TenantCard(tenant: Tenant, onClick: () -> Unit) {
    val (contractLabel, contractStatus) = UiUtils.contractStatusLabel(tenant.contractEndDate)
    val chipColor = when (contractStatus) {
        UiUtils.ContractStatus.CRITICAL, UiUtils.ContractStatus.EXPIRED -> RedOverdue
        UiUtils.ContractStatus.WARNING -> OrangeWarning
        UiUtils.ContractStatus.OK -> GreenPaid
        else -> GreyVacant
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!tenant.isActive) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            TenantAvatar(tenant.photoUri, size = 52)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${tenant.firstName} ${tenant.lastName}", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    if (!tenant.isActive) {
                        Spacer(Modifier.width(6.dp))
                        StatusChip("Inactive", Color(0xFF9E9E9E))
                    }
                }
                if (tenant.phone.isNotBlank())
                    Text(tenant.phone, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (tenant.contractEndDate > 0)
                        StatusChip(contractLabel, chipColor)
                    if (tenant.monthlyRent > 0)
                        StatusChip(UiUtils.formatCurrency(tenant.monthlyRent), BluePending)
                }
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}
