package com.example.myhealth.subfolder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
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
import com.example.myhealth.R
import com.example.myhealth.document.DocumentActivity
import com.example.myhealth.utils.SubFolder
import com.example.myhealth.utils.bigFolderList
import com.example.myhealth.utils.deleteSubFolder
import com.example.myhealth.utils.editSubFolder
import com.example.myhealth.utils.showToast
import java.time.LocalDate

class SubFolderAdapter(private var myObjects: List<SubFolder>) :
    RecyclerView.Adapter<SubFolderAdapter.MyObjectViewHolder>() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<SubFolder>) {
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
            // Set click listener for the entire item
            itemView.setOnClickListener {
                handleItemClick()
            }

            // Set click listener for the three-dots button
            buttonThreeDots.setOnClickListener {
                handleThreeDotsClick()
            }
        }

        fun bind(myObject: SubFolder) {
            textViewName.text = myObject.name
            textViewDescription.text = myObject.date.toString()
        }

        private fun handleItemClick() {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val intent = Intent(itemView.context, DocumentActivity::class.java)
                intent.putExtra("folderId", intent.getStringExtra("folderId"))
                intent.putExtra("subFolderId", myObjects[position].folderId)
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
                        0 -> renameFunction(itemView.context, position)
                        1 -> changeDateFunction(itemView.context, position)
                        2 -> removeFunction(itemView.context, position)
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
        private fun renameFunction(context: Context, position: Int) {
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)

            val view: View = inflater.inflate(R.layout.dialog_text_input, null)
            val editText: EditText = view.findViewById(R.id.editText)
            val intent = Intent(context, SubFolderActivity::class.java)
            val folderId = intent.getStringExtra("folderId") ?: "0"

            builder.setView(view)
                .setTitle("Enter Text")
                .setPositiveButton("OK") { dialog, _ ->
                    val enteredText = editText.text.toString()

                    if (enteredText.isNotEmpty()) {
                        showToast(context, "Entered Text: $enteredText")
                        val folder = bigFolderList.find { it.folderId == folderId }?.subFolders?.get(position) ?: return@setPositiveButton
                        folder.name = enteredText
                        editSubFolder(folder.folderId, folder)
                    } else {
                        showToast(context, "Text is empty. Button text not changed.")
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Handle cancel button click events here
                    showToast(context, "Dialog canceled")
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
        private fun changeDateFunction(context: Context, position: Int) {
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)

            val view: View = inflater.inflate(R.layout.dialog_date_picker, null)
            val datePicker: DatePicker = view.findViewById(R.id.datePicker)
            val intent = Intent(context, SubFolderActivity::class.java)
            val folderId = intent.getStringExtra("folderId") ?: "0"

            builder.setView(view)
                .setTitle("Select Date")
                .setPositiveButton("OK") { dialog, _ ->
                    // Handle OK button click events here
                    val selectedDate = getDateFromDatePicker(datePicker)
                    val folder = bigFolderList.find { it.folderId == folderId }?.subFolders?.get(position) ?: return@setPositiveButton
                    folder.date = selectedDate
                    editSubFolder(folder.folderId, folder)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Handle cancel button click events here
                    showToast(context, "Dialog canceled")
                    dialog.dismiss()
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun getDateFromDatePicker(datePicker: DatePicker): LocalDate {
            val year = datePicker.year
            val month = datePicker.month + 1 // Month is zero-based
            val day = datePicker.dayOfMonth

            return LocalDate.of(year, month, day)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun removeFunction(context: Context, position: Int) {
        val intent = Intent(context, SubFolderActivity::class.java)
        val folderId = intent.getStringExtra("folderId") ?: "0"

        deleteSubFolder(folderId, position)
        showToast(context, "Remove clicked for item at position $position")
    }
}
