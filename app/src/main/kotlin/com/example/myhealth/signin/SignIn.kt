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
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.utils.db
import com.example.myhealth.utils.showToast

class SignIn : ComponentActivity() {

    private lateinit var sharePreference: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        sharePreference = getSharedPreferences("MyPrefsFile", MODE_PRIVATE) ?: return
        val editor = sharePreference.edit()

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.data["email"] == sharePreference.getString("email", "") && document.data["password"] == sharePreference.getString("password", "")) {
                        CurrentUser.createInstance(document.data["email"].toString(), document.data["password"].toString(), document.id)
                        this.startActivity(Intent(this, MainMenu::class.java))
                        showToast(this, "Login Successful")
                        finish()
                    }
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
        val button = findViewById<Button>(R.id.button_signin_app)
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


            db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.data["email"] == email.text.toString() && document.data["password"] == password.text.toString()) {
                            editor.putBoolean("isLogged", true)
                            editor.putString("email", email.text.toString())
                            editor.putString("password", password.text.toString())
                            editor.putString("id", document.id)
                            editor.apply()
                            this.startActivity(Intent(this, MainMenu::class.java))
                            progressDialog.dismiss()
                            showToast(this, "Login Successful")
                        }
                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }
    }

}