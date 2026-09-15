package com.subhunt.app.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhunt.app.billing.BillingManager
import com.subhunt.app.domain.model.*
import com.subhunt.app.domain.usecase.GetSubscriptionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    getSubscriptions: GetSubscriptionsUseCase,
    val billingManager: BillingManager
) : ViewModel() {

    val subscriptions: StateFlow<List<Subscription>> = getSubscriptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<DashboardStats> = subscriptions
        .map { computeDashboardStats(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats.EMPTY)

    val healthScore: StateFlow<HealthScore?> = combine(subscriptions, stats) { subs, s ->
        if (subs.isEmpty()) null else computeHealthScore(s, subs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val insights: StateFlow<SpendingInsights> = subscriptions
        .map { computeSpendingInsights(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingInsights(0.0, 0.0, null, emptyList(), emptyList(), emptyList(), 0, 0.0))
}
