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
    val isOneTime: Boolean,

    @ColumnInfo(name = "show_on_startup")
    @SerializedName(value = "show_on_startup")
    val showOnStartup: Boolean,

    val title: String? = null,
    val body: String,
    val duration: Int = 3

)
