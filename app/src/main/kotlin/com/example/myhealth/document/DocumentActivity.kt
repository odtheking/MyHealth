package com.example.myhealth.document

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.utils.Document
import com.example.myhealth.R
import com.example.myhealth.utils.bigFolderList
import com.example.myhealth.utils.createNewDocument
import com.example.myhealth.utils.showToast
import java.io.InputStream
import java.time.LocalDate

class DocumentActivity : ComponentActivity() {

    private lateinit var sortSpinner: Spinner

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.documents)

        val position = intent.getStringExtra("position")
        val folderPosition = intent.getStringExtra("positionFolder")
        val folder = bigFolderList[folderPosition!!.toInt()]
        val subFolderList = folder.subFiles
        val subFolder = subFolderList[position?.toInt()!!]
        val documentList = subFolder.documents
        val caseName = subFolder.name

        val textViewPosition: TextView = findViewById(R.id.documentName)

        textViewPosition.text = caseName

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter1 = DocumentAdapter(documentList)
        recyclerView.adapter = adapter1

        // Initialize views
        sortSpinner = findViewById(R.id.sortSpinner)

        // Set up Spinner with sorting options
        val sortOptions = arrayOf("Sort by Date", "Sort by Name (A-Z)", "Sort by Name (Z-A)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter


        // Set Spinner item click listener
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Handle sorting based on the selected option
                when (position) {
                    0 -> sortByDate(documentList)
                    1 -> sortByNameAscending(documentList)
                    2 -> sortByNameDescending(documentList)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        val plusButton = findViewById<View>(R.id.circleButton)
        plusButton.setOnClickListener {

            val options = arrayOf("Upload", "Scan")

            // Create AlertDialog.Builder
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> uploadFunction()
                        1 -> scanFunction()
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

            // Create and show the AlertDialog
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_FILE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    println("File picked")
                    val selectedFileUri: Uri? = data?.data
                    if (selectedFileUri != null) {
                        // Get the file name from the URI
                        val fileName = getFileName(selectedFileUri)

                        // Read the content of the file
                        val fileContent = readContentFromUri(selectedFileUri)

                        val position = intent.getStringExtra("position")
                        val folderPosition = intent.getStringExtra("positionFolder")
                        val folder = bigFolderList[folderPosition!!.toInt()]
                        val subFolderList = folder.subFiles
                        val subFolder = subFolderList[position?.toInt()!!]
                        println("Subfolder: $subFolder")
                        createNewDocument(
                            fileName,
                            LocalDate.now(),
                            subFolder,
                            "pdf",
                            fileContent
                        )
                        println(subFolder.documents)


                        showToast(this, "Document Added: $fileName")


                    }
                }
            }


        }
    }


    private fun readContentFromUri(uri: Uri): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val content = inputStream?.bufferedReader().use { it?.readText() } ?: ""
        inputStream?.close()
        return content
    }

    // Helper function to get the file name from the URI
    private fun getFileName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val fileNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (fileNameIndex != -1) {
                return it.getString(fileNameIndex)
            }
        }
        return ""
    }

    private val PICK_FILE_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 2

    private fun uploadFunction() {
        // Create an intent to open the file picker
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"  // Set the MIME type to all files

        // Start the file picker activity
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    private fun scanFunction() {
        // Create an intent to open the camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Start the camera activity
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }


    private fun sortByDate(unSortedList: List<Document>) {
        // Sort the list by date and update the adapter
        val sortedList = unSortedList.sortedBy { it.date }
        updateAdapter(sortedList)
    }

    private fun sortByNameAscending(unSortedList: List<Document>) {
        // Sort the list by name (A-Z) and update the adapter
        val sortedList = unSortedList.sortedBy { it.name }
        updateAdapter(sortedList)
    }

    private fun sortByNameDescending(unSortedList: List<Document>) {
        // Sort the list by name (Z-A) and update the adapter
        val sortedList = unSortedList.sortedByDescending { it.name }
        updateAdapter(sortedList)
    }

    private fun updateAdapter(sortedList: List<Document>) {
        // Update the adapter with the sorted list
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val adapter = recyclerView.adapter as DocumentAdapter
        adapter.updateList(sortedList)
    }
}