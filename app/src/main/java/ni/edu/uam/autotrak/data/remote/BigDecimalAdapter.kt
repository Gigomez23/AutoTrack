package ni.edu.uam.autotrak.data.remote

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.math.BigDecimal

class BigDecimalAdapter : TypeAdapter<BigDecimal>() {
    override fun write(out: JsonWriter, value: BigDecimal?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toPlainString())
        }
    }

    override fun read(`in`: JsonReader): BigDecimal? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        return try {
            BigDecimal(`in`.nextString())
        } catch (e: Exception) {
            null
        }
    }
}
