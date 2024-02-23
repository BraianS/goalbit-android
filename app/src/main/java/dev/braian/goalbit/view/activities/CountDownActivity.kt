package dev.braian.goalbit.view.activities

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.braian.goalbit.R
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.databinding.ActivityCountDownTimerBinding
import dev.braian.goalbit.model.DurationTimer
import dev.braian.goalbit.utils.DurationTimeUtil
import dev.braian.goalbit.view.viewholder.HabitViewModel
import java.time.LocalDate

class CountDownActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountDownTimerBinding

    private lateinit var habitViewModel: HabitViewModel
    private var counterDownTimer: CountDownTimer? = null
    private var millisInFuture: Long = 0
    private var timerRunning: Boolean = false
    private lateinit var newDuration: DurationTimer

    private lateinit var habitId: String
    private lateinit var selectedDate: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCountDownTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        habitViewModel = HabitViewModel()
        val extras = intent.extras

        habitId = extras?.getString(DataBaseConstants.HABIT.ID)!!
        val habitName = extras.getString(DataBaseConstants.HABIT.NAME)
        val hour = extras.getInt(DataBaseConstants.HABIT.HOUR)
        val minute = extras.getInt(DataBaseConstants.HABIT.MINUTE)
        val second = extras.getInt(DataBaseConstants.HABIT.SECOND)

        val selectedDateString = extras.getString(DataBaseConstants.HABIT.SELECTED_DATE)
        selectedDate = LocalDate.parse(selectedDateString)

        binding.selectedDate.text = selectedDateString

        newDuration = DurationTimer(hour, minute, second)

        millisInFuture =
            (newDuration.hour!!.toLong() * 60 * 60 + newDuration.minute!!.toLong() * 60 + newDuration.second!!.toLong()) * 1000

        binding.habitName.text = habitName.toString()

        binding.time.text =
            DurationTimeUtil.formatTime(hour.toLong(), minute.toLong(), second.toLong())

        binding.buttonStart.setOnClickListener {
            if (timerRunning) {
                counterDownTimer?.cancel()

                timerRunning = false

                binding.buttonStart.text = applicationContext.getString(R.string.start)
                habitViewModel.saveDailyDuration(habitId, selectedDate, newDuration)

            } else {
                start()
                binding.buttonStart.text = applicationContext.getString(R.string.pause)
                habitViewModel.saveDailyDuration(habitId, selectedDate, newDuration)
            }
        }

        binding.buttonFinish.setOnClickListener {

            counterDownTimer?.cancel()

            timerRunning = false
            updateTimer()

            habitViewModel.finishDailyActivity(
                habitId,
                selectedDate,
                DurationTimer(0, 0, 0), true
            )
            finish()
        }

    }

    private fun start() {
        counterDownTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(l: Long) {
                millisInFuture = l
                updateTimer()
            }

            override fun onFinish() {
                habitViewModel.finishDailyActivity(habitId, selectedDate, newDuration, true)
                updateTimer()
                finish()
            }
        }.start()

        binding.buttonStart.text = applicationContext.getString(R.string.pause)
        timerRunning = true
    }

    fun updateTimer() {
        Log.e(
            DataBaseConstants.TAG.HABIT_VIEW_HOLDER,
            "timerRunning: $timerRunning millisInFuture:  $millisInFuture"
        )

        val remainingMillis = millisInFuture

        val hours = remainingMillis / (60 * 60 * 1000)
        val minutes = remainingMillis % (60 * 60 * 1000) / (60 * 1000)
        val seconds = remainingMillis % (60 * 1000) / 1000

        newDuration.addTimeStamp(hours.toInt(), minutes.toInt(), seconds.toInt())
        binding.time.text = DurationTimeUtil.formatTime(hours, minutes, seconds)
    }

}




