package et.fira.freefeta.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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

    val showDeleteDialog: Flow<Boolean> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[SHOW_DELETE_DIALOG] ?: true
        }

    companion object {
        val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        val SHOW_DELETE_DIALOG = booleanPreferencesKey("show_delete_dialog")
        const val TAG = "UserPreferencesRepo"
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit {
                preferences ->
            preferences[THEME_MODE_KEY] = themeMode.ordinal
        }
    }

    suspend fun setShowDeleteDialog(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_DELETE_DIALOG] = show
        }
    }
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}
