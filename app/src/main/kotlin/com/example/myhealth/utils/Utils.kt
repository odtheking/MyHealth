package com.example.myhealth.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.myhealth.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.time.LocalDate

fun openNewActivity(button: Button, context: Context, activityClass: Class<*>) = button.setOnClickListener { context.startActivity(Intent(context, activityClass)) }


@SuppressLint("StaticFieldLeak")
val db = Firebase.firestore

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

@RequiresApi(Build.VERSION_CODES.O)
fun updateDBFiles(bigFolderList: List<Folder>) {
    val userId = CurrentUser.instance.id

    if (userId.isNullOrEmpty()) {
        throw IllegalArgumentException("Invalid user ID.")
    }

    val userDocumentRef = db.collection("users").document(userId)
    val casesCollectionRef = userDocumentRef.collection("cases")

    // Step 1: Delete existing `cases` collection
    casesCollectionRef.get().addOnSuccessListener { snapshot ->
        val batch = db.batch()
        for (document in snapshot.documents) {
            batch.delete(document.reference)
        }
        batch.commit().addOnSuccessListener {
            Log.d("TAG", "Existing cases collection deleted successfully!")
            for (folder in bigFolderList) {
                casesCollectionRef.add(folder)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
                    }.addOnFailureListener { exception ->
                        Log.w("TAG", "Error adding document", exception)
                    }
            }

        }.addOnFailureListener { exception ->
            Log.w("TAG", "Error deleting existing cases collection.", exception)
        }
    }.addOnFailureListener { exception ->
        Log.w("TAG", "Error getting existing cases collection.", exception)
    }
}

// Extension function to convert Folder object to a map
@RequiresApi(Build.VERSION_CODES.O)
fun Folder.toMap(): Map<String, Any> {
    val subFilesMap = subFolders.map { it.toMap() }
    return mapOf(
        "name" to name,
        "date" to mapOf(
            "year" to date.year,
            "monthValue" to date.monthValue,
            "dayOfMonth" to date.dayOfMonth
        ),
        "subFiles" to subFilesMap
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun SubFolder.toMap(): Map<String, Any> {
    return mapOf(
        "name" to name,
        "date" to mapOf(
            "year" to date.year,
            "monthValue" to date.monthValue,
            "dayOfMonth" to date.dayOfMonth
        ),
        "documents" to documents.map { it.toMap() }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun Document.toMap(): Map<String, Any> {
    return mapOf(
        "name" to name,
        "date" to mapOf(
            "year" to date.year,
            "monthValue" to date.monthValue,
            "dayOfMonth" to date.dayOfMonth
        ),
        "content" to content,
        "fileType" to fileType
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun mapToFolder(map: Map<String, Any>): Folder {
    val dateMap = map["date"] as Map<String, Number>
    val date = LocalDate.of(dateMap["year"]!!.toInt(), dateMap["monthValue"]!!.toInt(), dateMap["dayOfMonth"]!!.toInt())
    val subFilesList = (map["subFiles"] as List<Map<String, Any>>).map { mapToSubFolder(it) }.toMutableList()
    return Folder(
        map["name"] as String,
        date,
        subFilesList,

    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun mapToSubFolder(map: Map<String, Any>): SubFolder {
    val dateMap = map["date"] as Map<String, Number>
    val date = LocalDate.of(dateMap["year"]!!.toInt(), dateMap["monthValue"]!!.toInt(), dateMap["dayOfMonth"]!!.toInt())
    val documentsList = (map["documents"] as List<Map<String, Any>>).map { mapToDocument(it) }.toMutableList()
    return SubFolder(
        map["name"] as String,
        date,
        documentsList,
        map["id"].toString()
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun mapToDocument(map: Map<String, Any>): Document {
    val dateMap = map["date"] as Map<String, Number>
    val date = LocalDate.of(dateMap["year"]!!.toInt(), dateMap["monthValue"]!!.toInt(), dateMap["dayOfMonth"]!!.toInt())
    return Document(
        map["name"] as String,
        date,
        map["content"] as String,
        map["fileType"] as String,
        map["folderId"] as String,
        map["subFolderId"] as String,
        map["documentId"] as String
    )
}


@RequiresApi(Build.VERSION_CODES.O)
fun getDateFromDatePicker(datePicker: DatePicker): LocalDate {
    val year = datePicker.year
    val month = datePicker.month + 1 // Month is zero-based
    val day = datePicker.dayOfMonth
    return LocalDate.of(year, month, day)
}






