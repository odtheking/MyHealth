package com.example.myhealth.calender

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myhealth.R
import com.example.myhealth.getSoonestAppointment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@SuppressLint("SpecifyJobSchedulerIdRange")
class AppointmentJobService : JobService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartJob(params: JobParameters?): Boolean {
        // Run the job on a background thread to avoid blocking the main thread
        Thread {
            val soonestAppointment = getSoonestAppointment() // Replace with your method to fetch appointments
            val dateFormatter = DateTimeFormatter.ofPattern("d-M-yyyy")

            if (soonestAppointment != null) {
                val daysUntilNextAppointment = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(soonestAppointment.date, dateFormatter))
                if (daysUntilNextAppointment < 0) sendNotification(soonestAppointment)
            }
            // Job finished
            jobFinished(params, false)
        }.start()

        // Return true as the job is being executed in the background
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        // Return true to reschedule the job if it was terminated before completion
        return true
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
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        val intent = Intent("com.example.myhealth.REQUEST_NOTIFICATION_PERMISSION")
        applicationContext.sendBroadcast(intent)
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun scheduleAppointmentJob(context: Context) {
    val componentName = ComponentName(context, AppointmentJobService::class.java)
    val jobInfo = JobInfo.Builder(123, componentName)
        .setPeriodic(15 * 60 * 1000) // Schedule job every 15 minutes
        .setPersisted(true) // Persist across reboots
        .build()

    val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    jobScheduler.schedule(jobInfo)
}