package org.librevault.domain.repository.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface PreferencesRepository {
    val dataStore: DataStore<Preferences>

    suspend fun <T> getPreference(key: Preferences.Key<T>, default: T): T

    suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T)
}