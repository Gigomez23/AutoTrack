package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.autotrak.data.remote.model.Licencia
import ni.edu.uam.autotrak.data.remote.model.Usuario
import ni.edu.uam.autotrak.data.sync.SyncState
import ni.edu.uam.autotrak.ui.components.SyncStatusBadge
import ni.edu.uam.autotrak.viewmodel.LicenciaUiState
import ni.edu.uam.autotrak.viewmodel.LicenciaViewModel
import ni.edu.uam.autotrak.viewmodel.UiState
import ni.edu.uam.autotrak.viewmodel.UserViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenciaScreen(
    viewModel: LicenciaViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onEdit: (Licencia?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userState by userViewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val scrollState = rememberLazyListState()
    
    val currentUser = remember(userState) {
        (userState as? UiState.Success)?.data?.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Licencia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (uiState is LicenciaUiState.Empty) {
                ExtendedFloatingActionButton(
                    onClick = { onEdit(null) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Registrar Licencia") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.cargarLicencia() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is LicenciaUiState.Loading -> {
                    if (!isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is LicenciaUiState.Empty -> {
                    EmptyLicenciaState(onAdd = { onEdit(null) })
                }
                is LicenciaUiState.Error -> {
                    ErrorState(message = state.message, onRetry = { viewModel.cargarLicencia() })
                }
                is LicenciaUiState.Success -> {
                    LicenciaContent(
                        licencia = state.licencia,
                        usuario = currentUser,
                        scrollState = scrollState,
                        onEditClick = { onEdit(state.licencia) }
                    )
                }
            }
        }
    }
}

@Composable
fun LicenciaContent(
    licencia: Licencia,
    usuario: Usuario?,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    onEditClick: () -> Unit
) {
    val hoy = LocalDate.now()
    val fechaVencimiento = licencia.fechaVencimiento
    
    val (isExpired, isExpiringSoon) = remember(fechaVencimiento) {
        if (fechaVencimiento == null) Pair(false, false)
        else {
            val diasParaVencer = ChronoUnit.DAYS.between(hoy, fechaVencimiento)
            Pair(fechaVencimiento.isBefore(hoy), diasParaVencer in 0..30)
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        if (isExpired || isExpiringSoon) {
            item {
                ExpirationAlert(isExpired = isExpired)
            }
        }

        item {
            SyncStatusSection(syncState = licencia.syncState)
        }

        item {
            LicenciaCard(
                licencia = licencia, 
                usuario = usuario,
                onEditClick = onEditClick
            )
        }

        item {
            Text(
                text = "Documentos Adjuntos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            AttachmentSection(licencia = licencia)
        }
    }
}

@Composable
fun ExpirationAlert(isExpired: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired) MaterialTheme.colorScheme.errorContainer 
                            else MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isExpired) Icons.Default.Dangerous else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isExpired) MaterialTheme.colorScheme.onErrorContainer 
                       else MaterialTheme.colorScheme.onTertiaryContainer
            )
            Column {
                Text(
                    text = if (isExpired) "Licencia Vencida" else "Licencia Por Vencer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpired) MaterialTheme.colorScheme.onErrorContainer 
                            else MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = if (isExpired) "Su licencia ya no es válida. Por favor, renuévela lo antes posible."
                           else "Su licencia vencerá en menos de 30 días. Planifique su renovación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isExpired) MaterialTheme.colorScheme.onErrorContainer 
                            else MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
fun SyncStatusSection(syncState: SyncState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SyncStatusBadge(syncState = syncState)
    }
}

@Composable
fun LicenciaCard(licencia: Licencia, usuario: Usuario?, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "LICENCIA DE CONDUCIR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (usuario != null) "${usuario.nombres} ${usuario.apellidos}" else "Titular",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CATEGORÍAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = licencia.categorias.joinToString(", ").ifEmpty { "N/A" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "VENCIMIENTO",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = licencia.fechaVencimiento?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "--/--/----",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (licencia.fechaVencimiento?.isBefore(LocalDate.now()) == true) MaterialTheme.colorScheme.error 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AttachmentSection(licencia: Licencia) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AttachmentThumbnail(
            label = "Frontal",
            imageUrl = licencia.imagen,
            modifier = Modifier.weight(1f),
            onClick = { /* Ver foto frontal */ }
        )
        AttachmentThumbnail(
            label = "Trasera",
            imageUrl = null,
            modifier = Modifier.weight(1f),
            onClick = { /* Ver foto trasera */ }
        )
    }
}

@Composable
fun AttachmentThumbnail(
    label: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .aspectRatio(1.6f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null && imageUrl.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyLicenciaState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ContactPage,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No tienes una licencia registrada",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Registra tu licencia para recibir alertas de vencimiento y tener tus documentos a mano.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar Licencia")
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ha ocurrido un error",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}
