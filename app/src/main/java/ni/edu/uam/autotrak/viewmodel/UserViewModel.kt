package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.repository.UsuarioRepository

class UserViewModel(
    private val sessionManager: SessionManager,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val userId = sessionManager.getUserId()

    private val _error = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val uiState: StateFlow<UiState<List<Usuario>>> = if (userId != -1L) {
        combine(usuarioRepository.observeUsuario(userId), _error) { user, error ->
            when {
                user != null -> UiState.Success(listOf(user))
                error != null -> UiState.Error(error)
                else -> UiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)
    } else {
        MutableStateFlow(UiState.Error("No hay usuario seleccionado"))
    }

    init {
        if (userId != -1L) {
            buscarUsuario(userId)
        }
    }

    fun refreshProfile() {
        if (userId != -1L) {
            buscarUsuario(userId)
        }
    }

    fun buscarUsuario(id: Long) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                usuarioRepository.refreshUsuario(id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar perfil: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun crearUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                usuarioRepository.createUsuario(usuario)
            } catch (_: Exception) {}
        }
    }

    fun actualizarUsuario(id: Long, usuario: Usuario) {
        viewModelScope.launch {
            try {
                usuarioRepository.updateUsuario(id, usuario)
            } catch (_: Exception) {}
        }
    }

    fun eliminarUsuario(id: Long) {
        viewModelScope.launch {
            try {
                usuarioRepository.deleteUsuario(id)
            } catch (_: Exception) {}
        }
    }
}
