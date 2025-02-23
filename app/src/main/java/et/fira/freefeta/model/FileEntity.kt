package et.fira.freefeta.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import et.fira.freefeta.R

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey
    val id: Int,

    @SerializedName(value = "is_playable")
    @ColumnInfo(name = "is_playable")
    val isPlayable: Boolean = false,

    @SerializedName(value = "file_type")
    @ColumnInfo(name = "file_type")
    val fileType: FileType = FileType.UNKNOWN, // Needs converter and registration to DB

    @SerializedName(value = "media_type")
    @ColumnInfo(name = "media_type")
    val mediaType: MediaType? = null, // Needs converter and registration to DB

    val name: String,
    val size: String? = null,

    @SerializedName(value = "download_url")
    @ColumnInfo(name = "download_url")
    val downloadUrl: String,

    @SerializedName(value = "thumbnail_url")
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUlr: String? = null,

    val runtime: String? = null,

    @SerializedName(value = "is_new")
    @ColumnInfo(name = "is_new")
    val isNew: Boolean = true,

    @SerializedName(value = "download_id")
    @ColumnInfo(name = "download_id")
    val downloadId: Int? = null,

    @SerializedName(value = "created_at")
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

enum class FileType {
    VIDEO,
    AUDIO,
    IMAGE,
    DOCUMENT,
    APK,
    COMPRESSED,
    UNKNOWN
}

enum class MediaType {
    MOVIE,
    SERIES,
    MUSIC,
    PODCAST,
    AUDIOBOOK,
    DOCUMENTARY
}

val FileEntity.icon: Int
    get() = if (mediaType != null) {
        when(mediaType){
            MediaType.MOVIE -> R.drawable.file_movie
            MediaType.SERIES -> R.drawable.file_series
            MediaType.MUSIC -> R.drawable.file_music
            MediaType.PODCAST -> R.drawable.file_podcast
            MediaType.DOCUMENTARY -> R.drawable.file_documentary
            MediaType.AUDIOBOOK -> R.drawable.audiobook
            else -> R.drawable.file_unknown
        }
    } else {
        when(fileType) {
            FileType.VIDEO -> R.drawable.file_video
            FileType.AUDIO -> R.drawable.file_audio
            FileType.IMAGE -> R.drawable.file_image
            FileType.DOCUMENT -> R.drawable.file_document
            FileType.APK -> R.drawable.file_apk
            FileType.COMPRESSED -> R.drawable.file_compressed
            else -> R.drawable.file_unknown
        }
    }
