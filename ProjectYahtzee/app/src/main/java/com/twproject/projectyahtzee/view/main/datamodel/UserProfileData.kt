package com.twproject.projectyahtzee.view.main.datamodel

data class UserProfileData (
    val change_counter: Boolean = false,
    val email: String = "",
    val nickname: String = "",
    val score: Int = 0,
    val tier: String = "",
    val uid: String = ""
)