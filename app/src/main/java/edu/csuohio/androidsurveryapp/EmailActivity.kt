package edu.csuohio.androidsurveryapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import androidx.core.content.FileProvider


class EmailActivity : AppCompatActivity() {
    private var pdfPath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)
    }

    fun sendEmail(view: View) {
        if (pdfPath.isNullOrEmpty()) {
            Toast.makeText(this, "PDF file not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(pdfPath!!)
        if (!file.exists()) {
            Toast.makeText(this, "PDF file is missing!", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", file)

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("2806927@vikes.csuohio.edu"))
            putExtra(Intent.EXTRA_SUBJECT, "Survey Responses PDF")
            putExtra(Intent.EXTRA_TEXT, "Here is your survey response summary.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found!", Toast.LENGTH_SHORT).show()
        }
    }

}
