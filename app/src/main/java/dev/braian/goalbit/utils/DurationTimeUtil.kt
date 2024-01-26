package dev.braian.goalbit.utils

import dev.braian.goalbit.model.DurationTimer

object DurationTimeUtil {

    fun formatStringTime(durationTimer: DurationTimer): String {
        return String.format(
            "%02d:%02d:%02d",
            durationTimer.hour,
            durationTimer.minute,
            durationTimer.second
        )
    }

    fun formatTime(hours: Long, minutes: Long, seconds: Long): String {
        val formattedHours = if (hours < 10) "0$hours" else "$hours"
        val formattedMinutes = if (minutes < 10) "0$minutes" else "$minutes"
        val formattedSeconds = if (seconds < 10) "0$seconds" else "$seconds"

        return "$formattedHours:$formattedMinutes:$formattedSeconds"
    }
}