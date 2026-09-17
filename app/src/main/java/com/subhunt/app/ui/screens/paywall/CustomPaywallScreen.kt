package com.subhunt.app.ui.screens.paywall

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subhunt.app.ui.theme.*

enum class Plan { MONTHLY, YEARLY, LIFETIME }

enum class PaywallStep { PLAN_SELECT, CONFIRM, ACTIVATE_CODE }

// Anime-style gradient backgrounds
private val animeBgGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0A0A14),
        Color(0xFF12121E),
        Color(0xFF1A0A2E),
        Color(0xFF0A0A14)
    )
)

private val animeGlowGradient = Brush.radialGradient(
    colors = listOf(
        AnimeNeonPurple.copy(alpha = 0.3f),
        AnimeNeonPink.copy(alpha = 0.1f),
        Color.Transparent
    )
)

private val animeGoldGlow = Brush.radialGradient(
    colors = listOf(
        AnimePaywallHighlight.copy(alpha = 0.2f),
        Color.Transparent
    )
)

private val animePremiumGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF6C3CE1),
        Color(0xFFFF6B9D),
        Color(0xFFFFD700)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPaywallScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isSubscribed by viewModel.isSubscribed.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var selectedPlan by remember { mutableStateOf(Plan.YEARLY) }
    var currentStep by remember { mutableStateOf(PaywallStep.PLAN_SELECT) }
    var activationCode by remember { mutableStateOf("") }
    var activationEmail by remember { mutableStateOf("") }
    var isActivating by remember { mutableStateOf(false) }

    LaunchedEffect(isSubscribed) {
        if (isSubscribed) onNavigateBack()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
            isActivating = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Anime background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(animeBgGradient)
        )

        // Glow effect at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(animeGlowGradient)
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = {
                            when (currentStep) {
                                PaywallStep.CONFIRM -> currentStep = PaywallStep.PLAN_SELECT
                                PaywallStep.ACTIVATE_CODE -> currentStep = PaywallStep.CONFIRM
                                PaywallStep.PLAN_SELECT -> onNavigateBack()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = AnimeTextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            when (currentStep) {
                PaywallStep.PLAN_SELECT -> AnimePlanSelection(
                    padding = padding,
                    selectedPlan = selectedPlan,
                    onPlanSelected = { selectedPlan = it },
                    onContinue = { currentStep = PaywallStep.CONFIRM }
                )
                PaywallStep.CONFIRM -> AnimeConfirmPayment(
                    padding = padding,
                    selectedPlan = selectedPlan,
                    onPay = { currentStep = PaywallStep.ACTIVATE_CODE },
                    context = context
                )
                PaywallStep.ACTIVATE_CODE -> AnimeActivationCode(
                    padding = padding,
                    code = activationCode,
                    email = activationEmail,
                    onCodeChange = { activationCode = it },
                    onEmailChange = { activationEmail = it },
                    isActivating = isActivating,
                    onActivate = {
                        isActivating = true
                        viewModel.activate(activationCode, activationEmail)
                    }
                )
            }
        }
    }
}

// ============================================================
// TODO: Replace with YOUR Stripe Payment Links
// Create free account at: https://dashboard.stripe.com
// Then: Payment Links → Create payment link
// ============================================================
private const val STRIPE_MONTHLY_URL = "https://buy.stripe.com/YOUR_MONTHLY_LINK"
private const val STRIPE_YEARLY_URL = "https://buy.stripe.com/YOUR_YEARLY_LINK"
private const val STRIPE_LIFETIME_URL = "https://buy.stripe.com/YOUR_LIFETIME_LINK"

private fun getStripeUrl(plan: Plan): String = when (plan) {
    Plan.MONTHLY -> STRIPE_MONTHLY_URL
    Plan.YEARLY -> STRIPE_YEARLY_URL
    Plan.LIFETIME -> STRIPE_LIFETIME_URL
}

private fun getAmount(plan: Plan): String = when (plan) {
    Plan.MONTHLY -> "₹419"
    Plan.YEARLY -> "₹2,499"
    Plan.LIFETIME -> "₹6,649"
}

private fun getPlanName(plan: Plan): String = when (plan) {
    Plan.MONTHLY -> "Monthly"
    Plan.YEARLY -> "Yearly"
    Plan.LIFETIME -> "Lifetime"
}

private fun getMonthlyEquiv(plan: Plan): String = when (plan) {
    Plan.MONTHLY -> "₹419/month"
    Plan.YEARLY -> "Just ₹208/month"
    Plan.LIFETIME -> "One-time forever"
}

@Composable
private fun AnimePlanSelection(
    padding: PaddingValues,
    selectedPlan: Plan,
    onPlanSelected: (Plan) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Anime badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(animePremiumGradient)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                "★ SUBHUNT PRO ★",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main title — dramatic anime style
        Text(
            "Unlock Your\nFull Power",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = AnimeTextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Glowing subtitle
        Text(
          "Master your subscriptions\nlike an anime protagonist",
            style = MaterialTheme.typography.bodyLarge,
            color = AnimeNeonPurple,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Benefits — anime style cards
        val benefits = listOf(
            Triple(Icons.Rounded.AllInclusive, "Unlimited Subs", "No limits — track everything"),
            Triple(Icons.Rounded.Insights, "Power Analytics", "Health scores & trends"),
            Triple(Icons.Rounded.FileDownload, "Data Export", "CSV & JSON backups"),
            Triple(Icons.Rounded.MusicNote, "Battle Sounds", "Premium reminder tones"),
            Triple(Icons.Rounded.TrendingUp, "Savings Mode", "Watch your savings grow")
        )

        benefits.forEach { (icon, title, desc) ->
            AnimeBenefitCard(icon, title, desc)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Plan selector
        PlanSelector(selectedPlan) { onPlanSelected(it) }

        Spacer(modifier = Modifier.height(24.dp))

        // CTA Button with glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(16.dp), ambientColor = AnimeNeonPurple.copy(alpha = 0.5f))
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AnimePaywallHighlight,
                    contentColor = Color(0xFF0A0A14)
                )
            ) {
                Text(
                    "POWER UP →",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Cancel anytime · No commitment",
            style = MaterialTheme.typography.bodySmall,
            color = AnimeTextMuted,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun AnimeBenefitCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AnimeSurfaceLight.copy(alpha = 0.5f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AnimeNeonPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AnimeNeonPurple, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AnimeTextPrimary)
            Text(description, style = MaterialTheme.typography.bodySmall, color = AnimeTextSecondary)
        }
    }
}

@Composable
private fun AnimeConfirmPayment(
    padding: PaddingValues,
    selectedPlan: Plan,
    onPay: () -> Unit,
    context: Context
) {
    val amount = getAmount(selectedPlan)
    val planName = getPlanName(selectedPlan)
    val monthly = getMonthlyEquiv(selectedPlan)
    val url = getStripeUrl(selectedPlan)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Glowing icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(24.dp, CircleShape, ambientColor = AnimeNeonPurple.copy(alpha = 0.6f))
                .clip(CircleShape)
                .background(AnimePaywallCard)
                .border(2.dp, AnimeNeonPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = AnimePaywallHighlight, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Final Form",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = AnimeTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Choose your power level",
            style = MaterialTheme.typography.bodyLarge,
            color = AnimeTextSecondary
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Plan summary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AnimePaywallCard)
                .border(1.dp, AnimeNeonPurple.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Plan", style = MaterialTheme.typography.bodyMedium, color = AnimeTextSecondary)
                    Text(
                        planName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AnimeNeonPurple
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = AnimeTextMuted.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Total", style = MaterialTheme.typography.bodyMedium, color = AnimeTextSecondary)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            amount,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = AnimePaywallHighlight
                        )
                        Text(monthly, style = MaterialTheme.typography.bodySmall, color = AnimeTextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // What's included
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AnimeNeonGreen.copy(alpha = 0.08f))
                .border(1.dp, AnimeNeonGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "⚡ UNLOCKED FEATURES",
                    style = MaterialTheme.typography.labelMedium,
                    color = AnimeNeonGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                listOf(
                    "Unlimited subscriptions",
                    "Smart insights & analytics",
                    "CSV & JSON data export",
                    "Premium battle sounds",
                    "Savings tracker"
                ).forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✦", color = AnimeNeonGreen, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item, style = MaterialTheme.typography.bodySmall, color = AnimeTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Pay button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(16.dp), ambientColor = AnimePaywallHighlight.copy(alpha = 0.4f))
        ) {
            Button(
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        onPay()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Payment not configured. Contact support@subhunt.app", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AnimePaywallHighlight,
                    contentColor = Color(0xFF0A0A14)
                )
            ) {
                Text(
                    "PAY $amount →",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Shield, contentDescription = null, tint = AnimeTextMuted, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Secured by Stripe · 256-bit encryption",
                style = MaterialTheme.typography.bodySmall,
                color = AnimeTextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Check email for activation code after payment",
            style = MaterialTheme.typography.bodySmall,
            color = AnimeTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun AnimeActivationCode(
    padding: PaddingValues,
    code: String,
    email: String,
    onCodeChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    isActivating: Boolean,
    onActivate: () -> Unit
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

        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(24.dp, CircleShape, ambientColor = AnimeNeonGreen.copy(alpha = 0.6f))
                .clip(CircleShape)
                .background(AnimePaywallCard)
                .border(2.dp, AnimeNeonGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = AnimeNeonGreen, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Payment Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = AnimeTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Enter your activation code to unlock power",
            style = MaterialTheme.typography.bodyLarge,
            color = AnimeTextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email used for payment", color = AnimeTextSecondary) },
            placeholder = { Text("you@example.com", color = AnimeTextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AnimeNeonPurple,
                unfocusedBorderColor = AnimeTextMuted.copy(alpha = 0.3f),
                focusedTextColor = AnimeTextPrimary,
                unfocusedTextColor = AnimeTextPrimary,
                cursorColor = AnimeNeonPurple
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { onCodeChange(it.uppercase().take(12)) },
            label = { Text("Activation Code", color = AnimeTextSecondary) },
            placeholder = { Text("XXXXXXXXXXXX", color = AnimeTextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AnimePaywallHighlight,
                unfocusedBorderColor = AnimeTextMuted.copy(alpha = 0.3f),
                focusedTextColor = AnimeTextPrimary,
                unfocusedTextColor = AnimeTextPrimary,
                cursorColor = AnimePaywallHighlight
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "M = Monthly · Y = Yearly · L = Lifetime",
            style = MaterialTheme.typography.bodySmall,
            color = AnimeTextMuted
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(16.dp), ambientColor = AnimeNeonGreen.copy(alpha = 0.4f))
        ) {
            Button(
                onClick = onActivate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AnimeNeonGreen,
                    contentColor = Color(0xFF0A0A14)
                ),
                enabled = code.length == 12 && email.contains("@") && !isActivating
            ) {
                if (isActivating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF0A0A14),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "ACTIVATE →",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "One-time use · Tied to your email",
            style = MaterialTheme.typography.bodySmall,
            color = AnimeTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun PlanSelector(selected: Plan, onSelect: (Plan) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimePlanCard(
            title = "Yearly",
            price = "₹2,499",
            period = "/year",
            subtitle = "Just ₹208/month · Save 40%",
            badge = "BEST VALUE",
            badgeColor = AnimePaywallHighlight,
            isSelected = selected == Plan.YEARLY
        ) { onSelect(Plan.YEARLY) }

        AnimePlanCard(
            title = "Monthly",
            price = "₹419",
            period = "/month",
            subtitle = "Billed monthly",
            badge = null,
            badgeColor = Color.Transparent,
            isSelected = selected == Plan.MONTHLY
        ) { onSelect(Plan.MONTHLY) }

        AnimePlanCard(
            title = "Lifetime",
            price = "₹6,649",
            period = "",
            subtitle = "One-time · Forever yours",
            badge = "BEST DEAL",
            badgeColor = AnimeNeonGreen,
            isSelected = selected == Plan.LIFETIME
        ) { onSelect(Plan.LIFETIME) }
    }
}

@Composable
private fun AnimePlanCard(
    title: String,
    price: String,
    period: String,
    subtitle: String,
    badge: String?,
    badgeColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier
                    .border(2.dp, AnimePaywallHighlight, RoundedCornerShape(16.dp))
                    .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = AnimePaywallHighlight.copy(alpha = 0.3f))
                else Modifier.border(1.dp, AnimeTextMuted.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            )
            .background(
                if (isSelected) AnimePaywallHighlight.copy(alpha = 0.08f)
                else AnimePaywallCard
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
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
                        color = AnimeTextPrimary
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (badgeColor == AnimePaywallHighlight) Color(0xFF0A0A14) else Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AnimeTextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        price,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) AnimePaywallHighlight else AnimeTextPrimary
                    )
                    if (period.isNotEmpty()) {
                        Text(
                            period,
                            style = MaterialTheme.typography.bodySmall,
                            color = AnimeTextMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
