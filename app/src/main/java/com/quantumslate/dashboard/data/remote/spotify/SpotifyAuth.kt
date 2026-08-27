package com.quantumslate.dashboard.data.remote.spotify

import android.net.Uri
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Spotify Authorization Code flow with PKCE.
 *
 * PKCE rather than the classic Authorization Code flow because this is a mobile client: it
 * cannot keep a client secret. Shipping one in the APK would expose it to anyone who
 * unzips the app, so the flow is secured with a per-attempt code verifier instead.
 */
object SpotifyAuth {

    const val AUTH_ENDPOINT = "https://accounts.spotify.com/authorize"
    const val TOKEN_ENDPOINT = "https://accounts.spotify.com/api/token"

    /**
     * Must match a Redirect URI registered on the Spotify developer dashboard, and the
     * intent-filter declared in AndroidManifest.xml.
     */
    const val REDIRECT_URI = "quantumslate://spotify-callback"

    /** Read-only playback scopes: the widget displays state, it does not control it. */
    private val SCOPES = listOf(
        "user-read-currently-playing",
        "user-read-playback-state"
    )

    /** Generates a high-entropy PKCE code verifier (RFC 7636 allows 43–128 characters). */
    fun generateCodeVerifier(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return bytes.toBase64Url()
    }

    /** S256 challenge derived from [codeVerifier]. */
    fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return digest.toBase64Url()
    }

    /** Opaque value echoed back by Spotify, used to detect a forged or replayed callback. */
    fun generateState(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toBase64Url()
    }

    fun buildAuthorizationUrl(
        clientId: String,
        codeChallenge: String,
        state: String
    ): String = Uri.parse(AUTH_ENDPOINT).buildUpon()
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("response_type", "code")
        .appendQueryParameter("redirect_uri", REDIRECT_URI)
        .appendQueryParameter("code_challenge_method", "S256")
        .appendQueryParameter("code_challenge", codeChallenge)
        .appendQueryParameter("state", state)
        .appendQueryParameter("scope", SCOPES.joinToString(" "))
        .build()
        .toString()

    /**
     * Extracts the authorization code from a redirect callback.
     *
     * @param expectedState the state issued when the request was started
     * @return the code, or a failure describing why the callback was rejected
     */
    fun parseCallback(uri: Uri, expectedState: String?): Result<String> {
        uri.getQueryParameter("error")?.let {
            return Result.failure(SpotifyAuthError(it))
        }

        val returnedState = uri.getQueryParameter("state")
        if (expectedState == null || returnedState != expectedState) {
            // A mismatched state means this callback did not originate from our request.
            return Result.failure(SpotifyAuthError("state_mismatch"))
        }

        val code = uri.getQueryParameter("code")
            ?: return Result.failure(SpotifyAuthError("missing_code"))

        return Result.success(code)
    }

    private fun ByteArray.toBase64Url(): String =
        Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}

class SpotifyAuthError(val reason: String) : Exception("Spotify authorization failed: $reason")
