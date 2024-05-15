package com.example.myhealth.MainFolder

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.R
import com.example.myhealth.utils.bigFolderList
import com.example.myhealth.utils.createNewFolder
import com.example.myhealth.utils.folderName
import com.example.myhealth.utils.openTextInputDialog
import com.example.myhealth.utils.printFileStructure
import com.example.myhealth.utils.showToast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.time.LocalDate

class CreateCase : ComponentActivity() {

    private var lastClickedButton: Button? = null
    private val db = Firebase.firestore

    private lateinit var sharePreference: SharedPreferences
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_case)

        val square1: Button = findViewById(R.id.diabetes)
        val square2: Button = findViewById(R.id.cancer)
        val square3: Button = findViewById(R.id.heart)
        val square4: Button = findViewById(R.id.pregnancy)
        val square5: Button = findViewById(R.id.other)
        val continueButton: Button = findViewById(R.id.button_continue)


        setButtonClickListeners(square1, square2, square3, square4, square5)
        otherButton(square5)

        makeFile(continueButton)
    }

    private fun otherButton(button: Button) {
        button.setOnClickListener {
            openTextInputDialog(this, button)
            highlightButton(button)
        }
    }

    private fun setButtonClickListeners(vararg buttons: Button) {
        for (button in buttons) {
            button.setOnClickListener {
                handleButtonClick(button)
                highlightButton(button)
            }
        }
    }

    private fun handleButtonClick(clickedButton: Button) {
        lastClickedButton?.setBackgroundColor(Color.WHITE)
        clickedButton.setBackgroundColor(Color.CYAN)
        lastClickedButton = clickedButton
    }

    private fun highlightButton(button: Button) {
        lastClickedButton?.setBackgroundColor(Color.WHITE)
        button.setBackgroundColor(Color.CYAN)
        lastClickedButton = button
        folderName = button.text.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeFile(button: Button) {

        sharePreference = getSharedPreferences("MyPrefsFile", MODE_PRIVATE) ?: return

        button.setOnClickListener {
            if (folderName == "Other") {
                showToast(this, "Please enter a valid case name")
            } else {
                if (lastClickedButton == null) {
                    showToast(this, "Please select a case")
                    return@setOnClickListener
                }
                createNewFolder(folderName!!, LocalDate.now(), bigFolderList)
                printFileStructure(bigFolderList[bigFolderList.size - 1])
                showToast(this, "Case created")
                val intent = Intent(this, MainFolder::class.java)
                this.startActivity(intent)

                db.collection("users").document(CurrentUser.instance.id).collection("cases")
                    .add(bigFolderList[bigFolderList.size - 1])
                    .addOnSuccessListener { result ->
                        Log.d(TAG, "DocumentSnapshot added with ID: ${result.id}")
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents.", exception)
                    }
            }
        }
    }
}
