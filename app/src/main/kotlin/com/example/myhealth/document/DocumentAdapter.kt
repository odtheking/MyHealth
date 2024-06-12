package com.example.myhealth.document

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.R
import com.example.myhealth.utils.Document
import com.example.myhealth.utils.bigFolderList
import com.example.myhealth.utils.deleteDocument
import com.example.myhealth.utils.editFolder
import com.example.myhealth.utils.showToast
import java.io.File
import java.io.InputStream
import java.time.LocalDate

class DocumentAdapter(private var myObjects: List<Document>, private val folderId: String, private val subFolderPosition: Int) :
    RecyclerView.Adapter<DocumentAdapter.MyObjectViewHolder>() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Document>) {
        myObjects = newList
        notifyDataSetChanged()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyObjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
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
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val content = myObjects[position].content
                    val pdfFile = saveContentToTempFile(itemView.context, content)
                    openPdfWithChooser(itemView.context, pdfFile)
                }
            }

            // Set click listener for the three-dots button
            buttonThreeDots.setOnClickListener {
                handleThreeDotsClick(adapterPosition, itemView.context)
            }
        }

        fun bind(myObject: Document) {
            textViewName.text = myObject.name
            textViewDescription.text = myObject.date.toString()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun handleThreeDotsClick(position: Int, context: Context) {
        val options = arrayOf("Share", "Rename", "Change Date", "Remove")

        // Create AlertDialog.Builder
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareFunction(context)
                    1 -> renameFunction(context, position)
                    2 -> changeDateFunction(context, position)
                    3 -> removeFunction(context, position)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        // Create and show the AlertDialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun shareFunction(context: Context) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject") // Optional subject

        try {
            context.startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            showToast(context, "Error sharing: ${e.message}")
        }
    }

    fun readContentFromUri(uri: Uri, contentResolver: ContentResolver): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val content = inputStream?.bufferedReader().use { it?.readText() } ?: ""
        inputStream?.close()
        return content
    }

    fun saveContentToTempFile(context: Context, pdfContent: String): File {
        val tempFile = File(context.cacheDir, "temp.pdf")
        tempFile.writeText(pdfContent)
        return tempFile
    }

    fun openPdfWithChooser(context: Context, pdfFile: File) {
        val pdfUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pdfUri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Open PDF with")
        context.startActivity(chooser)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    private fun renameFunction(context: Context, position: Int) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)

        val view: View = inflater.inflate(R.layout.dialog_text_input, null)
        val editText: EditText = view.findViewById(R.id.editText)

        builder.setView(view)
            .setTitle("Enter Text")
            .setPositiveButton("OK") { dialog, _ ->
                // Handle OK button click events here
                val enteredText = editText.text.toString()

                if (enteredText.isNotEmpty()) {
                    showToast(context, "Entered Text: $enteredText")
                    val document = bigFolderList.find { it.folderId == folderId }?.subFolders?.get(subFolderPosition)?.documents?.get(position) ?: return@setPositiveButton
                    document.name = enteredText
                    editFolder(bigFolderList.find { it.folderId == folderId } ?: return@setPositiveButton)
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

        builder.setView(view)
            .setTitle("Select Date")
            .setPositiveButton("OK") { dialog, _ ->
                val selectedDate = getDateFromDatePicker(datePicker)
                bigFolderList.find { it.folderId == folderId }?.subFolders?.get(subFolderPosition)?.documents?.get(position)?.date = selectedDate
                editFolder(bigFolderList.find { it.folderId == folderId } ?: return@setPositiveButton)
                showToast(context, "Selected Date: $selectedDate")
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
        val month = datePicker.month + 1
        val day = datePicker.dayOfMonth
        return LocalDate.of(year, month, day)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged")
    private fun removeFunction(context: Context, position: Int) {

        deleteDocument(folderId, subFolderPosition, position)

        showToast(context, "Remove clicked for item at position $position")
    }

}
