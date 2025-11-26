package org.librevault.common.preferences

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.securityDataStore by preferencesDataStore("security_preferences")

