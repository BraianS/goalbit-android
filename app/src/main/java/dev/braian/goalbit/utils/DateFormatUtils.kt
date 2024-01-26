package dev.braian.goalbit.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatUtils {

    fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    fun formatYear(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("EEEE")).uppercase()
    }

    fun formatMonthWithYear(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase()
    }

    fun formatHourAndMinutes(hour: Int, minute: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    fun convertToTimestamp(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(calendar.time)
    }
}