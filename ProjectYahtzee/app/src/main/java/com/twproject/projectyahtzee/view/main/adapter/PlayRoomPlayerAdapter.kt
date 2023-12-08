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

class PlayRoomPlayerAdapter(
    private val roomData: Map<String,*>
): RecyclerView.Adapter<PlayRoomPlayerAdapter.ItemViewHolder>()  {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerName: TextView = view.findViewById(R.id.text_play_room_nickname)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_play_room_player, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = roomData.values.toList()[position]
        val castItem = item as Map<*,*>
        holder.playerName.text = castItem["nickname"].toString()
    }

    override fun getItemCount(): Int {
        return roomData.size
    }
}