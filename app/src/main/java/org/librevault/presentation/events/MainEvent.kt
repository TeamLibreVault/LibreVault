package org.librevault.presentation.events

import org.librevault.presentation.activities.main.MainActivity

sealed class MainEvent {

    data class InitSplashScreen(val activity: MainActivity) : MainEvent()
}