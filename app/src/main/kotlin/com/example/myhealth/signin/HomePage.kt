package com.example.myhealth.signin

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.myhealth.R
import com.example.myhealth.utils.openNewActivity

class HomePage : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        openNewActivity(findViewById(R.id.button_signup), this, SignUp::class.java)

        openNewActivity(findViewById(R.id.button_signin), this, SignIn::class.java)
    }
}