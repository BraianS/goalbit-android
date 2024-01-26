package dev.braian.goalbit.utils

import android.view.View
import android.widget.LinearLayout
import androidx.cardview.widget.CardView

object ViewAnimationUtil {

    fun animateAndMakeVisible(cardView: CardView) {

        cardView.visibility = View.VISIBLE
        cardView.translationX = cardView.width.toFloat()
        cardView.animate()
            .translationX(0f)
            .setDuration(300)
            .setListener(null)
            .start()
    }

    fun animateAndMakeInvisible(cardView: CardView) {
        cardView.visibility = View.INVISIBLE
        cardView.translationX = cardView.width.toFloat()
        cardView.animate().translationX(0f).setDuration(300)
            .setListener(null).start()
    }
}