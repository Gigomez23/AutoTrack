package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.SessionManager
import java.time.format.DateTimeFormatter
import java.util.*

enum class FuelChartType {
    LINE, BAR
}

data class EfficiencyPoint(
    val value: Float,
    val label: String
)

class RegistroCombustibleViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<RegistroCombustible>>>(UiState.Success(emptyList()))
    val uiState = _uiState.asStateFlow()

    private val _vehiclesState = MutableStateFlow<UiState<List<Vehiculo>>>(UiState.Loading)
    val vehiclesState = _vehiclesState.asStateFlow()

    private val _selectedVehiculoId = MutableStateFlow<Long?>(null)
    val selectedVehiculoId = _selectedVehiculoId.asStateFlow()

    private val _selectedChartType = MutableStateFlow(FuelChartType.LINE)
    val selectedChartType = _selectedChartType.asStateFlow()

    private val _lineEfficiencyData = MutableStateFlow<List<EfficiencyPoint>>(emptyList())
    val lineEfficiencyData = _lineEfficiencyData.asStateFlow()

    private val _monthlyEfficiencyData = MutableStateFlow<List<EfficiencyPoint>>(emptyList())
    val monthlyEfficiencyData = _monthlyEfficiencyData.asStateFlow()

    fun cargarVehiculos() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            _vehiclesState.value = UiState.Error("No hay usuario seleccionado")
            return
        }
        viewModelScope.launch {
            try {
                _vehiclesState.value = UiState.Loading
                val vehicles = RetrofitClient.api_vehiculo.getVehiculoByUsuarioId(userId)
                _vehiclesState.value = UiState.Success(vehicles)
            } catch (e: Exception) {
                _vehiclesState.value = UiState.Error("Error al cargar vehículos: ${e.message}")
            }
        }
    }

    fun seleccionarVehiculo(id: Long) {
        _selectedVehiculoId.value = id
        cargarRegistrosCombustible(id)
    }

    fun cargarRegistrosCombustible(vehiculoId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val registros = RetrofitClient.api_registro_combustible.getRegistroCombustibleByVehiculoId(vehiculoId)
                _uiState.value = UiState.Success(registros)
                calcularEstadisticas(registros)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al cargar los registro del consumo de combustible: ${e.message}")
            }
        }
    }

    fun crearRegistroCombustible(vehiculoId: Long, registroCombustible: RegistroCombustible) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_registro_combustible.createRegistroCombustible(vehiculoId, registroCombustible)
                cargarRegistrosCombustible(vehiculoId)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al crear el registro del consumo de combustible: ${e.message}"
                )
            }
        }
    }

    fun setChartType(type: FuelChartType) {
        _selectedChartType.value = type
    }

    private fun calcularEstadisticas(registros: List<RegistroCombustible>) {
        val sorted = registros.sortedBy { it.fechaRegistro }
        
        // Line chart data (individual refuels)
        val linePoints = mutableListOf<EfficiencyPoint>()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
        
        // Use a map to group efficiencies by month for the bar chart
        val monthEfficiencyMap = mutableMapOf<String, MutableList<Float>>()
        val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("es-NI"))
        val orderedMonths = mutableListOf<String>()

        for (i in 1 until sorted.size) {
            val prev = sorted[i-1]
            val curr = sorted[i]
            val distance = curr.odometro - prev.odometro
            
            if (distance > 0 && prev.cantidadCombustible > 0) {
                val efficiency = distance.toFloat() / prev.cantidadCombustible.toFloat()
                
                // Add to line chart
                linePoints.add(EfficiencyPoint(
                    value = efficiency,
                    label = curr.fechaRegistro?.format(dateFormatter) ?: ""
                ))
                
                // Add to monthly grouping
                val monthLabel = curr.fechaRegistro?.format(monthLabelFormatter) ?: "N/A"
                if (!monthEfficiencyMap.containsKey(monthLabel)) {
                    orderedMonths.add(monthLabel)
                }
                monthEfficiencyMap.getOrPut(monthLabel) { mutableListOf() }.add(efficiency)
            }
        }
        
        _lineEfficiencyData.value = linePoints

        // Calculate monthly averages in chronological order
        _monthlyEfficiencyData.value = orderedMonths.map { label ->
            EfficiencyPoint(
                value = monthEfficiencyMap[label]?.average()?.toFloat() ?: 0f,
                label = label
            )
        }
    }
}
