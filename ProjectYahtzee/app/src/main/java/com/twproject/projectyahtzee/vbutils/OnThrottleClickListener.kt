package com.twproject.projectyahtzee.vbutils

import android.view.View

class OnThrottleClickListener(
    private val clickListener: View.OnClickListener,
    private val interval: Long = 1000
) : View.OnClickListener {

    private var clickable = true

    override fun onClick(p0: View?) {
        if (clickable) {
            clickable = false
            p0?.run {
                postDelayed({
                    clickable = true
                }, interval)
                clickListener.onClick(p0)
            }
        }
    }
}

fun View.onThrottleClick(action: (v: View) -> Unit) {
    val listener = View.OnClickListener { action(it) }
    setOnClickListener(OnThrottleClickListener(listener))
}

fun View.onThrottleClick(action: (v: View) -> Unit, interval: Long) {
    val listener = View.OnClickListener { action(it) }
    setOnClickListener(OnThrottleClickListener(listener, interval))
}