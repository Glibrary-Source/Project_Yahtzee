package com.twproject.projectyahtzee.view.main.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.databinding.FragmentPlayRoomBinding
import com.twproject.projectyahtzee.databinding.ScoreBoardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScoreBoard(
    private val binding: ScoreBoardBinding,
    private val context: Context,
    private val bindingTurn: FragmentPlayRoomBinding,
    private val db: FirebaseFirestore,
    private val roomDocId: String,
    private val currentUid: String
) {

    private val certainList = mutableListOf<String>()
    private var scoreList = listOf<Int>()
    private val totalScore = mutableListOf<Int>()
    private val bonusScore = mutableListOf<Int>()
    private val bonusList = listOf("ones", "twos", "threes", "fours", "fives", "sixes")
    private var bonusFirst = true
    private val auth = FirebaseAuth.getInstance()

    fun setScoreList(score: List<Int>) {
        scoreList = score
    }

    fun viewScore() {
        clearView()
        ones()
        twos()
        threes()
        fours()
        fives()
        sixes()
        fourOfAKind()
        fullHouse()
        littleStraight()
        bigStraight()
        yacht()
        choice()
    }

    fun ones() {
        val num = 1
        val count = scoreList.count { it == 1 }
        val score = num * count
        val certain = "ones"
        setSelect(score, binding.textScoreBoard1OnesPoint, binding.imgScoreBoard1OnesCheck, certain)
    }

    fun twos() {
        val num = 2
        val count = scoreList.count { it == 2 }
        val score = num * count
        val certain = "twos"
        setSelect(score, binding.textScoreBoard1TwosPoint, binding.imgScoreBoard1TwosCheck, certain)
    }

    fun threes() {
        val num = 3
        val count = scoreList.count { it == 3 }
        val score = num * count
        val certain = "threes"
        setSelect(
            score,
            binding.textScoreBoard1ThreesPoint,
            binding.imgScoreBoard1ThreesCheck,
            certain
        )
    }

    fun fours() {
        val num = 4
        val count = scoreList.count { it == 4 }
        val score = num * count
        val certain = "fours"
        setSelect(
            score,
            binding.textScoreBoard1FoursPoint,
            binding.imgScoreBoard1FoursCheck,
            certain
        )
    }

    fun fives() {
        val num = 5
        val count = scoreList.count { it == 5 }
        val score = num * count
        val certain = "fives"
        setSelect(
            score,
            binding.textScoreBoard1FivesPoint,
            binding.imgScoreBoard1FivesCheck,
            certain
        )
    }

    fun sixes() {
        val num = 6
        val count = scoreList.count { it == 6 }
        val score = num * count
        val certain = "sixes"
        setSelect(
            score,
            binding.textScoreBoard1SixesPoint,
            binding.imgScoreBoard1SixesCheck,
            certain
        )
    }

    fun fourOfAKind() {
        val certain = "four_of_a_kind"
        var score = 0
        for (num in scoreList) {
            val count = scoreList.count { it == num }
            if (count >= 4) {
                score = scoreList.sum()
                break
            }
        }
        setSelect(
            score,
            binding.textScoreBoard1FourOfAKindPoint,
            binding.imgScoreBoard1FourOfAKindCheck,
            certain
        )
    }

    fun fullHouse() {
        val certain = "full_house"
        val isThreeOfAKind = checkFullHouse(scoreList, 3)
        val isTwoOfAKind = checkFullHouse(scoreList, 2)
        var score = 0

        if (isThreeOfAKind && isTwoOfAKind) {
            score = scoreList.sum()
        }
        setSelect(
            score,
            binding.textScoreBoard1FullHousePoint,
            binding.imgScoreBoard1FullHouseCheck,
            certain
        )
    }

    fun checkFullHouse(diceResults: List<Int>, n: Int): Boolean {
        val frequencyMap = diceResults.groupingBy { it }.eachCount()
        return frequencyMap.containsValue(n)
    }

    fun littleStraight() {
        val certain = "little_straight"
        val list = listOf(1, 2, 3, 4)
        val list2 = listOf(2, 3, 4, 5)
        val list3 = listOf(3, 4, 5, 6)
        var score = 0
        if (scoreList.containsAll(list) || scoreList.containsAll(list2) || scoreList.containsAll(
                list3
            )
        ) {
            score = 15
        }
        setSelect(
            score,
            binding.textScoreBoard1LittleStraightPoint,
            binding.imgScoreBoard1LittleStraightCheck,
            certain
        )
    }

    fun bigStraight() {
        val certain = "big_straight"
        val list1 = listOf(2, 3, 4, 5, 6)
        val list2 = listOf(1, 2, 3, 4, 5)
        var score = 0
        if (scoreList.containsAll(list1) || scoreList.containsAll(list2)) {
            score = 30
        }
        setSelect(
            score,
            binding.textScoreBoard1BigStraightPoint,
            binding.imgScoreBoard1BigStraightCheck,
            certain
        )
    }

    fun yacht() {
        val certain = "yacht"
        var score = 0
        if (scoreList.count { it == scoreList[0] } == 5) {
            score = 50
        }
        setSelect(
            score,
            binding.textScoreBoard1YachtPoint,
            binding.imgScoreBoard1YachtCheck,
            certain
        )
    }

    fun choice() {
        val certain = "choice"
        val score = scoreList.sum()
        setSelect(
            score,
            binding.textScoreBoard1ChoicePoint,
            binding.imgScoreBoard1ChoiceCheck,
            certain
        )
    }

    private fun setSelect(score: Int, pointView: TextView, checkView: ImageView, certain: String) {
        CoroutineScope(Main).launch {
            delay(3000)
            if (!certainList.contains(certain)) {
                pointView.text = score.toString()
                pointView.setTextColor(ContextCompat.getColor(context, R.color.hintYellow))
                checkView.visibility = View.VISIBLE
            }
        }
        checkView.setOnClickListener {
            pointView.setTextColor(ContextCompat.getColor(context, R.color.white))
            certainList.add(certain)
            totalScore.add(score)
            setBonusScore(certain, score)
            setScore(certain, score, totalScore.sum(), bonusScore.sum())
            clearView()
            binding.textScoreBoard1TotalPoint.text = totalScore.sum().toString()
            bindingTurn.btnPlayTest.performClick()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBonusScore(certain: String, score: Int) {
        if(bonusList.contains(certain)) {
            bonusScore.add(score)
            binding.textScoreBoard1BonusPoint.text = "${bonusScore.sum()} / 63"
            if(bonusScore.sum() >= 63) {
                binding.textScoreBoard1BonusPoint.text = "35"
                if(bonusFirst) {
                    totalScore.add(35)
                    bonusFirst = false
                }
            }
        }
    }

    private fun setScore(certain: String, score: Int, total: Int, bonusSum: Int) {
        CoroutineScope(IO).launch {
            val dbScore = mapOf(
                currentUid to mapOf(
                    certain to score,
                    "total" to total,
                    "bonus" to bonusSum
                )
            )
            db.collection("room_score_board").document(roomDocId)
                .set(dbScore, SetOptions.merge())
        }
    }

    fun clearView() {
        val scoreBoardBindings = listOf(
            binding.textScoreBoard1OnesPoint,
            binding.textScoreBoard1TwosPoint,
            binding.textScoreBoard1ThreesPoint,
            binding.textScoreBoard1FoursPoint,
            binding.textScoreBoard1FivesPoint,
            binding.textScoreBoard1SixesPoint,
            binding.textScoreBoard1FourOfAKindPoint,
            binding.textScoreBoard1FullHousePoint,
            binding.textScoreBoard1LittleStraightPoint,
            binding.textScoreBoard1BigStraightPoint,
            binding.textScoreBoard1YachtPoint,
            binding.textScoreBoard1ChoicePoint
        )

        val imgView = listOf(
            binding.imgScoreBoard1OnesCheck,
            binding.imgScoreBoard1TwosCheck,
            binding.imgScoreBoard1ThreesCheck,
            binding.imgScoreBoard1FoursCheck,
            binding.imgScoreBoard1FivesCheck,
            binding.imgScoreBoard1SixesCheck,
            binding.imgScoreBoard1FourOfAKindCheck,
            binding.imgScoreBoard1FullHouseCheck,
            binding.imgScoreBoard1LittleStraightCheck,
            binding.imgScoreBoard1BigStraightCheck,
            binding.imgScoreBoard1YachtCheck,
            binding.imgScoreBoard1ChoiceCheck
        )

        for (index in scoreBoardBindings.indices) {
            if (!certainList.contains(getScoreBoardItemName(index))) {
                scoreBoardBindings[index].text = ""
            }
        }

        for (view in imgView) {
            view.visibility = View.GONE
        }
    }

    private fun getScoreBoardItemName(index: Int): String {
        return when (index) {
            0 -> "ones"
            1 -> "twos"
            2 -> "threes"
            3 -> "fours"
            4 -> "fives"
            5 -> "sixes"
            6 -> "four_of_a_kind"
            7 -> "full_house"
            8 -> "little_straight"
            9 -> "big_straight"
            10 -> "yacht"
            11 -> "choice"
            else -> ""
        }
    }

    fun certainListClear() {
        certainList.clear()
    }

    fun totalBonusClear() {
        totalScore.clear()
        bonusScore.clear()
    }


}