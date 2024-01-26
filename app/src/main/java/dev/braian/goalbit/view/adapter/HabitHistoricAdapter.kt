package dev.braian.goalbit.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.braian.goalbit.databinding.HabitViewHistoricBinding
import dev.braian.goalbit.model.HabitWithYearStreak

class HabitHistoricAdapter: RecyclerView.Adapter<HabitHistoricViewHolder>() {

    private var habits = listOf<HabitWithYearStreak>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitHistoricViewHolder {
        val item = HabitViewHistoricBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitHistoricViewHolder(item)
    }

    override fun getItemCount(): Int  = habits.size

    override fun onBindViewHolder(holder: HabitHistoricViewHolder, position: Int) {
       val habit = habits[position]
        holder.bind(habit)
    }

    fun addList(habits:List<HabitWithYearStreak>){
        this.habits = habits
        notifyDataSetChanged()
    }
}