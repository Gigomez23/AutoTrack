package ni.edu.uam.autotrak.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.viewmodel.UiState

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
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
