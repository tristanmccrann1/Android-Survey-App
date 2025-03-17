package edu.csuohio.androidsurveryapp

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PdfGenerator {
    fun generatePdf(context: Context, responses: Map<String, String>): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = android.graphics.Paint()
        paint.textSize = 14f

        var yPos = 50
        canvas.drawText("Survey Responses", 200f, yPos.toFloat(), paint)
        yPos += 30

        for ((question, answer) in responses) {
            canvas.drawText("$question: $answer", 50f, yPos.toFloat(), paint)
            yPos += 25
        }

        document.finishPage(page)

        // Create file to save the PDF
        val fileName = "SurveyResponses.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        val pdfFile = File(context.getExternalFilesDir(null), "SurveyResponses.pdf")
        Log.d("PDF_PATH", "PDF saved at: ${pdfFile.absolutePath}")


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
}
