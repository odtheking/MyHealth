package com.example.myhealth.subfolder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.R
import com.example.myhealth.utils.SubFolder
import com.example.myhealth.utils.bigFolderList
import com.example.myhealth.utils.createNewSubFolder
import com.example.myhealth.utils.showToast

class SubFolderActivity : ComponentActivity() {

    private lateinit var sortSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubFolderAdapter
    private lateinit var caseNameTextView: TextView

    companion object {
        const val SORT_BY_DATE = 0
        const val SORT_BY_NAME_ASCENDING = 1
        const val SORT_BY_NAME_DESCENDING = 2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sub_folder)

        initViews()
        setupRecyclerView()
        setupCaseName()
        setupSortSpinner()
        setupPlusButton()
    }

    private fun initViews() {
        sortSpinner = findViewById(R.id.sortSpinner)
        recyclerView = findViewById(R.id.recyclerView)
        caseNameTextView = findViewById(R.id.caseName)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SubFolderAdapter(emptyList()) // Initial empty list
        recyclerView.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun setupCaseName() {
        val position = intent.getStringExtra("position")
        val folder = bigFolderList[position!!.toInt()]
        caseNameTextView.text = "My ${folder.name}"
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPlusButton() {
        val plusButton = findViewById<View>(R.id.circleButtonSubFile)
        plusButton.setOnClickListener {
            showCreateSubFolderDialog()
        }
    }

    private fun handleSorting(sortOption: Int) {
        when (sortOption) {
            SORT_BY_DATE -> sortByDate(bigFolderList[intent.getStringExtra("position")?.toInt()!!].subFiles)
            SORT_BY_NAME_ASCENDING -> sortByNameAscending(bigFolderList[intent.getStringExtra("position")?.toInt()!!].subFiles)
            SORT_BY_NAME_DESCENDING -> sortByNameDescending(bigFolderList[intent.getStringExtra("position")?.toInt()!!].subFiles)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCreateSubFolderDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)

        val view: View = inflater.inflate(R.layout.dialog_text_input, null)
        val editText: EditText = view.findViewById(R.id.editText)

        builder.setView(view)
            .setTitle("Enter Text")
            .setPositiveButton("OK") { dialog, _ ->
                // Handle OK button click events here
                val enteredText = editText.text.toString()
                createNewSubFolder(enteredText, biggerFolder = bigFolderList[intent.getIntExtra("position", 0)])

                if (enteredText.isNotEmpty()) {
                    showToast(this, "Entered Text: $enteredText")
                } else {
                    showToast(this, "Text is empty. Button text not changed.")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Handle cancel button click events here
                showToast(this, "Dialog canceled")
                dialog.dismiss()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun sortByDate(unSortedList: List<SubFolder>) {
        val sortedList = unSortedList.sortedBy { it.date }
        updateAdapter(sortedList)
    }

    private fun sortByNameAscending(unSortedList: List<SubFolder>) {
        val sortedList = unSortedList.sortedBy { it.name }
        updateAdapter(sortedList)
    }

    private fun sortByNameDescending(unSortedList: List<SubFolder>) {
        val sortedList = unSortedList.sortedByDescending { it.name }
        updateAdapter(sortedList)
    }

    private fun updateAdapter(sortedList: List<SubFolder>) {
        adapter.updateList(sortedList)
    }
}
