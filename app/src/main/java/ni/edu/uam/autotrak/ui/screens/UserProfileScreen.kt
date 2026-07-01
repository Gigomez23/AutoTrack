package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.ui.components.ProfileTextField
import ni.edu.uam.autotrak.ui.components.SyncStatusBadge
import ni.edu.uam.autotrak.viewmodel.UiState
import ni.edu.uam.autotrak.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: UserViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshProfile() },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is UiState.Loading -> if (!isRefreshing) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is UiState.Error -> Text(
                text = state.message,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.error
            )
            is UiState.Success -> {
                val user = state.data.firstOrNull()
                if (user != null) {
                    UserEditForm(user = user, onSave = { updatedUser ->
                        user.id?.let { viewModel.actualizarUsuario(it, updatedUser) }
                    })
                } else {
                    Text(text = "Usuario no encontrado", modifier = Modifier.align(Alignment.Center))
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
    var numeroTel by remember { mutableStateOf(user.numeroTel ?: "") }
    var username by remember { mutableStateOf(user.username ?: "") }
    var password by remember { mutableStateOf(user.password ?: "") }
    var pais by remember { mutableStateOf(user.pais ?: "") }

    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Session and Sync status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (nombres.isNotEmpty()) "$nombres $apellidos" else "Perfil de Usuario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50)) // Green for active session
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sesión Activa",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                SyncStatusBadge(syncState = user.syncState)
            }
        }

        Text(
            text = "Información Personal",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        ProfileTextField(
            value = username,
            onValueChange = { username = it },
            label = "Nombre de usuario",
            icon = Icons.Default.AccountCircle
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileTextField(
                value = nombres,
                onValueChange = { nombres = it },
                label = "Nombres",
                icon = Icons.Default.Badge,
                modifier = Modifier.weight(1f)
            )
            ProfileTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = "Apellidos",
                icon = Icons.Default.Badge,
                modifier = Modifier.weight(1f)
            )
        }

        ProfileTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo Electrónico",
            icon = Icons.Default.Email
        )

        ProfileTextField(
            value = numeroTel,
            onValueChange = { numeroTel = it },
            label = "Teléfono",
            icon = Icons.Default.Phone
        )

        ProfileTextField(
            value = pais,
            onValueChange = { pais = it },
            label = "País",
            icon = Icons.Default.Public
        )

        Text(
            text = "Seguridad",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Ocultar" else "Mostrar")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(user.copy(
                    nombres = nombres,
                    apellidos = apellidos,
                    email = email,
                    numeroTel = numeroTel,
                    username = username,
                    password = password,
                    pais = pais
                ))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
