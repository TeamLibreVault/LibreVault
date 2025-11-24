package org.librevault.common.permissions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

private const val TAG = "FilePermissionManager"

class FilePermissionManager(private val activity: AppCompatActivity) {

    private var onPermissionResult: ((granted: Boolean) -> Unit)? = null

    // Launcher for MANAGE_EXTERNAL_STORAGE intent
    private val manageStorageLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onPermissionResult?.invoke(hasManageAllFilesPermission())
        }

    // Launcher for WRITE_EXTERNAL_STORAGE request
    private val legacyPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            onPermissionResult?.invoke(granted)
        }

    /**
     * Request the appropriate file management permission.
     * Automatically handles Android version differences.
     */
    fun requestPermission(onResult: (Boolean) -> Unit) {
        onPermissionResult = onResult

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (hasManageAllFilesPermission()) {
                    onResult(true)
                } else {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = "package:${activity.packageName}".toUri()
                        manageStorageLauncher.launch(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        Log.e(TAG, "requestPermission: Error launching intent.", e)
                        manageStorageLauncher.launch(intent)
                    }
                }
            }

            else -> {
                if (hasLegacyStoragePermission()) {
                    onResult(true)
                } else {
                    legacyPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }

    /**
     * Check whether the permission is already granted.
     */
    fun isPermissionGranted(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        hasManageAllFilesPermission()
    } else {
        hasLegacyStoragePermission()
    }

    private fun hasManageAllFilesPermission(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else true

    private fun hasLegacyStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}