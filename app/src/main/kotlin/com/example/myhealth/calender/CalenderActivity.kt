package com.example.myhealth.calender

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
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.utils.db

val doctorsList = mutableListOf<Appointment>()

data class Appointment(
    var doctorName: String,
    var date: String,
    val time: String,
    var id: String,
)

class CalenderActivity : ComponentActivity() {

    private lateinit var sortSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var calenderAdapter: CalenderAdapter

    companion object {
        const val SORT_BY_DATE = 0
        const val SORT_BY_NAME_ASCENDING = 1
        const val SORT_BY_NAME_DESCENDING = 2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calender)
        calenderAdapter = CalenderAdapter(doctorsList)

        val createCase = findViewById<View>(R.id.circleButtonCalender)
        createCase?.isClickable = true
        createCase.setOnClickListener {
            this.startActivity(Intent(this, AppointmentActivity::class.java))
        }

        setupViews()
        setupRecyclerView()
        setupSortSpinner()
        setupRealtimeUpdates()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun setupRealtimeUpdates() {
        db.collection("users").document(CurrentUser.instance.id).collection("appointments")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener
                doctorsList.clear()
                for (document in snapshots) {
                    val doctorName = document.data["doctorName"]
                    val date = document.data["date"]
                    val time = document.data["time"]
                    doctorsList.add(Appointment(doctorName as String, date as String, time as String, document.id))
                    Log.d(TAG, "${document.id} UPDATING => $doctorName")
                }
                calenderAdapter.updateList(doctorsList)
            }
    }

    private fun setupViews() {
        sortSpinner = findViewById(R.id.sortSpinner)
        recyclerView = findViewById(R.id.recyclerView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = calenderAdapter
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
        val sortedList = doctorsList.sortedBy { it.date }
        calenderAdapter.updateList(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortByNameAscending() {
        val sortedList = doctorsList.sortedBy { it.doctorName }
        calenderAdapter.updateList(sortedList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sortByNameDescending() {
        val sortedList = doctorsList.sortedByDescending { it.doctorName }
        calenderAdapter.updateList(sortedList)
    }
}