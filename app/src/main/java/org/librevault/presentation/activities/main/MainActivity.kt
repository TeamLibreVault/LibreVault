package org.librevault.presentation.activities.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import org.librevault.R
import org.librevault.common.activity.base.BaseLockActivity
import org.librevault.common.permissions.FilePermissionManager
import org.librevault.common.state.SplashScreenConditionState
import org.librevault.presentation.screens.gallery.GalleryScreen
import org.librevault.presentation.screens.lock.LockScreen
import org.librevault.presentation.theme.LibreVaultTheme

class MainActivity : BaseLockActivity() {

    private val fpManager by lazy { FilePermissionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        SplashScreenConditionState.isDecrypting = true
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { SplashScreenConditionState.isDecrypting }

        if (fpManager.isPermissionGranted()) {
            tryShowBiometric()
        } else {
            fpManager.requestPermission { succeeded ->
                if (!succeeded) finish()
                else tryShowBiometric()
            }
        }

        setContent {
            LibreVaultTheme {
                Navigator(screen = LockScreen()) { navigator ->
                    LaunchedEffect(key1 = isLoggedIn) {
                        with(navigator) {
                            if (isLoggedIn) {
                                if (lastItem is LockScreen)
                                    replaceAll(GalleryScreen())
                            } else {
                                replaceAll(LockScreen())
                            }
                        }
                    }

                    CurrentScreen()
                }
            }
        }
    }

    override fun getBiometricTitle(): String = getString(R.string.app_name)
    override fun getBiometricSubtitle(): String = getString(R.string.authenticate_to_unlock_the_vault)
}