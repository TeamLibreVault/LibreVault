package org.librevault.common.activity.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.librevault.R

abstract class BaseLockActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var autoLockTimeout = 2000L
    private val lockRunnable = Runnable { lockApp() }

    private val executor by lazy { ContextCompat.getMainExecutor(this) }

    protected var isLoggedIn by mutableStateOf(false)
        private set

    private var isBiometricVisible = false

    private var isAnonymousMode = true

    private val lifecycleEventObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> handler.postDelayed(lockRunnable, autoLockTimeout)
            Lifecycle.Event.ON_START -> {
                handler.removeCallbacks(lockRunnable)
                tryShowBiometric()
            }
            else -> {}
        }
    }

    private val biometricPrompt by lazy {
        BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isLoggedIn = true
                isBiometricVisible = false
                onAuthenticated()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                isBiometricVisible = false
                onAuthenticationFailed(errorCode, errString)
            }
        })
    }

    private val promptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .setTitle(getBiometricTitle())
            .setSubtitle(getBiometricSubtitle())
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disable screenshots and screen recordings
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleEventObserver)
    }

    protected open fun getBiometricTitle(): String = getString(R.string.unlock)
    protected open fun getBiometricSubtitle(): String = getString(R.string.authenticate_to_continue)

    protected open fun onAuthenticated() {}
    protected open fun onAuthenticationFailed(errorCode: Int, errString: CharSequence) {
        finish()
    }

    protected fun tryShowBiometric() = CoroutineScope(Dispatchers.Main).launch {
        delay(100L)
        if (!isLoggedIn && !isBiometricVisible) {
            isBiometricVisible = true
            biometricPrompt.authenticate(promptInfo)
        }
    }

    protected fun lockApp() {
        isLoggedIn = false
    }
}
