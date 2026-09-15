package com.subhunt.app.ui.screens.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhunt.app.security.AppLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LockMode { UNLOCK, SET, CHANGE }

@HiltViewModel
class LockViewModel @Inject constructor(
    private val appLockManager: AppLockManager
) : ViewModel() {

    val lockEnabled: StateFlow<Boolean> = appLockManager.lockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val biometricAllowed: StateFlow<Boolean> = appLockManager.biometricAllowed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val sessionUnlocked: StateFlow<Boolean> = appLockManager.sessionUnlocked

    val failedAttempts: StateFlow<Int> = appLockManager.failedAttempts
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val cooldownUntil: StateFlow<Long> = appLockManager.cooldownUntil
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val canUseBiometric: Boolean = appLockManager.canUseBiometric()

    fun markUnlocked() = appLockManager.markUnlocked()

    fun checkPin(pin: String, onResult: (Boolean) -> Unit) {
        if (System.currentTimeMillis() < cooldownUntil.value) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val ok = appLockManager.checkPin(pin)
            if (ok) {
                appLockManager.setFailedAttempts(0)
                appLockManager.setCooldownUntil(0L)
                appLockManager.markUnlocked()
            } else {
                val attempts = (failedAttempts.value) + 1
                if (attempts >= MAX_ATTEMPTS) {
                    val backoff = COOLDOWN_BASE_MS * (1L shl (attemptStage.value.coerceAtMost(MAX_STAGES - 1)))
                    appLockManager.setFailedAttempts(0)
                    appLockManager.setCooldownUntil(System.currentTimeMillis() + backoff)
                    attemptStage.value = (attemptStage.value + 1).coerceAtMost(MAX_STAGES)
                } else {
                    appLockManager.setFailedAttempts(attempts)
                }
            }
            onResult(ok)
        }
    }

    fun setPin(pin: String, onDone: () -> Unit) {
        viewModelScope.launch {
            appLockManager.setPin(pin)
            onDone()
        }
    }

    private val attemptStage = kotlinx.coroutines.flow.MutableStateFlow(0)

    companion object {
        const val MAX_ATTEMPTS = 5
        const val COOLDOWN_BASE_MS = 30_000L
        const val MAX_STAGES = 5
    }
}
