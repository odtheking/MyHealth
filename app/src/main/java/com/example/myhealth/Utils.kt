package com.example.myhealth

import android.content.Context
import android.content.Intent
import android.widget.Button

fun openNewActivity(button: Button, context: Context, activityClass: Class<*>) {
    button.setOnClickListener {
        val intent = Intent(context, activityClass)
        context.startActivity(intent)
    }
}