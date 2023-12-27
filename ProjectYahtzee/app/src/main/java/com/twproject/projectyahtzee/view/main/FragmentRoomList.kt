package com.twproject.projectyahtzee.view.main

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.twproject.projectyahtzee.databinding.FragmentRoomListBinding
import com.twproject.projectyahtzee.view.main.adapter.RoomLIstAdapter
import com.twproject.projectyahtzee.view.main.datamodel.PlayerData
import com.twproject.projectyahtzee.view.main.datamodel.RoomData
import com.twproject.projectyahtzee.view.main.datamodel.UserProfileData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FragmentRoomList : Fragment() {

    private lateinit var binding: FragmentRoomListBinding
    private lateinit var rcView: RecyclerView
    private lateinit var mContext: Context
    private var userProfile = UserProfileData()
    private var playerData = PlayerData()
    private var roomDataList = mutableListOf<RoomData>()
    private val currentUid = FirebaseAuth.getInstance().uid.toString()
    val db = Firebase.firestore

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getUserProfile()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoomListBinding.inflate(inflater)

        rcView = binding.rcRoomList
        rcView.setHasFixedSize(true)

        getRoomList()

        binding.btnRefresh.bringToFront()
        binding.btnRefresh.setOnClickListener {
            binding.btnRefresh.playAnimation()
            getRoomList()
        }

        return binding.root
    }

    private fun getUserProfile() {
        CoroutineScope(IO).launch {
            db.collection("user_db").document(currentUid)
                .get()
                .addOnSuccessListener {
                    val data = it.data
                    if (data == null) {
                        Log.d("DataTest", "No Existed")
                    } else {
                        userProfile = it.toObject(UserProfileData::class.java)!!
                        setPlayerProfile()
                        getRoomList()
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
            0
        )
    }

    private fun getRoomList() {
        db.collection("room_list")
            .get()
            .addOnSuccessListener {
                roomDataList.clear()

                val roomItem = it.documents
                for (document in roomItem) {
                    val roomData = document.toObject(RoomData::class.java)
                    if (roomData != null) {
                        roomDataList.add(roomData)
                    }
                }
                roomDataList.sortBy { data -> data.room_number }
                rcView.adapter = RoomLIstAdapter(
                    roomDataList, db, playerData, currentUid, mContext
                )
                if(roomDataList.isEmpty()) {
                    binding.textNoRoom.invalidate()
                    binding.textNoRoom.visibility = View.VISIBLE
                } else {
                    binding.textNoRoom.visibility = View.GONE
                }
            }
    }
}