package com.example.myhealth

var fileName: String? = null

data class Document(val name: String, val content: String)

class SubFile(val name: String, val documents: List<Document> = emptyList())

class File(val name: String, val subFiles: List<SubFile>)

fun createNewFile(fileName: String): File {
    val testResults = SubFile("Test Results")
    val summaries = SubFile("Summaries")
    val prescriptions = SubFile("Prescriptions")
    val invoices = SubFile("Invoices")

    return File(fileName, listOf(testResults, summaries, prescriptions, invoices))
}

fun main() {

    // Creating a file with subfiles
    val file = createNewFile("Main File")

    // Printing file structure
    printFileStructure(file)
}

fun printFileStructure(file: File, depth: Int = 0) {
    println("${"\t".repeat(depth)}File: ${file.name}")

    for (subFile in file.subFiles) {
        println("${"\t".repeat(depth + 1)}Subfile: ${subFile.name}")

        for (document in subFile.documents) {
            println("${"\t".repeat(depth + 2)}Document: ${document.name}")
            println("${"\t".repeat(depth + 2)}Content: ${document.content}")
        }
    }
}
