package dev.braian.goalbit.view.viewholder

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.model.DailyActivity
import dev.braian.goalbit.model.Day
import dev.braian.goalbit.model.DurationTimer
import dev.braian.goalbit.model.Habit
import dev.braian.goalbit.model.HabitWithYearStreak
import dev.braian.goalbit.utils.DailyActivityUtil
import dev.braian.goalbit.utils.DateFormatUtils
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class HabitViewModel : ViewModel() {

    private val habitDatabase: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("habits")
    }

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun getHabitReferenceById(habitId: String): DatabaseReference {
        return habitDatabase.child(habitId)
    }

    private val _habitsLiveData = MutableLiveData<MutableList<Pair<Habit, DailyActivity?>>>()
    val habitsLiveData: LiveData<MutableList<Pair<Habit, DailyActivity?>>> = _habitsLiveData

    private val _habitsWithYearStreak = MutableLiveData<List<HabitWithYearStreak>>()
    val habitsWithYearStreak: MutableLiveData<List<HabitWithYearStreak>> = _habitsWithYearStreak

    private val _errorMessageLiveDate = MutableLiveData<String?>()
    val errorMessageLiveData: LiveData<String?> get() = _errorMessageLiveDate

    fun updateHabitById(habitId: String, habit: Habit) {
        val habitReference = getHabitReferenceById(habitId)
//        val postValues = HashMap<String, Any?>()
//        postValues["id"] = habit.id
//        postValues["name"] = habit.name
//        postValues["description"] = habit.description
//        postValues["userId"] = habit.userId
//        postValues["reminder"] = habit.reminder
//        postValues["dateEnd"] = habit.dateEnd
//        postValues["rangeOption"] = habit.rangeOption.name
//        postValues["goal"] = habit.goal.name
//        postValues["duration"] = habit.duration
//        postValues["icon"] = habit.icon
        habitReference.updateChildren(mapHabit(habit))
    }

    fun mapHabit(habit: Habit): HashMap<String, Any?> {
        val habitValue = HashMap<String, Any?>()
        habitValue["id"] = habit.id
        habitValue["name"] = habit.name
        habitValue["description"] = habit.description
        habitValue["userId"] = habit.userId
        habitValue["reminder"] = habit.reminder
        habitValue["dateEnd"] = habit.dateEnd
        habitValue["rangeOption"] = habit.rangeOption.name
        habitValue["goal"] = habit.goal.name
        habitValue["duration"] = habit.duration
        habitValue["icon"] = habit.icon
        return habitValue
    }

    fun deleteHabitById(habitId: String) {
        habitDatabase.child(habitId).setValue(null)
    }

    fun transferAnonymousHabitToGoogleAccount(userId: String, userGoogleId: String) {
        habitDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    for (habitSnapShot in snapshot.children) {
                        var habit = habitSnapShot.getValue(Habit::class.java)!!

                        if (habit.userId == userId) {
                            habit.userId = userGoogleId


                            val postValues = HashMap<String, Any?>()
                            postValues["id"] = habit.id
                            postValues["name"] = habit.name
                            postValues["description"] = habit.description
                            postValues["userId"] = habit.userId
                            postValues["reminder"] = habit.reminder
                            postValues["dateEnd"] = habit.dateEnd
                            postValues["rangeOption"] = habit.rangeOption.name
                            postValues["goal"] = habit.goal.name
                            postValues["duration"] = habit.duration
                            postValues["icon"] = habit.icon
                            habitSnapShot.ref?.updateChildren(postValues)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(DataBaseConstants.TAG.HABIT_VIEW_MODEL, databaseError.message)
            }
        })
    }

    fun getHabitByIdAndUserId(habitId: String, userId: String?, onResult: (Habit?) -> Unit) {
        habitDatabase.child(habitId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val habit = snapshot.getValue(Habit::class.java)
                if (habit?.userId == userId) {
                    onResult(habit)
                } else {
                    onResult(null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                val errorMessage = "Database error: ${databaseError.message}"
                _errorMessageLiveDate.value = errorMessage
                Log.e(DataBaseConstants.TAG.HABIT_VIEW_MODEL, errorMessage)
                onResult(null)
            }
        })
    }

    fun getDailyActivitiesDescendingByHabitId2(
        habitId: String, onResult: (List<DailyActivity>?) -> Unit
    ) {

        val dailyActivityRef =
            habitDatabase.child(habitId).child("dailyActivities").orderByChild("date")

        dailyActivityRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dailyActivities = mutableListOf<DailyActivity>()
                for (activitySnapshot in snapshot.children.sortedByDescending {
                    it.child("date").getValue(String::class.java)
                }) {

                    val dailyActivity = activitySnapshot.getValue(DailyActivity::class.java)

                    val date = dailyActivity?.date
                    val done = dailyActivity?.done

                    if (date != null && done != null) {
                        val dailyActivity = DailyActivity()
                        dailyActivity.date = date
                        dailyActivity.done = done
                        dailyActivities.add(dailyActivity)
                    }
                }
                onResult(dailyActivities)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                val errorMessage = "Database error: ${databaseError.message}"
                _errorMessageLiveDate.value = errorMessage
                Log.e(DataBaseConstants.TAG.HABIT_VIEW_MODEL, errorMessage)
            }
        })

    }

    suspend fun getDailyActivitiesDescendingByHabitId(
        habitId: String

    ): List<DailyActivity> {

        val dailyActivityRef =
            habitDatabase.child(habitId).child("dailyActivities").orderByChild("date")

        val dataSnapshot = dailyActivityRef.get().await()
        val dailyActivities = mutableListOf<DailyActivity>()

        dataSnapshot.children.sortedByDescending { it.child("date").getValue(String::class.java) }
            .forEach { data ->
                val date = data.child("date").getValue(String::class.java)
                val done = data.child("done").getValue(Boolean::class.java)

                if (date != null && done != null) {
                    val dailyActivity = DailyActivity()
                    dailyActivity.date = date
                    dailyActivity.done = done
                    dailyActivities.add(dailyActivity)
                }
            }
        return dailyActivities
    }

    fun getHabitsWithYearlyOccurrencies(userId: String) {
        habitDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val habits = ArrayList<HabitWithYearStreak>()

                for (snapShot in dataSnapshot.children) {
                    val habit = snapShot.getValue(Habit::class.java)

                    if (habit?.userId == userId) {
                        habit?.let {
                            getDailyActivitiesForHabit(it.id!!) { dailyActivities ->
                                var habitWithYearStreak = HabitWithYearStreak(
                                    habit,
                                    DailyActivityUtil.getYearlyOccurrences(dailyActivities)
                                )
                                habits.add(habitWithYearStreak)
                                _habitsWithYearStreak.value = habits
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                val errorMessage = "Database error: ${databaseError.message}"
                _errorMessageLiveDate.value = errorMessage
                Log.e(DataBaseConstants.TAG.HABIT_VIEW_MODEL, errorMessage)
            }
        })
    }

    private fun getDailyActivitiesForHabit(
        habitId: String?,
        callback: (List<DailyActivity>) -> Unit
    ) {
        habitDatabase.child(habitId!!).child("dailyActivities")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val dailyActivities = mutableListOf<DailyActivity>()

                    for (data in dataSnapshot.children) {
                        val dailyActivity = data.getValue(DailyActivity::class.java)
                        val date = data.child("date").getValue(String::class.java)
                        val done = data.child("done").getValue(Boolean::class.java)

                        if (date != null && done != null) {
                            dailyActivities.add(dailyActivity!!)
                        }
                    }
                    callback(dailyActivities)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    val errorMessage = "Database error: ${databaseError.message}"
                    _errorMessageLiveDate.value = errorMessage
                    Log.e(DataBaseConstants.TAG.HABIT_VIEW_MODEL, errorMessage)
                }
            })
    }


    fun getHabitsByUserIdAndOrderByDateEnd(selectedDate: LocalDate?, userId: String) {
        selectedDate?.let { date ->
            val selectedDateString =
                DateFormatUtils.formatDate(date)
            val selectedDayOfWeek =
                DateFormatUtils.formatYear(date)

            habitDatabase.orderByChild("dateEnd")
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val habitList = mutableListOf<Pair<Habit, DailyActivity?>>()

                        for (habitSnapshot in dataSnapshot.children) {

                          habitSnapshot.getValue(Habit::class.java)?.run {

                                if (this.userId == userId) {
                                    val hasWeek =
                                        reminder.days.weekdays.contains(
                                            Day.valueOf(
                                                selectedDayOfWeek
                                            )
                                        )
                                    val isDataNull = dateEnd.isNullOrEmpty()

                                    if (hasWeek && (isDataNull || selectedDateString <= dateEnd!!)) {
                                        val dailyActivitiesRef =
                                            habitSnapshot.child("dailyActivities")
                                        val firstDailyActivity = dailyActivitiesRef.children
                                            .map { it.getValue(DailyActivity::class.java) }
                                            .firstOrNull { it?.date?.contains(selectedDateString) == true }

                                        habitList.add(this to firstDailyActivity)
                                    }
                                }
                            }
                        }
                        _habitsLiveData.postValue(habitList)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        val errorMessage = "Database error: ${databaseError.message}"
                        _errorMessageLiveDate.value = errorMessage
                        Log.e(DataBaseConstants.TAG.HABIT_VIEW_MODEL, errorMessage)
                    }
                })
        }
    }

    fun updateDailyActivity(habitId: String, date: LocalDate, done: Boolean) {
        val dailyActivityRef = habitDatabase.child(habitId).child("dailyActivities")


        date?.let {
            val selectedDateString = DateFormatUtils.formatDate(date)

            dailyActivityRef.orderByChild("date").equalTo(selectedDateString)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val existingSnapshot = snapshot.getValue(DailyActivity::class.java)

                            existingSnapshot?.let {
                                it.done = done
                                dailyActivityRef.child(snapshot.key!!).setValue(existingSnapshot)

                                // Notify the observers about the change
                                refreshHabitsLiveData(
                                    auth.currentUser!!.uid,
                                    date
                                )
                                return
                            }
                        }
                        val newDailyActivity =
                            DailyActivity(selectedDateString, done, DurationTimer())
                        dailyActivityRef.push().setValue(newDailyActivity)


                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        val errorMessage = "Database error: ${databaseError.message}"
                        Log.e(DataBaseConstants.TAG.HABIT_VIEW_MODEL, errorMessage)
                    }
                })


        }
    }

    private fun refreshHabitsLiveData(userId: String, selectedDateString: LocalDate) {
        // Fetch and update the habits data
        getHabitsByUserIdAndOrderByDateEnd(selectedDateString, userId)
    }

    fun saveDailyDuration(habitId: String, date: LocalDate, duration: DurationTimer) {

        val dailyActivityRef = habitDatabase.child(habitId).child("dailyActivities")

        date?.let {
            val selectedDateString = DateFormatUtils.formatDate(date)

            dailyActivityRef.orderByChild("date").equalTo(selectedDateString)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val existingSnapshot = snapshot.getValue(DailyActivity::class.java)
                            existingSnapshot?.let {
                                it.duration = duration
                                dailyActivityRef.child(snapshot.key!!).setValue(existingSnapshot)
                                return
                            }
                        }

                        val newDailyActivity = DailyActivity(selectedDateString, false, duration)
                        dailyActivityRef.push().setValue(newDailyActivity)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        val errorMessage = "Database error: ${databaseError.message}"
                        Log.e(DataBaseConstants.TAG.HABIT_VIEW_MODEL, errorMessage)
                    }
                })
        }
    }

    fun finishDailyActivity(
        habitId: String,
        date: LocalDate,
        duration: DurationTimer,
        done: Boolean
    ) {
        val dailyActivityRef = habitDatabase.child(habitId).child("dailyActivities")

        date?.let {
            val selectedDateString = DateFormatUtils.formatDate(date)

            dailyActivityRef.orderByChild("date").equalTo(selectedDateString)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val existingSnapshot = snapshot.getValue(DailyActivity::class.java)
                            existingSnapshot?.let {
                                it.duration = duration
                                it.done = done
                                dailyActivityRef.child(snapshot.key!!).setValue(existingSnapshot)
                                return
                            }
                        }

                        val newDailyActivity = DailyActivity(selectedDateString, done, duration)
                        dailyActivityRef.push().setValue(newDailyActivity)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        val errorMessage = "Database error: ${databaseError.message}"
                        Log.e(DataBaseConstants.TAG.HABIT_VIEW_MODEL, errorMessage)
                    }
                })
        }
    }

}