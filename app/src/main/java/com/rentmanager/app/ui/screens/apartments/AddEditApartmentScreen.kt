package com.rentmanager.app.ui.screens.apartments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rentmanager.app.data.entities.Apartment
import com.rentmanager.app.data.repository.RentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditApartmentScreen(
    apartmentId: Long?,
    repository: RentRepository,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val isEdit = apartmentId != null

    var name by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var bedrooms by remember { mutableStateOf("1") }
    var bathrooms by remember { mutableStateOf("1") }
    var area by remember { mutableStateOf("") }
    var monthlyRent by remember { mutableStateOf("") }
    var amenities by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isOccupied by remember { mutableStateOf(false) }

    LaunchedEffect(apartmentId) {
        if (apartmentId != null) {
            repository.getApartmentById(apartmentId)?.let { apt ->
                name = apt.name; floor = apt.floor; address = apt.address
                description = apt.description; bedrooms = apt.bedrooms.toString()
                bathrooms = apt.bathrooms.toString(); area = apt.areaSqm.let { if (it > 0) it.toString() else "" }
                monthlyRent = if (apt.monthlyRent > 0) apt.monthlyRent.toString() else ""
                amenities = apt.amenities; notes = apt.notes; isOccupied = apt.isOccupied
            }
        }
    }

    fun save() {
        if (name.isBlank()) return
        val apt = Apartment(
            id = apartmentId ?: 0,
            name = name.trim(), floor = floor.trim(), address = address.trim(),
            description = description.trim(),
            bedrooms = bedrooms.toIntOrNull() ?: 1, bathrooms = bathrooms.toIntOrNull() ?: 1,
            areaSqm = area.toDoubleOrNull() ?: 0.0,
            monthlyRent = monthlyRent.toDoubleOrNull() ?: 0.0,
            amenities = amenities.trim(), notes = notes.trim(), isOccupied = isOccupied
        )
        scope.launch(Dispatchers.IO) {
            if (isEdit) repository.updateApartment(apt) else repository.insertApartment(apt)
        }
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Apartment" else "New Apartment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = ::save) {
                        Icon(Icons.Default.Save, "Save")
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Basic Information", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Apartment Name *") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true)

            OutlinedTextField(value = floor, onValueChange = { floor = it },
                label = { Text("Floor") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(value = address, onValueChange = { address = it },
                label = { Text("Address") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = bedrooms, onValueChange = { bedrooms = it },
                    label = { Text("Bedrooms") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true)

                OutlinedTextField(value = bathrooms, onValueChange = { bathrooms = it },
                    label = { Text("Bathrooms") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true)
            }

            OutlinedTextField(value = area, onValueChange = { area = it },
                label = { Text("Area (m²)") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true)

            Spacer(Modifier.height(4.dp))
            Text("Financial", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(value = monthlyRent, onValueChange = { monthlyRent = it },
                label = { Text("Monthly Rent") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true)

            Spacer(Modifier.height(4.dp))
            Text("Additional Info", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(value = amenities, onValueChange = { amenities = it },
                label = { Text("Amenities (comma separated)") },
                placeholder = { Text("e.g. Parking, Balcony, AC") },
                modifier = Modifier.fillMaxWidth())

            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            OutlinedTextField(value = notes, onValueChange = { notes = it },
                label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = isOccupied, onCheckedChange = { isOccupied = it })
                Spacer(Modifier.width(8.dp))
                Text("Currently Occupied")
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = ::save, modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()) {
                Text(if (isEdit) "Save Changes" else "Add Apartment")
            }
        }
    }
}
