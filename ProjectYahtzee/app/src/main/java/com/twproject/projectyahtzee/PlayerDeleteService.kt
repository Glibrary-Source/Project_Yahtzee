package com.twproject.projectyahtzee

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class PlayerDeleteService: Service() {

    private lateinit var docId: String
    private lateinit var currentUid: String
    private lateinit var userMap: Map<String, Any>
    private val db = Firebase.firestore

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("testService", "service start")

        if (intent != null && intent.hasExtra("DOC_ID_EXTRA")) {
            docId = intent.getStringExtra("DOC_ID_EXTRA") ?: ""
        }

        if (intent != null && intent.hasExtra("CURRENT_UID_EXTRA")) {
            currentUid = intent.getStringExtra("CURRENT_UID_EXTRA") ?: ""
        }

        userMap = mapOf(
            currentUid to true
        )

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        Log.d("testService", "service end")

        CoroutineScope(IO).launch{
            db.collection("room_exit_user").document(docId)
                .set(userMap, SetOptions.merge())
        }
    }
}