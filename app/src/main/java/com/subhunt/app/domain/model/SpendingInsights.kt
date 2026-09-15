package com.subhunt.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SpendingInsights(
    val monthlyAverage: Double,
    val yearlyProjection: Double,
    val highestCategory: CategorySpending?,
    val categoryBreakdown: List<CategorySpending>,
    val savingsOpportunities: List<SavingsOpportunity>,
    val monthlyTrend: List<MonthlyData>,
    val streakDays: Int,
    val totalSaved: Double
)

data class CategorySpending(
    val category: SubscriptionCategory,
    val monthlyCost: Double,
    val percentage: Double,
    val subscriptionCount: Int
)

data class SavingsOpportunity(
    val title: String,
    val description: String,
    val potentialSavings: Double,
    val type: OpportunityType,
    val affectedSubscriptions: List<String>
)

enum class OpportunityType {
    SWITCH_TO_YEARLY,
    DUPLICATE_SERVICE,
    UNDERUSED_SERVICE,
    CHEAPER_ALTERNATIVE,
    TRIAL_EXPIRING
}

data class MonthlyData(
    val month: String,
    val cost: Double,
    val isCurrent: Boolean = false
)

fun computeSpendingInsights(subscriptions: List<Subscription>): SpendingInsights {
    if (subscriptions.isEmpty()) {
        return SpendingInsights(
            monthlyAverage = 0.0,
            yearlyProjection = 0.0,
            highestCategory = null,
            categoryBreakdown = emptyList(),
            savingsOpportunities = emptyList(),
            monthlyTrend = emptyList(),
            streakDays = 0,
            totalSaved = 0.0
        )
    }

    val totalMonthly = subscriptions.sumOf { it.monthlyCost }
    val totalYearly = totalMonthly * 12

    // Category breakdown
    val categoryMap = mutableMapOf<SubscriptionCategory, MutableList<Subscription>>()
    subscriptions.forEach { sub ->
        categoryMap.getOrPut(sub.category) { mutableListOf() }.add(sub)
    }
    val categoryBreakdown = categoryMap.map { (cat, subs) ->
        val cost = subs.sumOf { it.monthlyCost }
        CategorySpending(
            category = cat,
            monthlyCost = cost,
            percentage = if (totalMonthly > 0) (cost / totalMonthly) * 100 else 0.0,
            subscriptionCount = subs.size
        )
    }.sortedByDescending { it.monthlyCost }

    val highestCategory = categoryBreakdown.maxByOrNull { it.monthlyCost }

    // Savings opportunities
    val opportunities = mutableListOf<SavingsOpportunity>()

    // Check for monthly subscriptions that could be yearly
    val monthlySubs = subscriptions.filter { it.billingCycle == BillingCycle.MONTHLY }
    if (monthlySubs.isNotEmpty()) {
        val yearlySavings = monthlySubs.sumOf { it.cost * 12 * 0.15 }
        opportunities.add(SavingsOpportunity(
            title = "Switch to yearly billing",
            description = "${monthlySubs.size} subscription(s) on monthly billing. Yearly plans typically save 15-20%.",
            potentialSavings = yearlySavings,
            type = OpportunityType.SWITCH_TO_YEARLY,
            affectedSubscriptions = monthlySubs.map { it.name }
        ))
    }

    // Check for potential duplicates in same category
    categoryMap.forEach { (cat, subs) ->
        if (subs.size > 1 && cat != SubscriptionCategory.OTHER) {
            val totalCatCost = subs.sumOf { it.monthlyCost }
            opportunities.add(SavingsOpportunity(
                title = "Potential duplicate: ${cat.label}",
                description = "You have ${subs.size} ${cat.label.lowercase()} subscriptions. Consider if you need all of them.",
                potentialSavings = totalCatCost * 0.5,
                type = OpportunityType.DUPLICATE_SERVICE,
                affectedSubscriptions = subs.map { it.name }
            ))
        }
    }

    // Check for old subscriptions (started > 6 months ago, likely underused)
    val oldSubs = subscriptions.filter {
        ChronoUnit.MONTHS.between(it.startDate, LocalDate.now()) > 6
    }
    if (oldSubs.isNotEmpty() && oldSubs.size > subscriptions.size * 0.5) {
        opportunities.add(SavingsOpportunity(
            title = "Review long-standing subscriptions",
            description = "${oldSubs.size} subscription(s) active for 6+ months. Ensure they still provide value.",
            potentialSavings = oldSubs.take(2).sumOf { it.monthlyCost } * 12,
            type = OpportunityType.UNDERUSED_SERVICE,
            affectedSubscriptions = oldSubs.take(3).map { it.name }
        ))
    }

    // Monthly trend (last 6 months estimate)
    val today = LocalDate.now()
    val monthlyTrend = (5 downTo 0).map { monthsAgo ->
        val month = today.minusMonths(monthsAgo.toLong())
        MonthlyData(
            month = month.month.name.take(3),
            cost = totalMonthly,
            isCurrent = monthsAgo == 0
        )
    }

    return SpendingInsights(
        monthlyAverage = totalMonthly,
        yearlyProjection = totalYearly,
        highestCategory = highestCategory,
        categoryBreakdown = categoryBreakdown,
        savingsOpportunities = opportunities,
        monthlyTrend = monthlyTrend,
        streakDays = 0,
        totalSaved = 0.0
    )
}
