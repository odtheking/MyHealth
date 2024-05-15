package com.example.myhealth

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import com.example.myhealth.MainFolder.MainFolder
import com.example.myhealth.calender.Calender
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.utils.showToast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainMenu : ComponentActivity() {

    private val db = Firebase.firestore
    private lateinit var sharePreference: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        sharePreference = getSharedPreferences("MyPrefsFile", MODE_PRIVATE) ?: return

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.data["email"] == sharePreference.getString("email", "") && document.data["password"] == sharePreference.getString("password", "")) {
                        CurrentUser.createInstance(document.data["email"].toString(), document.data["password"].toString(), document.id)
                        //this.startActivity(Intent(this, MainMenu::class.java))
                        showToast(this, "Login Successful")
                    }
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        val createCase = findViewById<View>(R.id.drive)
        createCase.isClickable = true
        createCase.setOnClickListener {
            this.startActivity(Intent(this, MainFolder::class.java))
        }

        val calendar = findViewById<View>(R.id.calendar)
        calendar.isClickable = true
        calendar.setOnClickListener {
            this.startActivity(Intent(this, Calender::class.java))
        }



        //openNewActivity(findViewById(R.id.drive), this, CreateCase::class.java)

        //openNewActivity(findViewById(R.id.calendar), this, Calendar::class.java)

        //openNewActivity(findViewById(R.id.document), this, History::class.java)

        //openNewActivity(findViewById(R.id.login), this, Settings::class.java)

    }

}
