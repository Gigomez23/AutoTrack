package ni.edu.uam.autotrak.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ni.edu.uam.autotrak.data.remote.ServerStatus
import ni.edu.uam.autotrak.data.remote.ServerStatusMonitor

@Composable
fun ServerStatusIndicator(monitor: ServerStatusMonitor, modifier: Modifier = Modifier) {
    val status by monitor.status.collectAsState()

    AnimatedVisibility(
        visible = status != ServerStatus.ONLINE,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .background(
                    color = when (status) {
                        ServerStatus.OFFLINE -> Color.Red
                        ServerStatus.SERVER_UNREACHABLE -> Color.Yellow
                        else -> Color.Transparent
                    },
                    shape = CircleShape
                )
                .padding(4.dp)
        ) {
            Icon(
                imageVector = when (status) {
                    ServerStatus.OFFLINE -> Icons.Default.WifiOff
                    ServerStatus.SERVER_UNREACHABLE -> Icons.Default.CloudOff
                    else -> Icons.Default.CloudOff
                },
                contentDescription = "Status",
                tint = if (status == ServerStatus.SERVER_UNREACHABLE) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
