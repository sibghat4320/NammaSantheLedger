package com.example.nammasantheledger.feature.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nammasantheledger.R
import com.example.nammasantheledger.domain.repository.CustomerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WorkManager Worker that fires weekly to remind the shop owner
 * about customers with outstanding dues.
 *
 * Design decisions:
 * - Uses HiltWorker for dependency injection
 * - Sends a local notification with a summary of all pending dues
 * - Each customer with dues gets an individual notification with
 *   quick-action to send SMS/Telegram reminder
 * - Completely free — no external APIs, just local notifications + intents
 */
@HiltWorker
class WeeklyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val customerRepository: CustomerRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "weekly_reminder_channel"
        const val CHANNEL_NAME = "Weekly Reminders"
        const val SUMMARY_NOTIFICATION_ID = 9000
        const val WORK_NAME = "weekly_due_reminder"
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()

        val customers = try {
            customerRepository.getCustomersWithOutstandingDues().first()
        } catch (_: Exception) {
            return Result.retry()
        }

        if (customers.isEmpty()) return Result.success()

        // Filter customers with phone numbers and actual dues
        val customersWithDues = customers.filter {
            it.phoneNumber.isNotEmpty() && it.outstandingBalance > 0
        }

        if (customersWithDues.isEmpty()) return Result.success()

        val totalDues = customersWithDues.sumOf { it.outstandingBalance }
        val formattedTotal = String.format("₹%.0f", totalDues)

        // Summary notification
        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📋 Weekly Dues Reminder")
            .setContentText("${customersWithDues.size} customers owe $formattedTotal total")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildSummaryText(customersWithDues))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)

        // Individual notifications for top 5 debtors (with send actions)
        customersWithDues
            .sortedByDescending { it.outstandingBalance }
            .take(5)
            .forEachIndexed { index, customer ->
                val formattedAmount = String.format("₹%.0f", customer.outstandingBalance)
                val reminderMessage = "Hi ${customer.name}, " +
                        "this is a friendly reminder that you have a pending due of $formattedAmount " +
                        "at Namma Santhe. Please clear it at your earliest convenience. Thank you! 🙏"

                // SMS intent
                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${customer.phoneNumber}")).apply {
                    putExtra("sms_body", reminderMessage)
                }
                val smsPendingIntent = PendingIntent.getActivity(
                    context, index * 10, smsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Telegram intent
                val phone = customer.phoneNumber
                    .replace("[^0-9]".toRegex(), "")
                    .let { if (it.length == 10) "91$it" else it }
                val tgIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://t.me/+$phone?text=${Uri.encode(reminderMessage)}")
                )
                val tgPendingIntent = PendingIntent.getActivity(
                    context, index * 10 + 1, tgIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val customerNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("💰 ${customer.name} owes $formattedAmount")
                    .setContentText("Tap to send a reminder via SMS or Telegram")
                    .addAction(0, "📱 SMS", smsPendingIntent)
                    .addAction(0, "✈️ Telegram", tgPendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(SUMMARY_NOTIFICATION_ID + index + 1, customerNotification)
            }

        return Result.success()
    }

    private fun buildSummaryText(customers: List<com.example.nammasantheledger.domain.model.Customer>): String {
        return buildString {
            appendLine("Customers with pending dues:")
            appendLine()
            customers
                .sortedByDescending { it.outstandingBalance }
                .take(10)
                .forEach { customer ->
                    val amount = String.format("₹%.0f", customer.outstandingBalance)
                    appendLine("• ${customer.name}: $amount")
                }
            if (customers.size > 10) {
                appendLine("...and ${customers.size - 10} more")
            }
            appendLine()
            appendLine("Open the app to send reminders via SMS, Telegram, or WhatsApp.")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Weekly payment due reminders for your customers"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
