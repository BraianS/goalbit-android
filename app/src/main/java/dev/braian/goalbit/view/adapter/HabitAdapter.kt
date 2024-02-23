package dev.braian.goalbit.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.braian.goalbit.databinding.HabitViewBinding
import dev.braian.goalbit.view.listener.OnHabitListener
import dev.braian.goalbit.model.DailyActivity
import dev.braian.goalbit.model.Habit
import java.time.LocalDate

class HabitAdapter : RecyclerView.Adapter<HabitViewHolder>() {

    private var habitsWithDailyActivity: MutableList<Pair<Habit, DailyActivity?>> = mutableListOf()

    private var selectedDate = LocalDate.now()

    private lateinit var listener: OnHabitListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val item = HabitViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(item, listener)
    }

    override fun getItemCount(): Int = habitsWithDailyActivity .size

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habitWithDailyActivity = habitsWithDailyActivity[position]
        holder.bind(selectedDate, habitWithDailyActivity)
    }

    fun addList(selectedDate:LocalDate, habitsWithDailyActivities: MutableList<Pair<Habit, DailyActivity?>>) {
        this.selectedDate = selectedDate
        habitsWithDailyActivity  = habitsWithDailyActivities
        notifyDataSetChanged()
        Log.i("AdapterUpdate", "Updated habitsWithDailyActivity with ${habitsWithDailyActivities.size} items")
    }

    fun attachListener( habitListener: OnHabitListener) {
        listener = habitListener
    }

}