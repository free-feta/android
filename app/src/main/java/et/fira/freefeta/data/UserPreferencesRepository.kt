package et.fira.freefeta.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val themeMode: Flow<ThemeMode> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            ThemeMode.entries.toTypedArray().getOrElse(preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.ordinal) {
                ThemeMode.SYSTEM // Default is system theme
            }
        }

    companion object {
        val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        const val TAG = "UserPreferencesRepo"
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit {
                preferences ->
            preferences[THEME_MODE_KEY] = themeMode.ordinal
        }
    }
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}
