package com.subhunt.app.ui.screens.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    mode: LockMode,
    onUnlocked: () -> Unit,
    onCancel: (() -> Unit)? = null,
    viewModel: LockViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val biometricAllowed by viewModel.biometricAllowed.collectAsStateWithLifecycle()
    val cooldownUntil by viewModel.cooldownUntil.collectAsStateWithLifecycle()
    val stableOnUnlocked = rememberUpdatedState(onUnlocked)

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var remainingCooldown by remember { mutableStateOf(0L) }

    val inCooldown = cooldownUntil > System.currentTimeMillis()

    LaunchedEffect(cooldownUntil) {
        while (cooldownUntil > System.currentTimeMillis()) {
            remainingCooldown = (cooldownUntil - System.currentTimeMillis()) / 1000 + 1
            delay(500)
        }
        remainingCooldown = 0
    }

    fun showBiometric() {
        val activity = context as? FragmentActivity ?: return
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.markUnlocked()
                    stableOnUnlocked.value()
                }

                override fun onAuthenticationError(code: Int, message: CharSequence) {
                    if (code != BiometricPrompt.ERROR_USER_CANCELED &&
                        code != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        error = message.toString()
                    }
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock SubHunt")
                .setSubtitle("Use your biometrics to continue")
                .setNegativeButtonText("Use PIN")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .build()
        )
    }

    LaunchedEffect(mode, biometricAllowed) {
        if (mode == LockMode.UNLOCK && biometricAllowed && viewModel.canUseBiometric) {
            showBiometric()
        }
    }

    fun submitPin(entered: String) {
        error = null
        when (mode) {
            LockMode.UNLOCK -> {
                if (entered.length < 4) return
                viewModel.checkPin(entered) { ok ->
                    if (ok) onUnlocked()
                    else {
                        error = "Wrong PIN. Try again."
                        pin = ""
                    }
                }
            }
            LockMode.SET, LockMode.CHANGE -> {
                if (entered.length !in 4..6) {
                    error = "PIN must be 4–6 digits."
                    return
                }
                if (confirmPin == null) {
                    confirmPin = entered
                    pin = ""
                } else if (entered == confirmPin) {
                    viewModel.setPin(entered) { onUnlocked() }
                } else {
                    error = "PINs don't match. Start over."
                    confirmPin = null
                    pin = ""
                }
            }
        }
    }

    val title = when (mode) {
        LockMode.UNLOCK -> "Welcome back"
        LockMode.SET -> "Set app PIN"
        LockMode.CHANGE -> "Change app PIN"
    }
    val subtitle = when (mode) {
        LockMode.UNLOCK -> "Enter your PIN to continue"
        LockMode.SET, LockMode.CHANGE ->
            if (confirmPin == null) "Choose a 4–6 digit PIN" else "Confirm your PIN"
    }

    Scaffold(
        topBar = {
            if (onCancel != null) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            PinDots(pin.length)

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                when {
                    inCooldown -> Text(
                        "Too many attempts. Try again in ${remainingCooldown}s.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    error != null -> Text(
                        error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            PinPad(
                enabled = !inCooldown,
                canSubmit = pin.length in 4..5,
                onDigit = { d ->
                    if (pin.length < 6) {
                        val next = pin + d
                        pin = next
                        if (next.length == 6) submitPin(next)
                    }
                },
                onSubmit = { submitPin(pin) },
                onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            if (mode == LockMode.UNLOCK && biometricAllowed && viewModel.canUseBiometric) {
                TextButton(onClick = { showBiometric() }) {
                    Icon(
                        Icons.Rounded.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use biometrics")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PinDots(filled: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(6) { i ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        if (i < filled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

@Composable
private fun PinPad(
    enabled: Boolean,
    canSubmit: Boolean,
    onDigit: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("submit", "0", "⌫")
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { key ->
                    when {
                        key == "submit" -> if (canSubmit) {
                            Button(
                                onClick = onSubmit,
                                enabled = enabled,
                                modifier = Modifier.size(72.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Rounded.Check, contentDescription = "Done")
                            }
                        } else {
                            Spacer(modifier = Modifier.size(72.dp))
                        }
                        key == "⌫" -> IconButton(
                            onClick = onBackspace,
                            enabled = enabled,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Delete")
                        }
                        else -> FilledTonalButton(
                            onClick = { onDigit(key) },
                            enabled = enabled,
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(key, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
    }
}
