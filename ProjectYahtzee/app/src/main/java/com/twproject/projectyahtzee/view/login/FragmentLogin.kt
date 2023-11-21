package com.twproject.projectyahtzee.view.login

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.twproject.projectyahtzee.MyGlobals
import com.twproject.projectyahtzee.vbutils.ButtonAnimation
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.databinding.FragmentLoginBinding
import com.twproject.projectyahtzee.view.login.util.GoogleLoginModule
import com.twproject.projectyahtzee.view.main.MainActivity

class FragmentLogin : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var mContext: Context
    private lateinit var activity: MainActivity
    private lateinit var navController: NavController
    private lateinit var action: NavDirections

    private val animScale = 0.95F
    private val googleLoginModule = GoogleLoginModule()
    var auth = FirebaseAuth.getInstance()

    private var signInContracts = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                googleLoginModule.firebaseAuthWithGoogle(
                    account,
                    activity,
                    auth,
                    navController,
                    action
                )
            } catch (_: ApiException) {
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        activity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        action = FragmentLoginDirections.actionFragmentLoginToFragmentMenu()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)

        navController = findNavController()
        checkLogin()

        initializeGoogleSignIn()

        return binding.root
    }

    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(mContext, gso)

        binding.linearGoogleLogin.setOnClickListener {
            ButtonAnimation().startAnimation(it, animScale)

            val signInIntent = googleSignInClient.signInIntent
            signInContracts.launch(signInIntent)
        }
    }

    private fun checkLogin() {
        if(MyGlobals.instance!!.userLogin == 1) {
            navController.navigate(action)
        }
    }
}