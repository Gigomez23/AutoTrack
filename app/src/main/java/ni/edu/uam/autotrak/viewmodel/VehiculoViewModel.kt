package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.model.Vehiculo
import ni.edu.uam.autotrak.data.remote.RetrofitClient

class VehiculoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Vehiculo>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        cargarVehiculos()
    }

    fun cargarVehiculos() {
        viewModelScope.launch {
            try {
                val vehiculos = RetrofitClient.api_vehiculo.getVehiculos()
                _uiState.value = UiState.Success(vehiculos)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al cargar los vehiculos: ${e.message}")
            }
        }
    }

    fun buscarVehiculo(id: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val vehiculo = RetrofitClient.api_vehiculo.getVehiculoById(id)
                //Se mantiene un vehiuclo en el estado de éxito para simplificar la pantalla de detalles
                _uiState.value = UiState.Success(listOf(vehiculo))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al buscar el vehiculo: ${e.message}"
                )
            }
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
}