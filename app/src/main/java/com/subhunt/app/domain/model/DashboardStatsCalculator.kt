package com.subhunt.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun computeDashboardStats(
    subscriptions: List<Subscription>,
    today: LocalDate = LocalDate.now()
): DashboardStats {
    val upcomingBills = subscriptions
        .filter { it.nextBillingDate >= today }
        .sortedBy { it.nextBillingDate }
        .take(5)
        .map { sub ->
            UpcomingBill(
                subscription = sub,
                daysUntilCharge = ChronoUnit.DAYS.between(today, sub.nextBillingDate),
                chargeDate = sub.nextBillingDate
            )
        }

    val savingsTips = subscriptions.mapNotNull { sub ->
        val (annualAtCurrent, annualIfYearly) = when (sub.billingCycle) {
            BillingCycle.MONTHLY -> sub.cost * 12.0 to sub.cost * 10.0
            BillingCycle.QUARTERLY -> sub.cost * 4.0 to sub.cost * 3.0
            BillingCycle.WEEKLY -> sub.cost * 52.0 to sub.cost * 48.0
            BillingCycle.YEARLY -> return@mapNotNull null
        }
        val saved = annualAtCurrent - annualIfYearly
        if (saved > 0) {
            SavingsTip(
                subscriptionName = sub.name,
                currentCost = sub.cost,
                currentCycle = sub.billingCycle,
                estimatedAnnualSavings = saved,
                suggestion = "Switch to yearly billing and save ~$${"%.0f".format(saved)}/year"
            )
        } else null
    }

    return DashboardStats(
        totalMonthlyCost = subscriptions.sumOf { it.monthlyCost },
        totalYearlyCost = subscriptions.sumOf { it.yearlyCost },
        activeCount = subscriptions.size,
        dueSoonCount = subscriptions.count { it.isDueSoon },
        overdueCount = subscriptions.count { it.isOverdue },
        categoryBreakdown = subscriptions.groupBy { it.category }
            .mapValues { (_, subs) -> subs.sumOf { it.monthlyCost } },
        upcomingBills = upcomingBills,
        savingsTips = savingsTips
    )
}
