package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.SessionManager

enum class ProblemFilter {
    ALL, ACTIVE, RESOLVED
}

enum class ProblemSort {
    NEWEST, OLDEST
}

class RegistroProblemaViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<RegistroProblema>>>(UiState.Success(emptyList()))
    val uiState = _uiState.asStateFlow()

    private val _vehiclesState = MutableStateFlow<UiState<List<Vehiculo>>>(UiState.Loading)
    val vehiclesState = _vehiclesState.asStateFlow()

    private val _selectedVehiculoId = MutableStateFlow<Long?>(null)
    val selectedVehiculoId = _selectedVehiculoId.asStateFlow()

    // Filtering and Searching
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(ProblemFilter.ALL)
    val filter = _filter.asStateFlow()

    private val _sort = MutableStateFlow(ProblemSort.NEWEST)
    val sort = _sort.asStateFlow()

    private val _allRegistros = MutableStateFlow<List<RegistroProblema>>(emptyList())

    val filteredRegistros = combine(_allRegistros, _searchQuery, _filter, _sort) { registros, query, filter, sort ->
        var filtered = registros.filter {
            (it.tipoProblema?.contains(query, ignoreCase = true) == true || 
             it.nota?.contains(query, ignoreCase = true) == true)
        }

        filtered = when (filter) {
            ProblemFilter.ALL -> filtered
            ProblemFilter.ACTIVE -> filtered.filter { it.activo }
            ProblemFilter.RESOLVED -> filtered.filter { !it.activo }
        }

        when (sort) {
            ProblemSort.NEWEST -> filtered.sortedByDescending { it.fechaRegistro }
            ProblemSort.OLDEST -> filtered.sortedBy { it.fechaRegistro }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        cargarRegistrosProblema(id)
    }

    fun cargarRegistrosProblema(vehiculoId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val registros = RetrofitClient.api_registro_problema.getRegistroProblemaByVehiculoId(vehiculoId)
                _allRegistros.value = registros
                _uiState.value = UiState.Success(registros)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al cargar los registros de problemas: ${e.message}")
            }
        }
    }

    fun crearRegistroProblema(vehiculoId: Long, registro: RegistroProblema) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_registro_problema.createRegistroProblema(vehiculoId, registro)
                cargarRegistrosProblema(vehiculoId)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al crear el registro: ${e.message}")
            }
        }
    }

    fun actualizarRegistroProblema(registro: RegistroProblema) {
        val id = registro.id ?: return
        val vehiculoId = _selectedVehiculoId.value ?: return
        viewModelScope.launch {
            try {
                RetrofitClient.api_registro_problema.updateRegistroProblema(id, registro)
                cargarRegistrosProblema(vehiculoId)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al actualizar el registro: ${e.message}")
            }
        }
    }

    fun markAsResolved(registro: RegistroProblema) {
        actualizarRegistroProblema(registro.copy(activo = false))
    }

    fun eliminarRegistroProblema(id: Long) {
        val vehiculoId = _selectedVehiculoId.value ?: return
        viewModelScope.launch {
            try {
                RetrofitClient.api_registro_problema.deleteRegistroProblema(id)
                cargarRegistrosProblema(vehiculoId)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al eliminar el registro: ${e.message}")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: ProblemFilter) {
        _filter.value = filter
    }

    fun setSort(sort: ProblemSort) {
        _sort.value = sort
    }
}
