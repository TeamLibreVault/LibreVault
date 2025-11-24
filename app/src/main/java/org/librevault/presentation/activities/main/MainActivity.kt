package org.librevault.presentation.activities.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
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
import androidx.core.net.toUri
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

    private fun hasManageStoragePermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

    private fun requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = "package:$packageName".toUri()
                startActivity(intent)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1001
            )
        }
    }

}