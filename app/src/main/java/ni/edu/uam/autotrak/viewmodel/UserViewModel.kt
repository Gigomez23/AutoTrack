package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.SessionManager

class UserViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Usuario>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        val userId = sessionManager.getUserId()
        if (userId != -1L) {
            buscarUsuario(userId)
        } else {
            _uiState.value = UiState.Error("No hay usuario seleccionado")
        }
    }

    fun buscarUsuario(id: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val usuario = RetrofitClient.api_usuario.getUsuario(id)
                //Se mantiene un vehiuclo en el estado de éxito para simplificar la pantalla de detalles
                _uiState.value = UiState.Success(listOf(usuario))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al buscar el vehiculo: ${e.message}"
                )
            }
        }
    }

    fun crearUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_usuario.createUsuario(usuario)
                val userId = sessionManager.getUserId()
                if (userId != -1L) buscarUsuario(userId)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al crear el usuario: ${e.message}"
                )
            }
        }
    }

    fun actualizarUsuario(id: Long, usuario: Usuario) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_usuario.updateUsuario(id, usuario)
                val userId = sessionManager.getUserId()
                if (userId != -1L) buscarUsuario(userId)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al actualizar el usuario: ${e.message}"
                )
            }
        }
    }

    fun eliminarUsuario(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_usuario.deleteUsuario(id)
                val userId = sessionManager.getUserId()
                if (userId != -1L) buscarUsuario(userId)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al eliminar el usuario: ${e.message}"
                )
            }
        }
    }
}