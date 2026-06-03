package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.model.Usuario
import ni.edu.uam.autotrak.data.model.Vehiculo
import ni.edu.uam.autotrak.data.remote.RetrofitClient

//todo: implementar que el get de usuario tenga el id del usuario logueado.
//impementacion temporal de usar id = 1
class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Usuario>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        buscarUsuario(1)
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
                buscarUsuario(1)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al crear el vehiculo: ${e.message}"
                )
            }
        }
    }

    fun actualizarUsuario(id: Long, usuario: Usuario) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_usuario.updateUsuario(id, usuario)
                buscarUsuario(1)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al actualizar el vehiculo: ${e.message}"
                )
            }
        }
    }

    fun eliminarUsuario(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.api_usuario.deleteUsuario(id)
                buscarUsuario(1)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    "Error al eliminar el vehiculo: ${e.message}"
                )
            }
        }
    }
}