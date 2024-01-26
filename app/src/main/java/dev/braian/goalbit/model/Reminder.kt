package dev.braian.goalbit.model

class Reminder {
    var enable: Boolean? = false
    var hour: Int? = null
    var minute: Int? = null
    var days: Weekday = Weekday()
}