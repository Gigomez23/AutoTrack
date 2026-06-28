package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class HomeViewModel(private val sessionManager: SessionManager) : ViewModel() {

    data class HomeUiState(
        val isLoading: Boolean = true,
        val user: Usuario? = null,
        val vehicles: List<Vehiculo> = emptyList(),
        val totalVehicles: Int = 0,
        val openIssues: Int = 0,
        val fleetEfficiency: Float = 0f,
        val totalFuelRecords: Int = 0,
        val recentActivity: List<ActivityItem> = emptyList(),
        val insights: List<String> = emptyList(),
        val vehicleStats: Map<Long, VehicleMetrics> = emptyMap(),
        val error: String? = null
    )

    data class VehicleMetrics(
        val efficiency: Double = 0.0,
        val costPerKm: Double = 0.0,
        val hasActiveIssues: Boolean = false
    )

    data class ActivityItem(
        val id: String,
        val type: ActivityType,
        val title: String,
        val subtitle: String,
        val timestamp: LocalDateTime
    )

    enum class ActivityType {
        FUEL, ISSUE, MAINTENANCE, VEHICLE_ADDED
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            _uiState.value = _uiState.value.copy(error = "No active session", isLoading = false)
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val user = RetrofitClient.api_usuario.getUsuario(userId)
                val vehicles = RetrofitClient.api_vehiculo.getVehiculoByUsuarioId(userId)
                
                val allIssues = mutableListOf<RegistroProblema>()
                val allFuelRecords = mutableListOf<RegistroCombustible>()
                val vehicleMetricsMap = mutableMapOf<Long, VehicleMetrics>()
                
                vehicles.forEach { v ->
                    v.id?.let { id ->
                        val issues = RetrofitClient.api_registro_problema.getRegistroProblemaByVehiculoId(id)
                        allIssues.addAll(issues)
                        
                        val fuel = RetrofitClient.api_registro_combustible.getRegistroCombustibleByVehiculoId(id)
                        allFuelRecords.addAll(fuel)
                        
                        var rend = 0.0
                        var totalSpent = 0.0
                        try {
                            rend = RetrofitClient.api_registro_combustible.getRendimientoByVehiculoId(id)
                            totalSpent = RetrofitClient.api_registro_combustible.getTotalGastadoByVehiculoId(id)
                        } catch (_: Exception) {}

                        // Calculate cost per km if possible
                        val totalKm = if (fuel.size >= 2) {
                            val sorted = fuel.sortedBy { it.fechaRegistro }
                            (sorted.last().odometro - sorted.first().odometro).toDouble()
                        } else 0.0

                        vehicleMetricsMap[id] = VehicleMetrics(
                            efficiency = rend,
                            costPerKm = if (totalKm > 0) totalSpent / totalKm else 0.0,
                            hasActiveIssues = issues.any { it.activo }
                        )
                    }
                }

                val openIssuesCount = allIssues.count { it.activo }
                val efficiencies = vehicleMetricsMap.values.map { it.efficiency }.filter { it > 0 }
                val avgEfficiency = if (efficiencies.isNotEmpty()) efficiencies.average().toFloat() else 0f
                
                // Build Recent Activity
                val activityItems = mutableListOf<ActivityItem>()
                
                allFuelRecords.forEach { 
                    activityItems.add(ActivityItem(
                        id = "fuel_${it.id}",
                        type = ActivityType.FUEL,
                        title = "Combustible registrado",
                        subtitle = "${it.cantidadCombustible}L - ${it.fechaRegistro}",
                        timestamp = it.fechaRegistro?.atStartOfDay() ?: LocalDateTime.now()
                    ))
                }
                
                allIssues.forEach {
                    activityItems.add(ActivityItem(
                        id = "issue_${it.id}",
                        type = if (it.activo) ActivityType.ISSUE else ActivityType.MAINTENANCE,
                        title = if (it.activo) "Problema reportado" else "Mantenimiento completado",
                        subtitle = it.tipoProblema ?: "",
                        timestamp = it.fechaRegistro?.atStartOfDay() ?: LocalDateTime.now()
                    ))
                }
                
                val sortedActivity = activityItems.sortedByDescending { it.timestamp }.take(10)
                
                // Generate Insights
                val insights = mutableListOf<String>()
                
                if (vehicleMetricsMap.isNotEmpty()) {
                    val efficientEntry = vehicleMetricsMap.filter { it.value.efficiency > 0 }.maxByOrNull { it.value.efficiency }
                    efficientEntry?.let { entry ->
                        val mostEfficientVehicle = vehicles.find { it.id == entry.key }
                        mostEfficientVehicle?.let {
                            insights.add("${it.marca} ${it.modelo} es actualmente tu vehículo con mejor rendimiento.")
                        }
                    }
                }
                
                if (vehicles.any { it.estado == "CHACATA" }) {
                    val criticalCount = vehicles.count { it.estado == "CHACATA" }
                    insights.add("¡Atención! Tienes $criticalCount vehículo(s) en estado crítico que requieren revisión.")
                }
                
                if (openIssuesCount > 0) {
                    insights.add("Tienes $openIssuesCount problemas pendientes por resolver en tu flota.")
                } else if (vehicles.isNotEmpty()) {
                    insights.add("¡Excelente! No tienes problemas mecánicos reportados en este momento.")
                }

                _uiState.value = HomeUiState(
                    isLoading = false,
                    user = user,
                    vehicles = vehicles,
                    totalVehicles = vehicles.size,
                    openIssues = openIssuesCount,
                    fleetEfficiency = avgEfficiency,
                    totalFuelRecords = allFuelRecords.size,
                    recentActivity = sortedActivity,
                    insights = insights,
                    vehicleStats = vehicleMetricsMap
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }
    
    fun getRelativeTime(timestamp: LocalDateTime): String {
        val now = LocalDateTime.now()
        val days = ChronoUnit.DAYS.between(timestamp.toLocalDate(), now.toLocalDate())
        return when {
            days == 0L -> "Hoy"
            days == 1L -> "Ayer"
            days < 7L -> "Hace $days días"
            else -> "Hace ${days / 7} semanas"
        }
    }
}
