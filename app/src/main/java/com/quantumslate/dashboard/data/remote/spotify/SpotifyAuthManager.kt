package com.quantumslate.dashboard.data.remote.spotify

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.quantumslate.dashboard.data.local.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the Spotify OAuth lifecycle: starting authorization, handling the redirect, and
 * keeping a valid access token available to the API layer.
 */
@Singleton
class SpotifyAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    okHttpClient: OkHttpClient
) {

    companion object {
        /**
         * Refresh this long before actual expiry, so a request that starts just under the
         * wire does not arrive with a token that expired in flight.
         */
        private const val EXPIRY_MARGIN_MS = 60_000L
    }

    private val authApi: SpotifyAuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(SpotifyAuthApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyAuthApiService::class.java)
    }

    // Held only for the duration of one authorization attempt.
    private var pendingCodeVerifier: String? = null
    private var pendingState: String? = null

    /** Serialises refreshes so concurrent widget loads cannot each spend the refresh token. */
    private val refreshMutex = Mutex()

    val isConnected: Boolean
        get() = !preferencesManager.getSpotifyRefreshToken().isNullOrBlank()

    /**
     * Opens the Spotify consent page in a Custom Tab.
     *
     * A Custom Tab rather than a WebView: it shares the system browser's cookie jar (so an
     * already-signed-in user often skips the password step), shows the real URL bar, and
     * keeps the user's Spotify credentials out of the app's process entirely.
     *
     * @return failure when no client ID has been configured yet
     */
    fun beginAuthorization(): Result<Unit> {
        val clientId = preferencesManager.getSpotifyClientId()
        if (clientId.isNullOrBlank()) {
            return Result.failure(SpotifyAuthError("missing_client_id"))
        }

        val verifier = SpotifyAuth.generateCodeVerifier()
        val state = SpotifyAuth.generateState()
        pendingCodeVerifier = verifier
        pendingState = state

        val url = SpotifyAuth.buildAuthorizationUrl(
            clientId = clientId,
            codeChallenge = SpotifyAuth.generateCodeChallenge(verifier),
            state = state
        )

        return try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .apply { intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                .launchUrl(context, Uri.parse(url))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Completes authorization from the redirect URI, exchanging the code for tokens.
     */
    suspend fun handleRedirect(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        val clientId = preferencesManager.getSpotifyClientId()
            ?: return@withContext Result.failure(SpotifyAuthError("missing_client_id"))

        val verifier = pendingCodeVerifier
            ?: return@withContext Result.failure(SpotifyAuthError("no_pending_request"))

        val code = SpotifyAuth.parseCallback(uri, pendingState)
            .getOrElse { return@withContext Result.failure(it) }

        try {
            val response = authApi.exchangeCode(
                code = code,
                redirectUri = SpotifyAuth.REDIRECT_URI,
                clientId = clientId,
                codeVerifier = verifier
            )
            persist(response)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            // One-shot values: never reuse a verifier or state across attempts.
            pendingCodeVerifier = null
            pendingState = null
        }
    }

    /**
     * Returns a currently-valid access token, refreshing it first if necessary.
     *
     * @return null when the user has not connected Spotify, or the refresh token is no
     *         longer accepted (in which case stored credentials are cleared so the UI can
     *         prompt for a fresh connection).
     */
    suspend fun getValidAccessToken(): String? = refreshMutex.withLock {
        val token = preferencesManager.getSpotifyAccessToken()
        val expiry = preferencesManager.getSpotifyTokenExpiry()

        if (!token.isNullOrBlank() && System.currentTimeMillis() < expiry - EXPIRY_MARGIN_MS) {
            return token
        }

        val refreshToken = preferencesManager.getSpotifyRefreshToken()
        if (refreshToken.isNullOrBlank()) return null

        val clientId = preferencesManager.getSpotifyClientId() ?: return null

        return try {
            val response = withContext(Dispatchers.IO) {
                authApi.refreshToken(refreshToken = refreshToken, clientId = clientId)
            }
            if (response.accessToken.isNullOrBlank()) {
                preferencesManager.clearSpotifyTokens()
                null
            } else {
                persist(response)
                response.accessToken
            }
        } catch (e: Exception) {
            // A rejected refresh token cannot be recovered from; force re-authorization
            // rather than retrying a credential Spotify has already refused.
            preferencesManager.clearSpotifyTokens()
            null
        }
    }

    fun disconnect() {
        preferencesManager.clearSpotifyTokens()
    }

    private fun persist(response: SpotifyTokenResponse) {
        response.accessToken?.let { preferencesManager.saveSpotifyAccessToken(it) }
        // Spotify omits refresh_token on some refresh responses; keep the existing one then.
        response.refreshToken?.let { preferencesManager.saveSpotifyRefreshToken(it) }
        preferencesManager.saveSpotifyTokenExpiry(
            System.currentTimeMillis() + response.expiresIn * 1000L
        )
    }
}
