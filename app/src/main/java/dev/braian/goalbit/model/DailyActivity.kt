package dev.braian.goalbit.model

import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyActivity {

    var date: String? = ""
    var done: Boolean? = false
    var duration: DurationTimer = DurationTimer()

    constructor(date: String?, done: Boolean?, duration: DurationTimer) {
        this.date = date
        this.done = done
        this.duration = duration
    }

    constructor()

    override fun toString(): String {
        return "DailyActivity(date=$date, done=$done)"
    }

    fun parseDateField(date: String?): Triple<Int, Int, Int> {
        if (date.isNullOrEmpty()) {
            // Handle the case where the date is null or empty
            return Triple(0, 0, 0)
        }

        val parsedLocalDate = LocalDate.parse(date)

        return Triple(parsedLocalDate.dayOfMonth, parsedLocalDate.monthValue, parsedLocalDate.year)
    }

    fun isSameDay(day: CalendarDay): Boolean {
        val (parsedDay, parsedMonth, parsedYear) = parseDateField(date)
        return (
                parsedDay == day.day &&
                        parsedMonth == day.month &&
                        parsedYear == day.year
                )
    }

}