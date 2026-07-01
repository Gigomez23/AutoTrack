package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.viewmodel.UiState
import ni.edu.uam.autotrak.viewmodel.VehiculoViewModel

import ni.edu.uam.autotrak.ui.components.OfflineBanner
import ni.edu.uam.autotrak.ui.components.SyncStatusBadge
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculoScreen(
    viewModel: VehiculoViewModel,
    isOffline: Boolean,
    onVehicleClick: (Long) -> Unit,
    onAddVehicle: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicleSummaries by viewModel.vehicleSummaries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val filteredVehiculos = remember(uiState, searchQuery) {
        if (uiState is UiState.Success) {
            val data = (uiState as UiState.Success<List<Vehiculo>>).data
            if (searchQuery.isBlank()) {
                data
            } else {
                data.filter {
                    it.placa?.contains(searchQuery, ignoreCase = true) == true ||
                    it.vin?.contains(searchQuery, ignoreCase = true) == true ||
                    it.apodo?.contains(searchQuery, ignoreCase = true) == true ||
                    it.marca?.contains(searchQuery, ignoreCase = true) == true ||
                    it.modelo?.contains(searchQuery, ignoreCase = true) == true
                }
            }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Mis Vehículos") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                OfflineBanner(isOffline = isOffline)
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddVehicle) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por placa, VIN o apodo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.cargarVehiculos() },
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                when (val state = uiState) {
                    is UiState.Loading -> if (!isRefreshing) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is UiState.Error -> Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    is UiState.Success -> {
                        VehiculoList(
                            vehiculos = filteredVehiculos,
                            vehicleSummaries = vehicleSummaries,
                            onVehicleClick = onVehicleClick,
                            onDelete = { it.id?.let { id -> viewModel.eliminarVehiculo(id) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehiculoList(
    vehiculos: List<Vehiculo>,
    vehicleSummaries: Map<Long, VehiculoViewModel.VehicleStats>,
    onVehicleClick: (Long) -> Unit,
    onDelete: (Vehiculo) -> Unit
) {
    if (vehiculos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay vehículos registrados")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vehiculos) { vehiculo ->
                VehiculoItem(
                    vehiculo = vehiculo,
                    stats = vehicleSummaries[vehiculo.id],
                    onClick = { vehiculo.id?.let { onVehicleClick(it) } },
                    onDelete = { onDelete(vehiculo) }
                )
            }
        }
    }
}

@Composable
fun VehiculoItem(
    vehiculo: Vehiculo,
    stats: VehiculoViewModel.VehicleStats?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!vehiculo.apodo.isNullOrEmpty()) vehiculo.apodo else "${vehiculo.marca ?: ""} ${vehiculo.modelo ?: ""}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${vehiculo.marca ?: ""} ${vehiculo.modelo ?: ""} (${vehiculo.anio ?: ""})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = vehiculo.placa ?: "SIN PLACA",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        SyncStatusBadge(syncState = vehiculo.syncState)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            if (stats != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryStatItem(
                        icon = Icons.Default.LocalGasStation,
                        label = "Rendimiento",
                        value = if (stats.rendimientoPromedio > 0) String.format(Locale.getDefault(), "%.1f km/L", stats.rendimientoPromedio) else "N/A",
                        color = MaterialTheme.colorScheme.primary
                    )
                    VerticalDivider(modifier = Modifier.height(32.dp), thickness = 1.dp)
                    SummaryStatItem(
                        icon = Icons.Default.AttachMoney,
                        label = "Gasto Mensual",
                        value = if (stats.costoMensualPromedio > 0) String.format(Locale.getDefault(), "$%.0f", stats.costoMensualPromedio) else "N/A",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryStatItem(icon: ImageVector, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculoFormScreen(
    viewModel: VehiculoViewModel,
    vehicleId: Long? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("EN_MARCHA") }
    var apodo by remember { mutableStateOf("") }

    var showYearPicker by remember { mutableStateOf(false) }
    var expandedEstado by remember { mutableStateOf(false) }
    
    val estadosVehiculo = listOf(
        "EN_MARCHA" to "En Marcha",
        "DETENIDO" to "Detenido",
        "EN_MARCHA_CON_FALLAS" to "En Marcha con Fallas"
    )

    if (showYearPicker) {
        YearPickerDialog(
            selectedYear = anio.toIntOrNull() ?: java.time.Year.now().value,
            onYearSelected = {
                anio = it.toString()
                showYearPicker = false
            },
            onDismissRequest = { showYearPicker = false }
        )
    }

    val detailUiState by viewModel.detailUiState.collectAsState()

    LaunchedEffect(vehicleId) {
        if (vehicleId != null) {
            viewModel.buscarVehiculo(vehicleId)
        }
    }

    LaunchedEffect(detailUiState) {
        if (vehicleId != null && detailUiState is UiState.Success) {
            val v = (detailUiState as UiState.Success<Vehiculo>).data
            marca = v.marca ?: ""
            modelo = v.modelo ?: ""
            anio = v.anio?.toString() ?: ""
            placa = v.placa ?: ""
            vin = v.vin ?: ""
            estado = v.estado ?: "EN_MARCHA"
            apodo = v.apodo ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vehicleId == null) "Nuevo Vehículo" else "Editar Vehículo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = apodo, onValueChange = { apodo = it }, label = { Text("Apodo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = marca, onValueChange = { marca = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = modelo, onValueChange = { modelo = it }, label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            
            Box(modifier = Modifier.fillMaxWidth().clickable { showYearPicker = true }) {
                OutlinedTextField(
                    value = anio,
                    onValueChange = { },
                    label = { Text("Año") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar año")
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            OutlinedTextField(value = placa, onValueChange = { placa = it }, label = { Text("Placa") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = vin, onValueChange = { vin = it }, label = { Text("VIN") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = expandedEstado,
                onExpandedChange = { expandedEstado = !expandedEstado },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = estadosVehiculo.find { it.first == estado }?.second ?: "En Marcha",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstado) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expandedEstado,
                    onDismissRequest = { expandedEstado = false }
                ) {
                    estadosVehiculo.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                estado = value
                                expandedEstado = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (marca.isBlank() || modelo.isBlank() || placa.isBlank()) {
                        // Show error or simple validation
                        return@Button
                    }
                    val userId = viewModel.getUserId()
                    val vehiculo = Vehiculo(
                        id = vehicleId,
                        marca = marca,
                        modelo = modelo,
                        anio = anio.toIntOrNull(),
                        placa = placa,
                        vin = vin,
                        estado = estado,
                        apodo = apodo,
                        imagenes = emptyList(),
                        usuario = if (userId != -1L) ni.edu.uam.autotrak.data.remote.model.Usuario(id = userId) else null
                    )
                    if (vehicleId == null) {
                        viewModel.crearVehiculo(vehiculo)
                    } else {
                        viewModel.actualizarVehiculo(vehicleId, vehiculo)
                    }
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (vehicleId == null) "Guardar" else "Actualizar", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun YearPickerDialog(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val currentYear = java.time.Year.now().value
    val years = (1950..currentYear + 1).toList().reversed()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = years.indexOf(selectedYear).coerceAtLeast(0))

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancelar") }
        },
        title = { Text("Seleccionar Año") },
        text = {
            Box(modifier = Modifier.height(250.dp)) {
                LazyColumn(state = listState) {
                    items(years) { year ->
                        TextButton(
                            onClick = { onYearSelected(year) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (year == selectedYear) 
                                ButtonDefaults.textButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                else ButtonDefaults.textButtonColors()
                        ) {
                            Text(year.toString(), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    )
}
