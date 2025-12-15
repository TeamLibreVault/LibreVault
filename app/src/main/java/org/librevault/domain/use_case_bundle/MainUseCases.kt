package org.librevault.domain.use_case_bundle

import org.librevault.domain.use_case.preferences.security.GetAnonymousMode
import org.librevault.domain.use_case.preferences.security.GetAutoLockEnabled
import org.librevault.domain.use_case.preferences.security.GetAutoLockTimeout

data class MainUseCases(
    val getAutoLockEnabled: GetAutoLockEnabled,
    val getAutoLockTimeout: GetAutoLockTimeout,
    val getAnonymousMode: GetAnonymousMode
)