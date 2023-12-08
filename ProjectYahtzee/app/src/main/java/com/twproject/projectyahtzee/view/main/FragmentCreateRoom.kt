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
    private var playRoomSize = 0
    var db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        activity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomListSize()
        currentUid = auth.currentUser!!.uid
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

            val roomName = binding.editRoomName.text.toString()

            if(roomNameBlankCheck(roomName)) {
                Toast.makeText(mContext, "Write room name", Toast.LENGTH_SHORT).show()
            } else {
                createRoom(roomName, playerNum)
            }
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

    private fun setSpinnerView() {
        val userSpinner = binding.spinnerUserNum
        userSpinner.adapter = ArrayAdapter.createFromResource(mContext, R.array.itemList, R.layout.item_spinner_player)
        userSpinner.onItemSelectedListener = setSpinnerListener()
    }

    private fun setSpinnerListener() : AdapterView.OnItemSelectedListener {
        val listener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> { playerNum = 2 }
                    1 -> { playerNum = 3 }
                    2 -> { playerNum = 4 }
                    3 -> { playerNum = 5 }
                    4 -> { playerNum = 6 }
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

    private fun roomNameBlankCheck(
        roomName: String
    ): Boolean {
        return roomName == ""
    }

    private fun roomListSize() {
        CoroutineScope(IO).launch{
            db.collection("room_list")
                .addSnapshotListener { value, _ ->
                    playRoomSize =  value!!.documents.size + 1
                }
        }
    }
    private fun createRoom(
        name: String,
        num: Int
    ) {
        val roomDocId = UUID.randomUUID().toString()
        val roomData = mapOf(
            "room_doc_id" to roomDocId,
            "room_name" to name,
            "room_max_num" to num,
            "room_manager" to currentUid,
            "room_state" to "wait",
            "room_player_turn" to 1,
            "room_number" to playRoomSize,
            "room_dice_roll" to false,
            "player_data" to mapOf(
                currentUid to playerData
            )
        )

        db.collection("room_list").document(roomDocId)
            .set(roomData)
            .addOnSuccessListener {
                val action = FragmentCreateRoomDirections.actionFragmentCreateRoomToFragmentReadyRoom(roomDocId)
                navController.navigate(action)
            }
    }

}