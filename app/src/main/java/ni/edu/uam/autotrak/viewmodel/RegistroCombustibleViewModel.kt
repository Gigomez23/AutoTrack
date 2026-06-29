package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.repository.RegistroCombustibleRepository
import ni.edu.uam.autotrak.data.repository.VehiculoRepository
import java.time.format.DateTimeFormatter
import java.util.*

enum class FuelChartType {
    LINE, BAR
}

data class EfficiencyPoint(
    val value: Float,
    val label: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroCombustibleViewModel(
    private val sessionManager: SessionManager,
    private val fuelRepository: RegistroCombustibleRepository,
    private val vehiculoRepository: VehiculoRepository
) : ViewModel() {

    private val userId = sessionManager.getUserId()

    val vehiclesState: StateFlow<UiState<List<Vehiculo>>> = if (userId != -1L) {
        vehiculoRepository.observeVehiculos(userId)
            .map { list -> UiState.Success(list) as UiState<List<Vehiculo>> }
            .onStart { emit(UiState.Loading) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)
    } else {
        MutableStateFlow(UiState.Error("No hay usuario seleccionado"))
    }

    private val _selectedVehiculoId = MutableStateFlow<Long?>(null)
    val selectedVehiculoId = _selectedVehiculoId.asStateFlow()

    private val _selectedChartType = MutableStateFlow(FuelChartType.LINE)
    val selectedChartType = _selectedChartType.asStateFlow()

    // Observable data from Room, reacting to selected vehicle
    val uiState: StateFlow<UiState<List<RegistroCombustible>>> = _selectedVehiculoId
        .flatMapLatest { id ->
            if (id == null) flowOf(UiState.Success(emptyList()))
            else fuelRepository.observeByVehiculoId(id).map { UiState.Success(it) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    // Derived statistics
    val lineEfficiencyData = uiState.map { state ->
        if (state is UiState.Success) calculateLineStats(state.data) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val monthlyEfficiencyData = uiState.map { state ->
        if (state is UiState.Success) calculateMonthlyStats(state.data) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun cargarVehiculos() {
        if (userId == -1L) return
        viewModelScope.launch {
            try {
                vehiculoRepository.refreshVehiculos(userId)
                
                // If a vehicle is selected, refresh its records too
                _selectedVehiculoId.value?.let { vid ->
                    fuelRepository.refreshByVehiculoId(vid)
                }
            } catch (_: Exception) {}
        }
    }

    fun seleccionarVehiculo(id: Long) {
        _selectedVehiculoId.value = id
        cargarRegistrosCombustible(id)
    }

    fun cargarRegistrosCombustible(vehiculoId: Long) {
        viewModelScope.launch {
            try {
                fuelRepository.refreshByVehiculoId(vehiculoId)
            } catch (_: Exception) {}
        }
    }

    fun crearRegistroCombustible(vehiculoId: Long, registroCombustible: RegistroCombustible) {
        viewModelScope.launch {
            try {
                fuelRepository.create(vehiculoId, registroCombustible)
            } catch (_: Exception) {}
        }
    }

    fun setChartType(type: FuelChartType) {
        _selectedChartType.value = type
    }

    private fun calculateLineStats(registros: List<RegistroCombustible>): List<EfficiencyPoint> {
        val sorted = registros.sortedBy { it.fechaRegistro }
        val points = mutableListOf<EfficiencyPoint>()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
        for (i in 1 until sorted.size) {
            val prev = sorted[i-1]
            val curr = sorted[i]
            val distance = curr.odometro - prev.odometro
            if (distance > 0 && prev.cantidadCombustible > 0) {
                points.add(EfficiencyPoint(
                    value = distance.toFloat() / prev.cantidadCombustible.toFloat(),
                    label = curr.fechaRegistro?.format(dateFormatter) ?: ""
                ))
            }
        }
        return points
    }

    private fun calculateMonthlyStats(registros: List<RegistroCombustible>): List<EfficiencyPoint> {
        val sorted = registros.sortedBy { it.fechaRegistro }
        val monthEfficiencyMap = mutableMapOf<String, MutableList<Float>>()
        val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("es-NI"))
        val orderedMonths = mutableListOf<String>()

        for (i in 1 until sorted.size) {
            val prev = sorted[i-1]
            val curr = sorted[i]
            val distance = curr.odometro - prev.odometro
            if (distance > 0 && prev.cantidadCombustible > 0) {
                val efficiency = distance.toFloat() / prev.cantidadCombustible.toFloat()
                val monthLabel = curr.fechaRegistro?.format(monthLabelFormatter) ?: "N/A"
                if (!monthEfficiencyMap.containsKey(monthLabel)) orderedMonths.add(monthLabel)
                monthEfficiencyMap.getOrPut(monthLabel) { mutableListOf() }.add(efficiency)
            }
        }
        return orderedMonths.map { label ->
            EfficiencyPoint(value = monthEfficiencyMap[label]?.average()?.toFloat() ?: 0f, label = label)
        }
    }
}
