package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import ni.edu.uam.autotrak.data.remote.model.Vehiculo
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.repository.RegistroProblemaRepository
import ni.edu.uam.autotrak.data.repository.VehiculoRepository

enum class ProblemFilter {
    ALL, ACTIVE, RESOLVED
}

enum class ProblemSort {
    NEWEST, OLDEST
}

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroProblemaViewModel(
    private val sessionManager: SessionManager,
    private val problemaRepository: RegistroProblemaRepository,
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(ProblemFilter.ALL)
    val filter = _filter.asStateFlow()

    private val _sort = MutableStateFlow(ProblemSort.NEWEST)
    val sort = _sort.asStateFlow()

    // Single source of truth from Room, reacting to selection
    private val _rawRegistros: Flow<List<RegistroProblema>> = _selectedVehiculoId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else problemaRepository.observeByVehiculoId(id)
        }

    val uiState: StateFlow<UiState<List<RegistroProblema>>> = _rawRegistros
        .map { list -> UiState.Success(list) as UiState<List<RegistroProblema>> }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    val filteredRegistros: StateFlow<List<RegistroProblema>> = combine(
        _rawRegistros, _searchQuery, _filter, _sort
    ) { registros, query, filter, sort ->
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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun cargarVehiculos() {
        if (userId == -1L) return
        viewModelScope.launch {
            try {
                vehiculoRepository.refreshVehiculos(userId)
                
                // If a vehicle is selected, refresh its records too
                _selectedVehiculoId.value?.let { vid ->
                    problemaRepository.refreshByVehiculoId(vid)
                }
            } catch (_: Exception) {}
        }
    }

    fun seleccionarVehiculo(id: Long) {
        _selectedVehiculoId.value = id
        cargarRegistrosProblema(id)
    }

    fun cargarRegistrosProblema(vehiculoId: Long) {
        viewModelScope.launch {
            try {
                problemaRepository.refreshByVehiculoId(vehiculoId)
            } catch (_: Exception) {}
        }
    }

    fun crearRegistroProblema(vehiculoId: Long, registro: RegistroProblema) {
        viewModelScope.launch {
            try {
                problemaRepository.create(vehiculoId, registro)
            } catch (_: Exception) {}
        }
    }

    fun actualizarRegistroProblema(registro: RegistroProblema) {
        val id = registro.id ?: return
        viewModelScope.launch {
            try {
                problemaRepository.update(id, registro)
            } catch (_: Exception) {}
        }
    }

    fun markAsResolved(registro: RegistroProblema) {
        actualizarRegistroProblema(registro.copy(activo = false))
    }

    fun eliminarRegistroProblema(id: Long) {
        viewModelScope.launch {
            try {
                problemaRepository.delete(id)
            } catch (_: Exception) {}
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
