package com.example.myhealth.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myhealth.R

fun openNewActivity(button: Button, context: Context, activityClass: Class<*>) = button.setOnClickListener { context.startActivity(Intent(context, activityClass)) }

@SuppressLint("MissingInflatedId")
fun openTextInputDialog(context: Context, buttonToUpdate: Button) {
    val builder = AlertDialog.Builder(context)
    val inflater = LayoutInflater.from(context)

    val view: View = inflater.inflate(R.layout.dialog_text_input, null)
    val editText: EditText = view.findViewById(R.id.editText)

    builder.setView(view)
        .setTitle("Enter Text")
        .setPositiveButton("OK") { dialog, _ ->
            // Handle OK button click events here
            val enteredText = editText.text.toString()
            if (enteredText.isNotEmpty()) {
                showToast(context, "Entered Text: $enteredText")
                buttonToUpdate.text = enteredText
                folderName = enteredText
            } else {
                showToast(context, "Text is empty. Button text not changed.")
            }
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            // Handle cancel button click events here
            showToast(context, "Dialog canceled")
            dialog.dismiss()
        }

    val dialog: AlertDialog = builder.create()
    dialog.show()
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}






