package com.subhunt.app.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSounds: () -> Unit = {},
    onNavigateToSetPin: () -> Unit = {},
    onNavigateToChangePin: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val reminderSoundId by viewModel.reminderSound.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val lockEnabled by viewModel.lockEnabled.collectAsStateWithLifecycle()
    val biometricAllowed by viewModel.biometricAllowed.collectAsStateWithLifecycle()
    val isSubscribed by viewModel.isSubscribed.collectAsStateWithLifecycle()
    var showLockOptions by remember { mutableStateOf(false) }
    var showPinVerifyDialog by remember { mutableStateOf(false) }
    var showNotificationRationale by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationsEnabled(granted)
    }

    fun handleExportResult(
        result: com.subhunt.app.export.ExportResult,
        mimeType: String
    ) {
        scope.launch {
            when (result) {
                is com.subhunt.app.export.ExportResult.Saved -> {
                    val action = snackbarHostState.showSnackbar(
                        message = "Saved ${result.fileName}",
                        actionLabel = "Share",
                        withDismissAction = true
                    )
                    if (action == SnackbarResult.ActionPerformed) {
                        viewModel.shareExportedFile(context, result.uri, mimeType)
                    }
                }
                com.subhunt.app.export.ExportResult.Empty ->
                    snackbarHostState.showSnackbar("Nothing to export yet")
                is com.subhunt.app.export.ExportResult.Error ->
                    snackbarHostState.showSnackbar("Export failed: ${result.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "General",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsItem(
                icon = Icons.Rounded.Notifications,
                title = "Bill Reminders",
                subtitle = if (notificationsEnabled) "Enabled" else "Disabled",
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                showNotificationRationale = true
                            } else {
                                viewModel.setNotificationsEnabled(enabled)
                            }
                        }
                    )
                }
            )

            SettingsItem(
                icon = Icons.Rounded.MusicNote,
                title = "Reminder Sound",
                subtitle = com.subhunt.app.notification.ReminderSounds.byId(reminderSoundId).label,
                onClick = { onNavigateToSounds() }
            )

            SettingsItem(
                icon = Icons.Rounded.AttachMoney,
                title = "Currency",
                subtitle = when (currencySymbol) {
                    "$" -> "US Dollar ($)"
                    "₹" -> "Indian Rupee (₹)"
                    "€" -> "Euro (€)"
                    "£" -> "British Pound (£)"
                    "¥" -> "Yen (¥)"
                    else -> currencySymbol
                },
                onClick = {
                    val currencies = listOf("$", "₹", "€", "£", "¥")
                    val currentIndex = currencies.indexOf(currencySymbol).coerceAtLeast(0)
                    viewModel.setCurrencySymbol(currencies[(currentIndex + 1) % currencies.size])
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "Security",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsItem(
                icon = Icons.Rounded.Lock,
                title = "App Lock",
                subtitle = if (lockEnabled) "PIN required to open" else "Protect with PIN or biometrics",
                onClick = {
                    if (lockEnabled) showLockOptions = true
                    else onNavigateToSetPin()
                }
            )

            if (lockEnabled) {
                SettingsItem(
                    icon = Icons.Rounded.Fingerprint,
                    title = "Unlock with biometrics",
                    subtitle = if (!viewModel.canUseBiometric) "Not available on this device"
                    else if (biometricAllowed) "Enabled" else "Face / fingerprint",
                    trailing = {
                        Switch(
                            checked = biometricAllowed,
                            enabled = viewModel.canUseBiometric,
                            onCheckedChange = { viewModel.setBiometricAllowed(it) }
                        )
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "Data",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsItem(
                icon = Icons.Rounded.FileDownload,
                title = "Export to CSV",
                subtitle = "Save spreadsheet to Downloads",
                onClick = {
                    viewModel.exportCsv(context) { result ->
                        handleExportResult(result, "text/csv")
                    }
                }
            )

            SettingsItem(
                icon = Icons.Rounded.DataObject,
                title = "Export to JSON",
                subtitle = "Save full backup to Downloads",
                onClick = {
                    viewModel.exportJson(context) { result ->
                        handleExportResult(result, "application/json")
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "About",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsItem(
                icon = Icons.Rounded.Info,
                title = "Version",
                subtitle = "1.0.0"
            )

            SettingsItem(
                icon = Icons.Rounded.Person,
                title = "Developer",
                subtitle = "Manideep Daram"
            )

            SettingsItem(
                icon = Icons.Rounded.Code,
                title = "Rate SubHunt",
                subtitle = "Help us grow with a review"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "Legal",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SettingsItem(
                icon = Icons.Rounded.Policy,
                title = "Privacy Policy",
                subtitle = "How your data is handled",
                onClick = onNavigateToPrivacy
            )

            SettingsItem(
                icon = Icons.Rounded.Description,
                title = "Terms of Service",
                subtitle = "Rules for using SubHunt",
                onClick = onNavigateToTerms
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
    }

    if (showLockOptions) {
        AlertDialog(
            onDismissRequest = { showLockOptions = false },
            title = { Text("App Lock") },
            text = { Text("Your data stays protected by your PIN.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLockOptions = false
                        onNavigateToChangePin()
                    }
                ) { Text("Change PIN") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showLockOptions = false
                            showPinVerifyDialog = true
                        }
                    ) { Text("Turn off") }
                    TextButton(onClick = { showLockOptions = false }) { Text("Cancel") }
                }
            }
        )
    }

    if (showPinVerifyDialog) {
        var pinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showPinVerifyDialog = false },
            title = { Text("Enter PIN to disable lock") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("PIN") },
                        isError = pinError,
                        supportingText = if (pinError) {
                            { Text("Wrong PIN") }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.verifyPinAndDisable(pinInput) { ok ->
                            if (ok) {
                                showPinVerifyDialog = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("App Lock turned off")
                                }
                            } else {
                                pinError = true
                            }
                        }
                    },
                    enabled = pinInput.length in 4..6
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showPinVerifyDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showNotificationRationale) {
        AlertDialog(
            onDismissRequest = { showNotificationRationale = false },
            title = { Text("Enable Notifications") },
            text = { Text("SubHunt needs notification permission to remind you before subscription bills are due. Without it, you may miss payment deadlines.") },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationRationale = false }) { Text("Not now") }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProBadge() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            "PRO",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
