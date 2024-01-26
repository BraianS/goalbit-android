package dev.braian.goalbit.view.listener

import dev.braian.goalbit.model.DurationTimer
import java.util.Date

interface OnHabitListener {

   fun onClick(id:String, name:String)

   fun onEdit(id:String)
}