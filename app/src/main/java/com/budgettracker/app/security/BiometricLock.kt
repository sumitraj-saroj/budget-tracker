package com.budgettracker.app.security

import android.app.Activity
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricAvailability { AVAILABLE, NONE_ENROLLED, NO_HARDWARE, UNAVAILABLE }

object BiometricLock {

    fun availability(context: Context): BiometricAvailability {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_NOT_PRESENT ->
                BiometricAvailability.NO_HARDWARE
            else -> BiometricAvailability.UNAVAILABLE
        }
    }

    fun canUseDeviceCredential(context: Context): Boolean =
        BiometricManager.from(context)
            .canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            ) == BiometricManager.BIOMETRIC_SUCCESS

    /** Prompts the user to authenticate. Callbacks run on the main thread. */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    onError("cancelled")
                } else {
                    onError(errString.toString())
                }
            }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .setDeviceCredentialAllowed(true)
            .build()
        prompt.authenticate(info)
    }
}
