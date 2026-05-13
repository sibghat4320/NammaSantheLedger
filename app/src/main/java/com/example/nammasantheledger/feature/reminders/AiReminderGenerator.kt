package com.example.nammasantheledger.feature.reminders

/**
 * Architecture placeholder for Gemini AI integration.
 *
 * Design decisions:
 * - Uses interface-based design for easy mocking and testing
 * - Supports offline fallback with pre-defined templates
 * - Language and tone parameters for regional customization
 *
 * In production, this would call the Gemini API via:
 *   implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
 *
 * For now, we provide smart template-based generation that works offline.
 */
interface AiReminderGenerator {
    suspend fun generateReminder(
        customerName: String,
        amount: Double,
        shopName: String,
        language: String,
        tone: String
    ): String

    suspend fun generateDailySummary(
        totalCredit: Double,
        totalPayment: Double,
        transactionCount: Int,
        topDebtors: List<Pair<String, Double>>
    ): String

    suspend fun generateBusinessInsight(
        weeklyCredit: Double,
        weeklyPayment: Double,
        customerCount: Int
    ): String
}

/**
 * Offline-first implementation using intelligent templates.
 * Falls back to this when Gemini API is unavailable.
 */
class OfflineAiReminderGenerator : AiReminderGenerator {

    override suspend fun generateReminder(
        customerName: String,
        amount: Double,
        shopName: String,
        language: String,
        tone: String
    ): String {
        return WhatsAppReminderHelper.generateMessage(
            customerName = customerName,
            amount = amount,
            shopName = shopName,
            language = when (language.lowercase()) {
                "kannada" -> WhatsAppReminderHelper.ReminderLanguage.KANNADA
                "hindi" -> WhatsAppReminderHelper.ReminderLanguage.HINDI
                else -> WhatsAppReminderHelper.ReminderLanguage.ENGLISH
            },
            tone = when (tone.lowercase()) {
                "friendly" -> WhatsAppReminderHelper.ReminderTone.FRIENDLY
                "firm" -> WhatsAppReminderHelper.ReminderTone.FIRM
                else -> WhatsAppReminderHelper.ReminderTone.POLITE
            }
        )
    }

    override suspend fun generateDailySummary(
        totalCredit: Double,
        totalPayment: Double,
        transactionCount: Int,
        topDebtors: List<Pair<String, Double>>
    ): String {
        val formattedCredit = String.format("%.2f", totalCredit)
        val formattedPayment = String.format("%.2f", totalPayment)
        val netChange = totalCredit - totalPayment
        val emoji = if (netChange > 0) "📈" else "📉"

        val debtorsList = topDebtors.take(3).joinToString("\n") { (name, amount) ->
            "  • $name: ₹${String.format("%.2f", amount)}"
        }

        return buildString {
            appendLine("📊 Daily Business Summary")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("$emoji Today's Activity:")
            appendLine("  Credit Given: ₹$formattedCredit")
            appendLine("  Payments Received: ₹$formattedPayment")
            appendLine("  Transactions: $transactionCount")
            appendLine()
            if (topDebtors.isNotEmpty()) {
                appendLine("👤 Top Debtors:")
                appendLine(debtorsList)
            }
            appendLine()
            appendLine("💡 Keep tracking daily for better insights!")
        }
    }

    override suspend fun generateBusinessInsight(
        weeklyCredit: Double,
        weeklyPayment: Double,
        customerCount: Int
    ): String {
        val repaymentRate = if (weeklyCredit > 0)
            (weeklyPayment / weeklyCredit * 100) else 0.0

        return when {
            repaymentRate >= 80 ->
                "🎉 Great week! Your repayment rate is ${String.format("%.0f", repaymentRate)}%. " +
                "Most customers are paying on time."
            repaymentRate >= 50 ->
                "📊 Decent week with ${String.format("%.0f", repaymentRate)}% repayment rate. " +
                "Consider sending reminders to improve collections."
            repaymentRate >= 20 ->
                "⚠️ Low repayment rate of ${String.format("%.0f", repaymentRate)}%. " +
                "Send WhatsApp reminders to your top debtors this week."
            else ->
                "🔴 Very low collections this week. " +
                "Focus on following up with customers who have high outstanding balances."
        }
    }
}
