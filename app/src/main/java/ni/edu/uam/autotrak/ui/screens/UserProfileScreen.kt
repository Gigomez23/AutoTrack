package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.viewmodel.UiState
import ni.edu.uam.autotrak.viewmodel.UserViewModel

@Composable
fun UserProfileScreen(
    viewModel: UserViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is UiState.Error -> Text(text = state.message, modifier = Modifier.align(Alignment.Center))
            is UiState.Success -> {
                val user = state.data.firstOrNull()
                if (user != null) {
                    UserEditForm(user = user, onSave = { updatedUser ->
                        user.id?.let { viewModel.actualizarUsuario(it, updatedUser) }
                    })
                }
            }
        }
    }
}

@Composable
fun UserEditForm(user: Usuario, onSave: (Usuario) -> Unit) {
    var nombres by remember { mutableStateOf(user.nombres ?: "") }
    var apellidos by remember { mutableStateOf(user.apellidos ?: "") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var nueroTel by remember { mutableStateOf(user.nueroTel ?: "") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "User Information", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = nombres, onValueChange = { nombres = it }, label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nueroTel, onValueChange = { nueroTel = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSave(user.copy(nombres = nombres, apellidos = apellidos, email = email, nueroTel = nueroTel))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Profile")
        }
    }
}
