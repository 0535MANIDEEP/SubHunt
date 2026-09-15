package com.subhunt.app.ui.screens.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhunt.app.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    val billingManager: BillingManager
) : ViewModel() {

    val isSubscribed: StateFlow<Boolean> = billingManager.isSubscribed
    val errorMessage: StateFlow<String?> = billingManager.errorMessage

    fun purchase(plan: Plan) {
        viewModelScope.launch {
            val packageToPurchase = billingManager.offerings.value
                ?.current
                ?.availablePackages
                ?.firstOrNull { pkg ->
                    when (plan) {
                        Plan.MONTHLY -> pkg.identifier.contains("monthly", ignoreCase = true)
                        Plan.YEARLY -> pkg.identifier.contains("yearly", ignoreCase = true)
                        Plan.LIFETIME -> pkg.identifier.contains("lifetime", ignoreCase = true)
                    }
                }

            if (packageToPurchase != null) {
                billingManager.purchase(packageToPurchase)
            } else {
                billingManager.buyPro(null)
            }
        }
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    fun clearError() {
        billingManager.clearError()
    }
}
