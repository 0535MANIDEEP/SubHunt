package com.subhunt.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhunt.app.billing.BillingManager
import com.subhunt.app.domain.model.*
import com.subhunt.app.domain.usecase.AddSubscriptionUseCase
import com.subhunt.app.domain.usecase.DeleteSubscriptionUseCase
import com.subhunt.app.domain.usecase.GetSubscriptionsUseCase
import com.subhunt.app.widget.WidgetRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getSubscriptions: GetSubscriptionsUseCase,
    private val addSubscriptionUseCase: AddSubscriptionUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase,
    private val billingManager: BillingManager,
    private val widgetRefresher: WidgetRefresher
) : ViewModel() {

    private val subscriptionsFlow = getSubscriptions()

    val subscriptions: StateFlow<List<Subscription>> = subscriptionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    val stats: StateFlow<DashboardStats> = subscriptions
        .map {
            _isLoading.value = false
            computeDashboardStats(it)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    val healthScore: StateFlow<HealthScore?> = combine(subscriptions, stats) { subs, s ->
        if (subs.isEmpty()) null else computeHealthScore(s, subs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSubscribed: StateFlow<Boolean> = billingManager.isSubscribed

    val showPaywallDialog: StateFlow<Boolean> = MutableStateFlow(false)

    fun canAddSubscription(): Boolean = true

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            deleteSubscriptionUseCase(subscription)
            widgetRefresher.refresh()
        }
    }

    fun restoreSubscription(subscription: Subscription) {
        viewModelScope.launch {
            addSubscriptionUseCase(subscription)
            widgetRefresher.refresh()
        }
    }
}
