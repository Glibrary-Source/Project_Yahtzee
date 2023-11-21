package com.twproject.projectyahtzee.view.main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.twproject.projectyahtzee.MyGlobals
import com.twproject.projectyahtzee.vbutils.ButtonAnimation
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.databinding.FragmentMyProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class FragmentMyProfile : Fragment() {

    private lateinit var binding: FragmentMyProfileBinding
    private lateinit var mContext: Context
    private lateinit var activity: MainActivity
    private lateinit var currentUid: String
    private var nickNameCounter = false
    private val animScale = 0.90F
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        activity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{ currentUid = auth.currentUser!!.uid }
        catch (_: Exception) {}
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

            if(binding.editUserNickname.text.toString() == "") {
                Toast.makeText(mContext, "Check text", Toast.LENGTH_SHORT).show()
            } else {
                changeNickName(binding.editUserNickname.text.trim().toString())
            }
        }

        binding.btnLogout.setOnClickListener {
            ButtonAnimation().startAnimation(it, animScale)
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



        return binding.root
    }

    private fun getUser() {
        CoroutineScope(IO).launch {
            db.collection("user_db").document(currentUid)
                .get()
                .addOnSuccessListener {
                    val item = it.data
                    if(item == null) {
                        Toast.makeText(mContext, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        val nickname = item["nickname"].toString()
                        val email = item["email"].toString()
                        val score = item["score"].toString()
                        val tier = item["tier"].toString()
                        nickNameCounter = item["change_counter"] as Boolean
                        settingData(nickname, email, score, tier)
                    }
                }
        }
    }

    private fun settingData(
        nickname: String,
        email: String,
        score: String,
        tier: String
    ) {
        CoroutineScope(Main).launch {
            binding.textUserNickname.text = nickname
            binding.textUserEmail.text = email
            binding.textUserScore.text = score
            binding.textUserTier.text = tier
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
}