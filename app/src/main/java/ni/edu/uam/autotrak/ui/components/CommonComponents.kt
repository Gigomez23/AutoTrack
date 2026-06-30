package ni.edu.uam.autotrak.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.sync.SyncState
import ni.edu.uam.autotrak.viewmodel.UiState

@Composable
fun SyncStatusBadge(syncState: SyncState, modifier: Modifier = Modifier) {
    val (icon, color, label) = when (syncState) {
        SyncState.SYNCED -> Triple(Icons.Default.CloudDone, Color(0xFF4CAF50), "Sincronizado")
        SyncState.PENDING_CREATE -> Triple(Icons.Default.CloudUpload, Color(0xFF2196F3), "Pendiente enviar")
        SyncState.PENDING_UPDATE -> Triple(Icons.Default.CloudSync, Color(0xFFFF9800), "Cambios pendientes")
        SyncState.PENDING_DELETE -> Triple(Icons.Default.DeleteSweep, Color(0xFFF44336), "Pendiente eliminar")
        SyncState.SYNC_FAILED -> Triple(Icons.Default.CloudOff, Color(0xFFF44336), "Error de sincronización")
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OfflineBanner(isOffline: Boolean) {
    if (isOffline) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Modo offline: Los cambios se sincronizarán al recuperar conexión", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculoSelector(
    vehiclesState: UiState<List<Vehiculo>>,
    selectedId: Long?,
    onVehiculoSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        when (vehiclesState) {
            is UiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            is UiState.Error -> Text("Error al cargar sus vehículos", color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                val vehicles = vehiclesState.data
                val selectedVehicle = vehicles.find { it.id == selectedId }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedVehicle?.let { "${it.marca} ${it.modelo} (${it.placa})" } ?: "Seleccionar Mi Vehículo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mi Vehículo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        vehicles.forEach { vehicle ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${vehicle.marca} ${vehicle.modelo}", fontWeight = FontWeight.Bold)
                                        Text("${vehicle.placa}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    vehicle.id?.let { onVehiculoSelected(it) }
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun FullScreenError(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reintentar")
                }
            }
        }
    }
}
