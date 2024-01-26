package dev.braian.goalbit.model

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.security.MessageDigest
import java.time.LocalDate

data class Habit
    (
    var id: String?,
    var name: String,
    var description: String,
    var userId: String,
    var reminder: Reminder = Reminder(),
    var dateEnd: String?,
    var icon: Int = 671,
    var rangeOption: RangeOption = RangeOption.Off,
    var goal: GoalType = GoalType.Off,
    var duration: DurationTimer? = DurationTimer(),
    var activities: List<DailyActivity>? = null
) {

    fun save() {
        if (id != null) {

            val database: DatabaseReference = FirebaseDatabase.getInstance().reference
            val entityReference = database.child("habits").push()
            this.id = entityReference.key
            entityReference.setValue(this.copy(this.id))
        }
    }

    constructor() : this(
        null,
        "",
        "",
        "",
        Reminder(),
        "",
        671,
        RangeOption.Off,
        GoalType.Off,
        DurationTimer(),
        arrayListOf()
    )
}