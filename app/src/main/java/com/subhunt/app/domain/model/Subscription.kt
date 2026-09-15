package com.subhunt.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Subscription(
    val id: Long = 0,
    val name: String,
    val cost: Double,
    val billingCycle: BillingCycle,
    val category: SubscriptionCategory,
    val startDate: LocalDate,
    val nextBillingDate: LocalDate,
    val color: Long = 0xFF1A1A2E,
    val notes: String = "",
    val isActive: Boolean = true,
    val reminderDaysBefore: Int = 3
) {
    val monthlyCost: Double
        get() = when (billingCycle) {
            BillingCycle.WEEKLY -> cost * 4.33
            BillingCycle.MONTHLY -> cost
            BillingCycle.QUARTERLY -> cost / 3.0
            BillingCycle.YEARLY -> cost / 12.0
        }

    val yearlyCost: Double
        get() = monthlyCost * 12.0

    val daysUntilBilling: Long
        get() = ChronoUnit.DAYS.between(LocalDate.now(), nextBillingDate)

    val isDueSoon: Boolean
        get() = daysUntilBilling in 0..reminderDaysBefore.toLong()

    val isOverdue: Boolean
        get() = daysUntilBilling < 0
}

enum class BillingCycle(val label: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    YEARLY("Yearly")
}

enum class SubscriptionCategory(val label: String, val icon: ImageVector) {
    ENTERTAINMENT("Entertainment", Icons.Rounded.PlayCircle),
    PRODUCTIVITY("Productivity", Icons.Rounded.Bolt),
    FINANCE("Finance", Icons.Rounded.AccountBalance),
    HEALTH("Health", Icons.Rounded.Favorite),
    EDUCATION("Education", Icons.Rounded.School),
    SOCIAL("Social", Icons.AutoMirrored.Rounded.Chat),
    CLOUD("Cloud", Icons.Rounded.Cloud),
    GAMING("Gaming", Icons.Rounded.SportsEsports),
    MUSIC("Music", Icons.Rounded.MusicNote),
    NEWS("News", Icons.AutoMirrored.Rounded.Article),
    UTILITIES("Utilities", Icons.Rounded.Build),
    OTHER("Other", Icons.Rounded.MoreHoriz)
}
