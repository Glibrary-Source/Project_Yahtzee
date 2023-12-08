package com.twproject.projectyahtzee.view.main.util

import android.animation.ValueAnimator
import android.widget.ImageView
import com.twproject.projectyahtzee.view.main.data.DiceImageControl
import java.util.Random

class Dice(
    private val view: ImageView
) {

    private val random = Random()
    private val diceImageList = DiceImageControl().getDiceImage()
    val animation = playDiceAnimation()
    var diceValue = 0

    fun roll() {
        diceValue = random.nextInt(6) + 1
    }

    private fun playDiceAnimation(): ValueAnimator {
        return ValueAnimator.ofInt(1, 7).apply {
            duration = 500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                view.setImageResource(diceImageList[it.animatedValue as Int])
            }
        }
    }

    fun setImage() {
        view.setImageResource(diceImageList[diceValue])
    }

    fun setServerImage(num: Int) {
        view.setImageResource(diceImageList[num])
    }

}