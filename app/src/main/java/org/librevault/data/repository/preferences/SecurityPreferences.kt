package org.librevault.data.repository.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.librevault.common.preferences.securityDataStore
import org.librevault.domain.repository.preferences.PreferencesRepository

class SecurityPreferences(context: Context) : PreferencesRepository {
    override val dataStore = context.securityDataStore

    override suspend fun <T> getPreference(key: Preferences.Key<T>, default: T): T =
        dataStore.data
            .map { prefs -> prefs[key] ?: default }
            .first()


    override suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    companion object {
        const val AUTO_LOCK_ENABLED_DEFAULT = true
        const val AUTO_LOCK_TIMEOUT_DEFAULT = 60000L
        const val GET_ANONYMOUS_MODE_DEFAULT = true

        val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
        val AUTO_LOCK_TIMEOUT = longPreferencesKey("auto_lock_timeout")
        val GET_ANONYMOUS_MODE = booleanPreferencesKey("get_anonymous_mode")
    }
}