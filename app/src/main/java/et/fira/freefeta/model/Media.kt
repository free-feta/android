package et.fira.freefeta.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medias")
data class Media(
    @PrimaryKey
    val id: Int,
    val isPlayable: Boolean = false,
    val mediaType: MediaType = MediaType.FILE, // Needs converter and registration to DB
    val title: String,
    val downloadUrl: String,
    val thumbnailUlr: String? = null,
    val runtime: String? = null,
    val isNew: Boolean = true,
    val downloadId: Int? = null
)

enum class MediaType {
    VIDEO,
    AUDIO,
    DOCUMENT,
    FILE
}
