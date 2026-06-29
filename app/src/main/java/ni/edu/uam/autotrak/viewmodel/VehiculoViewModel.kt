package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.repository.VehiculoRepository
import ni.edu.uam.autotrak.data.repository.RegistroCombustibleRepository

import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible

@OptIn(ExperimentalCoroutinesApi::class)
class VehiculoViewModel(
    private val sessionManager: SessionManager,
    private val vehiculoRepository: VehiculoRepository,
    private val fuelRepository: RegistroCombustibleRepository
) : ViewModel() {

    private val userId = sessionManager.getUserId()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedVehicleId = MutableStateFlow<Long?>(null)

    // Primary UI State for Vehicle List
    val uiState: StateFlow<UiState<List<Vehiculo>>> = if (userId != -1L) {
        vehiculoRepository.observeVehiculos(userId)
            .map { list -> UiState.Success(list) as UiState<List<Vehiculo>> }
            .onStart { emit(UiState.Loading) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)
    } else {
        MutableStateFlow(UiState.Error("No hay usuario seleccionado"))
    }

    // Vehicle Summaries (Calculated reactively)
    val vehicleSummaries: StateFlow<Map<Long, VehicleStats>> = uiState
        .flatMapLatest { state ->
            if (state is UiState.Success) {
                val vehicles = state.data
                if (vehicles.isEmpty()) flowOf(emptyMap())
                else {
                    val fuelFlows = vehicles.map { v ->
                        val vid = v.id ?: -1L
                        fuelRepository.observeByVehiculoId(vid).map { vid to it }
                    }
                    combine(fuelFlows) { list ->
                        list.associate { (id, records) ->
                            id to calculateSummary(records)
                        }
                    }
                }
            } else flowOf(emptyMap())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // Detail UI State
    val detailUiState: StateFlow<UiState<Vehiculo>> = _selectedVehicleId
        .flatMapLatest { id ->
            if (id == null) flowOf(UiState.Loading)
            else vehiculoRepository.observeVehiculoById(id)
                .map { v -> if (v != null) UiState.Success(v) else UiState.Loading }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    // Stats for detail view
    private val _detailRecords = _selectedVehicleId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else fuelRepository.observeByVehiculoId(id)
    }

    val registrosCombustible: StateFlow<List<RegistroCombustible>> = _detailRecords
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val rendimientoData: StateFlow<List<EfficiencyPoint>> = _detailRecords.map { records ->
        calculateMonthlyEfficiency(records)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val costosMensualesData: StateFlow<List<EfficiencyPoint>> = _detailRecords.map { records ->
        calculateMonthlyCosts(records)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val averageEfficiency: StateFlow<Double> = rendimientoData.map { points ->
        if (points.isNotEmpty()) points.map { it.value.toDouble() }.average() else 0.0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val averageMonthlyCost: StateFlow<Double> = costosMensualesData.map { points ->
        if (points.isNotEmpty()) points.map { it.value.toDouble() }.average() else 0.0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    data class VehicleStats(
        val rendimientoPromedio: Double = 0.0,
        val costoMensualPromedio: Double = 0.0
    )

    init {
        cargarVehiculos()
    }

    private fun calculateSummary(records: List<RegistroCombustible>): VehicleStats {
        val sorted = records.sortedBy { it.fechaRegistro }
        
        // Calculate efficiency
        var efficiency = 0.0
        if (sorted.size >= 2) {
            val distance = sorted.last().odometro - sorted.first().odometro
            val fuel = sorted.dropLast(1).sumOf { it.cantidadCombustible }
            if (distance > 0 && fuel > 0) {
                efficiency = distance.toDouble() / fuel
            }
        }

        // Calculate average monthly cost
        val costByMonth = sorted.groupBy { 
            val date = it.fechaRegistro ?: java.time.LocalDate.now()
            "${date.year}-${date.monthValue}"
        }.mapValues { entry ->
            entry.value.sumOf { it.cantidadPagado?.toDouble() ?: 0.0 }
        }
        
        val avgMonthlyCost = if (costByMonth.isNotEmpty()) {
            costByMonth.values.average()
        } else 0.0
        
        return VehicleStats(
            rendimientoPromedio = efficiency,
            costoMensualPromedio = avgMonthlyCost
        )
    }

    private fun calculateMonthlyEfficiency(records: List<RegistroCombustible>): List<EfficiencyPoint> {
        val sorted = records.sortedBy { it.fechaRegistro }
        val efficiencyByMonthMap = mutableMapOf<String, MutableList<Float>>()
        
        for (i in 1 until sorted.size) {
            val prev = sorted[i-1]
            val curr = sorted[i]
            val distance = curr.odometro - prev.odometro
            if (distance > 0 && prev.cantidadCombustible > 0) {
                val efficiency = distance.toFloat() / prev.cantidadCombustible.toFloat()
                val date = curr.fechaRegistro ?: java.time.LocalDate.now()
                val key = String.format(Locale.US, "%04d-%02d", date.year, date.monthValue)
                efficiencyByMonthMap.getOrPut(key) { mutableListOf() }.add(efficiency)
            }
        }

        return efficiencyByMonthMap.toSortedMap().map { (key, list) ->
            val monthNum = key.split("-")[1].toInt()
            val monthName = Month.of(monthNum).getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-NI"))
            EfficiencyPoint(list.average().toFloat(), monthName)
        }.takeLast(6)
    }

    private fun calculateMonthlyCosts(records: List<RegistroCombustible>): List<EfficiencyPoint> {
        val sorted = records.sortedBy { it.fechaRegistro }
        val costByMonth = sorted.groupBy { 
            val date = it.fechaRegistro ?: java.time.LocalDate.now()
            String.format(Locale.US, "%04d-%02d", date.year, date.monthValue)
        }.mapValues { entry ->
            entry.value.sumOf { it.cantidadPagado?.toDouble() ?: 0.0 }.toFloat()
        }

        return costByMonth.toSortedMap().map { (key, value) ->
            val parts = key.split("-")
            val year = parts[0]
            val monthNum = parts[1].toInt()
            val monthName = Month.of(monthNum).getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-NI"))
            EfficiencyPoint(value, "$monthName ${year.takeLast(2)}")
        }.takeLast(6)
    }

    fun cargarVehiculos() {
        if (userId == -1L) return
        viewModelScope.launch {
            try {
                vehiculoRepository.refreshVehiculos(userId)
                
                // Also refresh child data for all vehicles
                val vehicles = vehiculoRepository.observeVehiculos(userId).first()
                vehicles.forEach { v ->
                    v.id?.let { vid ->
                        fuelRepository.refreshByVehiculoId(vid)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun buscarVehiculo(id: Long) {
        _selectedVehicleId.value = id
        viewModelScope.launch {
            try {
                vehiculoRepository.refreshVehiculoById(id)
                fuelRepository.refreshByVehiculoId(id)
            } catch (_: Exception) {}
        }
    }

    fun crearVehiculo(vehiculo: Vehiculo) {
        viewModelScope.launch {
            try {
                vehiculoRepository.createVehiculo(vehiculo)
            } catch (_: Exception) {}
        }
    }

    fun actualizarVehiculo(id: Long, vehiculo: Vehiculo) {
        viewModelScope.launch {
            try {
                vehiculoRepository.updateVehiculo(id, vehiculo)
            } catch (_: Exception) {}
        }
    }

    fun eliminarVehiculo(id: Long) {
        viewModelScope.launch {
            try {
                vehiculoRepository.deleteVehiculo(id)
            } catch (_: Exception) {}
        }
    }

    fun getUserId(): Long {
        return userId
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
