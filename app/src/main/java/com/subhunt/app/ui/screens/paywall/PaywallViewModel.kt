package com.subhunt.app.ui.screens.paywall

import androidx.lifecycle.ViewModel
import com.subhunt.app.billing.BillingManager
import com.subhunt.app.billing.ActivationCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    val billingManager: BillingManager
) : ViewModel() {

    val isSubscribed: StateFlow<Boolean> = billingManager.isSubscribed
    val errorMessage: StateFlow<String?> = billingManager.errorMessage
    val activationEmail: StateFlow<String?> = billingManager.activationEmail
    val activationPlan: StateFlow<String?> = billingManager.activationPlan

    fun activate(code: String, email: String): Boolean {
        return billingManager.activateWithCode(code, email)
    }

    fun clearError() {
        billingManager.clearError()
    }

    fun getPlanDisplayName(plan: Plan): String {
        val planId = when (plan) {
            Plan.MONTHLY -> "monthly"
            Plan.YEARLY -> "yearly"
            Plan.LIFETIME -> "lifetime"
        }
        return ActivationCodeGenerator.getPlanDisplayName(planId)
    }

    fun getPlanPrice(plan: Plan): String {
        val planId = when (plan) {
            Plan.MONTHLY -> "monthly"
            Plan.YEARLY -> "yearly"
            Plan.LIFETIME -> "lifetime"
        }
        return ActivationCodeGenerator.getPlanPrice(planId)
    }

    fun getPlanId(plan: Plan): String {
        return when (plan) {
            Plan.MONTHLY -> "monthly"
            Plan.YEARLY -> "yearly"
            Plan.LIFETIME -> "lifetime"
        }
    }
}
