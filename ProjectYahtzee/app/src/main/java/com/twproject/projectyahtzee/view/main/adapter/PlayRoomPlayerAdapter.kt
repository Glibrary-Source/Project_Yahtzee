package com.twproject.projectyahtzee.view.main.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.databinding.ScoreBoardBinding
import com.twproject.projectyahtzee.vbutils.onThrottleClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PlayRoomPlayerAdapter(
    private val roomData: Map<String, *>,
    private val binding: ScoreBoardBinding,
    private val db: FirebaseFirestore,
    private val roomDocId: String,
    private val context: Context,
    private val scoreBoardPlayerNameView: TextView
) : RecyclerView.Adapter<PlayRoomPlayerAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerLinear: LinearLayout = view.findViewById(R.id.linear_play_room_item)
        val playerName: TextView = view.findViewById(R.id.text_play_room_nickname)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_play_room_player, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val itemValues = roomData.values.toList()[position]
        val itemUid = roomData.keys.toList()[position]
        val castItem = itemValues as Map<*, *>
        val userNickName = castItem["nickname"].toString()
        holder.playerName.text = castItem["nickname"].toString()
        holder.playerLinear.onThrottleClick {
            getUserData(roomDocId, itemUid, userNickName)
        }
    }

    override fun getItemCount(): Int {
        return roomData.size
    }

    private fun getUserData(roomDocId: String, uid: String, userNickName: String) {
        CoroutineScope(IO).launch{
            try{
                val getUser = db.collection("room_score_board").document(roomDocId)
                    .get()
                getUser.await()

                if (getUser.isSuccessful) {
                    val data = getUser.result.data
                    if (data != null) {
                        val score = data[uid] as? Map<String, *>
                        score?.let { viewScore(it, userNickName) }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun viewScore(score: Map<String, *>, userNickName: String) {
        withContext(Main){
            scoreBoardPlayerNameView.text = "Player: $userNickName"
            setView(binding.textScoreBoard1OnesPoint, score["ones"].toString())
            setView(binding.textScoreBoard1TwosPoint, score["twos"].toString())
            setView(binding.textScoreBoard1ThreesPoint, score["threes"].toString())
            setView(binding.textScoreBoard1FoursPoint, score["fours"].toString())
            setView(binding.textScoreBoard1FivesPoint, score["fives"].toString())
            setView(binding.textScoreBoard1SixesPoint, score["sixes"].toString())
            setView(binding.textScoreBoard1ChoicePoint, score["choice"].toString())
            setView(binding.textScoreBoard1FourOfAKindPoint, score["four_of_a_kind"].toString())
            setView(binding.textScoreBoard1FullHousePoint, score["full_house"].toString())
            setView(binding.textScoreBoard1LittleStraightPoint, score["little_straight"].toString())
            setView(binding.textScoreBoard1BigStraightPoint, score["big_straight"].toString())
            setView(binding.textScoreBoard1YachtPoint, score["yacht"].toString())
            setView(binding.textScoreBoard1TotalPoint, score["total"].toString())
            setBonusView(binding.textScoreBoard1BonusPoint, score["bonus"].toString())
        }
    }

    private fun setView(view: TextView, score: String) {
        if(score == "null") {
            view.text = ""
        } else {
            view.text = score
            view.setTextColor(ContextCompat.getColor(context,R.color.white))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBonusView(view:TextView, score: String) {
        if(score == "null") {
            view.text = ""
        } else {
            if(score.toInt() >= 63) {
                view.text = "35"
                view.setTextColor(ContextCompat.getColor(context,R.color.white))
            } else {
                view.text = "$score / 63"
            }
        }
    }
}