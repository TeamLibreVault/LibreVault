package org.librevault.presentation.viewmodels

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.librevault.common.state.SplashScreenConditionState
import org.librevault.domain.use_case_bundle.MainUseCases
import org.librevault.presentation.activities.main.MainActivity
import org.librevault.presentation.events.MainEvent

class MainViewModel(
    private val mainUseCases: MainUseCases,
) : ViewModel() {

    private var _autoLockEnabled = MutableStateFlow(false)
    val autoLockEnabled: StateFlow<Boolean> = _autoLockEnabled

    private var _autoLockTimeout = MutableStateFlow(0L)
    val autoLockTimeout: StateFlow<Long> = _autoLockTimeout

    private val _isAnonymousMode = MutableStateFlow(false)
    val isAnonymousMode: StateFlow<Boolean> = _isAnonymousMode

    init {
        getAutoLockEnabled()
        getAutoLockTimeout()
        getAnonymousMode()
    }

    fun onEvent(event: MainEvent) = when (event) {
        is MainEvent.InitSplashScreen -> initSplashScreen(event.activity)
    }

    private fun initSplashScreen(activity: MainActivity) {
        val splashScreen = activity.installSplashScreen()
        SplashScreenConditionState.isDecrypting = true
        splashScreen.setKeepOnScreenCondition { SplashScreenConditionState.isDecrypting }
    }

    private fun getAutoLockEnabled() {
        mainUseCases.getAutoLockEnabled { enabled ->
            _autoLockEnabled.value = enabled
        }
    }

    private fun getAutoLockTimeout() {
        mainUseCases.getAutoLockTimeout { timeout ->
            _autoLockTimeout.value = timeout
        }
    }

    private fun getAnonymousMode() {
        mainUseCases.getAnonymousMode { mode ->
            _isAnonymousMode.value = mode
        }
    }

}