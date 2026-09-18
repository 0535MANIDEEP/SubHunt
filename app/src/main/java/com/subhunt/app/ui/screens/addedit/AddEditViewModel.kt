package com.subhunt.app.ui.screens.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhunt.app.billing.BillingManager
import com.subhunt.app.domain.model.FREE_SUBSCRIPTION_LIMIT
import com.subhunt.app.domain.model.Subscription
import com.subhunt.app.domain.usecase.AddSubscriptionUseCase
import com.subhunt.app.domain.usecase.GetSubscriptionByIdUseCase
import com.subhunt.app.domain.usecase.GetSubscriptionsUseCase
import com.subhunt.app.domain.usecase.UpdateSubscriptionUseCase
import com.subhunt.app.widget.WidgetRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val addSubscriptionUseCase: AddSubscriptionUseCase,
    private val updateSubscriptionUseCase: UpdateSubscriptionUseCase,
    private val getSubscriptionById: GetSubscriptionByIdUseCase,
    getSubscriptions: GetSubscriptionsUseCase,
    private val billingManager: BillingManager,
    private val widgetRefresher: WidgetRefresher
) : ViewModel() {

    private val _loadedSubscription = MutableStateFlow<Subscription?>(null)
    val loadedSubscription: StateFlow<Subscription?> = _loadedSubscription

    private val _subscriptionCount = getSubscriptions()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isAtFreeLimit: StateFlow<Boolean> = MutableStateFlow(false)

    private val _saveResult = MutableSharedFlow<SaveResult>()
    val saveResult: SharedFlow<SaveResult> = _saveResult

    fun loadSubscription(id: Long) {
        viewModelScope.launch {
            _loadedSubscription.value = getSubscriptionById(id)
        }
    }

    fun canSave(): Boolean = true

    fun addSubscription(subscription: Subscription) {
        if (!canSave()) {
            viewModelScope.launch { _saveResult.emit(SaveResult.LimitReached) }
            return
        }
        viewModelScope.launch {
            addSubscriptionUseCase(subscription)
            widgetRefresher.refresh()
            _saveResult.emit(SaveResult.Success)
        }
    }

    fun updateSubscription(subscription: Subscription) {
        viewModelScope.launch {
            updateSubscriptionUseCase(subscription)
            widgetRefresher.refresh()
            _saveResult.emit(SaveResult.Success)
        }
    }
}

sealed interface SaveResult {
    data object Success : SaveResult
    data object LimitReached : SaveResult
}
