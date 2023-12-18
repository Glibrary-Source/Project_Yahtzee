package com.twproject.projectyahtzee.view.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.twproject.banyeomiji.vbutility.BackPressCallBackManager
import com.twproject.projectyahtzee.databinding.FragmentReadyRoomBinding
import com.twproject.projectyahtzee.view.main.adapter.PlayerListAdapter
import com.twproject.projectyahtzee.view.main.datamodel.RoomData

class FragmentReadyRoom : Fragment() {

    private lateinit var binding: FragmentReadyRoomBinding
    private lateinit var mContext: Context
    private lateinit var roomDocId: String
    private lateinit var rcView: RecyclerView
    private lateinit var callback: OnBackPressedCallback
    private lateinit var activity: FragmentActivity

    private val roomData by navArgs<FragmentReadyRoomArgs>()
    private var currentUid = FirebaseAuth.getInstance().uid.toString()
    val db = Firebase.firestore

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
        activity = context as MainActivity

        callback = BackPressCallBackManager.setBackPressCallBackNoting()
        activity.onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomDocId = roomData.RoomDocId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadyRoomBinding.inflate(inflater)

        roomRefreshListener()

        binding.linearBtnReadyRoom.setOnClickListener {
            readyPlayer()
        }

        binding.linearBtnExitRoom.setOnClickListener {
            exitPlayer()
        }

        rcView = binding.rcWaitRoomPlayer
        rcView.setHasFixedSize(true)

        return binding.root
    }

    private fun roomRefreshListener() {
        db.collection("room_list").document(roomDocId)
            .addSnapshotListener { value, _ ->
                if (value == null) {
                    Log.d("testRoom", "data failed")
                } else {
                    try {
                        val roomData = value.toObject(RoomData::class.java)!!
                        val playerDataList = roomData.player_data

                        setRoomName(roomData)
                        setPlayerNum(roomData, playerDataList)
                        setPlayerList(playerDataList)
                        checkAllReady(playerDataList, roomData)
                        roomStartState(roomData)
                        roomManagerSet(roomData)
                        setRoomTurn(roomData)
                    } catch (_: Exception) {
                    }
                }
            }
    }

    private fun setRoomName(roomData: RoomData) {
        binding.textWaitRoomName.text = roomData.room_name
    }

    @SuppressLint("SetTextI18n")
    private fun setPlayerNum(roomData: RoomData, playerDataList: Map<String, Any>) {
        binding.textWaitRoomPlayerNum.text =
            "${playerDataList.size}/${roomData.room_max_num}"
    }

    private fun setPlayerList(playerDataList: Map<String, Any>) {
        rcView.adapter = PlayerListAdapter(playerDataList)
    }

    private fun checkAllReady(playerDataList: Map<String, Any>, roomData: RoomData) {
        val readyList = mutableListOf<String>()
        for ((_, value) in playerDataList) {
            val player = value as Map<String, Any>
            readyList.add(player["waitstate"].toString())
        }
        if (!readyList.contains("wait") && currentUid == roomData.room_manager && readyList.size > 1) {
            binding.frameBtnStart.visibility = View.VISIBLE
            setStartBtn()
        }
    }

    private fun readyPlayer() {
        val user = mapOf(
            "player_data" to mapOf(
                currentUid to mapOf(
                    "waitstate" to "ready"
                )
            )
        )
        db.collection("room_list").document(roomDocId)
            .set(user, SetOptions.merge())
    }

    private fun setStartBtn() {
        val start = mapOf("room_state" to "start")
        binding.frameBtnStart.setOnClickListener {
            db.collection("room_list").document(roomDocId)
                .update(start)
        }
    }

    private fun roomStartState(roomData: RoomData) {
        if (roomData.room_state == "start") {
            val action =
                FragmentReadyRoomDirections.actionFragmentReadyRoomToFragmentPlayRoom(roomDocId)
            findNavController().navigate(action)
        }
    }

    private fun exitPlayer() {
        val roomRef = db.collection("room_list").document(roomDocId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(roomRef)

            val playerData = snapshot["player_data"] as? MutableMap<String, Any>
            val roomList = snapshot["room_player_list"] as? MutableList<String>

            roomList?.let {
                it.remove(currentUid)
                transaction.update(roomRef, "room_player_list", it)
            }

            playerData?.let {
                it.remove(currentUid)

                if (it.isEmpty()) {
                    // 방이 비어 있으면 삭제
                    transaction.delete(roomRef)
                } else {
                    // player_data 필드 업데이트
                    transaction.update(roomRef, "player_data", it)
                }
            }

            null
        }.addOnSuccessListener {
            val action = FragmentReadyRoomDirections.actionFragmentReadyRoomToFragmentRoomList()
            findNavController().navigate(action)
        }.addOnFailureListener { e ->
            // 실패 처리
            Log.e("exitPlayer", "트랜잭션 실패: $e")
        }
    }

    private fun roomManagerSet(roomData: RoomData) {
        if (roomData.room_player_list[0] == currentUid) {
            val manager = mapOf(
                "room_manager" to currentUid
            )
            db.collection("room_list").document(roomDocId)
                .update(manager)
        }
    }

    private fun setRoomTurn(roomData: RoomData) {
        if (roomData.room_player_list.contains(currentUid)) {
            val turn = roomData.room_player_list.indexOf(currentUid) + 1
            val player = mapOf(
                "player_data" to mapOf(
                    currentUid to mapOf(
                        "playerturn" to turn
                    )
                )
            )
            db.collection("room_list").document(roomDocId)
                .set(player, SetOptions.merge())
        }
    }

}