package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.ui.components.OfflineBanner
import ni.edu.uam.autotrak.viewmodel.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    isOffline: Boolean,
    onNavigateToVehicles: () -> Unit,
    onNavigateToFuel: () -> Unit,
    onNavigateToIssues: () -> Unit,
    onNavigateToMultas: () -> Unit,
    onNavigateToLicencia: () -> Unit,
    onVehicleClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OfflineBanner(isOffline = isOffline)
        
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadDashboardData() },
            modifier = Modifier.weight(1f)
        ) {
            if (uiState.isLoading && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadDashboardData() }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // 1. Greeting & Avatar
                    item {
                        HeaderSection(userName = uiState.user?.nombres ?: "Usuario")
                    }

                    // 2. Statistics 2x2 Grid
                    item {
                        StatsGrid(
                            totalVehicles = uiState.totalVehicles,
                            openIssues = uiState.openIssues,
                            avgEfficiency = uiState.fleetEfficiency,
                            totalFuelRecords = uiState.totalFuelRecords,
                            pendingFines = uiState.pendingFines
                        )
                    }

                    // 2.5 Multas Alert
                    if (uiState.pendingFines > 0) {
                        item {
                            FinesAlertSection(
                                pendingFines = uiState.pendingFines,
                                totalAmount = uiState.totalFinesAmount,
                                onAction = onNavigateToMultas
                            )
                        }
                    }

                    // 2.6 License Alert
                    if (uiState.isLicenciaExpired || uiState.isLicenciaExpiring) {
                        item {
                            LicenseAlertSection(
                                expiryDate = uiState.licencia?.fechaVencimiento?.toString() ?: "",
                                isExpired = uiState.isLicenciaExpired,
                                onAction = onNavigateToLicencia
                            )
                        }
                    }

                    // 3. Quick Actions
                    item {
                        QuickActionsSection(
                            onNavigateToFuel = onNavigateToFuel,
                            onNavigateToIssues = onNavigateToIssues,
                            onNavigateToVehicles = onNavigateToVehicles,
                            onNavigateToMultas = onNavigateToMultas
                        )
                    }

                    // 4. My Vehicles Carousel
                    item {
                        MyVehiclesSection(
                            vehicles = uiState.vehicles,
                            metricsMap = uiState.vehicleStats,
                            onVehicleClick = onVehicleClick,
                            onViewAll = onNavigateToVehicles
                        )
                    }

                    // 5. Recent Activity Timeline
                    if (uiState.recentActivity.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Actividad Reciente")
                        }
                        items(uiState.recentActivity) { activity ->
                            ActivityItemRow(
                                item = activity,
                                relativeTime = viewModel.getRelativeTime(activity.timestamp)
                            )
                        }
                    }

                    // 6. Insights
                    if (uiState.insights.isNotEmpty()) {
                        item {
                            InsightsSection(insights = uiState.insights)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "¡Buen día, $userName!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Este es el estado actual de tu flota.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun StatsGrid(
    totalVehicles: Int,
    openIssues: Int,
    avgEfficiency: Float,
    totalFuelRecords: Int,
    pendingFines: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Vehículos",
                value = totalVehicles.toString(),
                icon = Icons.Default.DirectionsCar,
                color = MaterialTheme.colorScheme.primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Problemas",
                value = openIssues.toString(),
                icon = Icons.Default.ReportProblem,
                color = if (openIssues > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Rendimiento",
                value = String.format(Locale.getDefault(), "%.1f km/L", avgEfficiency),
                icon = Icons.Default.LocalGasStation,
                color = MaterialTheme.colorScheme.tertiary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Multas",
                value = pendingFines.toString(),
                icon = Icons.Default.ReceiptLong,
                color = if (pendingFines > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickActionsSection(
    onNavigateToFuel: () -> Unit,
    onNavigateToIssues: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToMultas: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Acciones Rápidas")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Combustible",
                icon = Icons.Default.LocalGasStation,
                onClick = onNavigateToFuel
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Problemas",
                icon = Icons.Default.Warning,
                onClick = onNavigateToIssues
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Multas",
                icon = Icons.Default.ReceiptLong,
                onClick = onNavigateToMultas
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Flota",
                icon = Icons.AutoMirrored.Filled.List,
                onClick = onNavigateToVehicles
            )
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MyVehiclesSection(
    vehicles: List<Vehiculo>,
    metricsMap: Map<Long, HomeViewModel.VehicleMetrics>,
    onVehicleClick: (Long) -> Unit,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Mis Vehículos")
            TextButton(onClick = onViewAll) {
                Text("Ver todos")
            }
        }
        
        if (vehicles.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No tienes vehículos registrados")
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vehicles) { vehiculo ->
                    VehicleCarouselCard(
                        vehiculo = vehiculo,
                        metrics = metricsMap[vehiculo.id],
                        onClick = { vehiculo.id?.let { onVehicleClick(it) } }
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleCarouselCard(
    vehiculo: Vehiculo,
    metrics: HomeViewModel.VehicleMetrics?,
    onClick: () -> Unit
) {
    val statusColor = when (vehiculo.estado) {
        "CHUQUITI" -> Color(0xFF4CAF50) // Green
        "CHIQUITI" -> Color(0xFFFFC107) // Amber
        "CHACATA" -> Color(0xFFF44336) // Red
        else -> MaterialTheme.colorScheme.outline
    }
    
    val statusText = when (vehiculo.estado) {
        "CHUQUITI" -> "Saludable"
        "CHIQUITI" -> "Atención"
        "CHACATA" -> "Crítico"
        else -> vehiculo.estado ?: "Desconocido"
    }

    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
                    color = statusColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${vehiculo.marca} ${vehiculo.modelo}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Año: ${vehiculo.anio}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SummaryItem(
                        label = "Eficiencia",
                        value = if ((metrics?.efficiency ?: 0.0) > 0) String.format(Locale.getDefault(), "%.1f km/L", metrics?.efficiency) else "N/A"
                    )
                    SummaryItem(
                        label = "Costo Mensual",
                        value = if ((metrics?.averageMonthlyCost ?: 0.0) > 0) String.format(Locale.getDefault(), "$%.0f", metrics?.averageMonthlyCost) else "N/A"
                    )
                }
                
                if (metrics?.hasActiveIssues == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Problema activo", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF44336))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActivityItemRow(item: HomeViewModel.ActivityItem, relativeTime: String) {
    val icon = when (item.type) {
        HomeViewModel.ActivityType.FUEL -> Icons.Default.LocalGasStation
        HomeViewModel.ActivityType.ISSUE -> Icons.Default.ReportProblem
        HomeViewModel.ActivityType.MAINTENANCE -> Icons.Default.Build
        HomeViewModel.ActivityType.VEHICLE_ADDED -> Icons.Default.Add
    }
    
    val color = when (item.type) {
        HomeViewModel.ActivityType.FUEL -> MaterialTheme.colorScheme.primary
        HomeViewModel.ActivityType.ISSUE -> MaterialTheme.colorScheme.error
        HomeViewModel.ActivityType.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
        HomeViewModel.ActivityType.VEHICLE_ADDED -> MaterialTheme.colorScheme.secondary
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = relativeTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun InsightsSection(insights: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            insights.forEach { insight ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "•", modifier = Modifier.width(16.dp))
                    Text(text = insight, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun FinesAlertSection(
    pendingFines: Int,
    totalAmount: Double,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFE0B2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color(0xFFE65100))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tienes $pendingFines multas pendientes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "Total a pagar: $${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE65100).copy(alpha = 0.8f)
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFE65100))
        }
    }
}

@Composable
fun LicenseAlertSection(
    expiryDate: String,
    isExpired: Boolean,
    onAction: () -> Unit
) {
    val containerColor = if (isExpired) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
    }
    
    val contentColor = if (isExpired) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(contentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isExpired) Icons.Default.Dangerous else Icons.Default.Warning,
                    contentDescription = null,
                    tint = contentColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isExpired) "Tu licencia está vencida" else "Tu licencia está por vencer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = "Fecha de vencimiento: $expiryDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = contentColor)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
