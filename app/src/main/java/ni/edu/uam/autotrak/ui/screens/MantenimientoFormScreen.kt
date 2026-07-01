package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.autotrak.data.remote.model.ServicioMantenimiento
import ni.edu.uam.autotrak.data.remote.model.TipoMantenimiento
import ni.edu.uam.autotrak.viewmodel.MantenimientoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenimientoFormScreen(
    viewModel: MantenimientoViewModel,
    mantenimientoId: Long? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var titulo: String? by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var distanciaAgendada by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var tipoMantenimiento: TipoMantenimiento? by remember { mutableStateOf(TipoMantenimiento.PREVENTIVO) }
    var afectaVehiculo by remember { mutableStateOf(false) }
    var completado by remember { mutableStateOf(false) }

    var expandedDropdown by remember { mutableStateOf(false) }
    
    val formUiState by viewModel.formUiState.collectAsState()

    LaunchedEffect(mantenimientoId) {
        if (mantenimientoId != null) {
            viewModel.getMantenimientoSync(mantenimientoId)?.let { m ->
                titulo = m.titulo
                descripcion = m.descripcion ?: ""
                distanciaAgendada = m.distanciaAgendada?.toString() ?: ""
                observaciones = m.observaciones ?: ""
                tipoMantenimiento = m.tipoMantenimiento
                afectaVehiculo = m.afectaVehiculo
                completado = m.completado
            }
        }
    }

    LaunchedEffect(formUiState.isSuccess) {
        if (formUiState.isSuccess) {
            viewModel.resetFormState()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mantenimientoId == null) "Registrar Servicio" else "Editar Servicio", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detalles del Servicio",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            titulo?.let { it1 ->
                OutlinedTextField(
                    value = it1,
                    onValueChange = { titulo = it },
                    label = { Text("Título del Servicio") },
                    placeholder = { Text("Ej: Cambio de Aceite") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                val label = when (tipoMantenimiento) {
                    TipoMantenimiento.PREVENTIVO -> "Preventivo"
                    TipoMantenimiento.CORRECTIVO -> "Correctivo"
                    TipoMantenimiento.PREDICTIVO -> "Predictivo"
                    TipoMantenimiento.OTRO -> "Otro"
                    null -> "Otro"
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Mantenimiento") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    TipoMantenimiento.entries.forEach { tipo ->
                        val itemLabel = when(tipo) {
                            TipoMantenimiento.PREVENTIVO -> "Preventivo"
                            TipoMantenimiento.CORRECTIVO -> "Correctivo"
                            TipoMantenimiento.PREDICTIVO -> "Predictivo"
                            TipoMantenimiento.OTRO -> "Otro"
                        }
                        DropdownMenuItem(
                            text = { Text(itemLabel) },
                            onClick = {
                                tipoMantenimiento = tipo
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = distanciaAgendada,
                onValueChange = { if (it.all { char -> char.isDigit() }) distanciaAgendada = it },
                label = { Text("Kilometraje Agendado (km)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            // Switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Afecta uso general", style = MaterialTheme.typography.bodyLarge)
                    Text("Indica si este mantenimiento es crítico para el uso", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = afectaVehiculo, onCheckedChange = { afectaVehiculo = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("¿Servicio Completado?", style = MaterialTheme.typography.bodyLarge)
                    Text("Mueve este registro al historial", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = completado, onCheckedChange = { completado = it })
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (formUiState.error != null) {
                Text(text = formUiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val servicio = ServicioMantenimiento(
                        titulo = titulo,
                        descripcion = descripcion,
                        distanciaAgendada = distanciaAgendada.toIntOrNull(),
                        observaciones = observaciones,
                        tipoMantenimiento = tipoMantenimiento,
                        afectaVehiculo = afectaVehiculo,
                        completado = completado
                    )
                    
                    if (mantenimientoId == null) {
                        viewModel.createMantenimiento(servicio)
                    } else {
                        viewModel.updateMantenimiento(mantenimientoId, servicio)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !formUiState.isLoading && titulo?.isNotBlank() == true
            ) {
                if (formUiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (mantenimientoId == null) "Registrar" else "Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
