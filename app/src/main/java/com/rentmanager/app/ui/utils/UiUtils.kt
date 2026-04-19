package com.rentmanager.app.ui.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object UiUtils {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun formatDate(epochMillis: Long): String {
        if (epochMillis == 0L) return "—"
        return dateFormat.format(Date(epochMillis))
    }

    fun formatMonthYear(month: Int, year: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.YEAR, year)
        }
        return monthYearFormat.format(cal.time)
    }

    fun formatCurrency(amount: Double): String {
        return NumberFormat.getCurrencyInstance().format(amount)
    }

    fun daysUntil(epochMillis: Long): Int {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return ((epochMillis - now) / (1000 * 60 * 60 * 24)).toInt()
    }

    /** Returns a colour-coded status label and days-left string for a contract. */
    fun contractStatusLabel(contractEndDate: Long): Pair<String, ContractStatus> {
        if (contractEndDate == 0L) return Pair("No end date", ContractStatus.UNKNOWN)
        val days = daysUntil(contractEndDate)
        return when {
            days < 0 -> Pair("Expired ${-days}d ago", ContractStatus.EXPIRED)
            days == 0 -> Pair("Expires today!", ContractStatus.CRITICAL)
            days <= 14 -> Pair("Expires in ${days}d", ContractStatus.CRITICAL)
            days <= 30 -> Pair("Expires in ${days}d", ContractStatus.WARNING)
            else -> Pair("${days}d remaining", ContractStatus.OK)
        }
    }

    enum class ContractStatus { OK, WARNING, CRITICAL, EXPIRED, UNKNOWN }

    fun epochFromParts(year: Int, month: Int, day: Int): Long {
        return Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun calendarFromEpoch(epochMillis: Long): Calendar =
        Calendar.getInstance().also { it.timeInMillis = epochMillis }

    fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
}
