package et.fira.freefeta.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import et.fira.freefeta.model.FileEntity

@Database(entities = [FileEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FreeFetaDatabase: RoomDatabase() {
    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var Instance: FreeFetaDatabase? = null

        fun getDatabase(context: Context): FreeFetaDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context, FreeFetaDatabase::class.java, "free_feta_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}