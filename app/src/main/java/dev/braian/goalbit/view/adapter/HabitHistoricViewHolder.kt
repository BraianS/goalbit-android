package dev.braian.goalbit.view.adapter

import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dev.braian.goalbit.R
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.databinding.HabitViewHistoricBinding
import dev.braian.goalbit.model.HabitWithYearStreak
import dev.braian.goalbit.view.viewholder.HabitViewModel

class HabitHistoricViewHolder(private val bind: HabitViewHistoricBinding) :
    RecyclerView.ViewHolder(bind.root) {

    private lateinit var habitViewModel: HabitViewModel

    fun bind(habit: HabitWithYearStreak) {
        habitViewModel = HabitViewModel()
        bind.textViewHabitName.text = habit.habit.name
        setUpLineChart(habit)
        Log.d(DataBaseConstants.TAG.HABIT_HISTORIC_VIEW_HOLDER, "Habit: ${habit}")
    }

    private fun setUpLineChart(habitWithYearStreak: HabitWithYearStreak) {

        bind.lineChart.description = null
        bind.lineChart.axisRight.setDrawLabels(true)

        val maxValue: Map.Entry<String, Float>? =
            habitWithYearStreak.yearStreak.entries.maxByOrNull { it.value }
        val yearStreak: Map<String, Float> = habitWithYearStreak.yearStreak
        val xValues = yearStreak.keys.toList().reversed()

        val xAxis = bind.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(xValues)
        xAxis.textSize = 15f

        xAxis.labelCount = 4
        xAxis.granularity = 1f

        val yAxis = bind.lineChart.axisLeft
        yAxis.textSize = 15f
        yAxis.axisMinimum = 0f
        if (maxValue != null) {
            yAxis.axisMaximum = maxValue?.value!! * 1.5f
        } else {
            yAxis.axisMaximum = 10f
        }

        yAxis.axisLineWidth = 2f
        yAxis.axisLineColor = Color.BLACK

        yAxis.labelCount = 6

        var xValue = 0f
        val entries1: ArrayList<Entry> = ArrayList<Entry>()

        val reversedEntries = habitWithYearStreak.yearStreak.entries.toList().reversed()

        for ((year, count) in reversedEntries) {
            Log.d(
                DataBaseConstants.TAG.HABIT_HISTORIC_VIEW_HOLDER,
                "Year: $year, Habits done: $count"
            )
            entries1.add(Entry(xValue, count))
            xValue++
        }

        val l = bind.lineChart.legend
        l.textColor = Color.BLACK
        l.textSize = 15f


        val dataSet1 = LineDataSet(entries1, "Completed habits")
        dataSet1.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet1.setDrawFilled(true)
        dataSet1.valueTextSize = 15f
        dataSet1.formSize = 15f
        dataSet1.fillColor = ContextCompat.getColor(itemView.context, R.color.secondary_color_alt)

        val lineData = LineData(dataSet1)

        bind.lineChart.data = lineData
        bind.lineChart.invalidate()
    }
}