package ni.edu.uam.autotrak.data.remote.model

import ni.edu.uam.autotrak.data.sync.SyncState
import java.time.LocalDate
import java.time.LocalDateTime

data class Documento(
    val id: Long? = null,
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val fechaVencimiento: LocalDate? = null,
    val fechaEmitida: LocalDate? = null,
    val imagen: String? = "",
    val syncState: SyncState = SyncState.SYNCED
)

typealias DocumentoGeneral = Documento
