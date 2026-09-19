package com.subhunt.app.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.subhunt.app.data.local.SubscriptionDao
import com.subhunt.app.data.local.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

@HiltWorker
class BillingReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val subscriptionDao: SubscriptionDao,
    private val userPreferences: UserPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val allSubs = subscriptionDao.getAllSubscriptionsList()
            val today = LocalDate.now()
            val soundPrefs = userPreferences.snapshotSoundPrefs()

            allSubs.filter { it.isActive }.forEach { entity ->
                val sub = entity.toDomain()
                val daysUntil = ChronoUnit.DAYS.between(today, sub.nextBillingDate)

                if (daysUntil in 0..sub.reminderDaysBefore.toLong()) {
                    val title = if (daysUntil == 0L) {
                        "Bill due today: ${sub.name}"
                    } else {
                        "${sub.name} bill due in $daysUntil day(s)"
                    }
                    val message = "You'll be charged $${sub.cost} for ${sub.billingCycle.label.lowercase()} billing"

                    val overrideId = soundPrefs.overrides[sub.id]
                    val sound = ReminderSounds.effective(overrideId ?: soundPrefs.global, true)

                    NotificationHelper.showBillingReminder(
                        context = applicationContext,
                        subscriptionId = sub.id,
                        title = title,
                        message = message,
                        sound = sound
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BillingReminderWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "billing_reminders",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
