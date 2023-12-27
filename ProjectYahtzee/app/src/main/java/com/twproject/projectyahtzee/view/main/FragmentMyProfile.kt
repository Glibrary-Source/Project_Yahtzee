package com.twproject.projectyahtzee.view.main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.twproject.projectyahtzee.MyGlobals
import com.twproject.projectyahtzee.vbutils.ButtonAnimation
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.databinding.FragmentMyProfileBinding
import com.twproject.projectyahtzee.vbutils.onThrottleClick
import com.twproject.projectyahtzee.view.main.datamodel.UserProfileData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FragmentMyProfile : Fragment() {

    private lateinit var binding: FragmentMyProfileBinding
    private lateinit var mContext: Context
    private lateinit var activity: MainActivity
    private lateinit var currentUid: String
    private var nickNameCounter = false
    private val animScale = 0.90F

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        activity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentUid = auth.currentUser?.uid ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProfileBinding.inflate(inflater)

        getUser()

        binding.btnNicknameModifier.setOnClickListener {
            ButtonAnimation().startAnimation(it, animScale)
            controlChangeNickName()
        }

        binding.btnNicknameModifierSelect.setOnClickListener {
            ButtonAnimation().startAnimation(it, animScale)
            if(binding.editUserNickname.text.toString().isBlank()) {
                Toast.makeText(mContext, "Check text", Toast.LENGTH_SHORT).show()
            } else  {
                changeNickName(binding.editUserNickname.text.trim().toString())
            }
        }

        binding.btnLogout.onThrottleClick {
            ButtonAnimation().startAnimation(it, animScale)
            signOutUser()
        }

        return binding.root
    }

    private fun getUser() {
        lifecycleScope.launch {
            try{
                withContext(IO) {
                    val snapshot = db.collection("user_db").document(currentUid)
                        .get()
                    snapshot.await()

                    if (snapshot.isSuccessful) {
                        val item = snapshot.result
                        if (item == null) {
                            withContext(Main) {
                                Toast.makeText(mContext, "Try again", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val profile = item.toObject(UserProfileData::class.java)!!
                            nickNameCounter = profile.change_counter
                            updateProfileUI(profile)
                        }
                    }
                }
            } catch (_:Exception){}
        }
    }

    private suspend fun updateProfileUI(
        profile: UserProfileData
    ) {
        withContext(Main) {
            binding.textUserNickname.text = profile.nickname
            binding.textUserEmail.text = profile.email
            binding.textUserScore.text = profile.score.toString()
            binding.textUserTier.text = profile.tier
        }
    }

    private fun changeNickName(nickname: String) {
        val data = mapOf(
            "nickname" to nickname,
            "change_counter" to true
        )
        db.collection("user_db").document(currentUid)
            .update(data)
            .addOnSuccessListener {
                controlChangeNickName()
                nickNameCounter = true
                binding.textUserNickname.text = nickname
            }
    }

    private fun controlChangeNickName() {
        if(nickNameCounter) {
            Toast.makeText(mContext, "Already change", Toast.LENGTH_SHORT).show()
        } else {
            if( binding.csChangeNickname.visibility == View.VISIBLE ) {
                binding.csChangeNickname.visibility = View.GONE
            } else {
                binding.csChangeNickname.visibility = View.VISIBLE
            }
        }
    }

    private fun signOutUser() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(mContext, gso)
        auth.signOut()
        googleSignInClient.signOut()

        val action = FragmentMyProfileDirections.actionFragmentMyProfileToFragmentLogin()
        findNavController().navigate(action)
        MyGlobals.instance!!.userLogin = 0
    }
}