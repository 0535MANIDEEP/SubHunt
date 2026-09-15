package com.subhunt.app.ui.screens.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subhunt.app.domain.model.BillingCycle
import com.subhunt.app.domain.model.FREE_SUBSCRIPTION_LIMIT
import com.subhunt.app.domain.model.Subscription
import com.subhunt.app.domain.model.SubscriptionCategory
import java.time.LocalDate
import kotlinx.coroutines.launch

private val categoryColors = listOf(
    0xFF6750A4, 0xFF0061A4, 0xFF006D3C, 0xFF904D00,
    0xFFBA1A1A, 0xFF4A6267, 0xFF5C5D72, 0xFF006B5F,
    0xFF7D5260, 0xFF5D5F5F, 0xFF3F4845, 0xFF7C4DFF
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    subscriptionId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val isEditing = subscriptionId != null
    var name by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var billingCycle by remember { mutableStateOf(BillingCycle.MONTHLY) }
    var category by remember { mutableStateOf(SubscriptionCategory.OTHER) }
    var selectedColor by remember { mutableLongStateOf(0xFF6750A4) }
    var notes by remember { mutableStateOf("") }
    var existingStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var existingNextBillingDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(subscriptionId) {
        if (subscriptionId != null) {
            viewModel.loadSubscription(subscriptionId)
        }
    }

    val loadedSubscription by viewModel.loadedSubscription.collectAsStateWithLifecycle()
    val isAtFreeLimit by viewModel.isAtFreeLimit.collectAsStateWithLifecycle()
    var showLimitDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(loadedSubscription) {
        loadedSubscription?.let { sub ->
            name = sub.name
            cost = sub.cost.toString()
            billingCycle = sub.billingCycle
            category = sub.category
            selectedColor = sub.color
            notes = sub.notes
            existingStartDate = sub.startDate
            existingNextBillingDate = sub.nextBillingDate
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { result ->
            when (result) {
                is SaveResult.Success -> {
                    val msg = if (isEditing) "${name.trim()} updated" else "${name.trim()} added"
                    snackbarHostState.showSnackbar(msg)
                    onNavigateBack()
                }
                is SaveResult.LimitReached -> {
                    showLimitDialog = true
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Subscription" else "New Subscription",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val costValue = cost.toDoubleOrNull() ?: return@IconButton
                            if (name.isBlank()) return@IconButton

                            val startDate = if (isEditing) existingStartDate ?: LocalDate.now() else LocalDate.now()
                            val nextBilling = if (isEditing) existingNextBillingDate ?: when (billingCycle) {
                                BillingCycle.WEEKLY -> startDate.plusWeeks(1)
                                BillingCycle.MONTHLY -> startDate.plusMonths(1)
                                BillingCycle.QUARTERLY -> startDate.plusMonths(3)
                                BillingCycle.YEARLY -> startDate.plusYears(1)
                            } else when (billingCycle) {
                                BillingCycle.WEEKLY -> LocalDate.now().plusWeeks(1)
                                BillingCycle.MONTHLY -> LocalDate.now().plusMonths(1)
                                BillingCycle.QUARTERLY -> LocalDate.now().plusMonths(3)
                                BillingCycle.YEARLY -> LocalDate.now().plusYears(1)
                            }

                            val subscription = Subscription(
                                id = subscriptionId ?: 0,
                                name = name.trim(),
                                cost = costValue,
                                billingCycle = billingCycle,
                                category = category,
                                startDate = startDate,
                                nextBillingDate = nextBilling,
                                color = selectedColor,
                                notes = notes.trim()
                            )

                            if (isEditing) {
                                viewModel.updateSubscription(subscription)
                            } else {
                                viewModel.addSubscription(subscription)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Service Name") },
                placeholder = { Text("Netflix, Spotify, etc.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = cost,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d{0,7}\\.?\\d{0,2}$"))) {
                        cost = newValue
                    }
                },
                label = { Text("Cost") },
                prefix = { Text("$") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = cost.isNotEmpty() && cost.toDoubleOrNull() == null
            )

            Text(
                "Billing Cycle",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BillingCycle.entries.forEach { cycle ->
                    FilterChip(
                        selected = billingCycle == cycle,
                        onClick = { billingCycle = cycle },
                        label = { Text(cycle.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                "Category",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SubscriptionCategory.entries.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                leadingIcon = {
                                    Icon(
                                        cat.icon,
                                        contentDescription = cat.label,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(cat.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Text(
                "Color",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryColors.take(6).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(color))
                            .then(
                                if (selectedColor == color) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                                } else Modifier
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryColors.drop(6).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(color))
                            .then(
                                if (selectedColor == color) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                                } else Modifier
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Family plan, shared with...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }

    if (showLimitDialog) {
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            icon = { Icon(Icons.Rounded.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Free Limit Reached") },
            text = { Text("Free users can track up to $FREE_SUBSCRIPTION_LIMIT subscriptions. Upgrade to Pro for unlimited tracking.") },
            confirmButton = {
                Button(onClick = { showLimitDialog = false; onNavigateToPaywall() }) {
                    Text("Upgrade to Pro")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitDialog = false }) { Text("Maybe Later") }
            }
        )
    }
}
