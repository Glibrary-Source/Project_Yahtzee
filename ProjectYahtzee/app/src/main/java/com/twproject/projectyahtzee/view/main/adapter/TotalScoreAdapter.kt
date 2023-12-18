package com.twproject.projectyahtzee.view.main.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.twproject.projectyahtzee.R

class TotalScoreAdapter(
    private val userData: MutableMap<String, List<Int>>
): RecyclerView.Adapter<TotalScoreAdapter.ItemViewHolder>()  {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerNickName: TextView = view.findViewById(R.id.item_player_total_score_nickname)
        val playerTotalScore: TextView = view.findViewById(R.id.item_player_total_score_point)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_total_score, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val itemNickName = userData.keys.toList()[position]
        val itemScore = userData.values.toList()[position]
        holder.playerNickName.text = "${itemScore[0]} $itemNickName"
        holder.playerTotalScore.text = itemScore[1].toString()
    }

    override fun getItemCount(): Int {
        return userData.size
    }
}