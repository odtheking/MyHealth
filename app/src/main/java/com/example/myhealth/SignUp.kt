package com.example.myhealth

import android.os.Bundle
import androidx.activity.ComponentActivity

class SignUp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        openNewActivity(findViewById(R.id.button_signin), this, SignIn::class.java)
    }
}