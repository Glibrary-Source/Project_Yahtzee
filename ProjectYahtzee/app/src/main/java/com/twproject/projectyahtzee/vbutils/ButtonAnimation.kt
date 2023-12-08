package com.twproject.projectyahtzee.vbutils

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View

class ButtonAnimation {

    fun startAnimation(buttonView: View, scale: Float) {
        val initialWidth = buttonView.width
        val initialHeight = buttonView.height

        val animationDuration = 300L  // You can adjust this value as needed

        val widthAnimator =
            ValueAnimator.ofInt(buttonView.width, (initialWidth * scale).toInt())
        val heightAnimator =
            ValueAnimator.ofInt(buttonView.height, (initialHeight * scale).toInt())

        widthAnimator.duration = animationDuration
        heightAnimator.duration = animationDuration

        widthAnimator.addUpdateListener { animation ->
            val newWidth = animation.animatedValue as Int
            val params = buttonView.layoutParams
            params.width = newWidth
            buttonView.layoutParams = params
        }

        heightAnimator.addUpdateListener { animation ->
            val newHeight = animation.animatedValue as Int
            val params = buttonView.layoutParams
            params.height = newHeight
            buttonView.layoutParams = params
        }

        widthAnimator.start()
        heightAnimator.start()

        widthAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {

                val returnWidthAnimator =
                    ValueAnimator.ofInt(buttonView.width, initialWidth)
                val returnHeightAnimator =
                    ValueAnimator.ofInt(buttonView.height, initialHeight)

                returnWidthAnimator.duration = animationDuration
                returnHeightAnimator.duration = animationDuration

                returnWidthAnimator.addUpdateListener { animation1 ->
                    val newWidth = animation1.animatedValue as Int
                    val params = buttonView.layoutParams
                    params.width = newWidth
                    buttonView.layoutParams = params
                }

                returnHeightAnimator.addUpdateListener { animation2 ->
                    val newHeight = animation2.animatedValue as Int
                    val params = buttonView.layoutParams
                    params.height = newHeight
                    buttonView.layoutParams = params
                }

                returnWidthAnimator.start()
                returnHeightAnimator.start()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    fun startAnimationCreate(buttonView: View, scale: Float) {
        val initialHeight = buttonView.height

        val animationDuration = 300L

        val heightAnimator =
            ValueAnimator.ofInt(buttonView.height, (initialHeight * scale).toInt())

        heightAnimator.duration = animationDuration

        heightAnimator.addUpdateListener { animation ->
            val newHeight = animation.animatedValue as Int
            val params = buttonView.layoutParams
            params.height = newHeight
            buttonView.layoutParams = params
        }

        heightAnimator.start()

        heightAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {

                val returnHeightAnimator =
                    ValueAnimator.ofInt(buttonView.height, initialHeight)

                returnHeightAnimator.duration = animationDuration


                returnHeightAnimator.addUpdateListener { animation2 ->
                    val newHeight = animation2.animatedValue as Int
                    val params = buttonView.layoutParams
                    params.height = newHeight
                    buttonView.layoutParams = params
                }

                returnHeightAnimator.start()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

}