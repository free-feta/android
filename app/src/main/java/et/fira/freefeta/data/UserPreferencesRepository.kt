package et.fira.freefeta.data

import androidx.annotation.OptIn
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import et.fira.freefeta.util.Logger
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
                Logger.e(TAG, "Error reading preferences.", it)
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
                Logger.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[SHOW_DELETE_DIALOG_KEY] ?: true
        }

    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch {
            if(it is IOException) {
                Logger.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] ?: false
        }

    val resizeMode: Flow<ResizeMode> = dataStore.data
        .catch {
        if(it is IOException) {
            Logger.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }
    .map { preferences ->
        ResizeMode.entries.toTypedArray().getOrElse(preferences[RESIZE_MODE_KEY] ?: ResizeMode.FIT.ordinal) {
            ResizeMode.FIT // Default is fit
        }
    }

    companion object {
        val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        val SHOW_DELETE_DIALOG_KEY = booleanPreferencesKey("show_delete_dialog")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        val RESIZE_MODE_KEY = intPreferencesKey("resize_mode")
        const val TAG = "UserPreferencesRepo"
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                theme = preferences[THEME_MODE_KEY]?.let { ThemeMode.entries[it] } ?: ThemeMode.SYSTEM,
                showDeleteConfirmation = preferences[SHOW_DELETE_DIALOG_KEY] ?: true,
                resizeMode = preferences[RESIZE_MODE_KEY]?.let { ResizeMode.entries[it] } ?: ResizeMode.FIT,
                isFirstLaunch = preferences[ONBOARDING_COMPLETED_KEY] ?: true
            )
        }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit {
                preferences ->
            preferences[THEME_MODE_KEY] = themeMode.ordinal
        }
    }

    suspend fun setShowDeleteDialog(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_DELETE_DIALOG_KEY] = show
        }
    }

    suspend fun completeOnboarding(completed : Boolean = true) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    suspend fun setResizeMode(resizeMode: ResizeMode) {
        dataStore.edit { preferences ->
            preferences[RESIZE_MODE_KEY] = resizeMode.ordinal
        }
    }
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class ResizeMode {
    FIT, FILL, ZOOM, FIXED_WIDTH, FIXED_HEIGHT,
}

@OptIn(UnstableApi::class)
fun ResizeMode.toExoPlayerResizeMode(): Int = when (this) {
    ResizeMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    ResizeMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
    ResizeMode.FIXED_WIDTH -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
    ResizeMode.FIXED_HEIGHT -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
    ResizeMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
}

data class UserPreferences(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val showDeleteConfirmation: Boolean = true,
    val resizeMode: ResizeMode = ResizeMode.FIT,
    val isFirstLaunch: Boolean = true
)
