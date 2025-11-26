package org.librevault.domain.use_case.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun getUseCaseScope() = CoroutineScope(Dispatchers.IO + SupervisorJob())