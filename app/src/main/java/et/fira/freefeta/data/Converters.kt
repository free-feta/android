package et.fira.freefeta.data

import androidx.room.TypeConverter
import et.fira.freefeta.model.MediaType

class Converters {
    @TypeConverter
    fun fromMediaType(value: MediaType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toMediaType(value: String?): MediaType? {
        return value?.let { MediaType.valueOf(it) }
    }
}

