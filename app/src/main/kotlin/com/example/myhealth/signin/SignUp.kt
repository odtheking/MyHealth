package com.example.myhealth.signin

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.example.myhealth.MainMenu
import com.example.myhealth.R
import com.example.myhealth.utils.db
import com.example.myhealth.utils.openNewActivity
import com.example.myhealth.utils.showToast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class SignUp : ComponentActivity() {

    private lateinit var sharePreference: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        sharePreference = getSharedPreferences("MyPrefsFile", MODE_PRIVATE) ?: return

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.data["email"] == sharePreference.getString("email", "") && document.data["password"] == sharePreference.getString("password", "")) {
                        this.startActivity(Intent(this, MainMenu::class.java))
                        showToast(this, "Login Successful")
                    }
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        openNewActivity(findViewById(R.id.button_signin), this, SignIn::class.java)

        val button = findViewById<Button>(R.id.button_signup_app)
        button.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Creating Account")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val email = findViewById<EditText>(R.id.editText_email)
            val password = findViewById<EditText>(R.id.editText_password)
            if (email.text.toString().isEmpty() || password.text.toString().isEmpty()) {
                progressDialog.dismiss()
                showToast(this, "Please fill all the fields")
                return@setOnClickListener
            }
            val user = hashMapOf(
                "email" to email.text.toString(),
                "password" to password.text.toString()
            )
            var flag = false

            db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.data["email"] == email.text.toString())
                            flag = true
                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                    if (!flag) {
                        db.collection("users")
                            .add(user)
                            .addOnSuccessListener { documentReference ->
                                progressDialog.dismiss()
                                showToast(this, "Account Created Successfully")
                                this.startActivity(Intent(this, MainMenu::class.java))
                                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                        return@addOnSuccessListener
                    } else {
                        showToast(this, "Account Already Exists")
                        progressDialog.dismiss()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }
    }
}