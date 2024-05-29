package com.example.myhealth

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myhealth.articles.ArticleActivity
import com.example.myhealth.calender.Appointment
import com.example.myhealth.calender.AppointmentWorker
import com.example.myhealth.document.MainFolderActivity
import com.example.myhealth.calender.CalenderActivity
import com.example.myhealth.calender.deleteAppointment
import com.example.myhealth.calender.doctorsList
import com.example.myhealth.signin.SignUp
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.utils.db
import com.example.myhealth.utils.showToast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class MainMenu : ComponentActivity() {

    private lateinit var sharePreference: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        sharePreference = getSharedPreferences("MyPrefsFile", MODE_PRIVATE) ?: return
        if (sharePreference.getString("email", "") == null) {
            this.startActivity(Intent(this, SignUp::class.java))
        } else if (sharePreference.getString("email", "") != null) {
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
                    fetchAppointments()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }

        val createCase = findViewById<View>(R.id.drive)
        createCase.isClickable = true
        createCase.setOnClickListener {
            this.startActivity(Intent(this, MainFolderActivity::class.java))
        }

        val calendar = findViewById<View>(R.id.calendar)
        calendar.isClickable = true
        calendar.setOnClickListener {
            this.startActivity(Intent(this, CalenderActivity::class.java))
        }

        val articles = findViewById<View>(R.id.document)
        articles.isClickable = true
        articles.setOnClickListener {
            this.startActivity(Intent(this, ArticleActivity::class.java))
        }
        scheduleAppointmentCheck()

        // Find the soonest appointment

    //openNewActivity(findViewById(R.id.drive), this, CreateCase::class.java)

        //openNewActivity(findViewById(R.id.calendar), this, Calendar::class.java)

        //openNewActivity(findViewById(R.id.document), this, History::class.java)

        //openNewActivity(findViewById(R.id.login), this, Settings::class.java)

    }

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
                getSoonestAppointment()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents: ", e)
            }
    }



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
