package com.quantumslate.dashboard.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.quantumslate.dashboard.data.local.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsState by viewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    Text(
                        text = "←",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable { onNavigateBack() }
                    )
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // API Configuration Section
                SettingsSection(title = "API Configuration") {
                    ApiKeySetting(
                        label = "OpenWeatherMap API Key",
                        value = settingsState.openWeatherApiKey ?: "",
                        onSave = { viewModel.saveOpenWeatherApiKey(it) }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ApiKeySetting(
                        label = "Flight API Key",
                        value = settingsState.flightApiKey ?: "",
                        onSave = { viewModel.saveFlightApiKey(it) }
                    )
                }

                // Display Settings Section
                SettingsSection(title = "Display") {
                    DropdownSetting(
                        label = "Default UI Mode",
                        value = settingsState.defaultUiMode.name,
                        options = PreferencesManager.UiMode.values().map { it.name },
                        onValueChange = { viewModel.saveDefaultUiMode(PreferencesManager.UiMode.valueOf(it)) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DropdownSetting(
                        label = "Dark Mode",
                        value = settingsState.darkMode.name,
                        options = PreferencesManager.DarkMode.values().map { it.name },
                        onValueChange = { viewModel.saveDarkMode(PreferencesManager.DarkMode.valueOf(it)) }
                    )
                }

                // Update Settings Section
                SettingsSection(title = "Updates") {
                    DropdownSetting(
                        label = "Update Frequency",
                        value = settingsState.updateMode.name,
                        options = PreferencesManager.UpdateMode.values().map { it.name },
                        onValueChange = { viewModel.saveUpdateMode(PreferencesManager.UpdateMode.valueOf(it)) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TimeSetting(
                        label = "Auto-update Time",
                        value = settingsState.autoUpdateTime,
                        onSave = { viewModel.saveAutoUpdateTime(it) }
                    )
                }

                // Mascot Settings Section
                SettingsSection(title = "Mascot") {
                    var mascotDialogOpen by remember { mutableStateOf(false) }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Character",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = settingsState.mascotCharacter,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.clickable { mascotDialogOpen = true }
                        )
                    }
                    
                    if (mascotDialogOpen) {
                        MascotCharacterDialog(
                            currentCharacter = settingsState.mascotCharacter,
                            onCharacterSelected = { 
                                viewModel.saveMascotCharacter(it)
                                mascotDialogOpen = false
                            },
                            onDismiss = { mascotDialogOpen = false }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    BooleanSetting(
                        label = "Enable Animations",
                        value = settingsState.mascotAnimationsEnabled,
                        onValueChange = { viewModel.saveMascotAnimationsEnabled(it) }
                    )
                }

                // Location Settings
                SettingsSection(title = "Location") {
                    BooleanSetting(
                        label = "Use GPS for Weather",
                        value = settingsState.locationEnabled,
                        onValueChange = { viewModel.saveLocationEnabled(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun ApiKeySetting(
    label: String,
    value: String,
    onSave: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (value.isNotEmpty()) "••••••••" else "Not set",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
    
    if (showDialog) {
        ApiKeyDialog(
            label = label,
            currentValue = value,
            onSave = { 
                onSave(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun ApiKeyDialog(
    label: String,
    currentValue: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf(currentValue) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onSave(apiKey.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DropdownSetting(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        ExposedDropdownMenu(
            expanded = expanded,
            value = value,
            options = options,
            onDismiss = { expanded = false },
            onItemSelected = onValueChange
        )
    }
}

@Composable
fun ExposedDropdownMenu(
    expanded: Boolean,
    value: String,
    options: List<String>,
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit
) {
    // Simplified dropdown - in production use ExposedDropdownMenuBox
    if (expanded) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select option") },
            text = {
                Column {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onItemSelected(option)
                                onDismiss()
                            }
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun BooleanSetting(
    label: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = value,
            onCheckedChange = onValueChange
        )
    }
}

@Composable
fun TimeSetting(
    label: String,
    value: String,
    onSave: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var timeValue by remember { mutableStateOf(value) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = timeValue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(label) },
            text = {
                OutlinedTextField(
                    value = timeValue,
                    onValueChange = { timeValue = it },
                    label = { Text("Time (HH:mm)") },
                    placeholder = { Text("08:00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { 
                    onSave(timeValue)
                    showDialog = false 
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MascotCharacterDialog(
    currentCharacter: String,
    onCharacterSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val characters = listOf("robot", "cat", "bird", "creature")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Mascot") },
        text = {
            Column {
                characters.forEach { character ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCharacterSelected(character) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(character.replaceFirstChar { it.uppercase() })
                        if (character == currentCharacter) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
