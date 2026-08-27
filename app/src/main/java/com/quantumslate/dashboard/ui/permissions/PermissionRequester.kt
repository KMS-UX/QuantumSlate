package com.quantumslate.dashboard.ui.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Thin Compose wrapper over the runtime-permission APIs.
 *
 * Returns a callable that launches the system dialog and reports the result, so widgets can
 * offer a "Grant access" action without each one re-deriving the permission plumbing.
 */
@Composable
fun rememberPermissionRequester(
    permission: String,
    onResult: (granted: Boolean) -> Unit = {}
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )
    return remember(permission, launcher) {
        { launcher.launch(permission) }
    }
}

/**
 * Requester for location, used by the weather widget.
 *
 * Asks for both coarse and fine together: some devices only ever return a fix from the GPS
 * provider, so requesting coarse alone can leave the user granted-but-locationless.
 */
@Composable
fun rememberLocationPermissionRequester(onResult: (Boolean) -> Unit = {}): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { grants -> onResult(grants.values.any { it }) }
    )
    return remember(launcher) {
        {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }
}

/** Requester for READ_CALENDAR, used by the calendar widget. */
@Composable
fun rememberCalendarPermissionRequester(onResult: (Boolean) -> Unit = {}): () -> Unit =
    rememberPermissionRequester(Manifest.permission.READ_CALENDAR, onResult)

/**
 * Requester for POST_NOTIFICATIONS.
 *
 * The permission only exists from API 33; below that it is granted implicitly, so the
 * returned lambda reports success without showing a dialog.
 */
@Composable
fun rememberNotificationPermissionRequester(onResult: (Boolean) -> Unit = {}): () -> Unit {
    val realRequester = rememberPermissionRequester(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            // Never launched on older APIs; a valid string is still required.
            Manifest.permission.INTERNET
        },
        onResult = onResult
    )
    return remember(realRequester) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                realRequester()
            } else {
                onResult(true)
            }
        }
    }
}

fun hasPermission(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

/** True when the calendar can be read right now. */
@Composable
fun hasCalendarPermission(): Boolean {
    val context = LocalContext.current
    return hasPermission(context, Manifest.permission.READ_CALENDAR)
}
