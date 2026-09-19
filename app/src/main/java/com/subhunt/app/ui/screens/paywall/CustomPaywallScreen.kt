package com.subhunt.app.ui.screens.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val bgGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0A0A14), Color(0xFF12121E), Color(0xFF1A0A2E), Color(0xFF0A0A14))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPaywallScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaywallViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uriHandler = LocalUriHandler.current
    Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("About", color = Color.White, fontWeight = FontWeight.Bold) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.LockOpen, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("Free & Open Source", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Text("No subscriptions. No paywalls. No activation codes.", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(24.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Everything included:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        listOf(
                            "Unlimited subscription tracking",
                            "Smart dashboard + health score (A-F)",
                            "Bill reminders + custom sounds",
                            "Spending insights",
                            "CSV & JSON export",
                            "Biometric / PIN app lock",
                            "Material 3 UI + animations",
                            "Home screen widget"
                        ).forEach { feat ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(20.dp))
                                Text(feat, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4ADE80).copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.Favorite, contentDescription = null, tint = Color(0xFF4ADE80))
                        Text("Made with ❤️ for people who want to take control of their finances. Source on GitHub, MIT licensed.", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { uriHandler.openUri("https://github.com/0535MANIDEEP/SubHunt") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0A0A14))
                ) { Text("View on GitHub", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Back to app", fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.height(8.dp))
                Text("Previously: RevenueCat / activation codes. Now disabled — all features free.", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

// Keep enums for binary compatibility (no longer used in UI)
enum class Plan { MONTHLY, YEARLY, LIFETIME }
enum class PaywallStep { PLAN_SELECT, CONFIRM, ACTIVATE_CODE }
