package ni.edu.uam.autotrak.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ni.edu.uam.autotrak.data.local.dao.DocumentoDao
import ni.edu.uam.autotrak.data.local.dao.LicenciaDao
import ni.edu.uam.autotrak.data.local.dao.RegistroCombustibleDao
import ni.edu.uam.autotrak.data.local.dao.RegistroDao
import ni.edu.uam.autotrak.data.local.dao.RegistroProblemaDao
import ni.edu.uam.autotrak.data.local.dao.SyncMetadataDao
import ni.edu.uam.autotrak.data.local.dao.UsuarioDao
import ni.edu.uam.autotrak.data.local.dao.VehiculoDao
import ni.edu.uam.autotrak.data.local.model.DocumentoEntity
import ni.edu.uam.autotrak.data.local.model.LicenciaEntity
import ni.edu.uam.autotrak.data.local.model.RegistroCombustibleEntity
import ni.edu.uam.autotrak.data.local.model.RegistroEntity
import ni.edu.uam.autotrak.data.local.model.RegistroProblemaEntity
import ni.edu.uam.autotrak.data.local.model.SyncMetadataEntity
import ni.edu.uam.autotrak.data.local.model.UsuarioEntity
import ni.edu.uam.autotrak.data.local.model.VehiculoEntity
import ni.edu.uam.autotrak.data.sync.SyncConstants

@Database(
    entities = [
        UsuarioEntity::class,
        VehiculoEntity::class,
        LicenciaEntity::class,
        RegistroEntity::class,
        RegistroCombustibleEntity::class,
        RegistroProblemaEntity::class,
        DocumentoEntity::class,
        SyncMetadataEntity::class
    ],
    version = 6,
    exportSchema = false,
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun registroDao(): RegistroDao
    abstract fun registroCombustibleDao(): RegistroCombustibleDao
    abstract fun registroProblemaDao(): RegistroProblemaDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun licenciaDao(): LicenciaDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    SyncConstants.DATABASE_NAME
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
