package com.subhunt.app.billing

// Copyright (c) 2026 Manideep Daram. All rights reserved.

import android.content.Context
import android.util.Log
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.UpdatedCustomerInfoDelegate
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.Package
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
        const val ENTITLEMENT_ID = "subhunt_pro"
    }

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
    val customerInfo: StateFlow<CustomerInfo?> = _customerInfo

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun configure(apiKey: String) {
        try {
            Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR
            Purchases.configure(apiKey = apiKey) {
                appUserId = null
            }
            setupCustomerInfoListener()
            fetchCustomerInfo()
            fetchOfferings()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Failed to configure RevenueCat", e)
            _errorMessage.value = "Failed to initialize: ${e.message}"
        }
    }

    private fun setupCustomerInfoListener() {
        Purchases.sharedInstance.delegate = UpdatedCustomerInfoDelegate { customerInfo ->
            _customerInfo.value = customerInfo
            _isSubscribed.value = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
        }
    }

    fun fetchCustomerInfo() {
        try {
            Purchases.sharedInstance.getCustomerInfo(
                onError = { error ->
                    if (BuildConfig.DEBUG) Log.w(TAG, "Failed to fetch customer info: ${error.message}")
                    _errorMessage.value = "Failed to check subscription: ${error.message}"
                },
                onSuccess = { customerInfo ->
                    _customerInfo.value = customerInfo
                    _isSubscribed.value = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                }
            )
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Failed to fetch customer info", e)
        }
    }

    private fun fetchOfferings() {
        try {
            Purchases.sharedInstance.getOfferings(
                onError = { error ->
                    if (BuildConfig.DEBUG) Log.w(TAG, "Failed to fetch offerings: ${error.message}")
                    _errorMessage.value = "Failed to load offers: ${error.message}"
                },
                onSuccess = { offerings ->
                    _offerings.value = offerings
                }
            )
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Failed to fetch offerings", e)
        }
    }

    fun purchase(packageToPurchase: Package?) {
        val pkg = packageToPurchase ?: offerings.value?.current?.availablePackages?.firstOrNull()
        if (pkg == null) {
            _errorMessage.value = "No packages available for purchase"
            return
        }

        try {
            Purchases.sharedInstance.purchase(
                packageToPurchase = pkg,
                onError = { error, userCancelled ->
                    if (!userCancelled) {
                        if (BuildConfig.DEBUG) Log.w(TAG, "Purchase failed: ${error.message}")
                        _errorMessage.value = "Purchase failed: ${error.message}"
                    }
                },
                onSuccess = { transaction, customerInfo ->
                    _customerInfo.value = customerInfo
                    _isSubscribed.value = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                }
            )
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Purchase error", e)
            _errorMessage.value = "Purchase error: ${e.message}"
        }
    }

    fun buyPro(packageToPurchase: Package?) {
        purchase(packageToPurchase)
    }

    fun restorePurchases() {
        try {
            Purchases.sharedInstance.restorePurchases(
                onError = { error ->
                    if (BuildConfig.DEBUG) Log.w(TAG, "Failed to restore purchases: ${error.message}")
                    _errorMessage.value = "Failed to restore: ${error.message}"
                },
                onSuccess = { customerInfo ->
                    _customerInfo.value = customerInfo
                    _isSubscribed.value = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                }
            )
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Failed to restore purchases", e)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun hasProEntitlement(): Boolean {
        return _customerInfo.value?.entitlements?.get(ENTITLEMENT_ID)?.isActive == true
    }

    fun getActiveEntitlements(): Map<String, EntitlementInfo> {
        return _customerInfo.value?.entitlements?.active ?: emptyMap()
    }
}
