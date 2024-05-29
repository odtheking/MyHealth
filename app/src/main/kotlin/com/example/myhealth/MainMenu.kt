package com.example.myhealth

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myhealth.articles.ArticleActivity
import com.example.myhealth.calender.Appointment
import com.example.myhealth.calender.AppointmentWorker
import com.example.myhealth.main.MainFolderActivity
import com.example.myhealth.calender.CalenderActivity
import com.example.myhealth.calender.deleteAppointment
import com.example.myhealth.calender.doctorsList
import com.example.myhealth.signin.HomePage
import com.example.myhealth.signin.SignUp
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.utils.db
import com.example.myhealth.utils.showToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * The main menu screen of the application.
 */
class MainMenu : ComponentActivity() {

    private lateinit var sharePreference: SharedPreferences

    /**
     * Initializes the main menu activity.
     * @param savedInstanceState The saved instance state.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Initialize shared preferences
        sharePreference = getSharedPreferences("MyPrefsFile", MODE_PRIVATE) ?: return

        // Check if user is logged in, if not, redirect to SignUp activity
        if (sharePreference.getString("email", "") == null) {
            this.startActivity(Intent(this, SignUp::class.java))
        } else if (sharePreference.getString("email", "") != null) {
            // If user is logged in, show progress dialog and fetch appointments
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Logging in...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        if (document.data["email"] == sharePreference.getString("email", "") && document.data["password"] == sharePreference.getString("password", "")) {
                            CurrentUser.createInstance(document.data["email"].toString(), document.data["password"].toString(), document.id)
                            //this.startActivity(Intent(this, MainMenu::class.java))
                            //fetchNewsData()
                            showToast(this, "Login Successful")
                        }
                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                    progressDialog.dismiss()
                    fetchAppointments()
                }
                .addOnFailureListener { exception ->
                    progressDialog.dismiss()
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Bottom navigation item click listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_drive -> {
                    this.startActivity(Intent(this, MainFolderActivity::class.java))
                    false
                }
                R.id.nav_calendar -> {
                    this.startActivity(Intent(this, CalenderActivity::class.java))
                    false
                }
                R.id.nav_document -> {
                    this.startActivity(Intent(this, ArticleActivity::class.java))
                    false
                }
                else -> false
            }
        }

        // Schedule periodic appointment check
        scheduleAppointmentCheck()
    }

    /**
     * Fetches the user's appointments from Firestore.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun fetchAppointments() {
        if (CurrentUser.instance.id.isEmpty()) return
        db.collection("users").document(CurrentUser.instance.id).collection("appointments")
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots == null) return@addOnSuccessListener

                doctorsList.clear()
                for (document in snapshots) {
                    val doctorName = document.data["doctorName"]
                    val date = document.data["date"]
                    val time = document.data["time"]
                    doctorsList.add(Appointment(doctorName as String, date as String, time as String, document.id))
                    Log.d(TAG, "${document.id} => $doctorName")
                }
                updateSoonestAppointment()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents: ", e)
            }
    }

    /**
     * Updates the UI with the details of the soonest appointment.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateSoonestAppointment() {
        val dateFormatter = DateTimeFormatter.ofPattern("d-M-yyyy")
        val soonestAppointment = doctorsList.minByOrNull { LocalDate.parse(it.date, dateFormatter) }
        println("Soonest Appointment: $soonestAppointment, $doctorsList")

        // Update the TextViews with the soonest appointment details
        val nextAppointmentTextView = findViewById<TextView>(R.id.nextAppointmentTitleTextView)
        val appointmentDetailsTextView = findViewById<TextView>(R.id.appointmentDetailsTextView)

        if (soonestAppointment != null) {
            val daysUntilNextAppointment = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(soonestAppointment.date, dateFormatter))
            if (daysUntilNextAppointment < 0) deleteAppointment(soonestAppointment)
            nextAppointmentTextView?.text = "Your next appointment is in $daysUntilNextAppointment days"

            val appointmentDateTime = "${soonestAppointment.date} - ${soonestAppointment.time}"
            val appointmentDetails = "$appointmentDateTime - ${soonestAppointment.doctorName}"
            appointmentDetailsTextView?.text = appointmentDetails
        } else {
            // If there are no appointments, display a message indicating that
            nextAppointmentTextView?.text = "No appointments scheduled"
            appointmentDetailsTextView?.text = ""
        }
    }

    /**
     * Schedules a periodic check for appointments using WorkManager.
     */
    private fun scheduleAppointmentCheck() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AppointmentWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
}



@RequiresApi(Build.VERSION_CODES.O)
fun getSoonestAppointment(): Appointment? {
    val dateFormatter = DateTimeFormatter.ofPattern("d-M-yyyy")
    return doctorsList.minByOrNull { LocalDate.parse(it.date, dateFormatter) }
}
