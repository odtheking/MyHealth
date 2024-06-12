package com.example.myhealth.document

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.R
import com.example.myhealth.subfolder.SubFolderActivity
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.utils.Document
import com.example.myhealth.utils.bigFolderList
import com.example.myhealth.utils.createNewDocument
import com.example.myhealth.utils.db
import com.example.myhealth.utils.mapToFolder
import com.example.myhealth.utils.showToast
import java.io.InputStream
import java.time.LocalDate

class DocumentActivity : ComponentActivity() {

    private lateinit var sortSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var documentAdapter: DocumentAdapter
    private lateinit var caseNameTextView: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.documents)

        initViews()
        setupRecyclerView()
        setupCaseName()
        setupSortSpinner()
        setupRealtimeUpdates()

        val plusButton = findViewById<View>(R.id.circleButton)
        plusButton.setOnClickListener {

            val options = arrayOf("Upload")

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

    private fun initViews() {
        sortSpinner = findViewById(R.id.sortSpinner)
        recyclerView = findViewById(R.id.recyclerView)
        caseNameTextView = findViewById(R.id.documentName)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val folderId = intent.getStringExtra("folderId") ?: ""
        val subFolderPosition = intent.getIntExtra("subFolderPosition", 0)
        documentAdapter = DocumentAdapter(emptyList(), folderId, subFolderPosition) // Initial empty list
        recyclerView.adapter = documentAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun setupCaseName() {
        val folderId = intent.getStringExtra("folderId") ?: return
        val subFolderPosition = intent.getIntExtra("subFolderPosition", 0)
        val folder = bigFolderList.find { it.folderId == folderId } ?: return
        val subFolder = folder.subFolders[subFolderPosition]
        caseNameTextView.text = "My ${subFolder.name}"
    }

    private fun setupSortSpinner() {
        val sortOptions = arrayOf("Sort by Date", "Sort by Name (A-Z)", "Sort by Name (Z-A)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                handleSorting(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleSorting(sortOption: Int) {
        val folderId = intent.getStringExtra("folderId")
        val subFolderId = intent.getStringExtra("subFolderId")
        val folder = bigFolderList.find { it.folderId == folderId } ?: return
        val subFolder = folder.subFolders.find { it.folderId == subFolderId } ?: return
        when (sortOption) {
            SubFolderActivity.SORT_BY_DATE -> sortByDate(subFolder.documents)
            SubFolderActivity.SORT_BY_NAME_ASCENDING -> sortByNameAscending(subFolder.documents)
            SubFolderActivity.SORT_BY_NAME_DESCENDING -> sortByNameDescending(subFolder.documents)
        }
    }

    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_FILE_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    val selectedFileUri: Uri? = data?.data
                    if (selectedFileUri != null) {
                        // Get the file name from the URI
                        val fileName = getFileName(selectedFileUri)

                        // Read the content of the file
                        val fileContent = readContentFromUri(selectedFileUri)

                        val folderId = intent.getStringExtra("folderId") ?: return
                        val subFolderPosition = intent.getIntExtra("subFolderPosition", 0)
                        createNewDocument(
                            fileName,
                            LocalDate.now(),
                            "pdf",
                            fileContent,
                            folderId,
                            subFolderPosition
                        )
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
        intent.type = "application/pdf"  // Set the MIME type to PDF

        // Optionally, you can add a chooser to make it clearer to the user that they should pick a PDF file
        val chooser = Intent.createChooser(intent, "Select a PDF file")

        // Start the file picker activity
        startActivityForResult(chooser, PICK_FILE_REQUEST_CODE)
    }


    private fun scanFunction() {
        // Create an intent to open the camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Start the camera activity
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortByDate(unSortedList: List<Document>) {
        // Sort the list by date and update the adapter
        val sortedList = unSortedList.sortedBy { it.date }
        updateAdapter(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortByNameAscending(unSortedList: List<Document>) {
        // Sort the list by name (A-Z) and update the adapter
        val sortedList = unSortedList.sortedBy { it.name }
        updateAdapter(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortByNameDescending(unSortedList: List<Document>) {
        // Sort the list by name (Z-A) and update the adapter
        val sortedList = unSortedList.sortedByDescending { it.name }
        updateAdapter(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAdapter(sortedList: List<Document>) {
        // Update the adapter with the sorted list
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val adapter = recyclerView.adapter as DocumentAdapter
        adapter.updateList(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun setupRealtimeUpdates() {
        if (CurrentUser.instance.id.isEmpty()) return
        val folderId = intent.getStringExtra("folderId")
        val subFolderPosition = intent.getIntExtra("subFolderPosition", 0)
        db.collection("users").document(CurrentUser.instance.id).collection("cases")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                val updatedFolders = snapshots.documents.map { document ->
                    mapToFolder(document.data as Map<String, Any>, document.id)
                }

                bigFolderList.clear()
                bigFolderList.addAll(updatedFolders)
                documentAdapter.updateList(bigFolderList.find { it.folderId == folderId }?.subFolders?.get(subFolderPosition)?.documents ?: emptyList())
            }
    }
}