package dev.braian.goalbit.utils

import dev.braian.goalbit.model.DailyActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DailyActivityUtil {

     fun sumDoneActivities(dailyActivities: List<DailyActivity>): String {
        var sum = 0
        for (dailyActivity in dailyActivities) {
            if (dailyActivity.done!!) {
                sum++
            }
        }
        return sum.toString()
    }


    fun countWeekdayOccurrences(dailyActivities: List<DailyActivity>?): Map<String, Int> {
        val weekdayCount = mutableMapOf<String, Int>()

        dailyActivities?.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (dailyActivity in it) {
                if (dailyActivity.done == true && !dailyActivity.date.isNullOrEmpty()) {
                    try {
                        val date = dateFormat.parse(dailyActivity.date)
                        val calendar = Calendar.getInstance().apply {
                            time = date
                        }

                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val dayOfWeekString = getAbbreviatedDayOfWeek(dayOfWeek)

                        if (weekdayCount.containsKey(dayOfWeekString)) {
                            weekdayCount[dayOfWeekString] = weekdayCount[dayOfWeekString]!! + 1
                        } else {
                            weekdayCount[dayOfWeekString] = 1
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return weekdayCount
    }


    fun getAbbreviatedDayOfWeek(dayOfWeek: Int): String {
        val daysOfWeek = arrayOf(
            "Sun",
            "Mon",
            "Tue",
            "Wed",
            "Thu",
            "Fri",
            "Sat"
        )
        return daysOfWeek[dayOfWeek - 1] // Adjust for array index starting at 0
    }

    fun getYearlyOccurrences(dailyActivities: List<DailyActivity>): Map<String, Float> {
        val yearlyOccurrences = mutableMapOf<String, Float>()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (dailyActivity in dailyActivities) {
            // Check if the activity is done
            if (dailyActivity.done!!) {
                try {
                    val date = dateFormat.parse(dailyActivity.date)

                    val calendar = Calendar.getInstance().apply {
                        time = date
                    }

                    val year = calendar.get(Calendar.YEAR)

                    // Ensure the year count is initialized
                    val yearOccurrences =
                        yearlyOccurrences.getOrPut(year.toString()) { 0.toFloat() }

                    // Update yearly occurrences
                    yearlyOccurrences[year.toString()] = yearOccurrences + 1

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return yearlyOccurrences
    }

    // Add this function to get the month string
    fun getMonthString(month: Int): String {
        val months = arrayOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "May",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Oct",
            "Nov",
            "Dec"
        )
        return months[month - 1] // Adjust for array index starting at 0
    }

}