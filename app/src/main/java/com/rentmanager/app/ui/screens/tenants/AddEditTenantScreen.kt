package com.rentmanager.app.ui.screens.tenants

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rentmanager.app.data.entities.Tenant
import com.rentmanager.app.data.repository.RentRepository
import com.rentmanager.app.ui.components.TenantAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTenantScreen(
    tenantId: Long?,
    repository: RentRepository,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEdit = tenantId != null

    // ── Form state ────────────────────────────────────────────────────────────
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf("") }
    var idPhotoFrontUri by remember { mutableStateOf("") }
    var idPhotoBackUri by remember { mutableStateOf("") }
    var apartmentId by remember { mutableStateOf<Long?>(null) }
    var contractStartDate by remember { mutableLongStateOf(0L) }
    var contractEndDate by remember { mutableLongStateOf(0L) }
    var rentDueDay by remember { mutableStateOf("1") }
    var monthlyRent by remember { mutableStateOf("") }
    var securityDeposit by remember { mutableStateOf("") }
    var depositPaid by remember { mutableStateOf(false) }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var moveInDate by remember { mutableStateOf(0L) }

    // Apartment dropdown
    val apartments by repository.allApartments.collectAsStateWithLifecycle(initialValue = emptyList()
    )
    var apartmentExpanded by remember { mutableStateOf(false) }
    val selectedApartmentName = apartments.find { it.id == apartmentId }?.name ?: "Select Apartment"

    // ── Camera/Gallery launchers ──────────────────────────────────────────────
    var cameraUriString by rememberSaveable { mutableStateOf("") }
    var activePhotoTarget by rememberSaveable { mutableStateOf("profile") }

    fun createTempUri(): Uri {
        // Use internal files dir (not cache) so the file survives process death.
        val imagesDir = File(context.filesDir, "images").also { it.mkdirs() }
        val file = File.createTempFile("img_", ".jpg", imagesDir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUriString = uri.toString()  // persist immediately before camera launches
        return uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        // Use the saved string — a local Uri var would be null after process death.
        if (success && cameraUriString.isNotEmpty()) {
            when (activePhotoTarget) {
                "profile" -> photoUri = cameraUriString
                "front"   -> idPhotoFrontUri = cameraUriString
                "back"    -> idPhotoBackUri = cameraUriString
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.toString()?.let { uriStr ->
            when (activePhotoTarget) {
                "profile" -> photoUri = uriStr
                "front" -> idPhotoFrontUri = uriStr
                "back" -> idPhotoBackUri = uriStr
            }
        }
    }

    var showPhotoDialog by remember { mutableStateOf(false) }

    fun launchPhoto(target: String) {
        activePhotoTarget = target
        showPhotoDialog = true
    }

    // New permission launcher — shows the system "Allow camera?" dialog
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(createTempUri())
    }

    // New helper that gates the camera behind a permission check
    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            cameraLauncher.launch(createTempUri())
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Choose Photo Source") },
            text = {
                Column {
                    TextButton(onClick = {
                        showPhotoDialog = false
                        // createTempUri() also saves the URI string to cameraUriString
                        launchCamera()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CameraAlt, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                    TextButton(onClick = {
                        showPhotoDialog = false
                        galleryLauncher.launch("image/*")
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── Load existing tenant data ─────────────────────────────────────────────
    LaunchedEffect(tenantId) {
        if (tenantId != null) {
            repository.getTenantById(tenantId)?.let { t ->
                firstName = t.firstName; lastName = t.lastName
                phone = t.phone; email = t.email
                nationalId = t.nationalId; nationality = t.nationality
                photoUri = t.photoUri; idPhotoFrontUri = t.idPhotoFrontUri
                idPhotoBackUri = t.idPhotoBackUri; apartmentId = t.apartmentId
                contractStartDate = t.contractStartDate; contractEndDate = t.contractEndDate
                rentDueDay = t.rentDueDay.toString()
                monthlyRent = if (t.monthlyRent > 0) t.monthlyRent.toString() else ""
                securityDeposit = if (t.securityDeposit > 0) t.securityDeposit.toString() else ""
                depositPaid = t.depositPaid; emergencyName = t.emergencyContactName
                emergencyPhone = t.emergencyContactPhone; notes = t.notes
                moveInDate = t.moveInDate
            }
        }
    }

    fun save() {
        if (firstName.isBlank()) return
        val tenant = Tenant(
            id = tenantId ?: 0,
            firstName = firstName.trim(), lastName = lastName.trim(),
            phone = phone.trim(), email = email.trim(),
            nationalId = nationalId.trim(), nationality = nationality.trim(),
            photoUri = photoUri, idPhotoFrontUri = idPhotoFrontUri, idPhotoBackUri = idPhotoBackUri,
            apartmentId = apartmentId,
            contractStartDate = contractStartDate, contractEndDate = contractEndDate,
            rentDueDay = rentDueDay.toIntOrNull()?.coerceIn(1, 28) ?: 1,
            monthlyRent = monthlyRent.toDoubleOrNull() ?: 0.0,
            securityDeposit = securityDeposit.toDoubleOrNull() ?: 0.0,
            depositPaid = depositPaid,
            emergencyContactName = emergencyName.trim(), emergencyContactPhone = emergencyPhone.trim(),
            notes = notes.trim(), isActive = true,
            moveInDate = if (moveInDate == 0L) contractStartDate else moveInDate
        )
        scope.launch(Dispatchers.IO) {
            if (isEdit) {
                repository.updateTenant(tenant)
            } else {
                repository.insertTenant(tenant)
                apartmentId?.let { aptId ->
                    repository.getApartmentById(aptId)?.let { apt ->
                        repository.updateApartment(apt.copy(isOccupied = true))
                    }
                }
            }
        }
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Tenant" else "New Tenant", fontWeight = FontWeight.Bold) },
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
            // ── Profile photo ─────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    TenantAvatar(photoUri, size = 90)
                    SmallFloatingActionButton(
                        onClick = { launchPhoto("profile") },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(16.dp), tint = Color.White)
                    }
                }
            }

            // ── Personal info ─────────────────────────────────────────────
            SectionLabel("Personal Information")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = firstName, onValueChange = { firstName = it },
                    label = { Text("First Name *") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = lastName, onValueChange = { lastName = it },
                    label = { Text("Last Name") }, modifier = Modifier.weight(1f), singleLine = true)
            }

            OutlinedTextField(value = phone, onValueChange = { phone = it },
                label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)

            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true)

            OutlinedTextField(value = nationalId, onValueChange = { nationalId = it },
                label = { Text("National ID / Passport No.") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(value = nationality, onValueChange = { nationality = it },
                label = { Text("Nationality") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            // ── ID document photos ────────────────────────────────────────
            SectionLabel("ID Document Photos")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoPickerBox("ID Front", idPhotoFrontUri, Modifier.weight(1f)) { launchPhoto("front") }
                PhotoPickerBox("ID Back", idPhotoBackUri, Modifier.weight(1f)) { launchPhoto("back") }
            }

            // ── Apartment assignment ──────────────────────────────────────
            SectionLabel("Apartment")
            ExposedDropdownMenuBox(
                expanded = apartmentExpanded,
                onExpandedChange = { apartmentExpanded = !apartmentExpanded }
            ) {
                OutlinedTextField(
                    value = selectedApartmentName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assigned Apartment") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = apartmentExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = apartmentExpanded,
                    onDismissRequest = { apartmentExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("None") }, onClick = {
                        apartmentId = null; apartmentExpanded = false
                    })
                    apartments.forEach { apt ->
                        DropdownMenuItem(
                            text = { Text("${apt.name}${if (apt.isOccupied && apt.id != apartmentId) " (Occupied)" else ""}") },
                            onClick = { apartmentId = apt.id; apartmentExpanded = false }
                        )
                    }
                }
            }

            // ── Contract ──────────────────────────────────────────────────
            SectionLabel("Contract & Financials")
            DatePickerField("Contract Start Date", contractStartDate) { contractStartDate = it }
            DatePickerField("Contract End Date", contractEndDate) { contractEndDate = it }
            DatePickerField("Move-in Date", moveInDate) { moveInDate = it }

            OutlinedTextField(value = rentDueDay, onValueChange = { rentDueDay = it },
                label = { Text("Rent Due Day (1–28)") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)

            OutlinedTextField(value = monthlyRent, onValueChange = { monthlyRent = it },
                label = { Text("Monthly Rent Amount") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)

            OutlinedTextField(value = securityDeposit, onValueChange = { securityDeposit = it },
                label = { Text("Security Deposit") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = depositPaid, onCheckedChange = { depositPaid = it })
                Spacer(Modifier.width(8.dp))
                Text("Security Deposit Paid")
            }

            // ── Emergency contact ─────────────────────────────────────────
            SectionLabel("Emergency Contact")
            OutlinedTextField(value = emergencyName, onValueChange = { emergencyName = it },
                label = { Text("Contact Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = emergencyPhone, onValueChange = { emergencyPhone = it },
                label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)

            // ── Notes ─────────────────────────────────────────────────────
            SectionLabel("Notes")
            OutlinedTextField(value = notes, onValueChange = { notes = it },
                label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            Spacer(Modifier.height(8.dp))
            Button(onClick = ::save, modifier = Modifier.fillMaxWidth(), enabled = firstName.isNotBlank()) {
                Text(if (isEdit) "Save Changes" else "Add Tenant")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun PhotoPickerBox(label: String, uri: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .height(90.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (uri.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(uri).crossfade(true).build(),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AddAPhoto, null,
                    tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(28.dp))
                Text(label, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, epochMillis: Long, onDateSelected: (Long) -> Unit) {
    val focusManager = LocalFocusManager.current
    var showPicker by remember { mutableStateOf(false) }
    val cal = if (epochMillis > 0) {
        Calendar.getInstance().also { it.timeInMillis = epochMillis }
    } else Calendar.getInstance()

    val displayText = if (epochMillis > 0) {
        com.rentmanager.app.ui.utils.UiUtils.formatDate(epochMillis)
    } else "Select date"

    OutlinedTextField(
        value = displayText,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
        modifier = Modifier.fillMaxWidth().onFocusChanged { focusState ->
            if (focusState.isFocused) {
                showPicker = true
                // Optional: Clear focus so the next tap also triggers the picker
                focusManager.clearFocus()
            }
        }
    )

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = if (epochMillis > 0) epochMillis else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDateSelected(it) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
