package com.twproject.banyeomiji.vbutility

import android.content.Context
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity

object BackPressCallBackManager {

    fun setBackPressCallBack(activity: FragmentActivity, context: Context) : OnBackPressedCallback {
        var backPressTime: Long = 0
        return object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressTime + 3000 > System.currentTimeMillis()) {
                    activity.finish()
                } else {
                    Toast.makeText(context , "한번 더 뒤로가기 버튼을 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
                }
                backPressTime = System.currentTimeMillis()
            }
        }
    }

    fun setBackPressCallBackNoting() : OnBackPressedCallback {
        return object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        }
    }

}