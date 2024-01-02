package com.twproject.projectyahtzee.view.main

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.twproject.banyeomiji.vbutility.BackPressCallBackManager
import com.twproject.projectyahtzee.R
import com.twproject.projectyahtzee.databinding.FragmentPlayRoomBinding
import com.twproject.projectyahtzee.vbutils.onThrottleClick
import com.twproject.projectyahtzee.view.main.adapter.PlayRoomPlayerAdapter
import com.twproject.projectyahtzee.view.main.adapter.TotalScoreAdapter
import com.twproject.projectyahtzee.view.main.data.DiceImageControl
import com.twproject.projectyahtzee.view.main.datamodel.RoomData
import com.twproject.projectyahtzee.view.main.util.Dice
import com.twproject.projectyahtzee.view.main.util.ScoreBoard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class FragmentPlayRoom : Fragment() {

    private lateinit var binding: FragmentPlayRoomBinding
    private lateinit var rcPlayerView: RecyclerView
    private lateinit var mContext: Context
    private lateinit var activity: MainActivity
    private lateinit var callback: OnBackPressedCallback
    private lateinit var roomDocId: String

    private lateinit var dice1: Dice
    private lateinit var dice2: Dice
    private lateinit var dice3: Dice
    private lateinit var dice4: Dice
    private lateinit var dice5: Dice

    private lateinit var scoreBoard: ScoreBoard

    private var roomListRefreshListenerRegistration: ListenerRegistration? = null
    private var roomTurnRefreshListenerRegistration: ListenerRegistration? = null
    private var roomDiceRefreshListenerRegistration: ListenerRegistration? = null


    private var dice1Clicked = false
    private var dice2Clicked = false
    private var dice3Clicked = false
    private var dice4Clicked = false
    private var dice5Clicked = false

    private var diceImage = DiceImageControl().getDiceImage()
    private var firstTurn = true
    private var diceRollCount = 1
    private var myTurnState = false
    private var boardFirstNameChecker = true
    private val roomData by navArgs<FragmentPlayRoomArgs>()
    private var currentUid = FirebaseAuth.getInstance().uid.toString()
    private val fadeIn01 = AlphaAnimation(0.3f, 1.0f).apply {
        duration = 2000
        repeatCount = -1
    }

    private val endTurnAnimator = endTurnAnimation()

    private var startTime = 0
    private val TAG = "testLifeCL"
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
        roomDocId = roomData.roomId
        setWaitState()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayRoomBinding.inflate(inflater)
        scoreBoard =
            ScoreBoard(binding.includeScoreBoard, mContext, binding, db, roomDocId, currentUid)
        binding.imgPlayRoomRollDice.progress = 0.97f

        dice1 = Dice(binding.imgPlayDice1)
        dice2 = Dice(binding.imgPlayDice2)
        dice3 = Dice(binding.imgPlayDice3)
        dice4 = Dice(binding.imgPlayDice4)
        dice5 = Dice(binding.imgPlayDice5)

        rcPlayerView = binding.rcPlayRoomExtendPlayer
        rcPlayerView.layoutManager = GridLayoutManager(mContext, 4)

        roomListRefreshListener()
        roomTurnRefreshListener()
        diceRefreshListener()

        binding.btnExitRoom.setOnClickListener {
            exitDialog()
        }

        binding.imgPlayRoomPlayer.setOnClickListener {
            playerVisibleControl()
        }

        binding.imgPlayRoomRollDice.onThrottleClick {
            playDiceSound()
            checkFirstRoll()
            diceRoll()
            CoroutineScope(Main).launch {
                it.isClickable = false
                delay(3000)
                roomRollDiceFalse()
                it.isClickable = true
            }

            binding.imgPlayRoomRollDice.playAnimation()

            if (diceRollCount <= 2) {
                diceRollCount++
            } else {
                binding.imgPlayRoomRollDice.isClickable = false
                binding.imgPlayRoomRollDice.isEnabled = false
            }
        }

        viewOnClick()
        setTimerData()

        Log.d(TAG, "onCreateView")
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

    }

    private fun playDiceSound() {
        val mediaPlayer = MediaPlayer.create(mContext, R.raw.roll_dice)
        mediaPlayer.start()
    }

    private fun playerVisibleControl() {
        if (binding.rcPlayRoomExtendPlayer.visibility == View.VISIBLE) {
            binding.rcPlayRoomExtendPlayer.visibility = View.GONE
        } else {
            binding.rcPlayRoomExtendPlayer.visibility = View.VISIBLE
        }
    }

    private fun roomListRefreshListener() {
        roomListRefreshListenerRegistration = db.collection("room_list").document(roomDocId)
            .addSnapshotListener { value, _ ->
                if (value == null) {
                    Log.d("testRoom", "data failed")
                } else {
                    val roomData = value.toObject(RoomData::class.java)!!
                    val playerDataList = roomData.player_data as Map<String, *>

                    scoreBoardNameInit(playerDataList)

                    if (firstTurn) {
                        setTurnCollection(roomData, playerDataList)
                        setMyTurnState(roomData, playerDataList)
                        firstTurn = false
                    }

                    binding.btnPlayTest.setOnClickListener {
                        endTurnRoom(roomData, playerDataList)
                        endTurn(roomData, playerDataList)
                    }

                    setRcViewPlayer(playerDataList)
                    setCurrentPlayerNotice(roomData, playerDataList)
                    userOutSetRoomTurn(roomData)
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun roomTurnRefreshListener() {
        roomTurnRefreshListenerRegistration = db.collection("room_turn").document(roomDocId)
            .addSnapshotListener { value, _ ->
                if (value == null) {
                    Log.d("testRoom", "data failed")
                } else {
                    val roomData = value.toObject(RoomData::class.java)
                    if (roomData == null) {
                        Log.d("testRoom", "data failed")
                    } else {
                        val playerDataList = roomData.player_data as Map<String, *>

                        setMyTurnState(roomData, playerDataList)
                        setTurnInit()
                        setRollBtnControl()

                        val roomCount = value.data!!["room_total_turn"].toString().toInt()
                        if (roomCount >= 13) {
                            Toast.makeText(mContext, "Game Over", Toast.LENGTH_SHORT).show()
                            binding.imgPlayRoomRollDice.isClickable = false
                            binding.imgPlayRoomRollDice.isEnabled = false
                            binding.imgPlayRoomRollDice.pauseAnimation()
                            binding.imgPlayRoomRollDice.progress = 0.97f
                            binding.imgPlayRoomRollDice.clearAnimation()

                            getUserScoreData(playerDataList)

                            endTurnAnimator?.cancel()

                            try { endGame() } catch (_: Exception) {}
                        }
                    }
                }
            }
    }

    private fun diceRefreshListener() {
        roomDiceRefreshListenerRegistration = db.collection("room_dice_num").document(roomDocId)
            .addSnapshotListener { value, _ ->
                if (value == null) {
                    Log.d("testRoom", "data failed")
                } else {
                    val diceData = value.data
                    if (diceData == null) {
                        Log.d("testRoom", "data failed")
                    } else {
                        val diceList = diceData["dicelist"] as List<Int>
                        val diceCheckList = diceData["dicechecklist"] as List<Boolean>

                        if (diceCheckList[0]) dice1.animation.start()
                        if (diceCheckList[1]) dice2.animation.start()
                        if (diceCheckList[2]) dice3.animation.start()
                        if (diceCheckList[3]) dice4.animation.start()
                        if (diceCheckList[4]) dice5.animation.start()

                        CoroutineScope(Main).launch {
                            delay(3000)
                            roomRollDiceFalse()
                            dice1.animation.pause()
                            dice2.animation.pause()
                            dice3.animation.pause()
                            dice4.animation.pause()
                            dice5.animation.pause()
                            dice1.setServerImage(diceList[0])
                            dice2.setServerImage(diceList[1])
                            dice3.setServerImage(diceList[2])
                            dice4.setServerImage(diceList[3])
                            dice5.setServerImage(diceList[4])
                        }
                    }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun scoreBoardNameInit(playerDataList: Map<String, *>) {
        if (boardFirstNameChecker) {
            val user = playerDataList[currentUid] as Map<String, *>
            binding.textScoreBoard1PlayerName.text = "Player: ${user["nickname"]}"
            boardFirstNameChecker = false
        }
    }

    private fun setMyTurnState(roomData: RoomData, playerDataList: Map<String, *>) {
        val user = playerDataList[currentUid] as Map<String, *>
        myTurnState = roomData.room_player_turn == user["playerturn"].toString().toInt()
    }

    private fun setTurnCollection(roomData: RoomData, playerDataList: Map<String, *>) {
        val turn = mapOf(
            "player_data" to playerDataList,
            "room_player_turn" to roomData.room_player_turn,
            "room_total_turn" to 1
        )
        db.collection("room_turn").document(roomDocId)
            .set(turn)
    }

    private fun setRcViewPlayer(playerDataList: Map<String, *>) {
        rcPlayerView.adapter = PlayRoomPlayerAdapter(
            playerDataList,
            binding.includeScoreBoard,
            db,
            roomDocId,
            mContext,
            binding.textScoreBoard1PlayerName
        )
    }

    private fun setCurrentPlayerNotice(roomData: RoomData, playerDataList: Map<String, *>) {
        for ((_, value) in playerDataList) {
            val user = value as Map<String, *>
            if (roomData.room_player_turn == user["playerturn"].toString().toInt()) {
                binding.textPlayRoomCurrentPlayer.text = user["nickname"].toString()
            }
        }
    }

    private fun setTurnInit() {
        setDiceNotClick(binding.imgPlayDice1, 1)
        setDiceNotClick(binding.imgPlayDice2, 2)
        setDiceNotClick(binding.imgPlayDice3, 3)
        setDiceNotClick(binding.imgPlayDice4, 4)
        setDiceNotClick(binding.imgPlayDice5, 5)

        binding.imgPlayDice1.setImageResource(diceImage[0])
        binding.imgPlayDice2.setImageResource(diceImage[0])
        binding.imgPlayDice3.setImageResource(diceImage[0])
        binding.imgPlayDice4.setImageResource(diceImage[0])
        binding.imgPlayDice5.setImageResource(diceImage[0])
    }

    @SuppressLint("SetTextI18n")
    private fun setRollBtnControl() {
        if (myTurnState) {
            binding.imgPlayRoomRollDice.isClickable = true
            binding.imgPlayRoomRollDice.isEnabled = true

            binding.imgPlayRoomRollDice.startAnimation(fadeIn01)

            endTurnAnimator?.start()
        } else {
            binding.imgPlayRoomRollDice.isClickable = false
            binding.imgPlayRoomRollDice.isEnabled = false

            binding.imgPlayRoomRollDice.clearAnimation()

            endTurnAnimator?.cancel()
            binding.textScoreBoard1EndTime.text = "END TURN 180"
        }
    }

    private fun checkFirstRoll() {
        binding.imgPlayDice1.isClickable = true
        binding.imgPlayDice2.isClickable = true
        binding.imgPlayDice3.isClickable = true
        binding.imgPlayDice4.isClickable = true
        binding.imgPlayDice5.isClickable = true
    }

    private fun diceRoll() {
        val diceList = mutableListOf<Int>()

        if (dice1Clicked) {
            dice1.roll()
            dice1.animation.start()
        }

        if (dice2Clicked) {
            dice2.roll()
            dice2.animation.start()
        }
        if (dice3Clicked) {
            dice3.roll()
            dice3.animation.start()
        }
        if (dice4Clicked) {
            dice4.roll()
            dice4.animation.start()
        }
        if (dice5Clicked) {
            dice5.roll()
            dice5.animation.start()
        }

        diceList.add(dice1.diceValue)
        diceList.add(dice2.diceValue)
        diceList.add(dice3.diceValue)
        diceList.add(dice4.diceValue)
        diceList.add(dice5.diceValue)

        scoreBoard.setScoreList(diceList)
        scoreBoard.viewScore()

        val diceCheckState = listOf(
            dice1Clicked,
            dice2Clicked,
            dice3Clicked,
            dice4Clicked,
            dice5Clicked,
        )
        var diceMap = mapOf(
            "dicelist" to diceList,
            "dicechecklist" to diceCheckState
        )

        CoroutineScope(IO).launch {
            db.collection("room_dice_num").document(roomDocId)
                .set(diceMap)
                .addOnSuccessListener {
                    diceList.clear()
                    diceMap = mapOf()
                }
        }
    }

    private fun roomRollDiceFalse() {
        dice1.animation.pause()
        dice2.animation.pause()
        dice3.animation.pause()
        dice4.animation.pause()
        dice5.animation.pause()
        dice1.setImage()
        dice2.setImage()
        dice3.setImage()
        dice4.setImage()
        dice5.setImage()
    }

    private fun endTurnRoom(roomData: RoomData, playerDataList: Map<String, *>) {
        if (roomData.room_player_turn + 1 <= playerDataList.size) {
            val roomTurn = mapOf(
                "room_player_turn" to roomData.room_player_turn + 1
            )
            db.collection("room_list").document(roomDocId)
                .update(roomTurn)
        } else {
            val roomTurn = mapOf(
                "room_player_turn" to 1
            )
            db.collection("room_list").document(roomDocId)
                .update(roomTurn)
        }
    }

    private fun endTurn(roomData: RoomData, playerDataList: Map<String, *>) {
        if (roomData.room_player_turn + 1 <= playerDataList.size) {
            val roomTurn = mapOf(
                "room_player_turn" to roomData.room_player_turn + 1
            )
            db.collection("room_turn").document(roomDocId)
                .update(roomTurn)
            diceRollCount = 1
        } else {
            val roomTurn = mapOf(
                "room_player_turn" to 1,
            )
            db.collection("room_turn").document(roomDocId)
                .update(roomTurn)

            db.collection("room_turn").document(roomDocId)
                .get()
                .addOnSuccessListener {
                    val data = it.data
                    if (data != null) {
                        val totalTurn = data["room_total_turn"].toString().toInt()

                        if (totalTurn == 14) {
                            Toast.makeText(mContext, "게임종료", Toast.LENGTH_SHORT).show()
                        } else {
                            val roomTotalTurn = mapOf(
                                "room_total_turn" to totalTurn + 1,
                            )
                            db.collection("room_turn").document(roomDocId)
                                .update(roomTotalTurn)
                        }
                    }
                }
            diceRollCount = 1
        }
    }

    private fun setDiceNotClick(view: ImageView, num: Int) {
        view.isClickable = false
        when (num) {
            1 -> {
                dice1Clicked = true
            }

            2 -> {
                dice2Clicked = true
            }

            3 -> {
                dice3Clicked = true
            }

            4 -> {
                dice4Clicked = true
            }

            5 -> {
                dice5Clicked = true
            }
        }
        view.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
    }

    private fun viewOnClick() {
        val diceViewList = listOf(
            binding.imgPlayDice1,
            binding.imgPlayDice2,
            binding.imgPlayDice3,
            binding.imgPlayDice4,
            binding.imgPlayDice5
        )
        for ((index, view) in diceViewList.withIndex()) {
            view.setOnClickListener {
                when (index) {
                    0 -> {
                        dice1Clicked = !dice1Clicked
                        if (dice1Clicked) {
                            binding.imgPlayDice1.setColorFilter(
                                Color.WHITE,
                                PorterDuff.Mode.MULTIPLY
                            )
                        } else {
                            binding.imgPlayDice1.setColorFilter(
                                Color.parseColor("#BDBDBD"),
                                PorterDuff.Mode.MULTIPLY
                            )
                        }
                    }

                    1 -> {
                        dice2Clicked = !dice2Clicked
                        if (dice2Clicked) {
                            binding.imgPlayDice2.setColorFilter(
                                Color.WHITE,
                                PorterDuff.Mode.MULTIPLY
                            )
                        } else {
                            binding.imgPlayDice2.setColorFilter(
                                Color.parseColor("#BDBDBD"),
                                PorterDuff.Mode.MULTIPLY
                            )
                        }
                    }

                    2 -> {
                        dice3Clicked = !dice3Clicked
                        if (dice3Clicked) {
                            binding.imgPlayDice3.setColorFilter(
                                Color.WHITE,
                                PorterDuff.Mode.MULTIPLY
                            )
                        } else {
                            binding.imgPlayDice3.setColorFilter(
                                Color.parseColor("#BDBDBD"),
                                PorterDuff.Mode.MULTIPLY
                            )
                        }
                    }

                    3 -> {
                        dice4Clicked = !dice4Clicked
                        if (dice4Clicked) {
                            binding.imgPlayDice4.setColorFilter(
                                Color.WHITE,
                                PorterDuff.Mode.MULTIPLY
                            )
                        } else {
                            binding.imgPlayDice4.setColorFilter(
                                Color.parseColor("#BDBDBD"),
                                PorterDuff.Mode.MULTIPLY
                            )
                        }
                    }

                    4 -> {
                        dice5Clicked = !dice5Clicked
                        if (dice5Clicked) {
                            binding.imgPlayDice5.setColorFilter(
                                Color.WHITE,
                                PorterDuff.Mode.MULTIPLY
                            )
                        } else {
                            binding.imgPlayDice5.setColorFilter(
                                Color.parseColor("#BDBDBD"),
                                PorterDuff.Mode.MULTIPLY
                            )
                        }
                    }
                }

            }
        }
    }

    private fun getUserScoreData(playerDataList: Map<String, *>) {
        db.collection("room_score_board").document(roomDocId)
            .get()
            .addOnSuccessListener {
                val data = it.data
                if (data != null) {

                    binding.rcPlayRoomTotalScore.visibility = View.VISIBLE

                    val nickNameMap = mutableMapOf<String, String>()
                    for ((key, value) in playerDataList) {
                        val castValue = value as Map<String, String>
                        nickNameMap[key] = castValue["nickname"].toString()
                    }

                    val scoreRank = mutableListOf<Int>()
                    for ((_, value) in data) {
                        val castValue = value as Map<String, Int>
                        scoreRank.add(castValue["total"]!!.toInt())
                    }

                    val setScoreRank = scoreRank.toSet().toList().sortedDescending()
                    val rankUserMap = mutableMapOf<String, List<Int>>()
                    for ((key, value) in data) {
                        val castValue = value as Map<String, Int>
                        val total = castValue["total"]!!.toInt()
                        val rank = setScoreRank.indexOf(total) + 1
                        rankUserMap[nickNameMap[key].toString()] = listOf(rank, total)
                    }

                    val sortedRankUserMap = rankUserMap.entries
                        .sortedBy { num -> num.value[0] }
                        .associateTo(mutableMapOf()) { number -> number.key to number.value }

                    binding.rcPlayRoomTotalScore.adapter = TotalScoreAdapter(sortedRankUserMap)
                }
            }
    }

    private fun setWaitState() {
        lifecycleScope.launch {
            delay(1500)
            withContext(IO) {
                val map = mapOf(
                    "player_data" to mapOf(
                        currentUid to mapOf(
                            "waitstate" to "wait"
                        )
                    ),
                    "room_state" to "wait"
                )
                db.collection("room_list").document(roomDocId)
                    .set(map, SetOptions.merge())
            }
        }
    }

    private fun endGame() {
        lifecycleScope.launch(Main) {
            Toast.makeText(
                mContext,
                "We'll be in the waiting room in 10 seconds",
                Toast.LENGTH_LONG
            ).show()
            delay(10000)
            clearRoomDB()
            val action =
                FragmentPlayRoomDirections.actionFragmentPlayRoomToFragmentReadyRoom(roomDocId)
            findNavController().navigate(action)
        }
    }

    private suspend fun clearRoomDB() {
        withContext(IO) {
            db.collection("room_dice_num").document(roomDocId)
                .delete()
            db.collection("room_score_board").document(roomDocId)
                .delete()
            db.collection("room_turn").document(roomDocId)
                .delete()
        }
    }

    private fun userOutSetRoomTurn(roomData: RoomData) {
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

            db.collection("room_turn").document(roomDocId)
                .set(player, SetOptions.merge())
        }
    }

    private fun exitDialog() {
        val builder = AlertDialog.Builder(mContext)

        builder.setTitle("Exit Room")
        builder.setMessage("Are you sure you want to leave the room?")

        builder.setNegativeButton("Cancel") { _, _ -> }
        builder.setPositiveButton("Exit") { _, _ ->
            val action = FragmentPlayRoomDirections.actionFragmentPlayRoomToFragmentRoomList()
            findNavController().navigate(action)
            playExitRoom()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun playExitRoom() {
        roomListRefreshListenerRegistration?.remove()
        roomTurnRefreshListenerRegistration?.remove()
        roomDiceRefreshListenerRegistration?.remove()
        CoroutineScope(IO).launch {
            roomScoreBoardDelete(currentUid)
            roomListDelete(currentUid)
            roomTurnDelete(currentUid)
            db.collection("room_list").document(roomDocId)
                .get()
                .addOnSuccessListener {
                    val data = it.data
                    if(data != null) {
                        val castData = data["room_player_list"] as List<String>
                        if(castData.size == 1) {
                            db.collection("room_list").document(roomDocId)
                                .delete()
                        }
                    }
                }
        }
    }

    private fun roomScoreBoardDelete(uid: String) {
        try {
            val map = mapOf(
                uid to FieldValue.delete()
            )
            db.collection("room_score_board").document(roomDocId)
                .update(map)
        } catch (_: Exception) {}
    }

    private fun roomListDelete(uid: String) {
        try {
            db.collection("room_list").document(roomDocId)
                .get()
                .addOnSuccessListener {
                    val data = it.data
                    if (data != null) {
                        val userData = data["player_data"] as MutableMap<String, Any>
                        val playerList = data["room_player_list"] as MutableList<String>
                        userData.remove(uid)
                        playerList.remove(uid)
                        val updateMap = mapOf(
                            "player_data" to userData,
                            "room_player_list" to playerList
                        )
                        db.collection("room_list").document(roomDocId)
                            .update(updateMap)
                    }
                }
        } catch (_: Exception) {}
    }

    private fun roomTurnDelete(uid: String) {
        try {
            db.collection("room_turn").document(roomDocId)
                .get()
                .addOnSuccessListener {
                    val data = it.data
                    if (data != null) {
                        val userData = data["player_data"] as MutableMap<String, Any>
                        userData.remove(uid)
                        val updateMap = mapOf(
                            "player_data" to userData
                        )
                        db.collection("room_turn").document(roomDocId)
                            .update(updateMap)
                    }
                }
        } catch (_: Exception) {}
    }

    private fun setTimerData() {
        val executor = Executors.newScheduledThreadPool(1)
        executor.scheduleAtFixedRate(
            {
                lifecycleScope.launch(IO){
                    startTime++
                    val map = mapOf(currentUid to startTime)
                    db.collection("room_user_timestamp").document(roomDocId)
                        .set(map, SetOptions.merge())
                        .addOnSuccessListener {
                            setUserExitCheck()
                        }
                }
            }, 0, 60, TimeUnit.SECONDS
        )
    }

    private fun setUserExitCheck() {
        db.collection("room_user_timestamp").document(roomDocId)
            .get()
            .addOnSuccessListener {
                val data = it.data
                if(data != null) {
                    val standard = data[currentUid] as Long
                    for((key, value) in data){
                        val castValue = value as Long
                        if(castValue + 3 <= standard) {
                            roomScoreBoardDelete(key)
                            roomListDelete(key)
                            roomTurnDelete(key)
                        }
                    }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun endTurnAnimation(): ValueAnimator? {
        val animator = ValueAnimator.ofInt(180, 0).apply {
            duration = 180000
            interpolator = LinearInterpolator()
            addUpdateListener {
                binding.textScoreBoard1EndTime.text = "END TURN ${it.animatedValue}"
                if(it.animatedValue == 0) {
                    binding.btnPlayTest.performClick()
                }
            }
        }
        return animator
    }

}