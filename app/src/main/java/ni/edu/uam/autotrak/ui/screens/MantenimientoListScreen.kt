package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ni.edu.uam.autotrak.data.remote.model.ServicioMantenimiento
import ni.edu.uam.autotrak.data.remote.model.TipoMantenimiento
import ni.edu.uam.autotrak.ui.components.SyncStatusBadge
import ni.edu.uam.autotrak.viewmodel.MantenimientoListUiState
import ni.edu.uam.autotrak.viewmodel.MantenimientoViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenimientoListScreen(
    viewModel: MantenimientoViewModel,
    onNavigateToForm: (Long?) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.listUiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val scrollState = rememberLazyListState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Programados", "Historial")

    val isExpanded by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex == 0 }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Mantenimiento", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToForm(null) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Servicio") },
                expanded = isExpanded,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshMantenimientos() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is MantenimientoListUiState.Loading -> {
                    if (!isRefreshing) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is MantenimientoListUiState.Empty -> {
                    EmptyMantenimientoState(onAdd = { onNavigateToForm(null) })
                }
                is MantenimientoListUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
                is MantenimientoListUiState.Success -> {
                    val filteredList = state.mantenimientos.filter { 
                        if (selectedTab == 0) !it.completado else it.completado
                    }
                    MantenimientoList(
                        mantenimientos = filteredList,
                        onEdit = onNavigateToForm,
                        onDelete = { it.id?.let { id -> viewModel.deleteMantenimiento(id) } },
                        scrollState = scrollState
                    )
                }
            }
        }
    }
}

@Composable
fun MantenimientoList(
    mantenimientos: List<ServicioMantenimiento>,
    onEdit: (Long) -> Unit,
    onDelete: (ServicioMantenimiento) -> Unit,
    scrollState: androidx.compose.foundation.lazy.LazyListState
) {
    if (mantenimientos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay registros en esta categoría", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mantenimientos, key = { it.id ?: it.hashCode() }) { item ->
                MantenimientoItemCard(
                    mantenimiento = item,
                    onEdit = { item.id?.let { onEdit(it) } },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

fun getTipoMantenimientoInfo(tipo: TipoMantenimiento?): Pair<String, Color> {
    return when (tipo) {
        TipoMantenimiento.CAMBIO_ACEITE_FILTRO -> "Aceite y Filtro" to Color(0xFF1976D2) // Blue
        TipoMantenimiento.SISTEMA_FRENOS -> "Frenos" to Color(0xFFD32F2F) // Red
        TipoMantenimiento.LLANTAS_Y_ALINEACION -> "Llantas/Alineación" to Color(0xFF388E3C) // Green
        TipoMantenimiento.SUSPENSION_DIRECCION -> "Suspensión" to Color(0xFF689F38) // Light Green
        TipoMantenimiento.SISTEMA_ELECTRICO -> "Eléctrico" to Color(0xFFFBC02D) // Yellow
        TipoMantenimiento.SISTEMA_ENFRIAMIENTO -> "Enfriamiento" to Color(0xFFFFA000) // Orange
        TipoMantenimiento.TRANSMISION_EMBRAGUE -> "Transmisión" to Color(0xFFE64A19) // Deep Orange
        TipoMantenimiento.MOTOR_Y_COMBUSTIBLE -> "Motor" to Color(0xFFBF360C) // Brownish Red
        TipoMantenimiento.AIRE_ACONDICIONADO -> "A/C" to Color(0xFF0288D1) // Light Blue
        TipoMantenimiento.REPARACION_CORRECTIVA -> "Correctiva" to Color(0xFFC2185B) // Pink
        TipoMantenimiento.INSPECCION_GENERAL -> "Inspección" to Color(0xFF455A64) // Blue Grey
        TipoMantenimiento.OTRO -> "Otro" to Color(0xFF7B1FA2) // Purple
        null -> "Otro" to Color(0xFF7B1FA2)
    }
}

@Composable
fun MantenimientoItemCard(
    mantenimiento: ServicioMantenimiento,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    mantenimiento.titulo?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = mantenimiento.fechaCreacion?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) ?: "Recién",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val (label, color) = getTipoMantenimientoInfo(mantenimiento.tipoMantenimiento)
                    val bgColor = color.copy(alpha = 0.1f)

                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "KM: ${mantenimiento.distanciaAgendada ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                SyncStatusBadge(syncState = mantenimiento.syncState)
            }
        }
    }
}

@Composable
fun EmptyMantenimientoState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Sin registros de mantenimiento",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Lleva un control de los servicios preventivos y correctivos de tu vehículo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar Servicio")
        }
    }
}
