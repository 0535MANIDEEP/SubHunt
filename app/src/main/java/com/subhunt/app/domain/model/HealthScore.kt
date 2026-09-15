package com.subhunt.app.domain.model

data class HealthScore(
    val score: Int,
    val grade: HealthGrade,
    val title: String,
    val message: String,
    val breakdown: List<HealthFactor>
)

data class HealthFactor(
    val label: String,
    val score: Int,
    val maxScore: Int,
    val impact: FactorImpact,
    val tip: String
)

enum class HealthGrade(val label: String, val emoji: String) {
    A("Excellent", "\uD83C\uDF1F"),
    B("Good", "\u2705"),
    C("Fair", "\u26A0\uFE0F"),
    D("Needs Work", "\uD83D\uDCC9"),
    F("Critical", "\uD83D\uDEA8")
}

enum class FactorImpact {
    POSITIVE,
    NEUTRAL,
    NEGATIVE
}

fun computeHealthScore(stats: DashboardStats, subscriptions: List<Subscription>): HealthScore {
    val factors = mutableListOf<HealthFactor>()
    var totalScore = 0
    var maxTotal = 0

    // Factor 1: Monthly spending relative to budget (0-25 pts)
    val budgetScore = when {
        stats.totalMonthlyCost <= 20 -> 25
        stats.totalMonthlyCost <= 50 -> 20
        stats.totalMonthlyCost <= 100 -> 15
        stats.totalMonthlyCost <= 200 -> 10
        else -> 5
    }
    factors.add(HealthFactor(
        label = "Monthly Spending",
        score = budgetScore,
        maxScore = 25,
        impact = if (budgetScore >= 20) FactorImpact.POSITIVE else if (budgetScore >= 15) FactorImpact.NEUTRAL else FactorImpact.NEGATIVE,
        tip = when {
            stats.totalMonthlyCost <= 20 -> "Your spending is very controlled!"
            stats.totalMonthlyCost <= 50 -> "Moderate spending. Consider reviewing add-ons."
            stats.totalMonthlyCost <= 100 -> "Consider canceling unused subscriptions."
            else -> "High spending. Audit each subscription for value."
        }
    ))
    totalScore += budgetScore
    maxTotal += 25

    // Factor 2: Savings opportunities (0-20 pts)
    val yearlySubCount = subscriptions.count { it.billingCycle == BillingCycle.YEARLY }
    val monthlySubCount = subscriptions.count { it.billingCycle == BillingCycle.MONTHLY }
    val savingsScore = if (monthlySubCount == 0) 20
    else if (yearlySubCount >= monthlySubCount) 15
    else if (yearlySubCount > 0) 10
    else 5
    factors.add(HealthFactor(
        label = "Billing Optimization",
        score = savingsScore,
        maxScore = 20,
        impact = if (savingsScore >= 15) FactorImpact.POSITIVE else if (savingsScore >= 10) FactorImpact.NEUTRAL else FactorImpact.NEGATIVE,
        tip = if (monthlySubCount > 0) "Switch $monthlySubCount monthly sub(s) to yearly to save ~${stats.potentialYearlySavings.toInt()}$/yr" else "Great! All subscriptions are on annual billing."
    ))
    totalScore += savingsScore
    maxTotal += 20

    // Factor 3: Overdue bills (0-20 pts)
    val overdueScore = when {
        stats.overdueCount == 0 -> 20
        stats.overdueCount == 1 -> 12
        stats.overdueCount <= 3 -> 5
        else -> 0
    }
    factors.add(HealthFactor(
        label = "Payment Punctuality",
        score = overdueScore,
        maxScore = 20,
        impact = if (overdueScore >= 15) FactorImpact.POSITIVE else if (overdueScore >= 10) FactorImpact.NEUTRAL else FactorImpact.NEGATIVE,
        tip = if (stats.overdueCount == 0) "All bills are on time!" else "${stats.overdueCount} bill(s) overdue. Enable reminders to avoid late fees."
    ))
    totalScore += overdueScore
    maxTotal += 20

    // Factor 4: Category diversity (0-15 pts)
    val categoryCount = stats.categoryBreakdown.size
    val diversityScore = when {
        categoryCount <= 2 -> 15
        categoryCount <= 4 -> 12
        categoryCount <= 6 -> 8
        else -> 4
    }
    factors.add(HealthFactor(
        label = "Focused Spending",
        score = diversityScore,
        maxScore = 15,
        impact = if (diversityScore >= 12) FactorImpact.POSITIVE else if (diversityScore >= 8) FactorImpact.NEUTRAL else FactorImpact.NEGATIVE,
        tip = if (categoryCount <= 2) "Your spending is focused on essentials." else "Spreading across $categoryCount categories. Consider consolidation."
    ))
    totalScore += diversityScore
    maxTotal += 15

    // Factor 5: Active subscription count (0-20 pts)
    val countScore = when {
        stats.activeCount <= 3 -> 20
        stats.activeCount <= 5 -> 16
        stats.activeCount <= 8 -> 12
        stats.activeCount <= 12 -> 8
        else -> 4
    }
    factors.add(HealthFactor(
        label = "Subscription Count",
        score = countScore,
        maxScore = 20,
        impact = if (countScore >= 16) FactorImpact.POSITIVE else if (countScore >= 12) FactorImpact.NEUTRAL else FactorImpact.NEGATIVE,
        tip = when {
            stats.activeCount <= 3 -> "Lean and efficient!"
            stats.activeCount <= 5 -> "Manageable number of subscriptions."
            stats.activeCount <= 8 -> "Consider if all are actively used."
            else -> "High count. Audit for duplicates and unused services."
        }
    ))
    totalScore += countScore
    maxTotal += 20

    val percentage = (totalScore * 100) / maxTotal
    val grade = when {
        percentage >= 90 -> HealthGrade.A
        percentage >= 75 -> HealthGrade.B
        percentage >= 55 -> HealthGrade.C
        percentage >= 35 -> HealthGrade.D
        else -> HealthGrade.F
    }

    val title = when (grade) {
        HealthGrade.A -> "Your subscriptions are in great shape!"
        HealthGrade.B -> "Looking good! A few tweaks could help."
        HealthGrade.C -> "Some opportunities to optimize."
        HealthGrade.D -> "Time for a subscription audit."
        HealthGrade.F -> "Your subscriptions need attention."
    }

    val message = "Score: $totalScore/$maxTotal (${grade.label})"

    return HealthScore(
        score = percentage,
        grade = grade,
        title = title,
        message = message,
        breakdown = factors
    )
}
