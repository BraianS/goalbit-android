package dev.braian.goalbit.model

data class DurationTimer(
    var hour: Int? = 0,
    var minute: Int? = 0,
    var second: Int? = 0
) {

    fun addTimeStamp(hour: Int, minute: Int, second: Int) {
        this.hour = hour
        this.minute = minute
        this.second = second
    }
}
