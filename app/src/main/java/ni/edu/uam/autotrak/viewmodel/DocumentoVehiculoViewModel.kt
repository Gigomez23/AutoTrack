package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.model.DocumentoVehiculo
import ni.edu.uam.autotrak.data.repository.DocumentoVehiculoRepository
import java.time.LocalDate

sealed interface DocumentoVehiculoUiState {
    object Loading : DocumentoVehiculoUiState
    data class Success(val documentos: List<DocumentoVehiculo>) : DocumentoVehiculoUiState
    data class Error(val message: String) : DocumentoVehiculoUiState
    object Empty : DocumentoVehiculoUiState
}

enum class DocumentoFilter {
    TODOS, SEGURO, CIRCULACION, VENCIDOS
}

class DocumentoVehiculoViewModel(
    private val repository: DocumentoVehiculoRepository
) : ViewModel() {

    private var vehiculoId: Long = -1L

    private val _filter = MutableStateFlow(DocumentoFilter.TODOS)
    val filter: StateFlow<DocumentoFilter> = _filter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _allDocumentos = MutableStateFlow<List<DocumentoVehiculo>>(emptyList())

    val uiState: StateFlow<DocumentoVehiculoUiState> = combine(_allDocumentos, _filter) { docs, filter ->
        val filtered = when (filter) {
            DocumentoFilter.TODOS -> docs
            DocumentoFilter.SEGURO -> docs.filter { it.nombre?.contains("Seguro", ignoreCase = true) == true }
            DocumentoFilter.CIRCULACION -> docs.filter { it.nombre?.contains("Circulación", ignoreCase = true) == true }
            DocumentoFilter.VENCIDOS -> docs.filter { it.fechaVencimiento?.isBefore(LocalDate.now()) == true }
        }

        if (docs.isEmpty()) {
            DocumentoVehiculoUiState.Empty
        } else {
            DocumentoVehiculoUiState.Success(filtered)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DocumentoVehiculoUiState.Loading)

    fun cargarDocumentos(id: Long) {
        if (id == vehiculoId) return
        vehiculoId = id
        observeDocumentos()
        refreshDocumentos()
    }

    private fun observeDocumentos() {
        if (vehiculoId == -1L) return
        viewModelScope.launch {
            repository.observeByVehiculoId(vehiculoId).collect {
                _allDocumentos.value = it
            }
        }
    }

    fun refreshDocumentos() {
        if (vehiculoId == -1L) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshByVehiculoId(vehiculoId)
            } catch (e: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setFilter(filter: DocumentoFilter) {
        _filter.value = filter
    }

    fun crearDocumento(documento: DocumentoVehiculo) {
        viewModelScope.launch {
            try {
                repository.create(vehiculoId, documento)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun actualizarDocumento(id: Long, documento: DocumentoVehiculo) {
        viewModelScope.launch {
            try {
                repository.update(id, documento)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun eliminarDocumento(id: Long) {
        viewModelScope.launch {
            try {
                repository.delete(id)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
