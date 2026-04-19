package com.rentmanager.app.ui.screens.apartments

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
import com.rentmanager.app.data.entities.Apartment
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.ui.components.EmptyState
import com.rentmanager.app.ui.components.StatusChip
import com.rentmanager.app.ui.navigation.Screen
import com.rentmanager.app.ui.theme.GreenPaid
import com.rentmanager.app.ui.theme.GreyVacant
import com.rentmanager.app.ui.utils.UiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentsScreen(repository: RentRepository, navController: NavController) {
    val apartments by repository.allApartments.collectAsStateWithLifecycle(emptyList())
    var searchQuery by remember { mutableStateOf("") }

    val filtered = apartments.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apartments", fontWeight = FontWeight.Bold) },
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
                onClick = { navController.navigate(Screen.AddEditApartment.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Apartment", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search apartments…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            if (filtered.isEmpty()) {
                EmptyState("No apartments yet. Tap + to add one.", Icons.Default.Apartment)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { apt ->
                        ApartmentCard(apt) {
                            navController.navigate(Screen.ApartmentDetail.createRoute(apt.id))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApartmentCard(apartment: Apartment, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(apartment.name, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                if (apartment.address.isNotBlank()) {
                    Text(apartment.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline)
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🛏 ${apartment.bedrooms}",
                        style = MaterialTheme.typography.bodySmall)
                    Text("🚿 ${apartment.bathrooms}",
                        style = MaterialTheme.typography.bodySmall)
                    if (apartment.areaSqm > 0)
                        Text("📐 ${apartment.areaSqm.toInt()} m²",
                            style = MaterialTheme.typography.bodySmall)
                }
                if (apartment.monthlyRent > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(UiUtils.formatCurrency(apartment.monthlyRent) + "/mo",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            StatusChip(
                label = if (apartment.isOccupied) "Occupied" else "Vacant",
                color = if (apartment.isOccupied) GreenPaid else GreyVacant
            )
        }
    }
}
