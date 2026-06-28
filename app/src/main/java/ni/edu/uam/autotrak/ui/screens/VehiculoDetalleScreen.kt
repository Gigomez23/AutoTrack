package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.viewmodel.UiState
import ni.edu.uam.autotrak.viewmodel.VehiculoViewModel

import ni.edu.uam.autotrak.viewmodel.EfficiencyPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculoDetalleScreen(
    viewModel: VehiculoViewModel,
    vehiculoId: Long,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val detailState by viewModel.detailUiState.collectAsState()
    val rendimientoData by viewModel.rendimientoData.collectAsState()
    val costosMensualesData by viewModel.costosMensualesData.collectAsState()
    val avgEfficiency by viewModel.averageEfficiency.collectAsState()
    val avgMonthlyCost by viewModel.averageMonthlyCost.collectAsState()

    LaunchedEffect(vehiculoId) {
        viewModel.buscarVehiculo(vehiculoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Vehículo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(vehiculoId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = detailState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                is UiState.Success -> {
                    VehiculoDetalleContent(
                        vehiculo = state.data,
                        rendimientoData = rendimientoData,
                        costosMensualesData = costosMensualesData,
                        avgEfficiency = avgEfficiency,
                        avgMonthlyCost = avgMonthlyCost
                    )
                }
            }
        }
    }
}

@Composable
fun VehiculoDetalleContent(
    vehiculo: Vehiculo,
    rendimientoData: List<EfficiencyPoint>,
    costosMensualesData: List<EfficiencyPoint>,
    avgEfficiency: Double,
    avgMonthlyCost: Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera con Apodo o Marca/Modelo
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = vehiculo.apodo?.takeIf { it.isNotBlank() } ?: "${vehiculo.marca} ${vehiculo.modelo}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vehiculo.marca} ${vehiculo.modelo} (${vehiculo.anio})",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Badge { Text("Placa: ${vehiculo.placa}") }
            }
        }

        // Resumen de Estadísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryStatCard(
                modifier = Modifier.weight(1f),
                label = "Eficiencia Promedio",
                value = String.format(java.util.Locale.getDefault(), "%.1f km/L", avgEfficiency),
                icon = Icons.Default.LocalGasStation,
                color = MaterialTheme.colorScheme.primary
            )
            SummaryStatCard(
                modifier = Modifier.weight(1f),
                label = "Gasto Mensual Promedio",
                value = String.format(java.util.Locale.getDefault(), "$%.0f", avgMonthlyCost),
                icon = Icons.Default.AttachMoney,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Información Técnica
        Text("Información General", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(Icons.Default.Fingerprint, "VIN", vehiculo.vin ?: "N/A")
                DetailRow(Icons.Default.Info, "Estado", vehiculo.estado ?: "N/A")
            }
        }

        // Gráficos de Consumo
        if (rendimientoData.isNotEmpty()) {
            Text("Rendimiento de Combustible (km/L)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SimpleBarChart(
                points = rendimientoData,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (costosMensualesData.isNotEmpty()) {
            Text("Costos Mensuales ($)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SimpleBarChart(
                points = costosMensualesData,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label:", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(80.dp))
        Text(text = value)
    }
}

@Composable
fun SummaryStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SimpleBarChart(points: List<EfficiencyPoint>, color: Color) {
    val data = points.map { it.value }
    val labels = points.map { it.label }
    val maxVal = (data.maxOrNull() ?: 1f) * 1.3f
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                points.forEach { point ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = if (point.value > 100) String.format(java.util.Locale.getDefault(), "$%.0f", point.value) 
                                   else String.format(java.util.Locale.getDefault(), "%.1f", point.value),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight((point.value / maxVal).coerceAtLeast(0.05f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(color)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                labels.forEach { label ->
                    Text(
                        text = label, 
                        style = MaterialTheme.typography.labelSmall, 
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
