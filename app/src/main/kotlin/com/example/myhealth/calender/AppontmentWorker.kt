package com.example.myhealth.calender

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myhealth.R
import com.example.myhealth.getSoonestAppointment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AppointmentWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        // Fetch the next appointment
        val soonestAppointment = getSoonestAppointment() // Replace with your method to fetch appointments
        val dateFormatter = DateTimeFormatter.ofPattern("d-M-yyyy")

        if (soonestAppointment == null) return Result.success()

        val daysUntilNextAppointment = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(soonestAppointment.date, dateFormatter))
        if (daysUntilNextAppointment < 0) sendNotification(soonestAppointment)

        return Result.success()
    }

    private fun sendNotification(appointment: Appointment) {
        val channelId = "appointment_channel"
        val notificationId = 1

        createNotificationChannel(channelId)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.doctor)
            .setContentTitle("Upcoming Appointment")
            .setContentText("You have an appointment tomorrow: ${appointment.doctorName}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, notify user to grant the permission
                requestNotificationPermission()
                return
            }
            notify(notificationId, notification)
        }
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Appointment Notifications"
            val descriptionText = "Notifications for upcoming appointments"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        // Since we cannot directly request permissions from a Worker, you can either:
        // 1. Schedule a task to request permissions in the main activity.
        // 2. Handle the logic within the main activity.
        // Here we will use a BroadcastReceiver approach to notify the activity.
        val intent = Intent("com.example.myhealth.REQUEST_NOTIFICATION_PERMISSION")
        applicationContext.sendBroadcast(intent)
    }
}