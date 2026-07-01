package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.autotrak.data.remote.model.Licencia
import ni.edu.uam.autotrak.viewmodel.LicenciaViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenciaFormScreen(
    viewModel: LicenciaViewModel,
    licenciaToEdit: Licencia? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var categoriasInput by remember { mutableStateOf(licenciaToEdit?.categorias?.joinToString(", ") ?: "") }
    var fechaEmitida by remember { mutableStateOf(licenciaToEdit?.fechaEmitida ?: LocalDate.now()) }
    var fechaVencimiento by remember { mutableStateOf(licenciaToEdit?.fechaVencimiento ?: LocalDate.now().plusYears(5)) }
    
    var showEmitidaPicker by remember { mutableStateOf(false) }
    var showVencimientoPicker by remember { mutableStateOf(false) }

    val isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (licenciaToEdit == null) "Registrar Licencia" else "Editar Licencia", fontWeight = FontWeight.Bold) },
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
                text = "Información de la Licencia",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = categoriasInput,
                onValueChange = { categoriasInput = it },
                label = { Text("Categorías (separadas por comas)") },
                placeholder = { Text("Ej: M, 1, 2, 3") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            // Fecha Emisión
            OutlinedCard(
                onClick = { showEmitidaPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Fecha de Emisión", style = MaterialTheme.typography.labelSmall)
                        Text(fechaEmitida.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // Fecha Vencimiento
            OutlinedCard(
                onClick = { showVencimientoPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Fecha de Vencimiento", style = MaterialTheme.typography.labelSmall)
                        Text(fechaVencimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val categorias = categoriasInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val nuevaLicencia = (licenciaToEdit ?: Licencia()).copy(
                        categorias = categorias,
                        fechaEmitida = fechaEmitida,
                        fechaVencimiento = fechaVencimiento
                    )
                    
                    if (licenciaToEdit == null) {
                        viewModel.crearLicencia(nuevaLicencia)
                    } else {
                        viewModel.actualizarLicencia(licenciaToEdit.id ?: 0L, nuevaLicencia)
                    }
                    onSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving && categoriasInput.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Licencia", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showEmitidaPicker) {
        DatePickerModal(
            initialDate = fechaEmitida,
            onDateSelected = { date ->
                date?.let { fechaEmitida = it }
                showEmitidaPicker = false
            },
            onDismiss = { showEmitidaPicker = false }
        )
    }

    if (showVencimientoPicker) {
        DatePickerModal(
            initialDate = fechaVencimiento,
            onDateSelected = { date ->
                date?.let { fechaVencimiento = it }
                showVencimientoPicker = false
            },
            onDismiss = { showVencimientoPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initialDate: LocalDate,
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                })
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
