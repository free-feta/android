package et.fira.freefeta.data

import android.content.Context
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

@Database(entities = [FileEntity::class, AppConfig::class, Advertisement::class], version = 13, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FreeFetaDatabase: RoomDatabase() {
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
                )
                    .createFromAsset("database/free_feta_database.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}