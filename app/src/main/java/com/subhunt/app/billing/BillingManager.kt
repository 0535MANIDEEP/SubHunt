package com.subhunt.app.billing

// Copyright (c) 2026 Manideep Daram. All rights reserved.
// Free and open source — no paywall, no activation gate.
// Activation-code module retained for reference but disabled; hasProEntitlement() always returns true.

import android.content.Context
import android.util.Log
import com.subhunt.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "BillingManager"

    companion object {
        private const val PREFS_NAME = "subhunt_activation"
        private const val KEY_ACTIVATED = "activated"
        private const val KEY_ACTIVATED_EMAIL = "activated_email"
        private const val KEY_ACTIVATED_PLAN = "activated_plan"
        private const val KEY_ACTIVATED_DATE = "activated_date"
        private const val KEY_ACTIVATED_EXPIRES = "activated_expires"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    private val _activationEmail = MutableStateFlow<String?>(null)
    val activationEmail: StateFlow<String?> = _activationEmail

    private val _activationPlan = MutableStateFlow<String?>(null)
    val activationPlan: StateFlow<String?> = _activationPlan

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        restoreActivation()
    }

    fun activateWithCode(code: String, email: String): Boolean {
        val cleanCode = code.trim().uppercase()
        val cleanEmail = email.trim().lowercase()

        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            _errorMessage.value = "Please enter a valid email address."
            return false
        }

        if (cleanCode.length != 12) {
            _errorMessage.value = "Invalid activation code format."
            return false
        }

        if (!ActivationCodeGenerator.verify(cleanCode, cleanEmail)) {
            _errorMessage.value = "Invalid activation code. Please check and try again."
            return false
        }

        val plan = ActivationCodeGenerator.getPlanFromCode(cleanCode)

        prefs.edit().apply {
            putBoolean(KEY_ACTIVATED, true)
            putString(KEY_ACTIVATED_EMAIL, cleanEmail)
            putString(KEY_ACTIVATED_PLAN, plan)
            putLong(KEY_ACTIVATED_DATE, System.currentTimeMillis())
            apply()
        }

        _isSubscribed.value = true
        _activationEmail.value = cleanEmail
        _activationPlan.value = plan

        if (BuildConfig.DEBUG) Log.d(TAG, "Activated: $cleanEmail, plan: $plan")
        return true
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun hasProEntitlement(): Boolean {
        return true // Free and open source — always entitled
    }

    fun getActivationInfo(): String? {
        val email = _activationEmail.value ?: return null
        val plan = _activationPlan.value ?: return "Pro"
        return "Activated for $email ($plan)"
    }

    fun isPlanActive(plan: String): Boolean {
        return true // Free and open source — all plans active
    }

    private fun restoreActivation() {
        val activated = prefs.getBoolean(KEY_ACTIVATED, false)
        if (activated) {
            _isSubscribed.value = true
            _activationEmail.value = prefs.getString(KEY_ACTIVATED_EMAIL, null)
            _activationPlan.value = prefs.getString(KEY_ACTIVATED_PLAN, null)
            if (BuildConfig.DEBUG) Log.d(TAG, "Restored activation: ${_activationEmail.value}")
        }
    }
}
