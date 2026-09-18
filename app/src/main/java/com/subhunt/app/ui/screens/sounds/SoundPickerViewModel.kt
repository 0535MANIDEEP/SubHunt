package com.subhunt.app.ui.screens.sounds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhunt.app.billing.BillingManager
import com.subhunt.app.data.local.UserPreferences
import com.subhunt.app.domain.model.Subscription
import com.subhunt.app.domain.usecase.GetSubscriptionsUseCase
import com.subhunt.app.notification.ReminderSounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundPickerViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val getSubscriptions: GetSubscriptionsUseCase,
    val billingManager: BillingManager
) : ViewModel() {

    val isSubscribed: StateFlow<Boolean> = billingManager.isSubscribed

    val globalSoundId: StateFlow<String> = userPreferences.reminderSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReminderSounds.SYSTEM_DEFAULT_ID)

    val subscriptions: StateFlow<List<Subscription>> = getSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val soundOverrides: StateFlow<Map<Long, String>> = userPreferences.soundOverrides
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun selectGlobalSound(soundId: String) {
        viewModelScope.launch {
            userPreferences.setReminderSound(soundId)
        }
    }

    fun setOverride(subscriptionId: Long, soundId: String?) {
        viewModelScope.launch {
            userPreferences.setReminderSoundFor(subscriptionId, soundId)
        }
    }
}
