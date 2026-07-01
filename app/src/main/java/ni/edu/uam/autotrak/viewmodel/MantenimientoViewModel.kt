package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.model.ServicioMantenimiento
import ni.edu.uam.autotrak.data.remote.model.TipoMantenimiento
import ni.edu.uam.autotrak.data.repository.ServicioMantenimientoRepository

sealed interface MantenimientoListUiState {
    object Loading : MantenimientoListUiState
    data class Success(val mantenimientos: List<ServicioMantenimiento>) : MantenimientoListUiState
    data class Error(val message: String) : MantenimientoListUiState
    object Empty : MantenimientoListUiState
}

data class MantenimientoFormUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class MantenimientoViewModel(
    private val repository: ServicioMantenimientoRepository
) : ViewModel() {

    private var vehiculoId: Long = -1L

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _allMantenimientos = MutableStateFlow<List<ServicioMantenimiento>>(emptyList())
    
    private val _listUiState = MutableStateFlow<MantenimientoListUiState>(MantenimientoListUiState.Loading)
    val listUiState: StateFlow<MantenimientoListUiState> = _listUiState.asStateFlow()

    private val _formUiState = MutableStateFlow(MantenimientoFormUiState())
    val formUiState: StateFlow<MantenimientoFormUiState> = _formUiState.asStateFlow()

    fun cargarMantenimientos(id: Long) {
        if (id == vehiculoId) return
        vehiculoId = id
        observeMantenimientos()
        refreshMantenimientos()
    }

    private fun observeMantenimientos() {
        if (vehiculoId == -1L) return
        viewModelScope.launch {
            repository.observeByVehiculoId(vehiculoId).collect { list ->
                _allMantenimientos.value = list
                if (list.isEmpty()) {
                    _listUiState.value = MantenimientoListUiState.Empty
                } else {
                    _listUiState.value = MantenimientoListUiState.Success(list)
                }
            }
        }
    }

    fun refreshMantenimientos() {
        if (vehiculoId == -1L) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshByVehiculoId(vehiculoId)
            } catch (e: Exception) {
                if (_allMantenimientos.value.isEmpty()) {
                    _listUiState.value = MantenimientoListUiState.Error(e.message ?: "Error al cargar mantenimientos")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun createMantenimiento(servicio: ServicioMantenimiento) {
        if (vehiculoId == -1L) return
        viewModelScope.launch {
            _formUiState.value = MantenimientoFormUiState(isLoading = true)
            try {
                repository.create(vehiculoId, servicio)
                _formUiState.value = MantenimientoFormUiState(isSuccess = true)
            } catch (e: Exception) {
                _formUiState.value = MantenimientoFormUiState(error = e.message ?: "Error al crear mantenimiento")
            }
        }
    }

    fun updateMantenimiento(id: Long, servicio: ServicioMantenimiento) {
        viewModelScope.launch {
            _formUiState.value = MantenimientoFormUiState(isLoading = true)
            try {
                repository.update(id, servicio)
                _formUiState.value = MantenimientoFormUiState(isSuccess = true)
            } catch (e: Exception) {
                _formUiState.value = MantenimientoFormUiState(error = e.message ?: "Error al actualizar mantenimiento")
            }
        }
    }

    fun deleteMantenimiento(id: Long) {
        viewModelScope.launch {
            try {
                repository.delete(id)
            } catch (e: Exception) {
                // Manejar error silenciosamente o notificar si es crítico
            }
        }
    }

    fun resetFormState() {
        _formUiState.value = MantenimientoFormUiState()
    }

    fun getMantenimientoSync(id: Long): ServicioMantenimiento? {
        return _allMantenimientos.value.find { it.id == id }
    }
}
