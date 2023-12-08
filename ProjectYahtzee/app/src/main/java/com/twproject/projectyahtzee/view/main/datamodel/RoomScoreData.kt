package com.twproject.projectyahtzee.view.main.datamodel

data class RoomScoreData (
    val ones: Int = 0,
    val twos: Int = 0,
    val threes: Int = 0,
    val fours: Int = 0,
    val fives: Int = 0,
    val sixes: Int = 0,
    val four_of_a_kind: Int = 0,
    val full_house: Int = 0,
    val little_straight: Int = 0,
    val big_straight: Int = 0,
    val yacht: Int = 0,
    val choice: Int = 0,
    val total: Int = 0,
    val nickname: String = ""
)