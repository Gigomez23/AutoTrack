package ni.edu.uam.autotrak.data.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal
import java.time.LocalDate
import ni.edu.uam.autotrak.data.remote.model.TipoMantenimiento
import ni.edu.uam.autotrak.data.sync.SyncState

class RoomTypeConverters {
    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String = value?.toPlainString() ?: BigDecimal.ZERO.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal = value
        ?.let { runCatching { BigDecimal(it) }.getOrNull() }
        ?: BigDecimal.ZERO

    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String?): List<String> = value
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { gson.fromJson<List<String>>(it, stringListType) }.getOrNull() }
        ?: emptyList()

    @TypeConverter
    fun fromSyncState(value: SyncState?): String = value?.name ?: SyncState.SYNCED.name

    @TypeConverter
    fun toSyncState(value: String?): SyncState = value
        ?.let { runCatching { SyncState.valueOf(it) }.getOrNull() }
        ?: SyncState.SYNCED

    @TypeConverter
    fun fromTipoMantenimiento(value: TipoMantenimiento?): String = value?.name ?: TipoMantenimiento.PREVENTIVO.name

    @TypeConverter
    fun toTipoMantenimiento(value: String?): TipoMantenimiento = value
        ?.let { runCatching { TipoMantenimiento.valueOf(it) }.getOrNull() }
        ?: TipoMantenimiento.PREVENTIVO

    @TypeConverter
    fun fromLocalDateTime(value: java.time.LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): java.time.LocalDateTime? = value?.let { java.time.LocalDateTime.parse(it) }
}
