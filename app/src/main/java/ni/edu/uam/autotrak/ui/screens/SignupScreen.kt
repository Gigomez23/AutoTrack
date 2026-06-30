package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ni.edu.uam.autotrak.data.remote.ServerStatusMonitor
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.ui.components.ServerStatusIndicator
import ni.edu.uam.autotrak.viewmodel.AuthViewModel
import ni.edu.uam.autotrak.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    serverStatusMonitor: ServerStatusMonitor,
    onSignupSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("") }

    val signupState by viewModel.signupState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    ServerStatusIndicator(monitor = serverStatusMonitor)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombres,
                onValueChange = { nombres = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = pais,
                onValueChange = { pais = it },
                label = { Text("Country") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.signup(
                        Usuario(
                            nombres = nombres,
                            apellidos = apellidos,
                            email = email,
                            username = username,
                            password = password,
                            nueroTel = telefono,
                            pais = pais
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = signupState !is UiState.Loading
            ) {
                if (signupState is UiState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Sign Up")
                }
            }

            if (signupState is UiState.Error) {
                Text(
                    text = (signupState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            LaunchedEffect(signupState) {
                if (signupState is UiState.Success && (signupState as UiState.Success<Boolean>).data) {
                    onSignupSuccess()
                }
            }
        }
    }
}
