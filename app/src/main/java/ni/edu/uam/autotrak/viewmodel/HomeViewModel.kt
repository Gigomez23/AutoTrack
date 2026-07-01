package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import ni.edu.uam.autotrak.data.remote.model.Multa
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.remote.model.Licencia
import ni.edu.uam.autotrak.data.repository.UsuarioRepository
import ni.edu.uam.autotrak.data.repository.VehiculoRepository
import ni.edu.uam.autotrak.data.repository.RegistroCombustibleRepository
import ni.edu.uam.autotrak.data.repository.RegistroProblemaRepository
import ni.edu.uam.autotrak.data.repository.MultaRepository
import ni.edu.uam.autotrak.data.repository.LicenciaRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val sessionManager: SessionManager,
    private val usuarioRepository: UsuarioRepository,
    private val vehiculoRepository: VehiculoRepository,
    private val fuelRepository: RegistroCombustibleRepository,
    private val problemaRepository: RegistroProblemaRepository,
    private val multaRepository: MultaRepository,
    private val licenciaRepository: LicenciaRepository
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
        val pendingFines: Int = 0,
        val totalFinesAmount: Double = 0.0,
        val licencia: Licencia? = null,
        val isLicenciaExpiring: Boolean = false,
        val isLicenciaExpired: Boolean = false,
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
    val isRefreshing = _refreshing.asStateFlow()

    val uiState: StateFlow<HomeUiState> = if (userId != -1L) {
        val userFlow = usuarioRepository.observeUsuario(userId)
        val vehiclesFlow = vehiculoRepository.observeVehiculos(userId)
        val multasFlow = multaRepository.observeByUsuarioId(userId)
        val licenciaFlow = licenciaRepository.observeByUsuarioId(userId)
        
        combine(
            userFlow,
            vehiclesFlow,
            multasFlow,
            licenciaFlow,
            _refreshing
        ) { user, vehicles, multas, licencias, refreshing ->
            HomeUiParams(user, vehicles, multas, licencias.firstOrNull(), refreshing)
        }.flatMapLatest { params ->
            val user = params.user
            val vehicles = params.vehicles
            val multas = params.multas
            val licencia = params.licencia
            val refreshing = params.refreshing
            
            if (vehicles.isEmpty() && multas.isEmpty()) {
                flowOf(HomeUiState(isLoading = refreshing, user = user, vehicles = emptyList(), licencia = licencia))
            } else {
                val issueFlows = vehicles.map { v -> 
                    val vid = v.id ?: -1L
                    problemaRepository.observeByVehiculoId(vid).map { vid to it } 
                }
                val fuelFlows = vehicles.map { v -> 
                    val vid = v.id ?: -1L
                    fuelRepository.observeByVehiculoId(vid).map { vid to it } 
                }

                if (issueFlows.isEmpty() && fuelFlows.isEmpty()) {
                     flowOf(calculateHomeState(user, vehicles, emptyMap(), emptyMap(), multas, licencia, refreshing))
                } else {
                    combine(
                        if (issueFlows.isNotEmpty()) combine(issueFlows) { it.toMap() } else flowOf(emptyMap()),
                        if (fuelFlows.isNotEmpty()) combine(fuelFlows) { it.toMap() } else flowOf(emptyMap())
                    ) { issuesMap, fuelMap ->
                        calculateHomeState(user, vehicles, issuesMap, fuelMap, multas, licencia, refreshing)
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState(isLoading = true))
    } else {
        MutableStateFlow(HomeUiState(isLoading = false, error = "No active session"))
    }

    private data class HomeUiParams(
        val user: Usuario?,
        val vehicles: List<Vehiculo>,
        val multas: List<Multa>,
        val licencia: Licencia?,
        val refreshing: Boolean
    )

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        if (userId == -1L) return

        viewModelScope.launch {
            _refreshing.value = true
            try {
                try { usuarioRepository.refreshUsuario(userId) } catch (e: Exception) {}
                try { vehiculoRepository.refreshVehiculos(userId) } catch (e: Exception) {}
                try { multaRepository.refreshByUsuarioId(userId) } catch (e: Exception) {}
                try { licenciaRepository.refreshByUsuarioId(userId) } catch (e: Exception) {}
                
                val vehicles = try {
                    vehiculoRepository.observeVehiculos(userId).first()
                } catch (e: Exception) {
                    emptyList()
                }

                coroutineScope {
                    vehicles.mapNotNull { v ->
                        v.id?.let { id ->
                            async {
                                try {
                                    problemaRepository.refreshByVehiculoId(id)
                                    fuelRepository.refreshByVehiculoId(id)
                                } catch (e: Exception) {
                                    // Keep the dashboard visible even if one child refresh fails.
                                }
                            }
                        }
                    }.forEach { it.await() }
                }
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
        multas: List<Multa>,
        licencia: Licencia?,
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
                val date = it.fechaRegistro ?: LocalDate.now()
                "${date.year}-${date.monthValue}"
            }.mapValues { entry ->
                entry.value.sumOf { it.cantidadPagado?.toDouble() ?: 0.0 }
            }
            val avgMonthlyCost = if (costByMonth.isNotEmpty()) costByMonth.values.average() else 0.0

            // Calculate cost per km
            var costPerKm = 0.0
            if (sortedFuel.size >= 2) {
                val distance = sortedFuel.last().odometro - sortedFuel.first().odometro
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
        multas.forEach {
             activityItems.add(ActivityItem(
                id = "multa_${it.id}",
                type = ActivityType.ISSUE,
                title = "Nueva multa",
                subtitle = it.descripcion ?: "",
                timestamp = it.fechaMulta?.atStartOfDay() ?: LocalDateTime.now()
            ))
        }

        val isLicenciaExpiring = licencia?.fechaVencimiento?.let {
            val daysUntilExp = ChronoUnit.DAYS.between(LocalDate.now(), it)
            daysUntilExp in 0..30
        } ?: false

        val isLicenciaExpired = licencia?.fechaVencimiento?.let {
            it.isBefore(LocalDate.now())
        } ?: false

        return HomeUiState(
            isLoading = refreshing,
            user = user,
            vehicles = vehicles,
            totalVehicles = vehicles.size,
            openIssues = allIssues.count { it.activo },
            fleetEfficiency = if (efficiencies.isNotEmpty()) efficiencies.average().toFloat() else 0f,
            totalFuelRecords = allFuel.size,
            pendingFines = multas.count { !it.pagada },
            totalFinesAmount = multas.filter { !it.pagada }.sumOf { it.monto.toDouble() },
            licencia = licencia,
            isLicenciaExpiring = isLicenciaExpiring,
            isLicenciaExpired = isLicenciaExpired,
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
