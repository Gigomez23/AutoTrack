package ni.edu.uam.autotrak.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Vehicles : Screen("vehicles")
    object VehicleDetail : Screen("vehicle_detail/{id}") {
        fun createRoute(id: Long) = "vehicle_detail/$id"
    }
    object VehicleDocuments : Screen("vehicle_documents/{vehiculoId}") {
        fun createRoute(vehiculoId: Long) = "vehicle_documents/$vehiculoId"
    }
    object FuelLogs : Screen("fuel_logs")
    object Issues : Screen("issues")
    object Documents : Screen("documents")
    object Licencia : Screen("licencia")
    object Multas : Screen("multas")
    object Notifications : Screen("notifications")
    object UserManagement : Screen("user_management")
}
