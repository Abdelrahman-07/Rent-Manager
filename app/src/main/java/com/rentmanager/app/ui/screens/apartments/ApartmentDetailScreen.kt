package com.rentmanager.app.ui.screens.apartments

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
import com.rentmanager.app.data.entities.Apartment
import com.rentmanager.app.data.entities.Tenant
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.ui.components.*
import com.rentmanager.app.ui.navigation.Screen
import com.rentmanager.app.ui.utils.UiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentDetailScreen(
    apartmentId: Long,
    repository: RentRepository,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var apartment by remember { mutableStateOf<Apartment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val tenants by repository.getTenantsByApartment(apartmentId)
        .collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(apartmentId) {
        apartment = repository.getApartmentById(apartmentId)
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete Apartment",
            message = "Are you sure you want to delete ${apartment?.name}? This cannot be undone.",
            onConfirm = {
                apartment?.let { apt ->
                    scope.launch(Dispatchers.IO) {
                        repository.deleteApartment(apt)
                    }
                }
                navController.popBackStack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(apartment?.name ?: "Apartment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.AddEditApartment.createRoute(apartmentId))
                    }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
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
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                apartment?.let { apt ->
                    Card(elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Details", style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                StatusChip(
                                    label = if (apt.isOccupied) "Occupied" else "Vacant",
                                    color = if (apt.isOccupied) Color(0xFF2E7D32) else Color(0xFF757575)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            InfoRow("Address", apt.address)
                            InfoRow("Floor", apt.floor)
                            InfoRow("Bedrooms", "${apt.bedrooms}")
                            InfoRow("Bathrooms", "${apt.bathrooms}")
                            InfoRow("Area", if (apt.areaSqm > 0) "${apt.areaSqm} m²" else "")
                            InfoRow("Monthly Rent", if (apt.monthlyRent > 0) UiUtils.formatCurrency(apt.monthlyRent) else "")
                            InfoRow("Amenities", apt.amenities)
                            InfoRow("Notes", apt.description)
                        }
                    }
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current Tenants", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        navController.navigate(Screen.AddEditTenant.createRoute())
                    }) {
                        Icon(Icons.Default.PersonAdd, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Tenant")
                    }
                }
            }

            if (tenants.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tenants assigned", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(tenants) { tenant ->
                    TenantMiniCard(tenant) {
                        navController.navigate(Screen.TenantDetail.createRoute(tenant.id))
                    }
                }
            }
        }
    }
}

@Composable
fun TenantMiniCard(tenant: Tenant, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TenantAvatar(tenant.photoUri, size = 44)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${tenant.firstName} ${tenant.lastName}", fontWeight = FontWeight.SemiBold)
                Text(tenant.phone, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}
