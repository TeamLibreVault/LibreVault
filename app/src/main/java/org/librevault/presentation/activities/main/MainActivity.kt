package org.librevault.presentation.activities.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.librevault.R
import org.librevault.common.activity.base.BaseLockActivity
import org.librevault.common.permissions.FilePermissionManager
import org.librevault.presentation.events.MainEvent
import org.librevault.presentation.screens.gallery.GalleryScreen
import org.librevault.presentation.screens.lock.LockScreen
import org.librevault.presentation.theme.LibreVaultTheme
import org.librevault.presentation.viewmodels.MainViewModel
import org.librevault.utils.lazyVar

class MainActivity : BaseLockActivity() {

    private val viewModel = getViewModel<MainViewModel>()
    private val fpManager by lazy { FilePermissionManager(this) }

    override var autoLockEnabled: Boolean by lazyVar { viewModel.autoLockEnabled.value }
    override var autoLockTimeout: Long by lazyVar { viewModel.autoLockTimeout.value }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.onEvent(MainEvent.InitSplashScreen(this))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
    override fun getBiometricSubtitle(): String =
        getString(R.string.authenticate_to_unlock_the_vault)
}