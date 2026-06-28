package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.remote.model.Vehiculo

import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import ni.edu.uam.autotrak.data.remote.model.RegistroCombustible

class VehiculoViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Vehiculo>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _detailUiState = MutableStateFlow<UiState<Vehiculo>>(UiState.Loading)
    val detailUiState = _detailUiState.asStateFlow()

    private val _registrosCombustible = MutableStateFlow<List<RegistroCombustible>>(emptyList())
    val registrosCombustible = _registrosCombustible.asStateFlow()

    private val _rendimientoData = MutableStateFlow<List<EfficiencyPoint>>(emptyList())
    val rendimientoData = _rendimientoData.asStateFlow()

    private val _costosMensualesData = MutableStateFlow<List<EfficiencyPoint>>(emptyList())
    val costosMensualesData = _costosMensualesData.asStateFlow()

    private val _averageEfficiency = MutableStateFlow(0.0)
    val averageEfficiency = _averageEfficiency.asStateFlow()

    private val _averageMonthlyCost = MutableStateFlow(0.0)
    val averageMonthlyCost = _averageMonthlyCost.asStateFlow()

    private val _vehicleSummaries = MutableStateFlow<Map<Long, VehicleStats>>(emptyMap())
    val vehicleSummaries = _vehicleSummaries.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    data class VehicleStats(
        val rendimientoPromedio: Double = 0.0,
        val costoMensualPromedio: Double = 0.0
    )

    init {
        cargarVehiculos()
    }

    fun cargarVehiculos() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            _uiState.value = UiState.Error("No hay usuario seleccionado")
            return
        }
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val vehiculos = RetrofitClient.api_vehiculo.getVehiculoByUsuarioId(userId)
                _uiState.value = UiState.Success(vehiculos)
                
                // Fetch summaries for each vehicle in parallel
                val summaries = mutableMapOf<Long, VehicleStats>()
                
                kotlinx.coroutines.coroutineScope {
                    vehiculos.map { v ->
                        v.id?.let { id ->
                            launch {
                                try {
                                    val rend = RetrofitClient.api_registro_combustible.getRendimientoByVehiculoId(id)
                                    val totalGastado = RetrofitClient.api_registro_combustible.getTotalGastadoByVehiculoId(id)
                                    val registros = RetrofitClient.api_registro_combustible.getRegistroCombustibleByVehiculoId(id)
                                    val months = registros.groupBy { it.fechaRegistro?.month }.size.coerceAtLeast(1)
                                    
                                    synchronized(summaries) {
                                        summaries[id] = VehicleStats(
                                            rendimientoPromedio = rend,
                                            costoMensualPromedio = totalGastado / months
                                        )
                                    }
                                } catch (_: Exception) {
                                    synchronized(summaries) {
                                        summaries[id] = VehicleStats()
                                    }
                                }
                            }
                        }
                    }
                }
                _vehicleSummaries.value = summaries
                
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al cargar los vehiculos: ${e.message}")
            }
        }
    }

    fun buscarVehiculo(id: Long) {
        viewModelScope.launch {
            try {
                _detailUiState.value = UiState.Loading
                val vehiculo = RetrofitClient.api_vehiculo.getVehiculoById(id)
                _detailUiState.value = UiState.Success(vehiculo)
                
                // Also load fuel records for charts
                val registros = RetrofitClient.api_registro_combustible.getRegistroCombustibleByVehiculoId(id)
                _registrosCombustible.value = registros
                calcularEstadisticasDetalle(registros)
                
            } catch (e: Exception) {
                _detailUiState.value = UiState.Error(
                    "Error al buscar el vehiculo: ${e.message}"
                )
            }
        }
    }

    private fun calcularEstadisticasDetalle(registros: List<RegistroCombustible>) {
        val sorted = registros.sortedBy { it.fechaRegistro }
        
        // Group efficiency by month
        val efficiencyByMonthMap = mutableMapOf<String, MutableList<Float>>()
        val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es-NI"))
        
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

        val formattedEfficiencyPoints = efficiencyByMonthMap.toSortedMap().map { (key, list) ->
            val parts = key.split("-")
            val monthNum = parts[1].toInt()
            val monthName = Month.of(monthNum).getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-NI"))
            EfficiencyPoint(list.average().toFloat(), monthName)
        }.takeLast(6)

        // Monthly Cost calculation
        val costByMonth = sorted.groupBy { 
            val date = it.fechaRegistro ?: java.time.LocalDate.now()
            String.format(Locale.US, "%04d-%02d", date.year, date.monthValue)
        }.mapValues { entry ->
            entry.value.sumOf { it.cantidadPagado?.toDouble() ?: 0.0 }.toFloat()
        }

        val formattedCostPoints = costByMonth.toSortedMap().map { (key, value) ->
            val parts = key.split("-")
            val year = parts[0]
            val monthNum = parts[1].toInt()
            val monthName = Month.of(monthNum).getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-NI"))
            EfficiencyPoint(value, "$monthName ${year.takeLast(2)}")
        }.takeLast(6)
        
        _rendimientoData.value = formattedEfficiencyPoints
        _costosMensualesData.value = formattedCostPoints
        
        // Calculate vehicle-specific averages
        if (formattedEfficiencyPoints.isNotEmpty()) {
            _averageEfficiency.value = formattedEfficiencyPoints.map { it.value }.average()
        } else {
            _averageEfficiency.value = 0.0
        }
        
        if (costByMonth.isNotEmpty()) {
            _averageMonthlyCost.value = costByMonth.values.average()
        } else {
            _averageMonthlyCost.value = 0.0
        }
    }

    fun crearVehiculo(vehiculo: Vehiculo) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_vehiculo.createVehiculo(vehiculo)
                cargarVehiculos()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al crear el vehiculo: ${e.message}"
                )
            }
        }
    }

    fun actualizarVehiculo(id: Long, vehiculo: Vehiculo) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_vehiculo.updateVehiculo(id, vehiculo)
                cargarVehiculos()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al actualizar el vehiculo: ${e.message}"
                )
            }
        }
    }

    fun eliminarVehiculo(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_vehiculo.deleteVehiculo(id)
                cargarVehiculos()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al eliminar el vehiculo: ${e.message}"
                )
            }
        }
    }

    fun getUserId(): Long {
        return sessionManager.getUserId()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
