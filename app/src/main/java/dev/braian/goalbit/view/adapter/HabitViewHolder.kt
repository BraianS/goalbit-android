package dev.braian.goalbit.view.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import dev.braian.goalbit.R
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
import dev.braian.goalbit.utils.ViewAnimationUtil
import java.time.LocalDate

class HabitViewHolder(private val bind: HabitViewBinding, private val listener: OnHabitListener) :
    RecyclerView.ViewHolder(bind.root) {

    private var timerRunning: Boolean = false
    var counterDownTimer: CountDownTimer? = null
    private val context: Context = itemView.context
    private var millisInFuture: Long = 0
    private lateinit var newDuration: DurationTimer
    private lateinit var selectedDate: LocalDate
    private var habitId: String = ""
    private lateinit var habitViewModel: HabitViewModel
    private val today = LocalDate.now()
    private lateinit var auth: FirebaseAuth

    private fun setTextDurationAndTime(durationTimer: DurationTimer) {
        bind.textDuration.text = DurationTimeUtil.formatStringTime(durationTimer)
        bind.textTimerDuration.text = DurationTimeUtil.formatStringTime(durationTimer)
    }

    private fun updateDailyActivity(done: Boolean) {
        bind.checkBoxDone.isChecked = done
        habitViewModel.updateDailyActivity(habitId, selectedDate, done)

    }

    private fun bindCHeckbox(habit:Habit, dailyActivity:DailyActivity?){
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

            timerRunning = false
            bind.countDownButton.text = context.getString(R.string.start)

            countDownTimer()
            bind.textDuration.visibility = View.VISIBLE

            if (dailyActivity == null || dailyActivity?.done == false) {
                bind.checkBoxDone.isChecked = false
                newDuration = habit.duration!!
                ColorUtils.setHabitUnchecked(bind.cardView, itemView.context)
            }

            if (dailyActivity!= null || dailyActivity?.done == true) {
                bind.checkBoxDone.isChecked = true
                newDuration = dailyActivity.duration
                ViewAnimationUtil.animateAndMakeInvisible(bind.cardBackDuration)
                ColorUtils.setHabitChecked(bind.cardView, itemView.context)
            }

            millisInFuture =
                (newDuration.hour!!.toLong() * 60 * 60 + newDuration.minute!!.toLong() * 60 + newDuration.second!!.toLong()) * 1000

            setTextDurationAndTime(newDuration)
        }
    }

    fun bind(selectedDate: LocalDate, habitWithDailyActivity: Pair<Habit, DailyActivity?>) {

        auth = FirebaseAuth.getInstance()

        habitViewModel = HabitViewModel()

        ViewAnimationUtil.animateAndMakeInvisible(bind.cardBackDuration)

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

        bind.checkBowToggleTimer.visibility = View.INVISIBLE
        bind.checkBowToggleTimer.isChecked = false

        val iconPack = (context.applicationContext as App).iconPack

        bind.imageIcon.setImageDrawable(iconPack?.getIcon(habit.icon)?.drawable)

        bind.editHabit.setOnClickListener {
            listener.onEdit(habit.id!!)
        }

        bindCHeckbox(habit,dailyActivity)

        bind.checkBoxDone.setOnClickListener {

            if (habit.goal == GoalType.Off) {
                if (dailyActivity == null) {
                    updateDailyActivity(true)
                }
               else if (dailyActivity != null && dailyActivity.done == true) {
                    updateDailyActivity(false)
                    ColorUtils.setHabitUnchecked(bind.cardView, itemView.context)
                } else if (dailyActivity != null && dailyActivity.done == false) {
                    updateDailyActivity(true)
                    ColorUtils.setHabitChecked(bind.cardView, itemView.context)
                }
            }



            if(habit.goal == GoalType.Duration){
                if ( dailyActivity?.done == false) {
                    ViewAnimationUtil.animateAndMakeVisible(bind.cardBackDuration)
                }

                else if (dailyActivity?.done == true) {
                    habitViewModel.saveDailyDuration(habitId, selectedDate, habit.duration!!)
                }

                else if (bind.checkBoxDone.isChecked ) {

                    bind.checkBoxDone.visibility = View.INVISIBLE
                    bind.checkBowToggleTimer.visibility = View.VISIBLE

                    newDuration = habit.duration!!
                    ViewAnimationUtil.animateAndMakeVisible(bind.cardBackDuration)

                } else if (!bind.checkBoxDone.isChecked && dailyActivity != null) {

                    bind.checkBoxDone.isChecked = false
                    ColorUtils.setHabitUnchecked(bind.cardView, itemView.context)

                    habitViewModel.finishDailyActivity(
                        habit.id!!,
                        selectedDate,
                        habit.duration!!,
                        false
                    )
                }
            }

            bind.finalizeTimerButton.setOnClickListener {
                counterDownTimer?.cancel()
                bind.countDownButton.text = context.getString(R.string.start)
                timerRunning = false
                updateTimer()

                habitViewModel.finishDailyActivity(
                    habit.id!!,
                    selectedDate,
                    DurationTimer(0, 0, 0), true
                )

                bind.checkBoxDone.visibility = View.VISIBLE
                ViewAnimationUtil.animateAndMakeInvisible(bind.cardBackDuration)
                ColorUtils.setHabitChecked(bind.cardView, itemView.context)
            }

            bind.checkBowToggleTimer.setOnClickListener {
                if (bind.checkBowToggleTimer.isChecked) {
                    bind.checkBowToggleTimer.visibility = View.INVISIBLE
                    bind.checkBoxDone.isChecked = false
                    bind.checkBoxDone.visibility = View.VISIBLE

                    bind.cardBackDuration.animate()
                        .translationX(bind.cardBackDuration.width.toFloat())
                        .setDuration(300)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                bind.cardBackDuration.visibility = View.INVISIBLE
                            }
                        })
                    bind.checkBowToggleTimer.isChecked = false
                }
            }

            bind.countDownButton.setOnClickListener {
                if (timerRunning) {
                    counterDownTimer?.cancel()
                    bind.countDownButton.text = context.getString(R.string.start)
                    timerRunning = false
                } else {
                    start()
                    bind.countDownButton.setOnLongClickListener { it ->
                        habitViewModel.saveDailyDuration(habitId, selectedDate, newDuration)
                        bind.countDownButton.text = context.getString(R.string.save)
                        timerRunning = false
                        bind.checkBowToggleTimer.visibility = View.INVISIBLE
                        bind.checkBowToggleTimer.isChecked = false
                        bind.checkBoxDone.visibility = View.VISIBLE
                        bind.checkBoxDone.isChecked = false
                        true
                    }
                }
            }
        }
    }

    private fun countDownTimer() {
        counterDownTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(l: Long) {
                millisInFuture = l
            }

            override fun onFinish() {
                habitViewModel.saveDailyDuration(habitId, selectedDate, newDuration)
            }
        }
    }

    private fun start() {
        counterDownTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(l: Long) {
                millisInFuture = l
                updateTimer()
            }

            override fun onFinish() {
                updateTimer()
            }
        }.start()

        bind.countDownButton.text = context.getString(R.string.pause)
        timerRunning = true
    }

    fun updateTimer() {
        Log.d(
            DataBaseConstants.TAG.HABIT_VIEW_HOLDER,
            "timerRunning: $timerRunning millisInFuture:  $millisInFuture"
        )

        val remainingMillis = millisInFuture

        val hours = remainingMillis / (60 * 60 * 1000)
        val minutes = remainingMillis % (60 * 60 * 1000) / (60 * 1000)
        val seconds = remainingMillis % (60 * 1000) / 1000

        newDuration.addTimeStamp(hours.toInt(), minutes.toInt(), seconds.toInt())
        bind.textTimerDuration.text = DurationTimeUtil.formatTime(hours, minutes, seconds)
    }
}