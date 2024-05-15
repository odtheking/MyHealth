package com.example.myhealth.utils

class CurrentUser private constructor() {

    var email: String = ""
    var password: String = ""
    var id: String = ""

    companion object {
        // Static-like properties
        var instance: CurrentUser = CurrentUser()

        fun createInstance(email: String, password: String, id: String): CurrentUser {
            val user = CurrentUser()
            user.email = email
            user.password = password
            user.id = id
            instance = user
            return user
        }
    }
}
