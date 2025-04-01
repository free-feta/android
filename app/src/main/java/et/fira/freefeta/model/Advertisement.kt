package et.fira.freefeta.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "advertisements")
data class Advertisement (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 1,

    @ColumnInfo(name = "is_one_time")
    @SerializedName(value = "is_one_time")
    val isOneTime: Boolean = false,

    @ColumnInfo(name = "show_on_startup")
    @SerializedName(value = "show_on_startup")
    val showOnStartup: Boolean? = null, // When set to null it will be used for both startup and on demand

    @ColumnInfo(name = "is_html")
    @SerializedName(value = "is_html")
    val isHtml: Boolean = false,

    val title: String? = null,
    val body: String,
    val url: String? = null,
    val duration: Int = 3,
    val expired: Boolean = false,

)
