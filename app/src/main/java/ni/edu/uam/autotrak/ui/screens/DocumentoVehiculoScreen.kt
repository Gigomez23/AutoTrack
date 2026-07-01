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
import ni.edu.uam.autotrak.data.remote.model.DocumentoVehiculo
import ni.edu.uam.autotrak.ui.components.SyncStatusBadge
import ni.edu.uam.autotrak.viewmodel.DocumentoFilter
import ni.edu.uam.autotrak.viewmodel.DocumentoVehiculoUiState
import ni.edu.uam.autotrak.viewmodel.DocumentoVehiculoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentoVehiculoScreen(
    viewModel: DocumentoVehiculoViewModel,
    onNavigateToCreateDocumento: () -> Unit,
    onNavigateToEditDocumento: (Long) -> Unit,
    onViewDocumentFile: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val scrollState = rememberLazyListState()

    val isExpanded by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex == 0 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documentos", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateDocumento,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Añadir Documento") },
                expanded = isExpanded,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips
            ScrollableTabRow(
                selectedTabIndex = currentFilter.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {},
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            ) {
                DocumentoFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshDocumentos() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is DocumentoVehiculoUiState.Loading -> {
                        if (!isRefreshing) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is DocumentoVehiculoUiState.Empty -> {
                        EmptyDocumentosState(onAdd = onNavigateToCreateDocumento)
                    }
                    is DocumentoVehiculoUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        }
                    }
                    is DocumentoVehiculoUiState.Success -> {
                        DocumentosList(
                            documentos = state.documentos,
                            onEdit = onNavigateToEditDocumento,
                            onDelete = { it.id?.let { id -> viewModel.eliminarDocumento(id) } },
                            onView = onViewDocumentFile,
                            scrollState = scrollState
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentosList(
    documentos: List<DocumentoVehiculo>,
    onEdit: (Long) -> Unit,
    onDelete: (DocumentoVehiculo) -> Unit,
    onView: (String) -> Unit,
    scrollState: androidx.compose.foundation.lazy.LazyListState
) {
    if (documentos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay documentos que coincidan con el filtro", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(documentos, key = { it.id ?: it.hashCode() }) { documento ->
                DocumentoItemCard(
                    documento = documento,
                    onEdit = { documento.id?.let { onEdit(it) } },
                    onDelete = { onDelete(documento) },
                    onView = { documento.imagen?.let { onView(it) } }
                )
            }
        }
    }
}

@Composable
fun DocumentoItemCard(
    documento: DocumentoVehiculo,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onView: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val hoy = LocalDate.now()
    val isSoon = documento.fechaVencimiento?.let {
        ChronoUnit.DAYS.between(hoy, it) in 0..30
    } ?: false
    val isExpired = documento.fechaVencimiento?.isBefore(hoy) ?: false

    val statusColor = when {
        isExpired -> MaterialTheme.colorScheme.error
        isSoon -> Color(0xFFE65100) // Amber
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = documento.nombre ?: "Documento sin nombre",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vehículo ID: ${documento.vehiculoId ?: "---"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onView) {
                        Icon(Icons.Default.Visibility, contentDescription = "Ver archivo")
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vence el",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = documento.fechaVencimiento?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "No definida",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                SyncStatusBadge(syncState = documento.syncState)
            }
        }
    }
}

@Composable
fun EmptyDocumentosState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No hay documentos guardados",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mantén digitalizados los seguros, circulaciones y otros documentos importantes de tu vehículo.",
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
            Text("Añadir Documento")
        }
    }
}
