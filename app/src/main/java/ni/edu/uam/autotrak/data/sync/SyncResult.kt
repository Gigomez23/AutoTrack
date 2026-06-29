package ni.edu.uam.autotrak.data.sync

sealed class SyncResult {
    object Success : SyncResult()

    data class Failure(
        val throwable: Throwable? = null,
        val message: String? = null
    ) : SyncResult()
}
