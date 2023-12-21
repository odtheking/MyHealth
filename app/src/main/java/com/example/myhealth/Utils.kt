package com.example.myhealth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.Button

    fun openNewActivity(button: Button, context: Context, activityClass: Class<*>) {
        button.setOnClickListener {
            val intent = Intent(context, activityClass)
            context.startActivity(intent)
        }
    }

    @SuppressLint("ResourceAsColor")
    fun toggleButton(button: Button) {
        button.setOnClickListener {
            button.isSelected = !button.isSelected
            if (button.isSelected) {
                button.setBackgroundColor(R.color.black)
            } else {
                button.setBackgroundResource(R.color.white)
            }
        }
    }

