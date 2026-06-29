package ni.edu.uam.autotrak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.ui.Screen
import ni.edu.uam.autotrak.ui.screens.LoginScreen
import ni.edu.uam.autotrak.ui.screens.MainScreen
import ni.edu.uam.autotrak.ui.theme.AutoTrakTheme
import ni.edu.uam.autotrak.viewmodel.AuthViewModel

import ni.edu.uam.autotrak.data.local.db.AppDatabase
import ni.edu.uam.autotrak.data.repository.UsuarioRepositoryImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)

        enableEdgeToEdge()
        setContent {
            AutoTrakTheme {
                AppNavigation(sessionManager)
            }
        }
    }
}

@Composable
fun AppNavigation(sessionManager: SessionManager) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = ni.edu.uam.autotrak.data.local.db.AppDatabase.getInstance(context)
    val usuarioRepository = ni.edu.uam.autotrak.data.repository.UsuarioRepositoryImpl(
        ni.edu.uam.autotrak.data.remote.RetrofitClient.api_usuario,
        database.usuarioDao()
    )

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(sessionManager, usuarioRepository) as T
        }
    })

    val startDestination = when {
        !sessionManager.isLoggedIn() -> Screen.Login.route
        else -> Screen.Home.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(authViewModel) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Home.route) {
            MainScreen(sessionManager = sessionManager, onLogout = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            })
        }
    }
}