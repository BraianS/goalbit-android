package dev.braian.goalbit.view.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import dev.braian.goalbit.utils.App
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.databinding.HabitViewBinding
import dev.braian.goalbit.view.listener.OnHabitListener
import dev.braian.goalbit.model.DailyActivity
import dev.braian.goalbit.model.DurationTimer
import dev.braian.goalbit.model.GoalType
import dev.braian.goalbit.model.Habit
import dev.braian.goalbit.view.viewholder.HabitViewModel
import dev.braian.goalbit.utils.DurationTimeUtil
import dev.braian.goalbit.utils.ColorUtils
import dev.braian.goalbit.utils.DateFormatUtils
import dev.braian.goalbit.view.activities.CountDownActivity
import java.time.LocalDate

class HabitViewHolder(private val bind: HabitViewBinding, private val listener: OnHabitListener) :
    RecyclerView.ViewHolder(bind.root) {

    private val context: Context = itemView.context

    private lateinit var newDuration: DurationTimer
    private lateinit var selectedDate: LocalDate
    private var habitId: String = ""
    private lateinit var habitViewModel: HabitViewModel
    private val today = LocalDate.now()
    private lateinit var auth: FirebaseAuth

    private fun setTextDurationAndTime(durationTimer: DurationTimer) {
        bind.textDuration.text = DurationTimeUtil.formatStringTime(durationTimer)

    }

    private fun updateDailyActivity(done: Boolean) {
        bind.checkBoxDone.isChecked = done
        habitViewModel.updateDailyActivity(habitId, selectedDate, done)
    }

    private fun bindCHeckbox(habit: Habit, dailyActivity: DailyActivity?) {
        if (habit.goal == GoalType.Off) {

            if (dailyActivity == null) {
                bind.checkBoxDone.isChecked = false
                ColorUtils.setHabitUnchecked(bind.cardView, itemView.context)
            }

            if (dailyActivity != null && dailyActivity.done == true) {
                bind.checkBoxDone.isChecked = true
                ColorUtils.setHabitChecked(bind.cardView, itemView.context)
            }

            if (dailyActivity != null && dailyActivity.done == false) {
                bind.checkBoxDone.isChecked = false
                ColorUtils.setHabitUnchecked(bind.cardView, itemView.context)
            }
        }

        if (habit.goal == GoalType.Duration) {



            bind.textDuration.visibility = View.VISIBLE

            if (dailyActivity == null) {
                bind.checkBoxDone.isChecked = false
                newDuration = habit.duration!!
                ColorUtils.setHabitUnchecked(bind.cardView, itemView.context)
            }

            if (dailyActivity?.done == false) {
                bind.checkBoxDone.isChecked = false
                newDuration = dailyActivity.duration
                ColorUtils.setHabitUnchecked(bind.cardView, itemView.context)
            }

            if (dailyActivity?.done == true) {
                bind.checkBoxDone.isChecked = true
                newDuration = dailyActivity.duration
                ColorUtils.setHabitChecked(bind.cardView, itemView.context)
            }

            setTextDurationAndTime(newDuration)
        }
    }

    fun bind(selectedDate: LocalDate, habitWithDailyActivity: Pair<Habit, DailyActivity?>) {

        auth = FirebaseAuth.getInstance()

        habitViewModel = HabitViewModel()

        this.selectedDate = selectedDate
        val habit = habitWithDailyActivity.first
        val dailyActivity = habitWithDailyActivity!!.second

        this.habitId = habit.id!!
        bind.textName.text = habit.name
        bind.textName.setOnClickListener {
            listener.onClick(habit.id!!, habit.name)
        }

        if (selectedDate > today) {
            bind.checkBoxDone.visibility = View.INVISIBLE
            bind.checkBoxDone.isClickable = false
            bind.checkBoxDone.isChecked = false
        } else {
            bind.checkBoxDone.visibility = View.VISIBLE
            bind.checkBoxDone.isClickable = true
            bind.checkBoxDone.isChecked = true
        }

        val iconPack = (context.applicationContext as App).iconPack

        bind.imageIcon.setImageDrawable(iconPack?.getIcon(habit.icon)?.drawable)

        bind.editHabit.setOnClickListener {
            listener.onEdit(habit.id!!)
        }

        bindCHeckbox(habit, dailyActivity)

        bind.checkBoxDone.setOnClickListener {

            if (habit.goal == GoalType.Off) {
                if (dailyActivity == null) {
                    updateDailyActivity(true)
                } else if (dailyActivity != null && dailyActivity.done == true) {
                    updateDailyActivity(false)
                    ColorUtils.setHabitUnchecked(bind.cardView, itemView.context)
                } else if (dailyActivity != null && dailyActivity.done == false) {
                    updateDailyActivity(true)
                    ColorUtils.setHabitChecked(bind.cardView, itemView.context)
                }
            }

            if (habit.goal == GoalType.Duration) {

                if (dailyActivity?.done == false || dailyActivity == null) {
                    bind.checkBoxDone.visibility = View.INVISIBLE

                    val intent = Intent(context, CountDownActivity::class.java)

                    val bundle = Bundle()
                    bundle.putString(DataBaseConstants.HABIT.NAME, habit.name)
                    bundle.putString(DataBaseConstants.HABIT.ID, habit.id)
                    bundle.putString(DataBaseConstants.HABIT.SELECTED_DATE, DateFormatUtils.formatDate(selectedDate))


                    val hour = newDuration.hour
                    val minute = newDuration.minute
                    val second = newDuration.second
                    if (hour != null) {
                        bundle.putInt(DataBaseConstants.HABIT.HOUR, hour)
                    }
                    if (minute != null) {
                        bundle.putInt(DataBaseConstants.HABIT.MINUTE, minute)
                    }
                    if (second != null) {
                        bundle.putInt(DataBaseConstants.HABIT.SECOND,second)
                    }

                    intent.putExtras(bundle)

                    context.startActivity(intent)

                } else if (dailyActivity?.done == true) {
                    updateDailyActivity(false)
                }
            }}
        }

}