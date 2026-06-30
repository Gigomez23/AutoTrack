package ni.edu.uam.autotrak.data.sync

enum class SyncState {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNC_FAILED
}
