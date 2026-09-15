package com.subhunt.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.subhunt.app.MainActivity
import com.subhunt.app.R

object NotificationHelper {
    const val CHANNEL_ID_BILLING = "billing_reminders"
    const val CHANNEL_NAME = "Billing Reminders"

    private fun channelIdFor(sound: ReminderSound): String =
        if (sound.resId == null) CHANNEL_ID_BILLING else "${CHANNEL_ID_BILLING}_${sound.id}"

    private fun soundUri(context: Context, sound: ReminderSound): Uri? =
        sound.resId?.let {
            Uri.parse("android.resource://${context.packageName}/$it")
        }

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        ReminderSounds.ALL.forEach { sound ->
            val channel = NotificationChannel(
                channelIdFor(sound),
                if (sound.resId == null) CHANNEL_NAME else "$CHANNEL_NAME \u00b7 ${sound.label}",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming subscription bills"
                val uri = soundUri(context, sound)
                if (uri != null) {
                    setSound(
                        uri,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showBillingReminder(
        context: Context,
        subscriptionId: Long,
        title: String,
        message: String,
        sound: ReminderSound = ReminderSounds.byId(null)
    ) {
        createChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            subscriptionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelIdFor(sound))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(subscriptionId.toInt(), notification)
    }
}
