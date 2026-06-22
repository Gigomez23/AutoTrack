package ni.edu.uam.autotrak.data.remote.model

data class DocumentoLicencia(
    override val id: Long? = null,
    override val fechaEmitida: String? = null,
    override val fechaVencimiento: String? = null,
//    override val imagen: String = "",
    val usuario: Usuario? = null
) : Documento(id, fechaEmitida, fechaVencimiento)
