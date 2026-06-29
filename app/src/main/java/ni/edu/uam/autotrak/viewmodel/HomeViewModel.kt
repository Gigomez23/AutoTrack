package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.repository.UsuarioRepository
import ni.edu.uam.autotrak.data.repository.VehiculoRepository
import ni.edu.uam.autotrak.data.repository.RegistroCombustibleRepository
import ni.edu.uam.autotrak.data.repository.RegistroProblemaRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val sessionManager: SessionManager,
    private val usuarioRepository: UsuarioRepository,
    private val vehiculoRepository: VehiculoRepository,
    private val fuelRepository: RegistroCombustibleRepository,
    private val problemaRepository: RegistroProblemaRepository
) : ViewModel() {

    private val userId = sessionManager.getUserId()

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
        val averageMonthlyCost: Double = 0.0,
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

    private val _refreshing = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = if (userId != -1L) {
        val userFlow = usuarioRepository.observeUsuario(userId)
        val vehiclesFlow = vehiculoRepository.observeVehiculos(userId)
        
        combine(
            userFlow,
            vehiclesFlow,
            _refreshing
        ) { user, vehicles, refreshing ->
            HomeUiParams(user, vehicles, refreshing)
        }.flatMapLatest { params ->
            val user = params.user
            val vehicles = params.vehicles
            val refreshing = params.refreshing
            
            if (vehicles.isEmpty()) {
                flowOf(HomeUiState(isLoading = refreshing, user = user, vehicles = emptyList()))
            } else {
                val issueFlows = vehicles.map { v -> 
                    val vid = v.id ?: -1L
                    problemaRepository.observeByVehiculoId(vid).map { vid to it } 
                }
                val fuelFlows = vehicles.map { v -> 
                    val vid = v.id ?: -1L
                    fuelRepository.observeByVehiculoId(vid).map { vid to it } 
                }

                combine(
                    combine(issueFlows) { it.toMap() },
                    combine(fuelFlows) { it.toMap() }
                ) { issuesMap, fuelMap ->
                    calculateHomeState(user, vehicles, issuesMap, fuelMap, refreshing)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState(isLoading = true))
    } else {
        MutableStateFlow(HomeUiState(isLoading = false, error = "No active session"))
    }

    private data class HomeUiParams(
        val user: Usuario?,
        val vehicles: List<Vehiculo>,
        val refreshing: Boolean
    )

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        if (userId == -1L) return

        viewModelScope.launch {
            try {
                _refreshing.value = true
                usuarioRepository.refreshUsuario(userId)
                vehiculoRepository.refreshVehiculos(userId)
                
                val vehicles = vehiculoRepository.observeVehiculos(userId).first()
                vehicles.forEach { v ->
                    v.id?.let { id ->
                        problemaRepository.refreshByVehiculoId(id)
                        fuelRepository.refreshByVehiculoId(id)
                    }
                }
            } catch (_: Exception) {
            } finally {
                _refreshing.value = false
            }
        }
    }

    private fun calculateHomeState(
        user: Usuario?,
        vehicles: List<Vehiculo>,
        issuesMap: Map<Long, List<RegistroProblema>>,
        fuelMap: Map<Long, List<RegistroCombustible>>,
        refreshing: Boolean
    ): HomeUiState {
        val vehicleMetricsMap = mutableMapOf<Long, VehicleMetrics>()
        val allIssues = issuesMap.values.flatten()
        val allFuel = fuelMap.values.flatten()
        val efficiencies = mutableListOf<Double>()

        vehicles.forEach { v ->
            val id = v.id ?: return@forEach
            val vehicleFuel = fuelMap[id] ?: emptyList()
            val vehicleIssues = issuesMap[id] ?: emptyList()
            
            // Calculate efficiency and cost for this vehicle
            val sortedFuel = vehicleFuel.sortedBy { it.fechaRegistro }
            var vehicleEff = 0.0
            if (sortedFuel.size >= 2) {
                val distance = sortedFuel.last().odometro - sortedFuel.first().odometro
                val totalFuel = sortedFuel.dropLast(1).sumOf { it.cantidadCombustible }
                if (distance > 0 && totalFuel > 0) {
                    vehicleEff = distance.toDouble() / totalFuel
                    efficiencies.add(vehicleEff)
                }
            }

            val costByMonth = sortedFuel.groupBy { 
                val date = it.fechaRegistro ?: java.time.LocalDate.now()
                "${date.year}-${date.monthValue}"
            }.mapValues { entry ->
                entry.value.sumOf { it.cantidadPagado?.toDouble() ?: 0.0 }
            }
            val avgMonthlyCost = if (costByMonth.isNotEmpty()) costByMonth.values.average() else 0.0

            // Calculate cost per km
            var costPerKm = 0.0
            if (sortedFuel.size >= 2) {
                val distance = sortedFuel.last().odometro - sortedFuel.first().odometro
                
                // For cost per km, we should ideally use the cost of fuel used for that distance
                // Here we sum all costs excluding the first record (since that's the starting point for distance)
                // but usually distance between records i and i+1 is powered by fuel at record i.
                // To keep it simple and consistent with efficiency calculation:
                val relevantCost = sortedFuel.dropLast(1).sumOf { it.cantidadPagado?.toDouble() ?: 0.0 }
                
                if (distance > 0) {
                    costPerKm = relevantCost / distance
                }
            }

            vehicleMetricsMap[id] = VehicleMetrics(
                efficiency = vehicleEff,
                costPerKm = costPerKm,
                averageMonthlyCost = avgMonthlyCost,
                hasActiveIssues = vehicleIssues.any { it.activo }
            )
        }

        val activityItems = mutableListOf<ActivityItem>()
        allFuel.forEach { 
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

        return HomeUiState(
            isLoading = refreshing,
            user = user,
            vehicles = vehicles,
            totalVehicles = vehicles.size,
            openIssues = allIssues.count { it.activo },
            fleetEfficiency = if (efficiencies.isNotEmpty()) efficiencies.average().toFloat() else 0f,
            totalFuelRecords = allFuel.size,
            recentActivity = activityItems.sortedByDescending { it.timestamp }.take(10),
            vehicleStats = vehicleMetricsMap
        )
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
