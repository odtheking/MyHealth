package com.example.myhealth

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity

class CreateCase : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_case)

        toggleButton(findViewById(R.id.diabetes))
    }
}