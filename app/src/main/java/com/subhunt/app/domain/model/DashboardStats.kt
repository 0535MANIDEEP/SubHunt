package com.subhunt.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DashboardStats(
    val totalMonthlyCost: Double = 0.0,
    val totalYearlyCost: Double = 0.0,
    val activeCount: Int = 0,
    val dueSoonCount: Int = 0,
    val overdueCount: Int = 0,
    val categoryBreakdown: Map<SubscriptionCategory, Double> = emptyMap(),
    val upcomingBills: List<UpcomingBill> = emptyList(),
    val savingsTips: List<SavingsTip> = emptyList()
) {
    val potentialYearlySavings: Double
        get() = savingsTips.sumOf { it.estimatedAnnualSavings }

    companion object {
        val EMPTY = DashboardStats()
    }
}

data class UpcomingBill(
    val subscription: Subscription,
    val daysUntilCharge: Long,
    val chargeDate: LocalDate
)

data class SavingsTip(
    val subscriptionName: String,
    val currentCost: Double,
    val currentCycle: BillingCycle,
    val estimatedAnnualSavings: Double,
    val suggestion: String
)

const val FREE_SUBSCRIPTION_LIMIT = 3
