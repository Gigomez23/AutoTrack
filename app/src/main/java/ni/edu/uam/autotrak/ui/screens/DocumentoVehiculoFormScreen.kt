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
import ni.edu.uam.autotrak.data.remote.model.DocumentoVehiculo
import ni.edu.uam.autotrak.viewmodel.DocumentoVehiculoViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentoVehiculoFormScreen(
    viewModel: DocumentoVehiculoViewModel,
    vehiculoId: Long,
    documentoId: Long? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var fechaEmitida by remember { mutableStateOf(LocalDate.now()) }
    var fechaVencimiento by remember { mutableStateOf(LocalDate.now().plusYears(1)) }
    
    var showEmitidaPicker by remember { mutableStateOf(false) }
    var showVencimientoPicker by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(documentoId) {
        if (documentoId != null && uiState is ni.edu.uam.autotrak.viewmodel.DocumentoVehiculoUiState.Success) {
            val doc = (uiState as ni.edu.uam.autotrak.viewmodel.DocumentoVehiculoUiState.Success).documentos.find { it.id == documentoId }
            doc?.let {
                nombre = it.nombre ?: ""
                fechaEmitida = it.fechaEmitida ?: LocalDate.now()
                fechaVencimiento = it.fechaVencimiento ?: LocalDate.now()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (documentoId == null) "Nuevo Documento" else "Editar Documento", fontWeight = FontWeight.Bold) },
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
                text = "Información del Documento",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del Documento (ej. Seguro, Circulación)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
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
                    val nuevoDoc = DocumentoVehiculo(
                        nombre = nombre,
                        fechaEmitida = fechaEmitida,
                        fechaVencimiento = fechaVencimiento,
                        vehiculoId = vehiculoId
                    )
                    
                    if (documentoId == null) {
                        viewModel.crearDocumento(nuevoDoc)
                    } else {
                        viewModel.actualizarDocumento(documentoId, nuevoDoc)
                    }
                    onSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = nombre.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Documento", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
