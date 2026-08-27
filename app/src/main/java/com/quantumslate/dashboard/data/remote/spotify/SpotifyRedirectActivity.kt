package com.quantumslate.dashboard.data.remote.spotify

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.quantumslate.dashboard.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives the `quantumslate://spotify-callback` redirect, completes the token exchange,
 * and returns the user to the dashboard.
 *
 * A separate activity (rather than a deep link into MainActivity) keeps the callback out of
 * the app's normal navigation: it has no UI, finishes immediately, and leaves no entry in
 * the back stack for the user to navigate back into.
 */
@AndroidEntryPoint
class SpotifyRedirectActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: SpotifyAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        if (uri == null) {
            finish()
            return
        }

        lifecycleScope.launch {
            authManager.handleRedirect(uri)
            startActivity(
                Intent(this@SpotifyRedirectActivity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }
    }
}
