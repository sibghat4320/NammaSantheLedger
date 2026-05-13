package com.example.nammasantheledger.feature.reminders

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * WhatsApp integration for sending payment reminders.
 * Uses WhatsApp's intent-based API for prefilled messages.
 *
 * Design decisions:
 * - No API key needed (intent-based)
 * - Works offline (opens WhatsApp directly)
 * - Supports multiple languages
 * - Graceful fallback if WhatsApp not installed
 */
object WhatsAppReminderHelper {

    enum class ReminderLanguage {
        KANNADA, HINDI, ENGLISH
    }

    enum class ReminderTone {
        POLITE, FRIENDLY, FIRM
    }

    /**
     * Generate a reminder message based on language and tone.
     */
    fun generateMessage(
        customerName: String,
        amount: Double,
        shopName: String,
        language: ReminderLanguage = ReminderLanguage.KANNADA,
        tone: ReminderTone = ReminderTone.POLITE
    ): String {
        val formattedAmount = String.format("%.2f", amount)

        return when (language) {
            ReminderLanguage.KANNADA -> when (tone) {
                ReminderTone.POLITE ->
                    "🙏 Namaskara $customerName avare, $shopName inda message. " +
                    "Nimma ₹$formattedAmount baki ide. " +
                    "Dayavittu settle madi. Dhanyavadagalu."

                ReminderTone.FRIENDLY ->
                    "👋 Hi $customerName, $shopName inda! " +
                    "Nimma ₹$formattedAmount baki ide guru. " +
                    "Mundina santhe ge baruvaga kodi. Thanks! 😊"

                ReminderTone.FIRM ->
                    "Namaskara $customerName avare, " +
                    "Nimma ₹$formattedAmount baki baaki ide $shopName alli. " +
                    "Dayavittu sheegra clear madi. Dhanyavadagalu. 🙏"
            }

            ReminderLanguage.HINDI -> when (tone) {
                ReminderTone.POLITE ->
                    "🙏 Namaste $customerName ji, $shopName se message. " +
                    "Aapka ₹$formattedAmount baaki hai. " +
                    "Kripya jald se jald clear karein. Dhanyavaad."

                ReminderTone.FRIENDLY ->
                    "👋 Hello $customerName bhai, $shopName se! " +
                    "Aapka ₹$formattedAmount baaki hai. " +
                    "Agle hafte de dena please. Thanks! 😊"

                ReminderTone.FIRM ->
                    "Namaste $customerName ji, " +
                    "Aapka ₹$formattedAmount baaki hai $shopName pe. " +
                    "Kripya jaldi se payment kar dijiye. Dhanyavaad. 🙏"
            }

            ReminderLanguage.ENGLISH -> when (tone) {
                ReminderTone.POLITE ->
                    "🙏 Dear $customerName, this is a reminder from $shopName. " +
                    "Your outstanding balance is ₹$formattedAmount. " +
                    "Kindly settle your dues at the earliest. Thank you!"

                ReminderTone.FRIENDLY ->
                    "👋 Hi $customerName! Reminder from $shopName - " +
                    "you have ₹$formattedAmount pending. " +
                    "Please pay when convenient. Thanks! 😊"

                ReminderTone.FIRM ->
                    "Dear $customerName, " +
                    "your outstanding amount of ₹$formattedAmount at $shopName " +
                    "is pending. Please clear your dues promptly. Thank you. 🙏"
            }
        }
    }

    /**
     * Open WhatsApp with a prefilled reminder message.
     * Falls back to share intent if WhatsApp is not installed.
     */
    fun sendWhatsAppReminder(
        context: Context,
        phoneNumber: String,
        message: String
    ) {
        try {
            // Format phone number (ensure country code)
            val formattedPhone = formatPhoneNumber(phoneNumber)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$formattedPhone?text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback: try without package specification
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$formattedPhone?text=${Uri.encode(message)}")
                }
                context.startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            // Final fallback: share via any app
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Send Reminder"))
            Toast.makeText(context, "WhatsApp not available, sharing via other apps", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace("[^0-9]".toRegex(), "")
        return when {
            cleaned.startsWith("91") -> cleaned
            cleaned.length == 10 -> "91$cleaned"
            else -> cleaned
        }
    }
}
