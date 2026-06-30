package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import ni.edu.uam.autotrak.ui.components.EmptyState
import ni.edu.uam.autotrak.ui.components.VehiculoSelector
import ni.edu.uam.autotrak.viewmodel.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import ni.edu.uam.autotrak.ui.components.SyncStatusBadge
import ni.edu.uam.autotrak.ui.components.OfflineBanner
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroProblemaScreen(
    viewModel: RegistroProblemaViewModel,
    isOffline: Boolean,
    initialVehiculoId: Long? = null,
    onAddRegistro: (Long) -> Unit,
    onEditRegistro: (Long, RegistroProblema) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehiclesState by viewModel.vehiclesState.collectAsState()
    val selectedVehiculoId by viewModel.selectedVehiculoId.collectAsState()
    val filteredRegistros by viewModel.filteredRegistros.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val currentSort by viewModel.sort.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarVehiculos()
        initialVehiculoId?.let { viewModel.seleccionarVehiculo(it) }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Registro de Problemas") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                        }
                    }
                )
                OfflineBanner(isOffline = isOffline)
            }
        },
        floatingActionButton = {
            selectedVehiculoId?.let { id ->
                FloatingActionButton(onClick = { onAddRegistro(id) }) {
                    Icon(Icons.Default.Add, contentDescription = "Reportar Problema")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            VehiculoSelector(
                vehiclesState = vehiclesState,
                selectedId = selectedVehiculoId,
                onVehiculoSelected = { viewModel.seleccionarVehiculo(it) }
            )

            if (selectedVehiculoId != null) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (showFilters) {
                    FilterSortSection(
                        currentFilter = currentFilter,
                        onFilterSelected = { viewModel.setFilter(it) },
                        currentSort = currentSort,
                        onSortSelected = { viewModel.setSort(it) }
                    )
                }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.cargarRegistrosProblema(selectedVehiculoId!!) },
                    modifier = Modifier.weight(1f)
                ) {
                    when (val state = uiState) {
                        is UiState.Loading -> if (!isRefreshing) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        is UiState.Error -> Text(text = state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                        is UiState.Success -> {
                            IssueList(
                                registros = filteredRegistros,
                                onResolve = { viewModel.markAsResolved(it) },
                                onEdit = { onEditRegistro(selectedVehiculoId!!, it) },
                                onDelete = { it.id?.let { id -> viewModel.eliminarRegistroProblema(id) } }
                            )
                        }
                    }
                }
            } else {
                EmptyState(message = "Seleccione un vehículo para ver sus problemas")
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Buscar por tipo o nota...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterSortSection(
    currentFilter: ProblemFilter,
    onFilterSelected: (ProblemFilter) -> Unit,
    currentSort: ProblemSort,
    onSortSelected: (ProblemSort) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Filtrar por estado:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProblemFilter.entries.forEach { filter ->
                FilterChip(
                    selected = currentFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Ordenar por fecha:", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProblemSort.entries.forEach { sort ->
                FilterChip(
                    selected = currentSort == sort,
                    onClick = { onSortSelected(sort) },
                    label = { Text(if (sort == ProblemSort.NEWEST) "Más recientes" else "Más antiguos") }
                )
            }
        }
    }
}

@Composable
fun IssueList(
    registros: List<RegistroProblema>,
    onResolve: (RegistroProblema) -> Unit,
    onEdit: (RegistroProblema) -> Unit,
    onDelete: (RegistroProblema) -> Unit
) {
    if (registros.isEmpty()) {
        EmptyState(message = "No se encontraron problemas")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(registros) { registro ->
                IssueItem(
                    registro = registro,
                    onResolve = { onResolve(registro) },
                    onEdit = { onEdit(registro) },
                    onDelete = { onDelete(registro) }
                )
            }
        }
    }
}

@Composable
fun IssueItem(
    registro: RegistroProblema,
    onResolve: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = registro.tipoProblema ?: "Problema Desconocido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = registro.fechaRegistro?.format(dateFormatter) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SyncStatusBadge(syncState = registro.syncState)
                }
                
                Row {
                    if (registro.afectaVehiculo) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Grave", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                labelColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            border = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    SuggestionChip(
                        onClick = {},
                        label = { Text(if (registro.activo) "Activo" else "Resuelto", style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (registro.activo) warningContainer() else successContainer(),
                            labelColor = if (registro.activo) onWarningContainer() else onSuccessContainer()
                        )
                    )
                }
            }

            if (!registro.nota.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = registro.nota,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (registro.activo) {
                    TextButton(onClick = onResolve) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resolver")
                    }
                }
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Helpers for colors since they aren't standard in M3 ColorScheme yet
@Composable fun successContainer() = Color(0xFFC6EFCE)
@Composable fun onSuccessContainer() = Color(0xFF006100)
@Composable fun warningContainer() = Color(0xFFFFEB9C)
@Composable fun onWarningContainer() = Color(0xFF9C5700)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueFormScreen(
    viewModel: RegistroProblemaViewModel,
    vehiculoId: Long,
    registroToEdit: RegistroProblema? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var tipoProblema by remember { mutableStateOf(registroToEdit?.tipoProblema ?: "") }
    var nota by remember { mutableStateOf(registroToEdit?.nota ?: "") }
    var fecha by remember { mutableStateOf(registroToEdit?.fechaRegistro?.toString() ?: LocalDate.now().toString()) }
    var afectaVehiculo by remember { mutableStateOf(registroToEdit?.afectaVehiculo ?: false) }
    var activo by remember { mutableStateOf(registroToEdit?.activo ?: true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (registroToEdit == null) "Reportar Problema" else "Editar Problema") },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (AAAA-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )
            
            OutlinedTextField(
                value = tipoProblema,
                onValueChange = { tipoProblema = it },
                label = { Text("Tipo de Problema") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Eje: Motor, Frenos, Luces...") }
            )
            
            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = afectaVehiculo, onCheckedChange = { afectaVehiculo = it })
                        Text("¿Es un problema grave que impide circular?")
                    }
                    
                    if (registroToEdit != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = activo, onCheckedChange = { activo = it })
                            Text("Estado Activo")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (tipoProblema.isBlank()) return@Button
                    
                    val registro = RegistroProblema(
                        id = registroToEdit?.id,
                        fechaRegistro = try { LocalDate.parse(fecha) } catch(e: Exception) { LocalDate.now() },
                        tipoProblema = tipoProblema,
                        nota = nota,
                        activo = activo,
                        afectaVehiculo = afectaVehiculo
                    )
                    
                    if (registroToEdit == null) {
                        viewModel.crearRegistroProblema(vehiculoId, registro)
                    } else {
                        viewModel.actualizarRegistroProblema(registro)
                    }
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (registroToEdit == null) "Guardar" else "Actualizar", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
