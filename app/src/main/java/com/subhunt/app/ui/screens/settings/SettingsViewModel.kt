package com.subhunt.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhunt.app.billing.BillingManager
import com.subhunt.app.data.local.UserPreferences
import com.subhunt.app.export.CsvExporter
import com.subhunt.app.export.ExportResult
import com.subhunt.app.security.AppLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val csvExporter: CsvExporter,
    private val appLockManager: AppLockManager,
    private val billingManager: BillingManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val notificationsEnabled: StateFlow<Boolean> = userPreferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val reminderSound: StateFlow<String> = userPreferences.reminderSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val currencySymbol: StateFlow<String> = userPreferences.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")

    val lockEnabled: StateFlow<Boolean> = appLockManager.lockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val biometricAllowed: StateFlow<Boolean> = appLockManager.biometricAllowed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val canUseBiometric: Boolean = appLockManager.canUseBiometric()

    val isSubscribed: StateFlow<Boolean> = billingManager.isSubscribed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setBiometricAllowed(allowed: Boolean) {
        viewModelScope.launch {
            appLockManager.setBiometricAllowed(allowed)
        }
    }

    fun disableLock() {
        viewModelScope.launch {
            appLockManager.disableLock()
        }
    }

    fun verifyPinAndDisable(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = appLockManager.checkPin(pin)
            if (ok) {
                appLockManager.disableLock()
            }
            onResult(ok)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            userPreferences.setCurrencySymbol(symbol)
        }
    }

    fun exportCsv(context: Context, onResult: (ExportResult) -> Unit) {
        viewModelScope.launch {
            onResult(csvExporter.exportCsv(context))
        }
    }

    fun exportJson(context: Context, onResult: (ExportResult) -> Unit) {
        viewModelScope.launch {
            onResult(csvExporter.exportJson(context))
        }
    }

    fun shareExportedFile(context: Context, uri: android.net.Uri, mimeType: String) {
        csvExporter.shareFile(context, uri, mimeType)
    }
}
