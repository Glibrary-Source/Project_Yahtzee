package com.twproject.projectyahtzee.view.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.databinding.FragmentCreateRoomBinding
import com.twproject.projectyahtzee.vbutils.ButtonAnimation
import com.twproject.projectyahtzee.vbutils.onThrottleClick
import com.twproject.projectyahtzee.view.main.datamodel.PlayerData
import com.twproject.projectyahtzee.view.main.datamodel.UserProfileData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class FragmentCreateRoom : Fragment() {

    private lateinit var binding: FragmentCreateRoomBinding
    private lateinit var mContext: Context
    private lateinit var activity: MainActivity
    private lateinit var navController: NavController
    private lateinit var currentUid: String
    private var userProfile = UserProfileData()
    private var playerData = PlayerData()
    private var animScale = 0.90F
    private var playerNum = 2

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
        getUserProfile()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateRoomBinding.inflate(inflater)

        navController = findNavController()
        setSpinnerView()

        binding.frameBtnCreate.onThrottleClick {
            ButtonAnimation().startAnimationCreate(it, animScale)
            roomNameBlankCheck()
        }

        binding.frameBtnCancel.onThrottleClick {
            ButtonAnimation().startAnimationCreate(it, animScale)

            val action = FragmentCreateRoomDirections.actionFragmentCreateRoomToFragmentMenu()
            CoroutineScope(Main).launch {
                delay(300)
                navController.navigate(action)
            }
        }

        return binding.root
    }

    private fun roomNameBlankCheck() {
        val roomName = binding.editRoomName.text.toString()
        if (roomName.isBlank()) {
            Toast.makeText(mContext, "Write room name", Toast.LENGTH_SHORT).show()
        } else {
            createRoom(roomName, playerNum)
        }
    }

    private fun createRoom(
        name: String,
        num: Int
    ) {
        val roomDocId = UUID.randomUUID().toString()
        val roomData = mutableMapOf(
            "room_doc_id" to roomDocId,
            "room_name" to name,
            "room_max_num" to num,
            "room_manager" to currentUid,
            "room_state" to "wait",
            "room_player_turn" to 1,
            "room_number" to 0,
            "room_dice_roll" to false,
            "room_player_list" to listOf(currentUid),
            "player_data" to mapOf(
                currentUid to playerData
            )
        )

        db.collection("room_list").get().addOnSuccessListener { querySnapshot ->
            val roomRef = querySnapshot.size()
            db.runTransaction { transaction ->

                roomData["room_number"] = roomRef + 1

                transaction.set(
                    db.collection("room_list").document(roomDocId),
                    roomData,
                    SetOptions.merge()
                )

            }.addOnSuccessListener {
                // 트랜잭션 및 데이터 설정이 성공하면, 이제 레디 룸으로 이동
                moveReadyRoom(roomDocId)
            }.addOnFailureListener { exception ->
                // 트랜잭션 실패를 처리하는 코드 (필요한 경우)
                Log.e("createRoom", "Transaction failed", exception)
            }
        }
    }

    private fun moveReadyRoom(roomDocId: String) {
        val action = FragmentCreateRoomDirections.actionFragmentCreateRoomToFragmentReadyRoom(
            roomDocId
        )
        navController.navigate(action)
    }

    private fun setSpinnerView() {
        val userSpinner = binding.spinnerUserNum
        userSpinner.adapter = ArrayAdapter.createFromResource(
            mContext,
            R.array.itemList,
            R.layout.item_spinner_player
        )
        userSpinner.onItemSelectedListener = setSpinnerListener()
    }

    private fun setSpinnerListener(): AdapterView.OnItemSelectedListener {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> { playerNum = 2 }
                    1 -> { playerNum = 3 }
                    2 -> { playerNum = 4 }
                    3 -> { playerNum = 5 }
                    4 -> { playerNum = 6 }
                    5 -> { playerNum = 7 }
                    6 -> { playerNum = 8 }
                }
            }
        }
        return listener
    }

    private fun getUserProfile() {
        CoroutineScope(IO).launch {
            db.collection("user_db").document(currentUid)
                .get()
                .addOnSuccessListener {
                    val data = it.data
                    if (data == null) {
                        Log.d("DataTest", "noExiste")
                    } else {
                        userProfile = it.toObject(UserProfileData::class.java)!!
                        setPlayerProfile()
                    }
                }
        }
    }

    private fun setPlayerProfile() {
        playerData = PlayerData(
            userProfile.change_counter,
            userProfile.email,
            userProfile.nickname,
            userProfile.score,
            userProfile.tier,
            userProfile.uid,
            "wait",
            1
        )
    }

}