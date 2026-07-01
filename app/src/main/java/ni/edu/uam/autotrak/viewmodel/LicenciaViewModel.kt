package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.remote.model.Licencia
import ni.edu.uam.autotrak.data.repository.LicenciaRepository
import java.time.LocalDate

sealed interface LicenciaUiState {
    object Loading : LicenciaUiState
    data class Success(val licencia: Licencia) : LicenciaUiState
    data class Error(val message: String) : LicenciaUiState
    object Empty : LicenciaUiState
}

class LicenciaViewModel(
    private val sessionManager: SessionManager,
    private val repository: LicenciaRepository
) : ViewModel() {

    private val usuarioId = sessionManager.getUserId()

    private val _uiState = MutableStateFlow<LicenciaUiState>(LicenciaUiState.Loading)
    val uiState: StateFlow<LicenciaUiState> = _uiState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        if (usuarioId != -1L) {
            observeLicencia()
            cargarLicencia()
        } else {
            _uiState.value = LicenciaUiState.Error("Usuario no autenticado")
        }
    }

    private fun observeLicencia() {
        viewModelScope.launch {
            repository.observeByUsuarioId(usuarioId)
                .collect { licencias ->
                    if (licencias.isEmpty()) {
                        _uiState.value = LicenciaUiState.Empty
                    } else {
                        // Forzamos la actualización del estado si hay datos locales
                        _uiState.value = LicenciaUiState.Success(licencias.first())
                    }
                }
        }
    }

    fun cargarLicencia() {
        if (usuarioId == -1L) return
        
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshByUsuarioId(usuarioId)
            } catch (e: Exception) {
                // Si ya tenemos datos, no mostramos error de pantalla completa
                if (_uiState.value !is LicenciaUiState.Success) {
                    _uiState.value = LicenciaUiState.Error(e.message ?: "Error desconocido")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun crearLicencia(licencia: Licencia) {
        viewModelScope.launch {
            try {
                repository.create(licencia.copy(usuarioId = usuarioId))
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun actualizarLicencia(id: Long, licencia: Licencia) {
        viewModelScope.launch {
            try {
                repository.update(id, licencia.copy(usuarioId = usuarioId))
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
