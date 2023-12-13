package com.twproject.projectyahtzee.view.main.datamodel

data class RoomData (
    val room_doc_id: String = "",
    val room_name: String = "",
    val room_max_num: Int = 0,
    val room_manager: String = "",
    val room_state: String ="",
    val room_number: Int = 0,
    val room_player_turn: Int = 0,
    val room_dice_roll: Boolean = false,
    val room_player_list: List<String> = listOf(),
    val player_data: Map<String, Any> = mapOf()
)