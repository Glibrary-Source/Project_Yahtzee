package com.twproject.projectyahtzee.view.main.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.twproject.projectyahtzee.R

class PlayerListAdapter(
    private val roomData: Map<String,Any>
): RecyclerView.Adapter<PlayerListAdapter.ItemViewHolder>()  {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerNumber: TextView = view.findViewById(R.id.text_wait_room_item_player_number)
        val playerTier: ImageView = view.findViewById(R.id.img_wait_room_item_player_tier)
        val playerName: TextView = view.findViewById(R.id.text_wait_room_item_player_name)
        val playerState: TextView = view.findViewById(R.id.text_wait_room_item_player_state)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_info, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = roomData.values.toList()[position]
        val castItem = item as Map<*,*>
        holder.playerNumber.text = "${position + 1}."
        holder.playerName.text = castItem["nickname"].toString()
        holder.playerState.text = castItem["waitstate"].toString()
    }

    override fun getItemCount(): Int {
        return roomData.size
    }
}