package et.fira.freefeta.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import et.fira.freefeta.data.ad.AdDao
import et.fira.freefeta.data.config.AppConfigDao
import et.fira.freefeta.data.file.FileDao
import et.fira.freefeta.model.Advertisement
import et.fira.freefeta.model.AppConfig
import et.fira.freefeta.model.FileEntity
import java.io.IOException

private const val DB_FILE = "database/free_feta_database.db"

@Database(
    entities = [FileEntity::class, AppConfig::class, Advertisement::class],
    version = 15,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FreeFetaDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun appConfigDao(): AppConfigDao
    abstract fun adDao(): AdDao

    companion object {
        @Volatile
        private var Instance: FreeFetaDatabase? = null

        fun getDatabase(context: Context): FreeFetaDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context, FreeFetaDatabase::class.java, "free_feta_database"
                ).apply {
                    if (assetExists(context, DB_FILE)) {
                        createFromAsset(DB_FILE)
                    } else {
                        Log.w(
                            "DatabaseWarning",
                            "Database asset file is missing. Creating an empty database."
                        )
                    }
                }
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

private fun assetExists(context: Context, fileName: String): Boolean {
    return try {
        context.assets.open(fileName).close()
        true
    } catch (e: IOException) {
        false
    }
}