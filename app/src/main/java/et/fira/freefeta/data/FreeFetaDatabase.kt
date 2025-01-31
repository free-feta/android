package et.fira.freefeta.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import et.fira.freefeta.model.Media

@Database(entities = [Media::class], version = 1, exportSchema = false)
abstract class FreeFetaDatabase: RoomDatabase() {
    abstract fun mediaDao(): MediaDao

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