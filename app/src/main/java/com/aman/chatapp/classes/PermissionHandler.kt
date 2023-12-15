package com.aman.chatapp.classes
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHandler(private val activity: Activity) {

    companion object {
        const val READ_EXTERNAL_STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE"
        const val INTERNET_PERMISSION = "android.permission.INTERNET"
        const val ACCESS_NETWORK_STATE_PERMISSION = "android.permission.ACCESS_NETWORK_STATE"
        const val RECORD_AUDIO_PERMISSION = "android.permission.RECORD_AUDIO"
        const val MODIFY_AUDIO_SETTINGS_PERMISSION = "android.permission.MODIFY_AUDIO_SETTINGS"
        const val CAMERA_PERMISSION = "android.permission.CAMERA"

        const val PERMISSION_REQUEST_CODE = 123
    }

    fun checkReadExternalStoragePermission(): Boolean {
        return checkPermission(READ_EXTERNAL_STORAGE_PERMISSION)
    }

    fun requestReadExternalStoragePermission() {
        requestPermission(READ_EXTERNAL_STORAGE_PERMISSION)
    }

    fun checkInternetPermission(): Boolean {
        return checkPermission(INTERNET_PERMISSION)
    }

    fun requestInternetPermission() {
        requestPermission(INTERNET_PERMISSION)
    }

    fun checkAccessNetworkStatePermission(): Boolean {
        return checkPermission(ACCESS_NETWORK_STATE_PERMISSION)
    }

    fun requestAccessNetworkStatePermission() {
        requestPermission(ACCESS_NETWORK_STATE_PERMISSION)
    }

    fun checkRecordAudioPermission(): Boolean {
        return checkPermission(RECORD_AUDIO_PERMISSION)
    }

    fun requestRecordAudioPermission() {
        requestPermission(RECORD_AUDIO_PERMISSION)
    }

    fun checkModifyAudioSettingsPermission(): Boolean {
        return checkPermission(MODIFY_AUDIO_SETTINGS_PERMISSION)
    }

    fun requestModifyAudioSettingsPermission() {
        requestPermission(MODIFY_AUDIO_SETTINGS_PERMISSION)
    }

    fun checkCameraPermission(): Boolean {
        return checkPermission(CAMERA_PERMISSION)
    }

    fun requestCameraPermission() {
        requestPermission(CAMERA_PERMISSION)
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            PERMISSION_REQUEST_CODE
        )
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    // Handle permission denied
                    return
                }
            }
        }
    }
}
