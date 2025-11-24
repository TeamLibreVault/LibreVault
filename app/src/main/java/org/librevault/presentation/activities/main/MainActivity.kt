package org.librevault.presentation.activities.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import org.librevault.R
import org.librevault.common.permissions.FilePermissionManager
import org.librevault.common.state.SplashScreenConditionState
import org.librevault.presentation.screens.gallery.GalleryScreen
import org.librevault.presentation.screens.lock.LockScreen
import org.librevault.presentation.theme.LibreVaultTheme

class MainActivity : AppCompatActivity() {

    private var isBiometricVisible by mutableStateOf(false)
    private var isLoggedIn by mutableStateOf(false)

    private val fpManager by lazy { FilePermissionManager(this) }

    private val executor by lazy { ContextCompat.getMainExecutor(this) }

    private val biometricPrompt by lazy {
        BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isLoggedIn = true
                isBiometricVisible = false
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                isBiometricVisible = false
                finish() // just exit; they failed or canceled (probably)
            }
        })
    }

    private val promptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .setTitle(getString(R.string.app_name))
            .setSubtitle(getString(R.string.authenticate_to_unlock_the_vault))
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        SplashScreenConditionState.isDecrypting = true
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { SplashScreenConditionState.isDecrypting }

        if (fpManager.isPermissionGranted()) {
            if (isLoggedIn.not() && isBiometricVisible.not()) {
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            fpManager.requestPermission {
                if (it.not()) {
                    finish()
                }
            }
        }

        setContent {
            LibreVaultTheme {
                LaunchedEffect(key1 = isLoggedIn, key2 = isBiometricVisible) {
                    if (isLoggedIn.not() && isBiometricVisible.not())
                        biometricPrompt.authenticate(promptInfo)
                }

                Navigator(LockScreen()) { navigator ->
                    LaunchedEffect(key1 = isLoggedIn) {
                        if (isLoggedIn && isBiometricVisible.not()) {
                            navigator.pop()
                            navigator += GalleryScreen()
                        }
                    }

                    CurrentScreen()
                }
            }
        }
    }

}