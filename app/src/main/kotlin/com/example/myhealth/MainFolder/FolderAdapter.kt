package com.example.myhealth.MainFolder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.utils.Folder
import com.example.myhealth.R
import com.example.myhealth.utils.bigFolderList
import com.example.myhealth.utils.showToast
import com.example.myhealth.subfolder.SubFolderActivity
import java.time.LocalDate

class FolderAdapter(private var myObjects: List<Folder>) :
    RecyclerView.Adapter<FolderAdapter.MyObjectViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Folder>) {
        myObjects = newList
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyObjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
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
            itemView.setOnClickListener {
                handleItemClick()
            }

            buttonThreeDots.setOnClickListener {
                handleThreeDotsClick()
            }
        }

        fun bind(myObject: Folder) {
            textViewName.text = myObject.name
            textViewDescription.text = myObject.date.toString()
        }

        private fun handleItemClick() {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val intent = Intent(itemView.context, SubFolderActivity::class.java)
                intent.putExtra("position", adapterPosition.toString())
                itemView.context.startActivity(intent)
            }
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
                    bigFolderList[position].name = enteredText
                    notifyDataSetChanged()
                    if (enteredText.isNotEmpty()) {
                        showToast(context, "Entered Text: $enteredText")
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
                    bigFolderList[position].date = selectedDate
                    notifyDataSetChanged()
                    showToast(context, "Selected Date: $selectedDate")
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
            bigFolderList.remove(bigFolderList[position])
            updateList(bigFolderList)
            showToast(context, "Remove clicked for item at position $position")
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun getDateFromDatePicker(datePicker: DatePicker): LocalDate {
            val year = datePicker.year
            val month = datePicker.month + 1 // Month is zero-based
            val day = datePicker.dayOfMonth
            return LocalDate.of(year, month, day)
        }
    }
}
