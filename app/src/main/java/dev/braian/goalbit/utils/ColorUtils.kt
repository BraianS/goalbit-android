package dev.braian.goalbit.utils

import android.content.Context
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import dev.braian.goalbit.R

object ColorUtils {

    fun setHabitChecked(cardView: CardView, context: Context) {
        setCardBackgroundColor(cardView, context, R.color.habit_finished)
    }

    fun setHabitUnchecked(cardView: CardView, context: Context) {
        setCardBackgroundColor(cardView, context, R.color.primary_color)
    }

    private fun setCardBackgroundColor(cardView: CardView, context: Context, colorResId: Int) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, colorResId))
    }
}