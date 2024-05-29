package com.example.myhealth.document

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.R
import com.example.myhealth.signin.CreateCase
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.utils.Folder
import com.example.myhealth.utils.bigFolderList
import com.example.myhealth.utils.db
import com.example.myhealth.utils.mapToFolder

class MainFolderActivity : ComponentActivity() {

    private lateinit var sortSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter

    companion object {
        const val SORT_BY_DATE = 0
        const val SORT_BY_NAME_ASCENDING = 1
        const val SORT_BY_NAME_DESCENDING = 2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_folder)

        val createCase = findViewById<View>(R.id.createNewCaseButton)
        createCase.isClickable = true
        createCase.setOnClickListener {
            this.startActivity(Intent(this, CreateCase::class.java))
        }
        setupViews()
        setupRecyclerView()
        setupSortSpinner()
        setupRealtimeUpdates()
    }

    private fun setupViews() {
        sortSpinner = findViewById(R.id.sortSpinner)
        recyclerView = findViewById(R.id.recyclerView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        folderAdapter = FolderAdapter(bigFolderList)
        recyclerView.adapter = folderAdapter
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
        when (sortOption) {
            SORT_BY_DATE -> sortByDate()
            SORT_BY_NAME_ASCENDING -> sortByNameAscending()
            SORT_BY_NAME_DESCENDING -> sortByNameDescending()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortByDate() {
        val sortedList = bigFolderList.sortedBy { it.date }
        updateAdapter(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortByNameAscending() {
        val sortedList = bigFolderList.sortedBy { it.name }
        updateAdapter(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortByNameDescending() {
        val sortedList = bigFolderList.sortedByDescending { it.name }
        updateAdapter(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAdapter(sortedList: List<Folder>) {
        folderAdapter.updateList(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun setupRealtimeUpdates() {
        if (CurrentUser.instance.id.isEmpty()) return

        db.collection("users").document(CurrentUser.instance.id).collection("cases")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                val updatedFolders = snapshots.documents.map { document ->
                    mapToFolder(document.data as Map<String, Any>)
                }

                bigFolderList.clear()
                bigFolderList.addAll(updatedFolders)
                folderAdapter.updateList(bigFolderList)
            }
    }
}


