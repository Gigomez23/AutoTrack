package ni.edu.uam.autotrak.data.remote

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(`in`: JsonReader): LocalDateTime? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val dateStr = `in`.nextString()
        return try {
            LocalDateTime.parse(dateStr, formatter)
        } catch (e: Exception) {
            // Fallback for different formats if needed
            null
        }
    }
}
