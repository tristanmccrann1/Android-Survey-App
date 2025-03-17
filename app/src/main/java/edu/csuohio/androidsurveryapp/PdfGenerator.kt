package edu.csuohio.androidsurveryapp

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {
    fun generatePdf(context: Context, responses: Map<String, String>): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = android.graphics.Paint()

        // Title style
        val titlePaint = android.graphics.Paint().apply {
            textSize = 18f
            isFakeBoldText = true
        }

        // Section header style
        val sectionPaint = android.graphics.Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            color = android.graphics.Color.rgb(0, 0, 128) // Navy blue
        }

        // Standard text style
        paint.textSize = 14f

        var yPos = 50

        // Add title and date
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        canvas.drawText("Survey Response Summary", 50f, yPos.toFloat(), titlePaint)
        yPos += 25
        canvas.drawText("Date: $currentDate", 50f, yPos.toFloat(), paint)
        yPos += 40

        // Calculate averages for each section
        var peerTeacherTotal = 0f
        var peerTeacherCount = 0
        var peerLearnerTotal = 0f
        var peerLearnerCount = 0

        for ((question, answer) in responses) {
            val score = answer.toFloatOrNull() ?: 0f
            if (question.contains("Peer Teacher")) {
                peerTeacherTotal += score
                peerTeacherCount++
            } else if (question.contains("Peer Learner")) {
                peerLearnerTotal += score
                peerLearnerCount++
            }
        }

        val peerTeacherAvg = if (peerTeacherCount > 0) peerTeacherTotal / peerTeacherCount else 0f
        val peerLearnerAvg = if (peerLearnerCount > 0) peerLearnerTotal / peerLearnerCount else 0f
        val overallAvg = if (peerTeacherCount + peerLearnerCount > 0)
            (peerTeacherTotal + peerLearnerTotal) / (peerTeacherCount + peerLearnerCount) else 0f

        // Display section averages
        canvas.drawText("Survey Results Summary:", 50f, yPos.toFloat(), sectionPaint)
        yPos += 30
        canvas.drawText("Peer Teacher Section Average: ${String.format("%.2f", peerTeacherAvg)}",
            70f, yPos.toFloat(), paint)
        yPos += 25
        canvas.drawText("Peer Learner Section Average: ${String.format("%.2f", peerLearnerAvg)}",
            70f, yPos.toFloat(), paint)
        yPos += 25
        canvas.drawText("Overall Average: ${String.format("%.2f", overallAvg)}",
            70f, yPos.toFloat(), paint)
        yPos += 40

        // Display individual responses
        canvas.drawText("Individual Question Responses:", 50f, yPos.toFloat(), sectionPaint)
        yPos += 30

        // Peer Teacher Section
        canvas.drawText("Peer Teacher Questions:", 70f, yPos.toFloat(), paint)
        yPos += 25

        // Sort responses to ensure they're in order
        val sortedResponses = responses.entries.sortedBy {
            val questionParts = it.key.split("Q")
            val sectionName = questionParts[0].trim()
            val questionNumber = if (questionParts.size > 1) questionParts[1].toIntOrNull() ?: 0 else 0

            // Sort by section first, then by question number
            if (sectionName.contains("Teacher")) 0 else 1 * 1000 + questionNumber
        }

        for ((question, answer) in sortedResponses) {
            if (question.contains("Peer Teacher")) {
                val questionNumber = question.replace("Peer Teacher Q", "").toIntOrNull() ?: 0
                val questionText = getPeerTeacherQuestionText(questionNumber)
                canvas.drawText("Q$questionNumber: $questionText", 90f, yPos.toFloat(), paint)
                yPos += 20
                canvas.drawText("Response: $answer - ${getLikertScaleText(answer.toIntOrNull() ?: 0)}",
                    110f, yPos.toFloat(), paint)
                yPos += 30
            }
        }

        yPos += 10

        // Peer Learner Section
        canvas.drawText("Peer Learner Questions:", 70f, yPos.toFloat(), paint)
        yPos += 25

        for ((question, answer) in sortedResponses) {
            if (question.contains("Peer Learner")) {
                val questionNumber = question.replace("Peer Learner Q", "").toIntOrNull() ?: 0
                val questionText = getPeerLearnerQuestionText(questionNumber)
                canvas.drawText("Q$questionNumber: $questionText", 90f, yPos.toFloat(), paint)
                yPos += 20
                canvas.drawText("Response: $answer - ${getLikertScaleText(answer.toIntOrNull() ?: 0)}",
                    110f, yPos.toFloat(), paint)
                yPos += 30
            }
        }

        document.finishPage(page)

        // Create file to save the PDF
        val fileName = "SurveyResponses_${formatDate()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        return try {
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()
            Log.d("PDFGenerator", "PDF saved at: ${file.absolutePath}")
            file
        } catch (e: IOException) {
            Log.e("PDFGenerator", "Error saving PDF: ${e.message}")
            null
        }
    }

    private fun getLikertScaleText(value: Int): String {
        return when (value) {
            1 -> "Strongly Disagree"
            2 -> "Disagree"
            3 -> "Neutral"
            4 -> "Agree"
            5 -> "Strongly Agree"
            else -> "Unknown"
        }
    }

    private fun getPeerTeacherQuestionText(questionNumber: Int): String {
        return when (questionNumber) {
            1 -> "Serving as a peer teacher increased my self-confidence in this course."
            2 -> "Serving as a peer teacher improved my course performance."
            3 -> "Serving as a peer teacher improved my communication and active listening skills."
            4 -> "I had the opportunity to consolidate my own knowledge through peer teaching."
            5 -> "I have a better understanding of teamwork and understanding roles within the team."
            6 -> "I gained many benefits from peer teaching experience and am willing to repeat it."
            7 -> "The students were actively engaged during the peer teaching sessions."
            8 -> "I think the students benefited from peer teaching experience."
            else -> "Unknown question"
        }
    }

    private fun getPeerLearnerQuestionText(questionNumber: Int): String {
        return when (questionNumber) {
            1 -> "The content of peer teaching sessions was appropriate for the student's level."
            2 -> "The Peer teacher's methods were helpful."
            3 -> "I understood the subject better after the peer teaching sessions."
            4 -> "I understood the content better when the peer teachers explained it."
            5 -> "I am more willing to engage in sessions taught by peers compared to faculty teachers."
            6 -> "The peer teaching sessions were better than I had expected."
            7 -> "The peer teachers were well-prepared and knowledgeable about the topic."
            8 -> "We should have more peer teaching sessions."
            else -> "Unknown question"
        }
    }

    private fun formatDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        return dateFormat.format(Date())
    }
}