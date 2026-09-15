package com.subhunt.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subhunt.app.data.local.UserPreferences
import com.subhunt.app.ui.screens.addedit.AddEditScreen
import com.subhunt.app.ui.screens.dashboard.DashboardScreen
import com.subhunt.app.ui.screens.paywall.CustomPaywallScreen
import com.subhunt.app.ui.screens.insights.InsightsScreen
import com.subhunt.app.ui.screens.onboarding.OnboardingScreen
import com.subhunt.app.ui.screens.legal.PrivacyPolicyScreen
import com.subhunt.app.ui.screens.legal.TermsOfServiceScreen
import com.subhunt.app.ui.screens.lock.LockMode
import com.subhunt.app.ui.screens.lock.LockScreen
import com.subhunt.app.ui.screens.lock.LockViewModel
import com.subhunt.app.ui.screens.paywall.PaywallScreen
import com.subhunt.app.ui.screens.settings.SettingsScreen
import com.subhunt.app.ui.screens.sounds.SoundPickerScreen
import kotlinx.coroutines.flow.map

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val ADD_SUBSCRIPTION = "add_subscription"
    const val EDIT_SUBSCRIPTION = "edit_subscription/{subscriptionId}"
    const val PAYWALL = "paywall"
    const val SETTINGS = "settings"
    const val SOUND_PICKER = "sound_picker"
    const val SET_PIN = "set_pin"
    const val CHANGE_PIN = "change_pin"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"
    const val INSIGHTS = "insights"

    fun editSubscription(id: Long) = "edit_subscription/$id"
}

@Composable
fun SubHuntNavHost(
    userPreferences: UserPreferences
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val lockViewModel: LockViewModel = hiltViewModel()
    val lockEnabled by lockViewModel.lockEnabled.collectAsStateWithLifecycle(initialValue = false)
    val sessionUnlocked by lockViewModel.sessionUnlocked.collectAsStateWithLifecycle()
    val onboardingCompleted by userPreferences.onboardingCompleted
        .map { it as Boolean? }
        .collectAsState(initial = null)

    if (onboardingCompleted == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (lockEnabled && !sessionUnlocked) {
        LockScreen(mode = LockMode.UNLOCK, onUnlocked = {})
        return
    }

    val startDestination = if (onboardingCompleted == true) Routes.DASHBOARD else Routes.ONBOARDING

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onAddClick = { navController.navigate(Routes.ADD_SUBSCRIPTION) },
                onSubscriptionClick = { id ->
                    navController.navigate(Routes.editSubscription(id))
                },
                onPaywallClick = { navController.navigate(Routes.PAYWALL) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onInsightsClick = { navController.navigate(Routes.INSIGHTS) },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Routes.ADD_SUBSCRIPTION) {
            AddEditScreen(
                subscriptionId = null,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Routes.EDIT_SUBSCRIPTION,
            arguments = listOf(navArgument("subscriptionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId")
            AddEditScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Routes.PAYWALL) {
            CustomPaywallScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSounds = { navController.navigate(Routes.SOUND_PICKER) },
                onNavigateToSetPin = { navController.navigate(Routes.SET_PIN) },
                onNavigateToChangePin = { navController.navigate(Routes.CHANGE_PIN) },
                onNavigateToPrivacy = { navController.navigate(Routes.PRIVACY) },
                onNavigateToTerms = { navController.navigate(Routes.TERMS) },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Routes.SET_PIN) {
            LockScreen(
                mode = LockMode.SET,
                onUnlocked = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Routes.CHANGE_PIN) {
            LockScreen(
                mode = LockMode.CHANGE,
                onUnlocked = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Routes.SOUND_PICKER) {
            SoundPickerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Routes.PRIVACY) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TERMS) {
            TermsOfServiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.INSIGHTS) {
            InsightsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
