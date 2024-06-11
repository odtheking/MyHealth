package com.example.myhealth.calender

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.R
import com.example.myhealth.utils.CurrentUser
import com.example.myhealth.utils.db
import com.example.myhealth.utils.getDateFromDatePicker
import com.example.myhealth.utils.showToast

class CalenderAdapter(private var myObjects: List<Appointment>) :
    RecyclerView.Adapter<CalenderAdapter.MyObjectViewHolder>() {


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Appointment>) {
        myObjects = newList
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyObjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.appointment, parent, false)
        return MyObjectViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyObjectViewHolder, position: Int) {
        val myObject = myObjects[position]
        holder.bind(myObject)
    }

    override fun getItemCount(): Int {
        return myObjects.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    inner class MyObjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        private val buttonThreeDots: Button = itemView.findViewById(R.id.buttonThreeDots)

        init {
            buttonThreeDots.setOnClickListener {
                handleThreeDotsClick()
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(myObject: Appointment) {
            textViewName.text = myObject.doctorName
            textViewDescription.text = "${myObject.date} - ${myObject.time}"
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun handleThreeDotsClick() {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                showOptionsDialog(position)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun showOptionsDialog(position: Int) {
            val options = arrayOf("Rename", "Change Date", "Remove")

            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> renameFunction(position)
                        1 -> changeDateFunction(position)
                        2 -> removeFunction(position)
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun renameFunction(position: Int) {
            val context = itemView.context
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)

            val view: View = inflater.inflate(R.layout.dialog_text_input, null)
            val editText: EditText = view.findViewById(R.id.editText)

            builder.setView(view)
                .setTitle("Enter Text")
                .setPositiveButton("OK") { dialog, _ ->
                    val enteredText = editText.text.toString()

                    if (enteredText.isNotEmpty()) {
                        showToast(context, "Entered Text: $enteredText")

                        editAppointment(Appointment(enteredText, doctorsList[position].date, doctorsList[position].time, doctorsList[position].id))
                        notifyDataSetChanged()
                    } else {
                        showToast(context, "Text is empty. Button text not changed.")
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    showToast(context, "Dialog canceled")
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
        private fun changeDateFunction(position: Int) {
            val context = itemView.context
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)

            val view: View = inflater.inflate(R.layout.dialog_date_picker, null)
            val datePicker: DatePicker = view.findViewById(R.id.datePicker)

            builder.setView(view)
                .setTitle("Select Date")
                .setPositiveButton("OK") { dialog, _ ->
                    val selectedDate = getDateFromDatePicker(datePicker)
                    val year = selectedDate.year
                    val month = selectedDate.monthValue
                    val day = selectedDate.dayOfMonth
                    editAppointment(Appointment(doctorsList[position].doctorName, "$day-${month + 1}-$year", doctorsList[position].time, doctorsList[position].id))

                    showToast(context, "Selected Date: $selectedDate")
                    updateList(doctorsList)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    showToast(context, "Dialog canceled")
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun removeFunction(position: Int) {
            val context = itemView.context
            deleteAppointment(doctorsList[position])
            showToast(context, "Remove clicked for item at position $position")
        }
    }

    fun editAppointment(appointment: Appointment) {
        if (appointment.id.isNotEmpty()) {
            db.collection("users").document(CurrentUser.instance.id).collection("appointments").document(appointment.id)
                .update(
                    "doctorName", appointment.doctorName,
                    "date", appointment.date,
                    "time", appointment.time,
                    "id", appointment.id
                )
                .addOnSuccessListener {
                    Log.d(TAG, "Appointment updated successfully")
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error updating appointment", exception)
                }
        } else {
            Log.w(TAG, "Appointment ID is null. Cannot update.")
        }

    }
}

fun deleteAppointment(appointment: Appointment) {
    db.collection("users").document(CurrentUser.instance.id).collection("appointments").document(appointment.id)
        .delete()
        .addOnSuccessListener {
            Log.d(TAG, "Appointment deleted successfully")
        }
        .addOnFailureListener { exception ->
            Log.w(TAG, "Error deleting appointment", exception)
        }
}