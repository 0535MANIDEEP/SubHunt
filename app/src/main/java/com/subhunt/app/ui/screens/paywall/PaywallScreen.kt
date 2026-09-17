package com.subhunt.app.ui.screens.paywall

// This screen now redirects to the CustomPaywallScreen which handles
// the UPI payment + activation code flow (zero fees).

import androidx.compose.runtime.Composable

@Composable
fun PaywallScreen(
    onNavigateBack: () -> Unit
) {
    CustomPaywallScreen(onNavigateBack = onNavigateBack)
}
