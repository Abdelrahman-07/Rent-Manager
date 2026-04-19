package com.rentmanager.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rentmanager.app.MainActivity

object NotificationHelper {

    const val RENT_DUE_CHANNEL_ID = "rent_due_channel"
    const val CONTRACT_EXPIRY_CHANNEL_ID = "contract_expiry_channel"

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        NotificationChannel(
            RENT_DUE_CHANNEL_ID,
            "Rent Due Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for upcoming rent payments"
            enableLights(true)
            enableVibration(true)
        }.also { manager.createNotificationChannel(it) }

        NotificationChannel(
            CONTRACT_EXPIRY_CHANNEL_ID,
            "Contract Expiry Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for contracts nearing expiration"
            enableLights(true)
            enableVibration(true)
        }.also { manager.createNotificationChannel(it) }
    }

    fun sendRentDueNotification(
        context: Context,
        tenantName: String,
        apartmentName: String,
        daysUntilDue: Int,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = when (daysUntilDue) {
            0 -> "$tenantName's rent for $apartmentName is due TODAY."
            1 -> "$tenantName's rent for $apartmentName is due TOMORROW."
            else -> "$tenantName's rent for $apartmentName is due in $daysUntilDue days."
        }

        val notification = NotificationCompat.Builder(context, RENT_DUE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💰 Rent Due Reminder")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    fun sendContractExpiryNotification(
        context: Context,
        tenantName: String,
        apartmentName: String,
        daysLeft: Int,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = when {
            daysLeft <= 0 -> "$tenantName's contract for $apartmentName has EXPIRED."
            daysLeft == 1 -> "$tenantName's contract for $apartmentName expires TOMORROW."
            else -> "$tenantName's contract for $apartmentName expires in $daysLeft days."
        }

        val notification = NotificationCompat.Builder(context, CONTRACT_EXPIRY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("📋 Contract Expiry Warning")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }
}
