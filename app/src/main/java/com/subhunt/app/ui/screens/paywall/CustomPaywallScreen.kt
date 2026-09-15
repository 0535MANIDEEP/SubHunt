package com.subhunt.app.ui.screens.paywall

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val premiumGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF16213E),
        Color(0xFF0F3460)
    )
)

private val goldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
)

// RevenueCat web billing URL — replace with your actual RevenueCat web link
// Create at: RevenueCat → Project Settings → Paywalls → Web Paywall
private const val WEB_PAYWALL_URL = "https://purchase.revenuecat.com/YOUR_WEB_PAYWALL_ID"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPaywallScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isSubscribed by viewModel.isSubscribed.collectAsStateWithLifecycle()
    var selectedPlan by remember { mutableStateOf(Plan.YEARLY) }

    LaunchedEffect(isSubscribed) {
        if (isSubscribed) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(premiumGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Premium badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(goldGradient)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        "SUBHUNT PRO",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Unlock Your\nSubscription Power",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Join 10,000+ users who save hundreds\non subscriptions every year",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Benefits list
                val benefits = listOf(
                    IconPair(Icons.Rounded.AllInclusive, "Unlimited Subscriptions", "Track every subscription, no limits"),
                    IconPair(Icons.Rounded.Insights, "Smart Insights", "Health score, spending analytics, trends"),
                    IconPair(Icons.Rounded.FileDownload, "Export Data", "CSV & JSON backups of your data"),
                    IconPair(Icons.Rounded.MusicNote, "Premium Sounds", "Exclusive reminder tones + per-subscription sounds"),
                    IconPair(Icons.Rounded.Notifications, "Bill Reminders", "Never miss a payment with smart alerts"),
                    IconPair(Icons.Rounded.TrendingUp, "Savings Tracker", "See how much you save over time")
                )

                benefits.forEach { benefit ->
                    BenefitRow(benefit)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Social proof
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "4.8 rating · 2,500+ reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Plan selector
                PlanSelector(selectedPlan) { selectedPlan = it }

                Spacer(modifier = Modifier.height(24.dp))

                // CTA Button — opens web payment (no Play Store needed)
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WEB_PAYWALL_URL))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF1A1A2E)
                    )
                ) {
                    Text(
                        when (selectedPlan) {
                            Plan.MONTHLY -> "Subscribe $4.99/mo"
                            Plan.YEARLY -> "Subscribe $29.99/yr"
                            Plan.LIFETIME -> "Get Lifetime $79.99"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Cancel anytime · No questions asked",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Savings callout
                if (selectedPlan == Plan.YEARLY) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Text(
                            "Save $29.89/year compared to monthly billing",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF81C784)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Legal
                Text(
                    "By subscribing, you agree to our Terms of Service and Privacy Policy. Subscription automatically renews unless cancelled at least 24 hours before the end of the current period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
    }
}

private data class IconPair(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String
)

@Composable
private fun BenefitRow(benefit: IconPair) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                benefit.icon,
                contentDescription = null,
                tint = Color(0xFF81C784),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                benefit.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                benefit.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

enum class Plan { MONTHLY, YEARLY, LIFETIME }

@Composable
private fun PlanSelector(selected: Plan, onSelect: (Plan) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Yearly (recommended)
        PlanCard(
            title = "Yearly",
            price = "$2.49",
            period = "/month",
            subtitle = "Billed annually at $29.99",
            badge = "SAVE 50%",
            isSelected = selected == Plan.YEARLY,
            onClick = { onSelect(Plan.YEARLY) }
        )

        // Monthly
        PlanCard(
            title = "Monthly",
            price = "$4.99",
            period = "/month",
            subtitle = "Billed monthly",
            badge = null,
            isSelected = selected == Plan.MONTHLY,
            onClick = { onSelect(Plan.MONTHLY) }
        )

        // Lifetime
        PlanCard(
            title = "Lifetime",
            price = "$79.99",
            period = "",
            subtitle = "One-time purchase · Forever yours",
            badge = "BEST VALUE",
            isSelected = selected == Plan.LIFETIME,
            onClick = { onSelect(Plan.LIFETIME) }
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    period: String,
    subtitle: String,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                } else {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                }
            )
            .background(
                if (isSelected) Color(0xFFFFD700).copy(alpha = 0.1f)
                else Color.White.copy(alpha = 0.05f)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (badge == "BEST VALUE") Color(0xFFFFD700)
                                    else Color(0xFF4CAF50)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (badge == "BEST VALUE") Color(0xFF1A1A2E) else Color.White
                            )
                        }
                    }
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        price,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (period.isNotEmpty()) {
                        Text(
                            period,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
