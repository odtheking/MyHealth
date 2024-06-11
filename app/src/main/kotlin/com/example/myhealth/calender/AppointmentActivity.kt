package com.example.myhealth.calender

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myhealth.R
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.utils.db
import java.util.Calendar

class AppointmentActivity : ComponentActivity() {

    private lateinit var etDoctorName: TextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvSelectedTime: TextView
    private lateinit var btnCreateAppointment: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        etDoctorName = findViewById(R.id.etDoctorName)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvSelectedTime = findViewById(R.id.tvSelectedTime)
        btnCreateAppointment = findViewById(R.id.btnCreateAppointment)

        tvSelectedDate.setOnClickListener {
            showDatePickerDialog()
        }

        tvSelectedTime.setOnClickListener {
            showTimePickerDialog()
        }

        btnCreateAppointment.setOnClickListener {
            createAppointment()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            tvSelectedDate.text = "$selectedDay-${selectedMonth + 1}-$selectedYear"
        }, year, month, day)

        datePickerDialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            tvSelectedTime.text = String.format("%02d:%02d", selectedHour, selectedMinute)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun createAppointment() {
        val doctorName = etDoctorName.text.toString()
        val date = tvSelectedDate.text.toString()
        val time = tvSelectedTime.text.toString()

        if (doctorName.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
        } else {
            db.collection("users")
                .document(CurrentUser.instance.id)
                .collection("appointments")
                .add(mapOf(
                    "doctorName" to doctorName,
                    "date" to date,
                    "time" to time
                )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Appointment Created", Toast.LENGTH_SHORT).show()
                    this.startActivity(Intent(this, CalenderActivity::class.java))
                    finish()
                }
        }
    }
}