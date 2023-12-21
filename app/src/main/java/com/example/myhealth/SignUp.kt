package com.example.myhealth

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity

class SignUp : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        openNewActivity(findViewById(R.id.button_signin), this, SignIn::class.java)

        openNewActivity(findViewById(R.id.button_signup_app), this, CreateCase::class.java)



    }
}