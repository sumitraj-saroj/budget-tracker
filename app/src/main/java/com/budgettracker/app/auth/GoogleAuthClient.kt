package com.budgettracker.app.auth

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleAccount(
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
)

/**
 * Google Sign-In via the Android Credential Manager.
 *
 * Setup: create an OAuth Web application client ID in Google Cloud Console and
 * paste it below (see README). Until configured, sign-in will fail gracefully.
 */
@Singleton
class GoogleAuthClient @Inject constructor() {

    // TODO: Replace with your own Web OAuth client ID from Google Cloud Console.
    var serverClientId: String = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    val isConfigured: Boolean get() = !serverClientId.startsWith("YOUR_WEB_CLIENT_ID")

    suspend fun signIn(activity: Activity): Result<GoogleAccount> = runCatching {
        val credentialManager = CredentialManager.create(activity)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val response = credentialManager.getCredential(activity, request)
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleId = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleAccount(
                    email = googleId.id,
                    displayName = googleId.displayName,
                    photoUrl = googleId.profilePictureUri?.toString(),
                )
            } catch (e: GoogleIdTokenParsingException) {
                error("Received an invalid Google sign-in response")
            }
        } else {
            error("No Google account credential returned")
        }
    }

    suspend fun signOut(activity: Activity) {
        runCatching {
            CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
        }
    }
}
