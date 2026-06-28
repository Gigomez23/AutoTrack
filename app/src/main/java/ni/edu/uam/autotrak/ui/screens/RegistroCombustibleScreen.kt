package ni.edu.uam.autotrak.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible
import ni.edu.uam.autotrak.ui.components.EmptyState
import ni.edu.uam.autotrak.ui.components.VehiculoSelector
import ni.edu.uam.autotrak.viewmodel.EfficiencyPoint
import ni.edu.uam.autotrak.viewmodel.FuelChartType
import ni.edu.uam.autotrak.viewmodel.RegistroCombustibleViewModel
import ni.edu.uam.autotrak.viewmodel.UiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroCombustibleScreen(
    viewModel: RegistroCombustibleViewModel,
    initialVehiculoId: Long? = null,
    onAddRegistro: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehiclesState by viewModel.vehiclesState.collectAsState()
    val selectedVehiculoId by viewModel.selectedVehiculoId.collectAsState()
    val selectedChartType by viewModel.selectedChartType.collectAsState()
    val lineData by viewModel.lineEfficiencyData.collectAsState()
    val monthlyData by viewModel.monthlyEfficiencyData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarVehiculos()
        initialVehiculoId?.let {
            viewModel.seleccionarVehiculo(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Combustible", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            selectedVehiculoId?.let { id ->
                ExtendedFloatingActionButton(
                    onClick = { onAddRegistro(id) },
                    icon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) },
                    text = { Text("Nuevo Registro") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            VehiculoSelector(
                vehiclesState = vehiclesState,
                selectedId = selectedVehiculoId,
                onVehiculoSelected = { viewModel.seleccionarVehiculo(it) }
            )

            HorizontalDivider()

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is UiState.Error -> ErrorState(message = state.message)
                    is UiState.Success -> {
                        if (selectedVehiculoId == null) {
                            EmptyState(message = "Seleccione un vehículo de su propiedad")
                        } else {
                            FuelContent(
                                registros = state.data,
                                selectedChartType = selectedChartType,
                                lineData = lineData,
                                monthlyData = monthlyData,
                                onChartTypeSelected = { viewModel.setChartType(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelContent(
    registros: List<RegistroCombustible>,
    selectedChartType: FuelChartType,
    lineData: List<EfficiencyPoint>,
    monthlyData: List<EfficiencyPoint>,
    onChartTypeSelected: (FuelChartType) -> Unit
) {
    val sortedRegistros = registros.sortedBy { it.fechaRegistro }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedChartType == FuelChartType.LINE) "Rendimiento por Carga (km/L)" else "Promedio Mensual (km/L)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    FuelChartType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = FuelChartType.entries.size),
                            onClick = { onChartTypeSelected(type) },
                            selected = selectedChartType == type,
                            label = { Text(if (type == FuelChartType.LINE) "Detallado" else "Mensual") }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                FuelEfficiencyVisualization(
                    selectedChartType = selectedChartType,
                    lineData = lineData,
                    monthlyData = monthlyData
                )
            }
        }

        item {
            Text("Historial de Cargas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (sortedRegistros.isEmpty()) {
            item {
                EmptyState(message = "No hay registros para este vehículo")
            }
        } else {
            items(sortedRegistros.reversed()) { registro ->
                FuelLogItem(registro = registro)
            }
        }
    }
}

@Composable
fun FuelEfficiencyVisualization(
    selectedChartType: FuelChartType,
    lineData: List<EfficiencyPoint>,
    monthlyData: List<EfficiencyPoint>
) {
    AnimatedContent(
        targetState = selectedChartType,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "ChartTransition"
    ) { targetType ->
        val data = if (targetType == FuelChartType.LINE) lineData else monthlyData
        
        if (data.size >= (if (targetType == FuelChartType.LINE) 1 else 1)) { // Adjusted for meaningful display
             Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        if (targetType == FuelChartType.LINE) {
                            LineChart(points = data)
                        } else {
                            BarChart(points = data)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        data.takeLast(5).forEach { point ->
                            Text(
                                text = point.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (targetType == FuelChartType.LINE) 
                            "Se necesitan al menos 2 registros para calcular rendimiento" 
                            else "No hay suficientes datos mensuales",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun LineChart(points: List<EfficiencyPoint>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val displayPoints = points.takeLast(10)
    
    if (displayPoints.isEmpty()) return

    val maxVal = (displayPoints.maxOfOrNull { it.value } ?: 1f) * 1.3f
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Y-axis labels
        Column(
            modifier = Modifier.fillMaxHeight().width(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(String.format(java.util.Locale.getDefault(), "%.0f", maxVal), style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.outline)
            Text(String.format(java.util.Locale.getDefault(), "%.0f", maxVal/2), style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.outline)
            Text("0", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.outline)
        }

        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        Canvas(modifier = Modifier.fillMaxSize().padding(start = 28.dp, top = 6.dp, bottom = 6.dp)) {
            val width = size.width
            val height = size.height
            
            // Draw horizontal grid lines
            drawLine(
                color = gridColor,
                start = Offset(0f, 0f),
                end = Offset(width, 0f),
                strokeWidth = 0.5.dp.toPx()
            )
            drawLine(
                color = gridColor,
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 0.5.dp.toPx()
            )
            drawLine(
                color = gridColor,
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 0.5.dp.toPx()
            )

            val spaceX = if (displayPoints.size > 1) width / (displayPoints.size - 1) else 0f
            val path = Path()

            displayPoints.forEachIndexed { index, point ->
                val x = index * spaceX
                val y = height - (point.value / maxVal * height)
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                drawCircle(color = primaryColor, radius = 3.dp.toPx(), center = Offset(x, y))
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun BarChart(points: List<EfficiencyPoint>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val displayPoints = points.takeLast(6)
    
    if (displayPoints.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        val maxVal = (displayPoints.maxOfOrNull { it.value } ?: 1f) * 1.3f
        displayPoints.forEach { point ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = String.format(java.util.Locale.getDefault(), "%.1f", point.value),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight((point.value / maxVal).coerceAtLeast(0.05f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(primaryColor)
                )
            }
        }
    }
}

@Composable
fun FuelLogItem(registro: RegistroCombustible) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val fechaFormatted = registro.fechaRegistro?.format(dateFormatter) ?: "Fecha desconocida"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = fechaFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$${registro.cantidadPagado}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(
                    icon = Icons.Default.LocalGasStation,
                    label = "Litros",
                    value = "${registro.cantidadCombustible} L"
                )
                InfoItem(
                    icon = Icons.Default.Speed,
                    label = "Odómetro",
                    value = "${registro.odometro} km"
                )
                InfoItem(
                    icon = Icons.Default.Paid,
                    label = "Precio/L",
                    value = if (registro.cantidadCombustible > 0) 
                        "$${String.format("%.2f", registro.cantidadPagado?.toDouble()?.div(registro.cantidadCombustible))}" 
                        else "N/A"
                )
            }

            if (!registro.nota.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = registro.nota,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelLogFormScreen(
    viewModel: RegistroCombustibleViewModel,
    vehiculoId: Long,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var cantidadCombustible by remember { mutableStateOf("") }
    var cantidadPagada by remember { mutableStateOf("") }
    var odometro by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now().toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Registro") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (AAAA-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )
            OutlinedTextField(
                value = cantidadCombustible,
                onValueChange = { cantidadCombustible = it },
                label = { Text("Cantidad (L)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocalGasStation, contentDescription = null) }
            )
            OutlinedTextField(
                value = cantidadPagada,
                onValueChange = { cantidadPagada = it },
                label = { Text("Precio Total Pagado") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }
            )
            OutlinedTextField(
                value = odometro,
                onValueChange = { odometro = it },
                label = { Text("Odómetro actual (km)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) }
            )
            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Notas opcionales") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val cantidad = cantidadCombustible.toDoubleOrNull()
                    val pagado = cantidadPagada.toBigDecimalOrNull()
                    val odo = odometro.toLongOrNull()
                    
                    if (cantidad == null || pagado == null || odo == null) {
                        return@Button
                    }

                    val registro = RegistroCombustible(
                        fechaRegistro = try { LocalDate.parse(fecha) } catch(e: Exception) { LocalDate.now() },
                        cantidadCombustible = cantidad,
                        cantidadPagado = pagado,
                        odometro = odo,
                        nota = nota
                    )
                    viewModel.crearRegistroCombustible(vehiculoId, registro)
                    onSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Guardar Registro", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
