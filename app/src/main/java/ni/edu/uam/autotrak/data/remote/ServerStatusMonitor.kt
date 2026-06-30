package ni.edu.uam.autotrak.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.net.SocketTimeoutException

enum class ServerStatus {
    ONLINE,
    OFFLINE,
    SERVER_UNREACHABLE
}

class ServerStatusMonitor(private val context: Context) {
    private val _status = MutableStateFlow(ServerStatus.ONLINE)
    val status: StateFlow<ServerStatus> = _status.asStateFlow()

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun reportSuccess() {
        _status.value = ServerStatus.ONLINE
    }

    fun reportError(e: Exception) {
        if (!isNetworkAvailable()) {
            _status.value = ServerStatus.OFFLINE
        } else if (e is SocketTimeoutException || e is IOException) {
            _status.value = ServerStatus.SERVER_UNREACHABLE
        }
    }
}
