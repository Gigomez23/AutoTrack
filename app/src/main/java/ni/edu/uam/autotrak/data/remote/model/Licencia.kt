package ni.edu.uam.autotrak.data.remote.model

import ni.edu.uam.autotrak.data.sync.SyncState
import java.time.LocalDate
import java.time.LocalDateTime

data class Licencia(
    val id: Long? = null,
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val fechaVencimiento: LocalDate? = null,
    val fechaEmitida: LocalDate? = null,
    val imagen: String? = "",
    val categorias: List<String> = emptyList(),
    val usuarioId: Long? = null,
    val syncState: SyncState = SyncState.SYNCED,
)
