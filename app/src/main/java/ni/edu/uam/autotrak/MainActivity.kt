package ni.edu.uam.autotrak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.ServerStatusMonitor
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.ui.Screen
import ni.edu.uam.autotrak.ui.screens.LoginScreen
import ni.edu.uam.autotrak.ui.screens.MainScreen
import ni.edu.uam.autotrak.ui.screens.SignupScreen
import ni.edu.uam.autotrak.ui.theme.AutoTrakTheme
import ni.edu.uam.autotrak.viewmodel.AuthViewModel

import ni.edu.uam.autotrak.data.local.db.AppDatabase
import ni.edu.uam.autotrak.data.repository.UsuarioRepositoryImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(this)
        val serverStatusMonitor = ServerStatusMonitor(applicationContext)
        RetrofitClient.init(sessionManager, serverStatusMonitor)

        enableEdgeToEdge()
        setContent {
            AutoTrakTheme {
                AppNavigation(sessionManager, database = AppDatabase.getInstance(this@MainActivity), serverStatusMonitor)
            }
        }
    }
}

@Composable
fun AppNavigation(sessionManager: SessionManager, database: AppDatabase, serverStatusMonitor: ServerStatusMonitor) {
    val syncManager = remember {
        ni.edu.uam.autotrak.data.sync.SyncManager(
            database,
            database.syncMetadataDao(),
            ni.edu.uam.autotrak.data.remote.RetrofitClient.api_usuario,
            database.usuarioDao(),
            ni.edu.uam.autotrak.data.remote.RetrofitClient.api_vehiculo,
            database.vehiculoDao(),
            ni.edu.uam.autotrak.data.remote.RetrofitClient.api_registro_combustible,
            database.registroCombustibleDao(),
            ni.edu.uam.autotrak.data.remote.RetrofitClient.api_registro_problema,
            database.registroProblemaDao(),
            ni.edu.uam.autotrak.data.remote.RetrofitClient.api_registro,
            database.registroDao(),
            ni.edu.uam.autotrak.data.remote.RetrofitClient.api_licencia,
            database.licenciaDao(),
            ni.edu.uam.autotrak.data.remote.RetrofitClient.api_documento,
            database.documentoDao()
        )
    }

    val usuarioRepository = remember {
        ni.edu.uam.autotrak.data.repository.UsuarioRepositoryImpl(
            database,
            ni.edu.uam.autotrak.data.remote.RetrofitClient.api_usuario,
            database.usuarioDao()
        ) { syncManager }
    }

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(database, sessionManager, usuarioRepository) as T
        }
    })

    val startDestination = when {
        !sessionManager.isLoggedIn() -> Screen.Login.route
        else -> Screen.Home.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                serverStatusMonitor = serverStatusMonitor,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignupClick = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                viewModel = authViewModel,
                serverStatusMonitor = serverStatusMonitor,
                onSignupSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Home.route) {
            MainScreen(
                sessionManager = sessionManager,
                serverStatusMonitor = serverStatusMonitor,
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }
                }
            )
        }
    }
}
