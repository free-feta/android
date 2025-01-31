package et.fira.freefeta.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medias")
data class Media(
    @PrimaryKey
    val id: Int,
    val isPlayable: Boolean = false,
    @ColumnInfo(name = "media_type")
    val mediaType: MediaType = MediaType.FILE, // Needs converter and registration to DB
    val title: String,
    @ColumnInfo(name = "download_url")
    val downloadUrl: String,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUlr: String? = null,
    val runtime: String? = null,
    @ColumnInfo(name = "is_new")
    val isNew: Boolean = true,
    @ColumnInfo(name = "download_id")
    val downloadId: Int? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

enum class MediaType {
    VIDEO,
    AUDIO,
    DOCUMENT,
    FILE
}
