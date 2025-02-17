package et.fira.freefeta.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "minimum_version")
    @SerializedName(value = "minimum_version")
    val minimumVersion: String,

    @ColumnInfo(name = "latest_version")
    @SerializedName(value = "latest_version")
    val latestVersion: String,

    @ColumnInfo(name = "version_description")
    @SerializedName(value = "version_description")
    val versionDescription: String? = null,

    @ColumnInfo(name = "download_url")
    @SerializedName(value = "download_url")
    val downloadUrl: String,

    @ColumnInfo(name = "alternative_url")
    @SerializedName(value = "alternative_url")
    val alternativeUrl: String? = null,

    @ColumnInfo(name = "is_service_ok")
    @SerializedName(value = "is_service_ok")
    val isServiceOk: Boolean,

    @ColumnInfo(name = "error_message")
    @SerializedName(value = "error_message")
    val errorMessage: String? = null

)
