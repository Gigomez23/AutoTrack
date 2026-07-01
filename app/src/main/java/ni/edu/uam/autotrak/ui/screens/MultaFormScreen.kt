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
import ni.edu.uam.autotrak.data.remote.model.Multa
import ni.edu.uam.autotrak.viewmodel.MultasViewModel
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultaFormScreen(
    viewModel: MultasViewModel,
    multaId: Long? = null,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    // En una implementación real buscaríamos la multa si multaId != null
    // Para este ejercicio asumiremos que la multa se pasa o se gestiona en el ViewModel
    // Pero implementaremos el estado local para el formulario
    
    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var fechaMulta by remember { mutableStateOf(LocalDate.now()) }
    var pagada by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }

    val isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(multaId) {
        if (multaId != null) {
            viewModel.getMultaSync(multaId)?.let { multa ->
                descripcion = multa.descripcion ?: ""
                monto = multa.monto.toString()
                fechaMulta = multa.fechaMulta ?: LocalDate.now()
                pagada = multa.pagada
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (multaId == null) "Registrar Multa" else "Editar Multa", fontWeight = FontWeight.Bold) },
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
                text = "Detalles de la Infracción",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción / Código de Infracción") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = monto,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) monto = it },
                label = { Text("Monto ($)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedCard(
                onClick = { showDatePicker = true },
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
                        Text("Fecha de la Multa", style = MaterialTheme.typography.labelSmall)
                        Text(fechaMulta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = pagada,
                    onCheckedChange = { pagada = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "¿La multa ya ha sido pagada?", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val nuevaMulta = Multa(
                        descripcion = descripcion,
                        monto = BigDecimal(monto.ifBlank { "0" }),
                        fechaMulta = fechaMulta,
                        fechaEmitida = fechaMulta, // El backend requiere fechaEmitida
                        fechaLimite = fechaMulta.plusDays(30), // Valor por defecto razonable
                        pagada = pagada
                    )
                    
                    if (multaId == null) {
                        viewModel.crearMulta(nuevaMulta)
                    } else {
                        viewModel.actualizarMulta(multaId, nuevaMulta)
                    }
                    onSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = descripcion.isNotBlank() && monto.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Multa", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaMulta.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaMulta = datePickerState.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    } ?: fechaMulta
                    showDatePicker = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
