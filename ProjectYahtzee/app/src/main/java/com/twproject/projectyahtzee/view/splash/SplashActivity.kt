package com.twproject.projectyahtzee.view.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.twproject.projectyahtzee.MyGlobals
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.view.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkLogin()

        val intent = Intent(this, MainActivity::class.java)

        CoroutineScope(Main).launch {
            delay(1500)
            startActivity(intent)
            finish()
        }
    }

    private fun checkLogin() {
        if(auth.currentUser == null) {
            MyGlobals.instance!!.userLogin = 0
        } else {
            MyGlobals.instance!!.userLogin = 1
        }
    }
}