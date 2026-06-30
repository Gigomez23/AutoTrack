package ni.edu.uam.autotrak.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.data.remote.model.LoginRequest
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.repository.UsuarioRepository

class AuthViewModel(
    private val sessionManager: SessionManager,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val response = usuarioRepository.login(LoginRequest(email, password))
                sessionManager.saveAuthData(response.token, response.userId)
                _loginState.value = UiState.Success(true)
            } catch (e: Exception) {
                sessionManager.clear()
                _loginState.value = UiState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun logout() {
        sessionManager.clear()
        _loginState.value = UiState.Success(false)
    }
}
