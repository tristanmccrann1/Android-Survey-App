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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        dbHelper = DatabaseHelper(this)

        questionGroups = arrayOf(
            findViewById(R.id.q1Group), findViewById(R.id.q2Group), findViewById(R.id.q3Group),
            findViewById(R.id.q4Group), findViewById(R.id.q5Group), findViewById(R.id.q6Group),
            findViewById(R.id.q7Group), findViewById(R.id.q8Group), findViewById(R.id.q9Group),
            findViewById(R.id.q10Group), findViewById(R.id.q11Group), findViewById(R.id.q12Group),
            findViewById(R.id.q13Group), findViewById(R.id.q14Group), findViewById(R.id.q15Group),
            findViewById(R.id.q16Group)
        )
    }

    fun submitSurvey(view: View) {
        val responses = mutableMapOf<String, String>()

        // Check if all questions are answered
        var allAnswered = true
        for ((index, group) in questionGroups.withIndex()) {
            val selectedId = group.checkedRadioButtonId
            if (selectedId == -1) {
                allAnswered = false
                break
            }

            val selectedButton = findViewById<RadioButton>(selectedId)
            val response = selectedButton.tag.toString().toInt()

            // Section determination (first 8 are peer teacher, last 4 are peer learner)
            val questionText = if (index < 8) {
                "Peer Teacher Q${index + 1}"
            } else {
                "Peer Learner Q${(index - 7)}"
            }

            dbHelper.insertSurveyResponse(questionText, response)
            responses[questionText] = response.toString()
        }

        if (!allAnswered) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate PDF file with survey responses
        val pdfFile = PdfGenerator.generatePdf(this, responses)
        if (pdfFile == null) {
            Toast.makeText(this, "Error generating PDF!", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Survey Submitted & PDF Generated!", Toast.LENGTH_SHORT).show()

        // Redirect to Summary Page with PDF path
        val intent = Intent(this, SummaryActivity::class.java)
        intent.putExtra("pdfPath", pdfFile.absolutePath)
        startActivity(intent)
        finish()
    }

    fun goBackToMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}