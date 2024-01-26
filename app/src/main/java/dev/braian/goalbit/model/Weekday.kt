package dev.braian.goalbit.model

class Weekday {
    var weekdays: ArrayList<Day> = ArrayList<Day>()

    fun addSelectedDays(selectedDays: MutableSet<Day>) {
        weekdays.clear()
        weekdays.addAll(selectedDays)
    }
}
