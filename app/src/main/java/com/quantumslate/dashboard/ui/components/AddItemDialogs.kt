package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

/**
 * Prompts for a flight number to track.
 *
 * Flight numbers are an airline code plus 1–4 digits (e.g. BA2490, U21234). Validating here
 * matters more than usual: the free flight tier allows only 100 lookups a month, so a typo
 * that reaches the network costs real quota.
 */
@Composable
fun AddFlightDialog(
    onDismiss: () -> Unit,
    onConfirm: (flightNumber: String) -> Unit,
    existingFlights: List<String> = emptyList()
) {
    var input by remember { mutableStateOf("") }

    val normalised = input.trim().uppercase().replace(" ", "")
    val duplicate = normalised in existingFlights.map { it.uppercase() }
    val validFormat = FLIGHT_NUMBER_REGEX.matches(normalised)
    val error = when {
        normalised.isEmpty() -> null
        !validFormat -> "Use an airline code and number, e.g. BA2490"
        duplicate -> "That flight is already tracked"
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track a flight") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Flight number") },
                    placeholder = { Text("BA2490") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(normalised) },
                enabled = validFormat && !duplicate
            ) {
                Text("Track")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private val FLIGHT_NUMBER_REGEX = Regex("^[A-Z0-9]{2,3}\\d{1,4}$")

/**
 * Prompts for an RSS/Atom feed URL.
 */
@Composable
fun AddFeedDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String) -> Unit,
    existingFeeds: List<String> = emptyList()
) {
    var input by remember { mutableStateOf("") }

    val normalised = input.trim()
    val duplicate = normalised in existingFeeds
    // Require HTTPS: the Bible mandates HTTPS for all network calls (§12), and cleartext
    // would be blocked by the platform's default network security config anyway.
    val validUrl = normalised.startsWith("https://") && normalised.length > "https://".length
    val error = when {
        normalised.isEmpty() -> null
        normalised.startsWith("http://") -> "Feed must use https://"
        !validUrl -> "Enter a full feed URL starting with https://"
        duplicate -> "That feed is already added"
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add news feed") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("RSS or Atom feed URL") },
                    placeholder = { Text("https://example.com/feed.xml") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(normalised) },
                enabled = validUrl && !duplicate
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
