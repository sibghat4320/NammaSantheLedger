package com.example.nammasantheledger.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Date and time formatting utilities.
 */
object DateTimeUtil {
    private val fullDateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatFull(date: Date): String = fullDateFormat.format(date)
    fun formatDateOnly(date: Date): String = dateOnlyFormat.format(date)
    fun formatDayName(date: Date): String = dayNameFormat.format(date)
    fun formatMonthYear(date: Date): String = monthYearFormat.format(date)
    fun formatIso(date: Date): String = isoDateFormat.format(date)

    fun now(): Long = System.currentTimeMillis()

    fun todayStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun todayEnd(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun daysAgo(days: Int): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun weekStart(): Long = daysAgo(7)

    fun monthStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun formatRelative(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 172_800_000 -> "Yesterday"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> dateOnlyFormat.format(Date(timestamp))
        }
    }
}

/**
 * Currency formatting utilities for Indian Rupee.
 */
object CurrencyUtil {
    private val indiaLocale = Locale("en", "IN")
    private val numberFormat = NumberFormat.getCurrencyInstance(indiaLocale)

    fun formatAmount(amount: Double): String {
        return numberFormat.format(amount)
    }

    fun formatAmountCompact(amount: Double): String {
        return when {
            amount >= 10_000_000 -> "₹${String.format("%.1f", amount / 10_000_000)}Cr"
            amount >= 100_000 -> "₹${String.format("%.1f", amount / 100_000)}L"
            amount >= 1_000 -> "₹${String.format("%.1f", amount / 1_000)}K"
            else -> "₹${String.format("%.0f", amount)}"
        }
    }

    fun formatPlain(amount: Double): String {
        return "₹${String.format("%.2f", amount)}"
    }
}

/**
 * String utilities.
 */
object StringUtil {
    fun getInitials(name: String): String {
        return name.trim()
            .split("\\s+".toRegex())
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }
    }

    fun getAvatarColor(name: String): Int {
        val colors = intArrayOf(
            0xFF6C63FF.toInt(), 0xFFFF6B6B.toInt(), 0xFF4ECDC4.toInt(),
            0xFFFFE66D.toInt(), 0xFFA8E6CF.toInt(), 0xFFFF8A80.toInt(),
            0xFF80D8FF.toInt(), 0xFFB388FF.toInt(), 0xFFFF80AB.toInt(),
            0xFF69F0AE.toInt(), 0xFFFFD54F.toInt(), 0xFF4FC3F7.toInt()
        )
        val hash = name.hashCode().let { if (it < 0) -it else it }
        return colors[hash % colors.size]
    }
}
