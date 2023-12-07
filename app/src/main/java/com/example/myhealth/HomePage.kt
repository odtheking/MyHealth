package com.example.myhealth

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity

class HomePage : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        openNewActivity(findViewById(R.id.button_signup), this, SignUp::class.java)

        openNewActivity(findViewById(R.id.button_signin), this, SignIn::class.java)
    }


}