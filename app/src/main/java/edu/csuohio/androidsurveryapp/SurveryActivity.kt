package edu.csuohio.androidsurveryapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File


class SurveyActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var questionGroups: Array<RadioGroup>
    private lateinit var pdfFile: File  // Store the generated PDF file

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        dbHelper = DatabaseHelper(this)

        questionGroups = arrayOf(
            findViewById(R.id.q1Group), findViewById(R.id.q2Group), findViewById(R.id.q3Group),
            findViewById(R.id.q4Group), findViewById(R.id.q5Group), findViewById(R.id.q6Group),
            findViewById(R.id.q7Group), findViewById(R.id.q8Group), findViewById(R.id.q9Group),
            findViewById(R.id.q10Group), findViewById(R.id.q11Group), findViewById(R.id.q12Group)
        )
    }

    fun submitSurvey(view: View) {
        val responses = mutableMapOf<String, String>() // Change to store String values

        for ((index, group) in questionGroups.withIndex()) {
            val selectedId = group.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedButton = findViewById<RadioButton>(selectedId)
                val response = selectedButton.tag.toString().toInt() // Ensure it's an Int

                dbHelper.insertSurveyResponse("Question ${index + 1}", response)
                responses["Question ${index + 1}"] = response.toString() // Convert to String
            }
        }

        // Generate the PDF and store the file reference
        pdfFile = PdfGenerator.generatePdf(this, responses) ?: return

        Toast.makeText(this, "Survey Submitted & PDF Generated!", Toast.LENGTH_SHORT).show()

        val pdfFile = PdfGenerator.generatePdf(this, responses)
        if (pdfFile == null) {
            Toast.makeText(this, "Error generating PDF!", Toast.LENGTH_SHORT).show()
            return
        }
        // Redirect to Summary Page
        val intent = Intent(this, SummaryActivity::class.java)
        intent.putExtra("pdfPath", pdfFile.absolutePath)  // Pass PDF path to SummaryActivity
        startActivity(intent)
        finish()
    }
}
