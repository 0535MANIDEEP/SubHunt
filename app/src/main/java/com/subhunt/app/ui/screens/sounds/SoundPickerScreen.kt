package com.subhunt.app.ui.screens.sounds

import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subhunt.app.domain.model.Subscription
import com.subhunt.app.notification.ReminderSound
import com.subhunt.app.notification.ReminderSounds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: SoundPickerViewModel = hiltViewModel()
) {
    val globalSoundId by viewModel.globalSoundId.collectAsStateWithLifecycle()
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
    val isSubscribed by viewModel.isSubscribed.collectAsStateWithLifecycle()
    val soundOverrides by viewModel.soundOverrides.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var playingId by remember { mutableStateOf<String?>(null) }
    var editingSub by remember { mutableStateOf<Subscription?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
            player = null
        }
    }

    fun preview(sound: ReminderSound) {
        player?.release()
        player = null
        if (sound.resId == null) return
        player = MediaPlayer.create(context, sound.resId)?.also {
            it.setOnCompletionListener { playingId = null }
            it.start()
            playingId = sound.id
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reminder Sounds", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
        ) {
            item {
                SectionLabel("Default tone")
                Text(
                    "Plays for every billing reminder unless a subscription has its own tone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(ReminderSounds.FREE) { sound ->
                SoundRow(
                    sound = sound,
                    selected = globalSoundId == sound.id,
                    playing = playingId == sound.id,
                    onPreview = { preview(sound) },
                    onSelect = { viewModel.selectGlobalSound(sound.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                ProSectionHeader(
                    title = "Pro tones",
                    isSubscribed = isSubscribed,
                    onUpgradeClick = onNavigateToPaywall
                )
            }
            items(ReminderSounds.PRO) { sound ->
                SoundRow(
                    sound = sound,
                    selected = globalSoundId == sound.id,
                    playing = playingId == sound.id,
                    locked = !isSubscribed,
                    onPreview = { if (isSubscribed) preview(sound) else onNavigateToPaywall() },
                    onSelect = {
                        if (isSubscribed) viewModel.selectGlobalSound(sound.id)
                        else onNavigateToPaywall()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                ProSectionHeader(
                    title = "Per-subscription tones",
                    isSubscribed = isSubscribed,
                    onUpgradeClick = onNavigateToPaywall
                )
                Text(
                    "Give each subscription its own vibe. Falls back to the default tone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            if (subscriptions.isEmpty()) {
                item {
                    Text(
                        "Add a subscription first, then assign it a tone here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(subscriptions, key = { it.id }) { sub ->
                    val overrideId = soundOverrides[sub.id]
                    val locked = !isSubscribed
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (locked) onNavigateToPaywall()
                                else editingSub = sub
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                sub.category.icon,
                                contentDescription = null,
                                tint = Color(sub.color),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sub.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (overrideId == null) "Default tone"
                                    else ReminderSounds.byId(overrideId).label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (locked) {
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = "Pro",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editingSub?.let { sub ->
        val overrideId = soundOverrides[sub.id]
        AlertDialog(
            onDismissRequest = { editingSub = null },
            title = { Text("${sub.name} tone") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val options = listOf(null) + ReminderSounds.FREE.map { it.id } +
                        ReminderSounds.PRO.map { it.id }
                    options.forEach { id ->
                        val label = if (id == null) "Default tone" else ReminderSounds.byId(id).label
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setOverride(sub.id, id)
                                    editingSub = null
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = overrideId == id,
                                onClick = {
                                    viewModel.setOverride(sub.id, id)
                                    editingSub = null
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { editingSub = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ProSectionHeader(
    title: String,
    isSubscribed: Boolean,
    onUpgradeClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SectionLabel(title)
        Spacer(modifier = Modifier.width(8.dp))
        if (!isSubscribed) {
            AssistChip(
                onClick = onUpgradeClick,
                label = { Text("PRO") },
                leadingIcon = {
                    Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            )
        }
    }
}

@Composable
private fun SoundRow(
    sound: ReminderSound,
    selected: Boolean,
    playing: Boolean,
    locked: Boolean = false,
    onPreview: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreview, modifier = Modifier.size(40.dp)) {
                Icon(
                    if (locked) Icons.Rounded.Lock
                    else if (playing) Icons.Rounded.Stop
                    else Icons.Rounded.PlayArrow,
                    contentDescription = if (locked) "Locked" else "Preview",
                    tint = if (locked) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sound.label, fontWeight = FontWeight.SemiBold)
                Text(
                    sound.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
