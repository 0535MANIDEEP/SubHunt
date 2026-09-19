package com.subhunt.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subhunt.app.domain.model.*
import com.subhunt.app.ui.util.HapticFeedback
import com.subhunt.app.ui.util.CelebrationOverlay
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private val cardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
)

private val savingsGreen = Color(0xFF4CAF50)
private val warningOrange = Color(0xFFFF9800)
private val categoryColors = listOf(
    Color(0xFF6366F1), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFFEF4444),
    Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFFF97316)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddClick: () -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    onPaywallClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onInsightsClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val healthScore by viewModel.healthScore.collectAsStateWithLifecycle()
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<Subscription?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("SubHunt", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                },
                actions = {
                    IconButton(onClick = { onInsightsClick() }) {
                        Icon(Icons.Rounded.Insights, contentDescription = "Insights")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add subscription")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
        ) {
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
            item { StatsCard(stats) }

            healthScore?.let { score ->
                item { HealthScorePreview(score, onInsightsClick) }
            }

            if (stats.dueSoonCount > 0 || stats.overdueCount > 0) {
                item { WarningBanner(stats.dueSoonCount, stats.overdueCount) }
            }

            if (stats.savingsTips.isNotEmpty()) {
                item {
                    SectionHeader("Savings Tips", Icons.Rounded.Lightbulb, savingsGreen)
                }
                items(stats.savingsTips) { tip ->
                    SavingsTipCard(tip)
                }
            }

            if (stats.upcomingBills.isNotEmpty()) {
                item {
                    SectionHeader("Upcoming Bills", Icons.Rounded.CalendarToday, MaterialTheme.colorScheme.primary)
                }
                items(stats.upcomingBills) { bill ->
                    UpcomingBillRow(bill)
                }
            }

            if (stats.categoryBreakdown.size > 1) {
                item {
                    SectionHeader("Spending by Category", Icons.Rounded.PieChart, MaterialTheme.colorScheme.primary)
                }
                item {
                    CategoryBreakdownCard(stats.categoryBreakdown, stats.totalMonthlyCost)
                }
            }

            if (subscriptions.isEmpty()) {
                item { EmptyState(onAddClick) }
            }

            if (subscriptions.isNotEmpty()) {
                item {
                    SectionHeader("All Subscriptions", Icons.Rounded.Subscriptions, MaterialTheme.colorScheme.primary)
                }
            }

            items(subscriptions, key = { it.id }) { subscription ->
                SubscriptionRow(
                    subscription = subscription,
                    onClick = { onSubscriptionClick(subscription.id) },
                    onDelete = { showDeleteDialog = subscription }
                )
            }

            } // end else (!isLoading)
        }
    }

    showDeleteDialog?.let { subscription ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove ${subscription.name}?") },
            text = { Text("This will permanently remove this subscription from your tracker.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubscription(subscription)
                        showDeleteDialog = null
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "${subscription.name} removed",
                                actionLabel = "Undo",
                                withDismissAction = true
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreSubscription(subscription)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatsCard(stats: DashboardStats) {
    val animatedMonthlyCost by animateFloatAsState(
        targetValue = stats.totalMonthlyCost.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "monthly_cost"
    )
    val animatedYearlyCost by animateFloatAsState(
        targetValue = stats.totalYearlyCost.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "yearly_cost"
    )
    val animatedSavings by animateFloatAsState(
        targetValue = stats.potentialYearlySavings.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "savings"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF667EEA),
                            Color(0xFF764BA2)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text("Monthly Spend", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$${String.format("%.2f", animatedMonthlyCost)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatColumn("Yearly", "$${String.format("%.0f", animatedYearlyCost)}")
                    StatColumn("Active", "${stats.activeCount}")
                    StatColumn("Save", "$${String.format("%.0f", animatedSavings)}/yr", Color(0xFF81C784))
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, valueColor: Color = Color.White) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun HealthScorePreview(score: HealthScore, onClick: () -> Unit) {
    val gradeColor = when (score.grade) {
        HealthGrade.A -> Color(0xFF4CAF50)
        HealthGrade.B -> Color(0xFF8BC34A)
        HealthGrade.C -> Color(0xFFFFC107)
        HealthGrade.D -> Color(0xFFFF9800)
        HealthGrade.F -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(gradeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${score.score}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = gradeColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Subscription Health",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    score.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WarningBanner(dueSoonCount: Int, overdueCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Warning, contentDescription = null, tint = warningOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                if (overdueCount > 0) {
                    Text("$overdueCount overdue", fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100), style = MaterialTheme.typography.bodyMedium)
                }
                if (dueSoonCount > 0) {
                    Text("$dueSoonCount due soon", color = Color(0xFFE65100), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SavingsTipCard(tip: SavingsTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Savings, contentDescription = null, tint = savingsGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tip.subscriptionName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(tip.suggestion, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
            }
            Text(
                "$${String.format("%.0f", tip.estimatedAnnualSavings)}",
                fontWeight = FontWeight.Bold,
                color = savingsGreen,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun UpcomingBillRow(bill: UpcomingBill) {
    val dateStr = bill.chargeDate.format(DateTimeFormatter.ofPattern("MMM d"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(bill.subscription.color).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    bill.subscription.category.icon,
                    contentDescription = null,
                    tint = Color(bill.subscription.color),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bill.subscription.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    when (bill.daysUntilCharge) {
                        0L -> "Charges today"
                        1L -> "Charges tomorrow"
                        else -> "Charges in ${bill.daysUntilCharge} days"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        bill.daysUntilCharge <= 1 -> warningOrange
                        bill.daysUntilCharge <= 3 -> Color(0xFFE65100)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${String.format("%.2f", bill.subscription.cost)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(breakdown: Map<SubscriptionCategory, Double>, totalMonthly: Double) {
    val sorted = breakdown.entries.sortedByDescending { it.value }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            sorted.forEachIndexed { index, (category, cost) ->
                val percentage = if (totalMonthly > 0) (cost / totalMonthly) else 0.0
                val barColor = categoryColors[index % categoryColors.size]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        tint = barColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        category.label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(90.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = percentage.toFloat())
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$${String.format("%.0f", cost)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionRow(
    subscription: Subscription,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(subscription.color).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(subscription.category.icon, contentDescription = subscription.category.label, tint = Color(subscription.color), modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(subscription.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(subscription.billingCycle.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${String.format("%.2f", subscription.cost)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                val statusText = when {
                    subscription.isOverdue -> "Overdue"
                    subscription.daysUntilBilling == 0L -> "Today"
                    else -> "In ${subscription.daysUntilBilling}d"
                }
                val statusColor = when {
                    subscription.isOverdue -> MaterialTheme.colorScheme.error
                    subscription.isDueSoon -> warningOrange
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                Icons.Rounded.Subscriptions,
                contentDescription = null,
                modifier = Modifier.padding(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("No subscriptions yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Tap + to add your first subscription and start tracking",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
