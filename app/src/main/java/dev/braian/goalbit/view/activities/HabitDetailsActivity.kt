package dev.braian.goalbit.view.activities


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import dev.braian.goalbit.R
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.databinding.FragmentCommonDetailBinding
import dev.braian.goalbit.model.DailyActivity
import dev.braian.goalbit.view.viewholder.HabitViewModel
import dev.braian.goalbit.utils.DailyActivityUtil
import dev.braian.goalbit.utils.PercentFormatter
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class HabitDetailsActivity : AppCompatActivity() {

    private lateinit var binding: FragmentCommonDetailBinding
    private lateinit var habitViewModel: HabitViewModel
    private lateinit var auth: FirebaseAuth
    private var today = LocalDate.now();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentCommonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        habitViewModel = HabitViewModel()

        val extras = intent.extras
        val id = extras?.getString(DataBaseConstants.HABIT.ID)
        val name = extras?.getString(DataBaseConstants.HABIT.NAME)
        binding.textName.text = name

        binding.buttonDelete.setOnClickListener {
            habitViewModel.deleteHabitById(id!!)
            Toast.makeText(applicationContext, "Habit deleted successfully", Toast.LENGTH_SHORT)
                .show()
            finish()
        }

        habitViewModel.getDailyActivitiesDescendingByHabitId2(id!!) {

            getCurrentStrike(it!!)

            binding.textViewFinished.text = DailyActivityUtil.sumDoneActivities(it)
            binding.calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_NONE


            binding.calendarView.addDecorator(object :
                com.prolificinteractive.materialcalendarview.DayViewDecorator {

                override fun shouldDecorate(day: CalendarDay): Boolean {

                    return it!!.any { activity ->
                        activity.isSameDay(day) && activity.done!!
                    }
                }

                override fun decorate(view: DayViewFacade) {
                    var highlightDrawable = R.drawable.highlight_middle
                    ContextCompat.getDrawable(applicationContext, highlightDrawable)
                        ?.let { view.setBackgroundDrawable(it) }
                }

            })

            val weekdayCount = DailyActivityUtil.countWeekdayOccurrences(it)
            Log.d(
                DataBaseConstants.TAG.HABIT_DETAILS_ACTIVITY,
                "Total activities: ${it?.size}"
            )
            Log.d(DataBaseConstants.TAG.HABIT_DETAILS_ACTIVITY, weekdayCount.toString())

            var entries = ArrayList<PieEntry>()
            for ((dayOfWeek, count) in weekdayCount) {
                Log.d(DataBaseConstants.TAG.HABIT_DETAILS_ACTIVITY, "$dayOfWeek: $count")
                entries.add(PieEntry(count.toFloat(), dayOfWeek))
            }

            var pieDataSet = PieDataSet(entries, "Weekdays")
            pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toMutableList()
            pieDataSet.valueTextColor = Color.BLACK
            pieDataSet.valueTextSize = 16f

            var pieData = PieData(pieDataSet)
            pieData.setValueFormatter(PercentFormatter())

            binding.barChart.data = pieData
            binding.barChart.description.isEnabled = false
            binding.barChart.centerText = "Weekdays"
            binding.barChart.animateY(2000)
        }
    }

    private fun getCurrentStrike(dailyActivities: List<DailyActivity>) {
        var currentStreak = 0
        for (activity in dailyActivities) {
            val activityDate = LocalDate.parse(activity.date, DateTimeFormatter.ISO_LOCAL_DATE)

            if (activity.done == true && activityDate.equals(today)) {
                currentStreak++
            } else {
                break
            }
            today = today.minusDays(1)
        }
        binding.textViewStrikeValue.text = currentStreak.toString()
        Log.d(DataBaseConstants.TAG.HABIT_DETAILS_ACTIVITY, "Current Streak: $currentStreak")
    }
}