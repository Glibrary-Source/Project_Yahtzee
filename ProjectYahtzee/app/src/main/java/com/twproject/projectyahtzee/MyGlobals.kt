package com.twproject.projectyahtzee

class MyGlobals {

    var userLogin = 0
    var currentUid = ""

    companion object {
        @get:Synchronized
        var instance: MyGlobals? = null
            get() {
                if (null == field) {
                    field = MyGlobals()
                }
                return field
            }
            private set
    }
}