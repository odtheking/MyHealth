package com.example.myhealth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class SignIn : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        openNewActivity(findViewById(R.id.button_signin), this, SignUp::class.java)

        openNewActivity(findViewById(R.id.button_signin_app), this, CreateCase::class.java)

    }
}