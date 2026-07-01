package ni.edu.uam.autotrak.data.remote.model.sync

import java.time.LocalDate
import java.time.LocalDateTime

data class DocumentoVehiculoSyncDTO(
    val id: Long? = null,
    val fechaCreacion: LocalDateTime? = null,
    val fechaActualizacion: LocalDateTime? = null,
    val fechaVencimiento: LocalDate? = null,
    val fechaEmitida: LocalDate? = null,
    val imagen: String? = "",
    val nombre: String? = "",
    val vehiculoId: Long? = null,
    val eliminado: Boolean = false
)
