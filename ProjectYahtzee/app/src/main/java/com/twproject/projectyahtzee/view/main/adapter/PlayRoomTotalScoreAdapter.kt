package com.twproject.projectyahtzee.view.main.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.twproject.projectyahtzee.R

class PlayRoomTotalScoreAdapter(
    private val roomData: Map<String,*>
): RecyclerView.Adapter<PlayRoomTotalScoreAdapter.ItemViewHolder>()  {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerName: TextView = view.findViewById(R.id.text_play_room_total_score_username)
        val playerScore: TextView = view.findViewById(R.id.text_play_room_total_score_userscore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_total_score, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = roomData.values.toList()[position]
        val castItem = item as Map<*,*>
        holder.playerName.text = castItem["nickname"].toString()
        holder.playerScore.text = castItem["total"].toString()
    }

    override fun getItemCount(): Int {
        return roomData.size
    }
}