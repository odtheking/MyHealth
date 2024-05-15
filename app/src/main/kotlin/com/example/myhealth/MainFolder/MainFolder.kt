package com.example.myhealth.MainFolder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.R
import com.example.myhealth.utils.Folder
import com.example.myhealth.utils.bigFolderList
import com.example.myhealth.utils.openNewActivity

class MainFolder : ComponentActivity() {

    private lateinit var sortSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter

    companion object {
        const val SORT_BY_DATE = 0
        const val SORT_BY_NAME_ASCENDING = 1
        const val SORT_BY_NAME_DESCENDING = 2
    }

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
    }

    private fun setupViews() {
        sortSpinner = findViewById(R.id.sortSpinner)
        recyclerView = findViewById(R.id.recyclerView)
    }

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
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                handleSorting(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun handleSorting(sortOption: Int) {
        when (sortOption) {
            SORT_BY_DATE -> sortByDate()
            SORT_BY_NAME_ASCENDING -> sortByNameAscending()
            SORT_BY_NAME_DESCENDING -> sortByNameDescending()
        }
    }

    private fun sortByDate() {
        val sortedList = bigFolderList.sortedBy { it.date }
        updateAdapter(sortedList)
    }

    private fun sortByNameAscending() {
        val sortedList = bigFolderList.sortedBy { it.name }
        updateAdapter(sortedList)
    }

    private fun sortByNameDescending() {
        val sortedList = bigFolderList.sortedByDescending { it.name }
        updateAdapter(sortedList)
    }

    private fun updateAdapter(sortedList: List<Folder>) {
        folderAdapter.updateList(sortedList)
    }
}