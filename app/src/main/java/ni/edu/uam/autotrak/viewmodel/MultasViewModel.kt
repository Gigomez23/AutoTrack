package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.remote.model.Multa
import ni.edu.uam.autotrak.data.repository.MultaRepository

sealed interface MultasUiState {
    object Loading : MultasUiState
    data class Success(val multas: List<Multa>) : MultasUiState
    data class Error(val message: String) : MultasUiState
    object Empty : MultasUiState
}

enum class MultaFilter {
    TODAS, PENDIENTES, PAGADAS
}

class MultasViewModel(
    private val sessionManager: SessionManager,
    private val repository: MultaRepository
) : ViewModel() {

    private val usuarioId = sessionManager.getUserId()

    private val _filter = MutableStateFlow(MultaFilter.TODAS)
    val filter: StateFlow<MultaFilter> = _filter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _allMultas = MutableStateFlow<List<Multa>>(emptyList())
    
    private val _uiState = MutableStateFlow<MultasUiState>(MultasUiState.Loading)
    val uiState: StateFlow<MultasUiState> = combine(_allMultas, _filter) { multas, filter ->
        val filtered = when (filter) {
            MultaFilter.TODAS -> multas
            MultaFilter.PENDIENTES -> multas.filter { !it.pagada }
            MultaFilter.PAGADAS -> multas.filter { it.pagada }
        }
        
        if (filtered.isEmpty() && multas.isNotEmpty() && filter != MultaFilter.TODAS) {
            // Si hay multas pero el filtro las oculta todas, mostramos lista vacía filtrada (o podriamos manejarlo diferente)
            MultasUiState.Success(emptyList())
        } else if (multas.isEmpty()) {
            MultasUiState.Empty
        } else {
            MultasUiState.Success(filtered)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MultasUiState.Loading)

    init {
        if (usuarioId != -1L) {
            observeMultas()
            cargarMultas()
        } else {
            _uiState.value = MultasUiState.Error("Usuario no autenticado")
        }
    }

    private fun observeMultas() {
        viewModelScope.launch {
            repository.observeByUsuarioId(usuarioId).collect {
                _allMultas.value = it
            }
        }
    }

    fun cargarMultas() {
        if (usuarioId == -1L) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshByUsuarioId(usuarioId)
            } catch (e: Exception) {
                if (_allMultas.value.isEmpty()) {
                    _uiState.value = MultasUiState.Error(e.message ?: "Error al cargar multas")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshMultas() = cargarMultas()

    fun setFilter(filter: MultaFilter) {
        _filter.value = filter
    }

    fun getMultaSync(id: Long): Multa? {
        return _allMultas.value.find { it.id == id }
    }

    fun crearMulta(multa: Multa) {
        viewModelScope.launch {
            try {
                repository.create(multa.copy(usuarioId = usuarioId))
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun actualizarMulta(id: Long, multa: Multa) {
        viewModelScope.launch {
            try {
                repository.update(id, multa.copy(usuarioId = usuarioId))
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun eliminarMulta(id: Long) {
        viewModelScope.launch {
            try {
                repository.delete(id)
            } catch (e: Exception) {
                // Manejar error de eliminación
            }
        }
    }
}
