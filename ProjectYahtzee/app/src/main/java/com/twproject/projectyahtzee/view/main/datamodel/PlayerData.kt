package com.twproject.projectyahtzee.view.main.datamodel

data class PlayerData (
    val change_counter: Boolean = false,
    val email: String = "",
    val nickname: String = "",
    val score: Int = 0,
    val tier: String = "",
    val uid: String = "",
    val waitstate: String ="",
    var playerturn: Int = 1,
)