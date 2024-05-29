package com.example.myhealth.utils

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.util.UUID


var folderName: String? = null

val bigFolderList = mutableListOf<Folder>()

class Folder(var name: String, var date: LocalDate, var subFolders: MutableList<SubFolder>, var folderId: String = UUID.randomUUID().toString())

class SubFolder(var name: String, var date: LocalDate, var documents: MutableList<Document>, var folderId: String, var subFolderId: String = UUID.randomUUID().toString())

data class Document(var name: String, var date: LocalDate, val content: String, val fileType: String, var folderId: String, var subFolderId: String, var documentId: String = UUID.randomUUID().toString())

@RequiresApi(Build.VERSION_CODES.O)
fun createNewFolder(fileName: String, date: LocalDate = LocalDate.now()) {
    addFolder(Folder(fileName, date, mutableListOf()))
}

@RequiresApi(Build.VERSION_CODES.O)
fun createNewSubFolder(subFolderName: String, date: LocalDate = LocalDate.now(), folderId: String) {
    addSubFolder(SubFolder(subFolderName, date, mutableListOf(), folderId))
}

@RequiresApi(Build.VERSION_CODES.O)
fun createNewDocument(fileName: String, date: LocalDate = LocalDate.now(), fileType: String, content: String, folderId: String, subFolderId: String) {
    addDocument(Document(fileName, date, content, fileType, folderId, subFolderId))
}

@RequiresApi(Build.VERSION_CODES.O)
fun addFolder(folder: Folder) {
    val folderMap = folder.toMap()
    db.collection("users").document(CurrentUser.instance.id).collection("cases").document(folder.folderId)
        .set(folderMap)
        .addOnSuccessListener { documentReference ->
            addSubFolder(SubFolder("Test Results", LocalDate.now(), mutableListOf(), folder.folderId))
            addSubFolder(SubFolder("Summaries", LocalDate.now(), mutableListOf(), folder.folderId))
            addSubFolder(SubFolder("Prescriptions", LocalDate.now(), mutableListOf(), folder.folderId))
            addSubFolder(SubFolder("Invoices", LocalDate.now(), mutableListOf(), folder.folderId))

            Log.d(TAG, "Folder added with ID: ${folder.folderId}")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding folder", e)
        }
}

@RequiresApi(Build.VERSION_CODES.O)
fun addSubFolder(subFolder: SubFolder) {
    val userDoc = db.collection("users").document(CurrentUser.instance.id)
    val folderDoc = userDoc.collection("cases").document(subFolder.folderId)

    db.runTransaction { transaction ->
        val folderSnapshot = transaction.get(folderDoc)
        val folderData = folderSnapshot.data ?: throw Exception("Folder not found")

        val folder = mapToFolder(folderData as Map<String, Any>)

        folder.subFolders.add(subFolder)

        transaction.set(folderDoc, folder.toMap())
    }.addOnSuccessListener {
        Log.d(TAG, "SubFolder added successfully")
    }.addOnFailureListener { e ->
        Log.w(TAG, "Error adding SubFolder", e)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun addDocument(document: Document) {
    val userDoc = db.collection("users").document(CurrentUser.instance.id)
    val folderDoc = userDoc.collection("cases").document(document.folderId)

    db.runTransaction { transaction ->
        val folderSnapshot = transaction.get(folderDoc)
        val folderData = folderSnapshot.data ?: throw Exception("Folder not found")

        // Convert the folder data to a Folder object
        val folder = mapToFolder(folderData as Map<String, Any>)

        val subFolder = folder.subFolders.find { it.subFolderId == document.subFolderId }
            ?: throw Exception("SubFolder not found")

        subFolder.documents.add(document)

        transaction.set(folderDoc, folder.toMap())
    }.addOnSuccessListener {
        Log.d(TAG, "Document added successfully")
    }.addOnFailureListener { e ->
        Log.w(TAG, "Error adding document", e)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun editFolder(folder: Folder) {
    if (folder.folderId.isNotEmpty()) {
        db.collection("users").document(CurrentUser.instance.id).collection("cases").document(folder.folderId)
            .update(folder.toMap()) // Use update() to update the existing document
            .addOnSuccessListener {
                Log.d(TAG, "Folder updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error updating folder", exception)
            }
    } else {
        Log.w(TAG, "Folder ID is null. Cannot update.")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun editSubFolder(folderId: String, subFolder: SubFolder) {
    val userDoc = db.collection("users").document(CurrentUser.instance.id)
    val folderDoc = userDoc.collection("cases").document(folderId)

    db.runTransaction { transaction ->
        val folderSnapshot = transaction.get(folderDoc)
        val folderData = folderSnapshot.data ?: throw Exception("Folder not found")

        // Convert the folder data to a Folder object
        val folder = mapToFolder(folderData as Map<String, Any>)

        // Find the subfolder and update its details
        val subFolderIndex = folder.subFolders.indexOfFirst { it.name == subFolder.name }
        if (subFolderIndex != -1) {
            folder.subFolders[subFolderIndex] = subFolder
        } else {
            throw Exception("SubFolder not found")
        }

        // Update the folder in Firestore
        transaction.set(folderDoc, folder.toMap())
    }.addOnSuccessListener {
        Log.d(TAG, "SubFolder updated successfully")
    }.addOnFailureListener { e ->
        Log.w(TAG, "Error updating subfolder", e)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun editDocument(folderId: String, subFolderId: String, document: Document) {
    val userDoc = db.collection("users").document(CurrentUser.instance.id)
    val folderDoc = userDoc.collection("cases").document(folderId)

    db.runTransaction { transaction ->
        val folderSnapshot = transaction.get(folderDoc)
        val folderData = folderSnapshot.data ?: throw Exception("Folder not found")

        // Convert the folder data to a Folder object
        val folder = mapToFolder(folderData as Map<String, Any>)

        // Find the subfolder and update the document details
        val subFolder = folder.subFolders.find { it.subFolderId == subFolderId }
            ?: throw Exception("SubFolder not found")

        val documentIndex = subFolder.documents.indexOfFirst { it.name == document.name }
        if (documentIndex != -1) {
            subFolder.documents[documentIndex] = document
        } else {
            throw Exception("Document not found")
        }

        // Update the folder in Firestore
        transaction.set(folderDoc, folder.toMap())
    }.addOnSuccessListener {
        Log.d(TAG, "Document updated successfully")
    }.addOnFailureListener { e ->
        Log.w(TAG, "Error updating document", e)
    }
}

fun deleteFolder(folderId: String) {
    db.collection("users").document(CurrentUser.instance.id).collection("cases").document(folderId)
        .delete()
        .addOnSuccessListener {
            Log.d(TAG, "Folder deleted successfully")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error deleting folder", e)
        }
}

@RequiresApi(Build.VERSION_CODES.O)
fun deleteSubFolder(folderId: String, index: Int) {
    val userDoc = db.collection("users").document(CurrentUser.instance.id)
    val folderDoc = userDoc.collection("cases").document(folderId)

    db.runTransaction { transaction ->
        val folderSnapshot = transaction.get(folderDoc)
        val folderData = folderSnapshot.data ?: throw Exception("Folder not found")

        // Convert the folder data to a Folder object
        val folder = mapToFolder(folderData as Map<String, Any>)

        // Remove the subfolder from the folder's subfolders list
        folder.subFolders.removeAt(index)

        // Update the folder in Firestore
        transaction.set(folderDoc, folder.toMap())
    }.addOnSuccessListener {
        Log.d(TAG, "SubFolder deleted successfully")
    }.addOnFailureListener { e ->
        Log.w(TAG, "Error deleting subfolder", e)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun deleteDocument(folderId: String, subFolderId: String, index: Int) {
    val userDoc = db.collection("users").document(CurrentUser.instance.id)
    val folderDoc = userDoc.collection("cases").document(folderId)

    db.runTransaction { transaction ->
        val folderSnapshot = transaction.get(folderDoc)
        val folderData = folderSnapshot.data ?: throw Exception("Folder not found")

        // Convert the folder data to a Folder object
        val folder = mapToFolder(folderData as Map<String, Any>)

        // Find the subfolder and remove the document
        val subFolder = folder.subFolders.find { it.subFolderId == subFolderId }
            ?: throw Exception("SubFolder not found")

        subFolder.documents.removeAt(index)

        // Update the folder in Firestore
        transaction.set(folderDoc, folder.toMap())
    }.addOnSuccessListener {
        Log.d(TAG, "Document deleted successfully")
    }.addOnFailureListener { e ->
        Log.w(TAG, "Error deleting document", e)
    }
}




fun printFileStructure(file: Folder, depth: Int = 0) {
    println("${"\t".repeat(depth)}File: ${file.name} - ${file.date}")

    for (subFile in file.subFolders) {
        println("${"\t".repeat(depth + 1)}Subfile: ${subFile.name}")

        for (document in subFile.documents) {
            println("${"\t".repeat(depth + 2)}Document: ${document.name} - ${document.date}")
            println("${"\t".repeat(depth + 2)}Content: ${document.content}")
        }
    }
}

