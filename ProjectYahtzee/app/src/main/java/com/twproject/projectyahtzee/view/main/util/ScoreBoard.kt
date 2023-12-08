package com.twproject.projectyahtzee.view.main.util

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.databinding.FragmentPlayRoomBinding
import com.twproject.projectyahtzee.databinding.ScoreBoardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScoreBoard(
    private val binding: ScoreBoardBinding,
    private val context: Context,
    private val bindingTurn: FragmentPlayRoomBinding
) {

    private val certainList = mutableListOf<String>()
    private var scoreList = listOf<Int>()
    private val totalScore = mutableListOf<Int>()

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
        val certain = "fourOfAKind"
        var score = 0
        for (num in scoreList) {
            val count = scoreList.count { it == num }
            if (count >= 4) {
                score = num * 4
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
        val certain = "fullHouse"
        val isThreeOfAKind = checkForNOfAKind(scoreList, 3)
        val isTwoOfAKind = checkForNOfAKind(scoreList, 2)
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

    fun checkForNOfAKind(diceResults: List<Int>, n: Int): Boolean {
        val certain = "checkForNOfAKind"
        val frequencyMap = diceResults.groupingBy { it }.eachCount()
        return frequencyMap.containsValue(n)
    }

    fun littleStraight() {
        val certain = "littleStraight"
        val list = listOf(1, 2, 3, 4, 5)
        var score = 0
        if (scoreList.containsAll(list)) {
            score = 30
        }
        setSelect(
            score,
            binding.textScoreBoard1LittleStraightPoint,
            binding.imgScoreBoard1LittleStraightCheck,
            certain
        )
    }

    fun bigStraight() {
        val certain = "bigStraight"
        val list = listOf(2, 3, 4, 5, 6)
        var score = 0
        if (scoreList.containsAll(list)) {
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
            clearView()
            binding.textScoreBoard1TotalPoint.text = totalScore.sum().toString()
            bindingTurn.btnPlayTest.performClick()
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

        val imgView = listOf(binding.imgScoreBoard1OnesCheck,
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
        binding.imgScoreBoard1ChoiceCheck)

        for (index in scoreBoardBindings.indices) {
            if (!certainList.contains(getScoreBoardItemName(index))) {
                scoreBoardBindings[index].text = ""
            }
        }

        for(view in imgView) {
            view.visibility = View.GONE
        }
    }

    fun getScoreBoardItemName(index: Int): String {
        return when (index) {
            0 -> "ones"
            1 -> "twos"
            2 -> "threes"
            3 -> "fours"
            4 -> "fives"
            5 -> "sixes"
            6 -> "fourOfAKind"
            7 -> "fullHouse"
            8 -> "littleStraight"
            9 -> "bigStraight"
            10 -> "yacht"
            11 -> "choice"
            else -> ""
        }
    }

    fun certainListClear() {
        certainList.clear()
    }

    fun totalClear() {
        totalScore.clear()
    }


}