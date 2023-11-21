package com.twproject.projectyahtzee.view.login.util

import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.twproject.projectyahtzee.MyGlobals
import com.twproject.projectyahtzee.view.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleLoginModule {

    private val db = Firebase.firestore

    fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        activity: MainActivity,
        auth: FirebaseAuth,
        navController: NavController,
        action: NavDirections
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        MyGlobals.instance!!.userLogin = 1
                        val currentUser = auth.currentUser
                        setUserDb(currentUser!!.uid, currentUser.email!!.toString())
                    }
                    navController.navigate(action)
                } else {
                    Toast.makeText(activity,"인터넷 연결을 확인해주세요",Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setUserDb(uid: String, email: String) {
        val setUserData = hashMapOf(
            "uid" to uid,
            "email" to email,
            "nickname" to "nickname",
            "change_counter" to false,
            "score" to 0,
            "tier" to "stone"
        )
        db.collection("user_db").document(uid)
            .get()
            .addOnSuccessListener { user ->
                if(!user.exists()) {
                    db.collection("user_db")
                        .document(uid)
                        .set(setUserData)
                }
            }

    }


}