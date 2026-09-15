package com.subhunt.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_SOUND = stringPreferencesKey("reminder_sound")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")

        fun soundForSubscription(subscriptionId: Long) =
            stringPreferencesKey("reminder_sound_sub_$subscriptionId")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val currencySymbol: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CURRENCY_SYMBOL] ?: "$"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val reminderSound: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.REMINDER_SOUND] ?: "system"
    }

    suspend fun setReminderSound(soundId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REMINDER_SOUND] = soundId
        }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENCY_SYMBOL] = symbol
        }
    }

    fun reminderSoundFor(subscriptionId: Long): Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.soundForSubscription(subscriptionId)]
        }

    val soundOverrides: Flow<Map<Long, String>> = context.dataStore.data.map { prefs ->
        prefs.asMap().mapNotNull { (key, value) ->
            val name = key.name
            val id = name.removePrefix("reminder_sound_sub_").toLongOrNull()
            if (name.startsWith("reminder_sound_sub_") && value is String && id != null) {
                id to value
            } else null
        }.toMap()
    }

    suspend fun setReminderSoundFor(subscriptionId: Long, soundId: String?) {
        context.dataStore.edit { prefs ->
            val key = Keys.soundForSubscription(subscriptionId)
            if (soundId == null) prefs.remove(key) else prefs[key] = soundId
        }
    }

    data class SoundPrefsSnapshot(
        val global: String,
        val overrides: Map<Long, String>
    )

    suspend fun snapshotSoundPrefs(): SoundPrefsSnapshot {
        val prefs = context.dataStore.data.first()
        val overrides = mutableMapOf<Long, String>()
        prefs.asMap().forEach { (key, value) ->
            val name = key.name
            if (name.startsWith("reminder_sound_sub_") && value is String) {
                name.removePrefix("reminder_sound_sub_").toLongOrNull()?.let { id ->
                    overrides[id] = value
                }
            }
        }
        return SoundPrefsSnapshot(
            global = prefs[Keys.REMINDER_SOUND] ?: "system",
            overrides = overrides
        )
    }
}
