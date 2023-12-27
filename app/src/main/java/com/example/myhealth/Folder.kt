package com.example.myhealth

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

var folderName: String? = null

val bigFolderList: MutableList<Folder> = mutableListOf()

data class Document(
    var name: String,
    var date: LocalDate,
    val content: String,
    val fileType: String
)

class SubFolder(var name: String, var date: LocalDate, var documents: MutableList<Document>)

class Folder(var name: String, var date: LocalDate, var subFiles: MutableList<SubFolder>)


@RequiresApi(Build.VERSION_CODES.O)
fun createNewDocument(fileName: String, date: LocalDate = LocalDate.now(), biggerFolder: SubFolder, fileType: String, content: String) {
    // Create a new list by copying the existing documents and adding the new document
    biggerFolder.documents = (biggerFolder.documents + Document(fileName, date, content, fileType)).toMutableList()
}

@RequiresApi(Build.VERSION_CODES.O)
fun createNewSubFolder(fileName: String, date: LocalDate = LocalDate.now(), biggerFolder: Folder) {
    val subFolder = SubFolder(fileName, date, mutableListOf())
    // Use the "plus" operator to create a new list with the added SubFolder
    biggerFolder.subFiles = (biggerFolder.subFiles + subFolder).toMutableList()
}

@SuppressLint("NewApi")
fun createNewFolder(fileName: String, date: LocalDate = LocalDate.now(), biggerFolder: MutableList<Folder>) {
    val folder = Folder(fileName, date, mutableListOf())
    createNewSubFolder("Test Results", date, folder)
    createNewSubFolder("Summaries", date, folder)
    createNewSubFolder("Prescriptions", date, folder)
    createNewSubFolder("Invoices", date, folder)

    // Use the "plus" operator to create a new list with the added Folder
    biggerFolder.add(folder)
}


fun main() {

    // Creating a file with subfiles
    val file = createNewFolder("Main File", biggerFolder = bigFolderList)

    // Creating a subfile with documents
    printFileStructure(bigFolderList[bigFolderList.size - 1])
}

fun printFileStructure(file: Folder, depth: Int = 0) {
    println("${"\t".repeat(depth)}File: ${file.name} - ${file.date}")

    for (subFile in file.subFiles) {
        println("${"\t".repeat(depth + 1)}Subfile: ${subFile.name}")

        for (document in subFile.documents) {
            println("${"\t".repeat(depth + 2)}Document: ${document.name} - ${document.date}")
            println("${"\t".repeat(depth + 2)}Content: ${document.content}")
        }
    }
}
