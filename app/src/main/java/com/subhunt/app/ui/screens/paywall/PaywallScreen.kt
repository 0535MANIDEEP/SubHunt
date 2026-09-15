package com.subhunt.app.ui.screens.paywall

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val isSubscribed by viewModel.isSubscribed.collectAsStateWithLifecycle()

    LaunchedEffect(isSubscribed) {
        if (isSubscribed) onNavigateBack()
    }

    val paywallOptions = remember(onNavigateBack) {
        PaywallOptions(dismissRequest = { onNavigateBack() })
    }

    Paywall(options = paywallOptions)
}
