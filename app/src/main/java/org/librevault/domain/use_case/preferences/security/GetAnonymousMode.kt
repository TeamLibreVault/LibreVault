package org.librevault.domain.use_case.preferences.security

import kotlinx.coroutines.launch
import org.librevault.data.repository.preferences.SecurityPreferences
import org.librevault.domain.use_case.utils.getUseCaseScope

class GetAnonymousMode(
    private val securityPreferences: SecurityPreferences,
) {
    private val scope = getUseCaseScope()

    operator fun invoke(callback: (Boolean) -> Unit) {
        scope.launch {
            callback(
                securityPreferences.getPreference(
                    key = SecurityPreferences.GET_ANONYMOUS_MODE,
                    default = SecurityPreferences.GET_ANONYMOUS_MODE_DEFAULT
                )
            )
        }
    }

}