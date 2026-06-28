package ni.edu.uam.autotrak.data.remote.model

interface Documento {
    val id: Long?
    val fechaEmitida: String?
    val fechaVencimiento: String?
}

data class DocumentoGeneral(
    override val id: Long? = null,
    override val fechaEmitida: String? = null,
    override val fechaVencimiento: String? = null,
) : Documento
