package ni.edu.uam.autotrak.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kotlinx.coroutines.launch
import ni.edu.uam.autotrak.data.remote.SessionManager
import ni.edu.uam.autotrak.ui.Screen
import ni.edu.uam.autotrak.viewmodel.RegistroCombustibleViewModel
import ni.edu.uam.autotrak.viewmodel.RegistroProblemaViewModel
import ni.edu.uam.autotrak.viewmodel.UserViewModel
import ni.edu.uam.autotrak.viewmodel.VehiculoViewModel
import ni.edu.uam.autotrak.ui.screens.VehiculoDetalleScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ni.edu.uam.autotrak.data.remote.RetrofitClient
import ni.edu.uam.autotrak.data.remote.model.RegistroProblema
import com.google.gson.Gson
import android.net.Uri

import ni.edu.uam.autotrak.data.sync.SyncManager
import ni.edu.uam.autotrak.viewmodel.HomeViewModel

import androidx.compose.ui.platform.LocalContext
import ni.edu.uam.autotrak.data.local.db.AppDatabase
import ni.edu.uam.autotrak.data.repository.*

data class MenuItem(val title: String, val route: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sessionManager: SessionManager,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)

    // SyncManager initialization
    val syncManager = remember {
        SyncManager(
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
            database.registroDao()
        )
    }

    val usuarioRepository = remember { UsuarioRepositoryImpl(ni.edu.uam.autotrak.data.remote.RetrofitClient.api_usuario, database.usuarioDao()) { syncManager } }
    val vehiculoRepository = remember { VehiculoRepositoryImpl(database.vehiculoDao()) { syncManager } }
    val fuelRepository = remember { RegistroCombustibleRepositoryImpl(database.registroCombustibleDao()) { syncManager } }
    val problemaRepository = remember { RegistroProblemaRepositoryImpl(database.registroProblemaDao()) { syncManager } }

    // App launch sync
    LaunchedEffect(Unit) {
        syncManager.syncAll()
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModel Factory
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(VehiculoViewModel::class.java) -> VehiculoViewModel(sessionManager, vehiculoRepository, fuelRepository) as T
                modelClass.isAssignableFrom(UserViewModel::class.java) -> UserViewModel(sessionManager, usuarioRepository) as T
                modelClass.isAssignableFrom(RegistroCombustibleViewModel::class.java) -> RegistroCombustibleViewModel(sessionManager, fuelRepository, vehiculoRepository) as T
                modelClass.isAssignableFrom(RegistroProblemaViewModel::class.java) -> RegistroProblemaViewModel(sessionManager, problemaRepository, vehiculoRepository) as T
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(sessionManager, usuarioRepository, vehiculoRepository, fuelRepository, problemaRepository) as T
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    val vehiculoViewModel: VehiculoViewModel = viewModel(factory = factory)
    val userViewModel: UserViewModel = viewModel(factory = factory)
    val fuelViewModel: RegistroCombustibleViewModel = viewModel(factory = factory)
    val issuesViewModel: RegistroProblemaViewModel = viewModel(factory = factory)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)

    val menuItems = listOf(
        MenuItem("Inicio", Screen.Home.route, Icons.Default.Home),
        MenuItem("Vehículos", Screen.Vehicles.route, Icons.Default.DirectionsCar),
        MenuItem("Combustible", Screen.FuelLogs.route, Icons.Default.LocalGasStation),
        MenuItem("Problemas", Screen.Issues.route, Icons.Default.ReportProblem),
        MenuItem("Perfil de Usuario", Screen.UserManagement.route, Icons.Default.Person)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "AutoTrak",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                HorizontalDivider()
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        onLogout()
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(menuItems.find { it.route == currentRoute }?.title ?: "AutoTrak")
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Home.route) { 
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToVehicles = { navController.navigate(Screen.Vehicles.route) },
                        onNavigateToFuel = { navController.navigate(Screen.FuelLogs.route) },
                        onNavigateToIssues = { navController.navigate(Screen.Issues.route) },
                        onVehicleClick = { id -> navController.navigate(Screen.VehicleDetail.createRoute(id)) }
                    )
                }
                
                composable(Screen.Vehicles.route) {
                    VehiculoScreen(
                        viewModel = vehiculoViewModel,
                        onVehicleClick = { id -> 
                            navController.navigate(Screen.VehicleDetail.createRoute(id))
                        },
                        onAddVehicle = {
                            navController.navigate("vehicle_form")
                        }
                    )
                }

                composable(
                    Screen.VehicleDetail.route,
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L
                    VehiculoDetalleScreen(
                        viewModel = vehiculoViewModel,
                        vehiculoId = id,
                        onEdit = { vehicleId ->
                            navController.navigate("vehicle_edit/$vehicleId")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("vehicle_form") {
                    VehiculoFormScreen(
                        viewModel = vehiculoViewModel,
                        onSuccess = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "vehicle_edit/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L
                    VehiculoFormScreen(
                        viewModel = vehiculoViewModel,
                        vehicleId = id,
                        onSuccess = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.FuelLogs.route) {
                    RegistroCombustibleScreen(
                        viewModel = fuelViewModel,
                        onAddRegistro = { id -> navController.navigate("fuel_form/$id") }
                    )
                }

                composable(
                    "fuel_logs/{vehiculoId}",
                    arguments = listOf(navArgument("vehiculoId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val vehiculoId = backStackEntry.arguments?.getLong("vehiculoId") ?: 0L
                    RegistroCombustibleScreen(
                        viewModel = fuelViewModel,
                        initialVehiculoId = vehiculoId,
                        onAddRegistro = { id -> navController.navigate("fuel_form/$id") }
                    )
                }

                composable(
                    "fuel_form/{vehiculoId}",
                    arguments = listOf(navArgument("vehiculoId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val vehiculoId = backStackEntry.arguments?.getLong("vehiculoId") ?: 0L
                    FuelLogFormScreen(
                        viewModel = fuelViewModel,
                        vehiculoId = vehiculoId,
                        onSuccess = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Issues.route) {
                    RegistroProblemaScreen(
                        viewModel = issuesViewModel,
                        onAddRegistro = { id -> navController.navigate("issue_form/$id") },
                        onEditRegistro = { vehicleId, issue -> 
                            val issueJson = RetrofitClient.gson.toJson(issue)
                            val encodedJson = Uri.encode(issueJson)
                            navController.navigate("issue_edit/$vehicleId/$encodedJson")
                        }
                    )
                }

                composable(
                    "issues/{vehiculoId}",
                    arguments = listOf(navArgument("vehiculoId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val vehiculoId = backStackEntry.arguments?.getLong("vehiculoId") ?: 0L
                    RegistroProblemaScreen(
                        viewModel = issuesViewModel,
                        initialVehiculoId = vehiculoId,
                        onAddRegistro = { id -> navController.navigate("issue_form/$id") },
                        onEditRegistro = { vehicleId, issue -> 
                            val issueJson = RetrofitClient.gson.toJson(issue)
                            val encodedJson = Uri.encode(issueJson)
                            navController.navigate("issue_edit/$vehicleId/$encodedJson")
                        }
                    )
                }

                composable(
                    "issue_form/{vehiculoId}",
                    arguments = listOf(navArgument("vehiculoId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val vehiculoId = backStackEntry.arguments?.getLong("vehiculoId") ?: 0L
                    IssueFormScreen(
                        viewModel = issuesViewModel,
                        vehiculoId = vehiculoId,
                        onSuccess = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "issue_edit/{vehiculoId}/{issueJson}",
                    arguments = listOf(
                        navArgument("vehiculoId") { type = NavType.LongType },
                        navArgument("issueJson") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val vehiculoId = backStackEntry.arguments?.getLong("vehiculoId") ?: 0L
                    val issueJson = backStackEntry.arguments?.getString("issueJson") ?: ""
                    val issue = RetrofitClient.gson.fromJson(issueJson, RegistroProblema::class.java)
                    IssueFormScreen(
                        viewModel = issuesViewModel,
                        vehiculoId = vehiculoId,
                        registroToEdit = issue,
                        onSuccess = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.UserManagement.route) {
                    UserProfileScreen(viewModel = userViewModel)
                }
            }
        }
    }
}
