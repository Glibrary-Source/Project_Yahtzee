package com.twproject.projectyahtzee.view.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.view.main.FragmentRoomListDirections
import com.twproject.projectyahtzee.view.main.datamodel.PlayerData
import com.twproject.projectyahtzee.view.main.datamodel.RoomData

class RoomLIstAdapter(
    private val roomData: MutableList<RoomData>,
    private val db: FirebaseFirestore,
    private val playerData: PlayerData,
    private val currentUid: String,
    private val context: Context
) : RecyclerView.Adapter<RoomLIstAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemCsView: ConstraintLayout = view.findViewById(R.id.cs_item_room_list)
        val itemNumber: TextView = view.findViewById(R.id.text_room_list_item_number)
        val itemTitle: TextView = view.findViewById(R.id.text_room_list_item_name)
        val itemPlayer: TextView = view.findViewById(R.id.text_room_list_item_player_num)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room_list, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = roomData[position]
        val playerNum = item.player_data.size
        holder.itemNumber.text = "${item.room_number}."
        holder.itemTitle.text = item.room_name
        holder.itemPlayer.text = "${playerNum}/${item.room_max_num}"
        holder.itemCsView.setOnClickListener {
            roomExistCheck(item, it)
        }
    }

    override fun getItemCount(): Int {
        return roomData.size
    }

    private fun roomExistCheck(item: RoomData, view: View) {
        db.collection("room_list").document(item.room_doc_id)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    if (checkMaxPlayerNum(item.player_data.size, item.room_max_num)) {
                        joinPlayer(item, view)
                    } else {
                        Toast.makeText(context, "Full", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Click refresh", Toast.LENGTH_SHORT).show()

                }
            }
    }

    private fun joinPlayer(item: RoomData, view: View) {
        val roomRef = db.collection("room_list").document(item.room_doc_id)

        db.runTransaction { transaction ->
            // 현재 playerturn의 값을 가져옴
            val currentData = transaction.get(roomRef)
            val roomData = currentData.toObject(RoomData::class.java)
            val currentPlayers = roomData?.player_data
            val roomPlayerList = roomData?.room_player_list?.toMutableList()
            val currentPlayerturn = currentPlayers?.size!!

            // playerturn을 업데이트
            playerData.playerturn = currentPlayerturn + 1
            roomPlayerList?.add(currentUid)

            val newData = mapOf(
                "player_data" to mapOf(
                    currentUid to playerData
                ),
                "room_player_list" to roomPlayerList
            )

            // 업데이트된 데이터를 설정
            transaction.set(roomRef, newData, SetOptions.merge())

            // 업데이트된 playerturn 값을 반환
            playerData.playerturn
        }.addOnSuccessListener { _ ->
            // 새 playerturn 값을 필요에 따라 사용
            val action = FragmentRoomListDirections.actionFragmentRoomListToFragmentReadyRoom(item.room_doc_id)
            view.findNavController().navigate(action)
        }.addOnFailureListener { e ->
            // 실패 처리
            Log.e("joinPlayer", "트랜잭션 실패: $e")
        }
    }

    private fun checkMaxPlayerNum(playerNum: Int, roomMax: Int): Boolean {
        return playerNum + 1 <= roomMax
    }

}