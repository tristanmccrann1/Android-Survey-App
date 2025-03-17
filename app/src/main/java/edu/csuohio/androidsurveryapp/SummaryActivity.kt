package edu.csuohio.androidsurveryapp

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import java.io.File
import android.os.Build
import android.Manifest
import android.graphics.Color
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import edu.csuohio.androidsurveryapp.DatabaseHelper
import edu.csuohio.androidsurveryapp.EmailActivity
import edu.csuohio.androidsurveryapp.PdfGenerator
import edu.csuohio.androidsurveryapp.R


class SummaryActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var pdfPath: String? = null
    private lateinit var barChart: BarChart
    private val STORAGE_PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // Get PDF path from intent
        pdfPath = intent.getStringExtra("pdfPath")

        // Check for storage permissions on older Android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }

        dbHelper = DatabaseHelper(this)
        barChart = findViewById(R.id.barChart)
        displayChart()
    }

    private fun displayChart() {
        val cursor: Cursor = dbHelper.getSurveyResults()
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val responseMap = mutableMapOf<String, Int>()
        var peerTeacherTotal = 0
        var peerTeacherCount = 0
        var peerLearnerTotal = 0
        var peerLearnerCount = 0
        var i = 0

        // If no results, show message
        if (cursor.count == 0) {
            Toast.makeText(this, "No survey results found", Toast.LENGTH_SHORT).show()
            return
        }

        while (cursor.moveToNext()) {
            val questionText = cursor.getString(cursor.getColumnIndexOrThrow("question"))
            val score = cursor.getInt(cursor.getColumnIndexOrThrow("response"))

            // Store in map for future reference
            responseMap[questionText] = score

            // Calculate section averages
            if (questionText.contains("Peer Teacher")) {
                peerTeacherTotal += score
                peerTeacherCount++
            } else if (questionText.contains("Peer Learner")) {
                peerLearnerTotal += score
                peerLearnerCount++
            }

            // Add data for bar chart
            entries.add(BarEntry(i.toFloat(), score.toFloat()))

            // Create shorter labels for display
            val shortLabel = if (questionText.contains("Peer Teacher")) {
                "PT-" + questionText.replace("Peer Teacher Q", "")
            } else {
                "PL-" + questionText.replace("Peer Learner Q", "")
            }
            labels.add(shortLabel)
            i++
        }

        // Calculate and display section averages
        val peerTeacherAvg = if (peerTeacherCount > 0) peerTeacherTotal.toFloat() / peerTeacherCount else 0f
        val peerLearnerAvg = if (peerLearnerCount > 0) peerLearnerTotal.toFloat() / peerLearnerCount else 0f

        val sectionAverageText = "Section Averages:\n" +
                "Peer Teacher: ${String.format("%.2f", peerTeacherAvg)}\n" +
                "Peer Learner: ${String.format("%.2f", peerLearnerAvg)}"

        // Create a TextView to display the averages
        val averageTextView = findViewById<TextView>(R.id.tvSectionAverages)
        averageTextView.text = sectionAverageText

        // Set up the bar chart data
        val dataSet = BarDataSet(entries, "Question Responses")

        // Create colors - blue for Peer Teacher, green for Peer Learner
        val colors = ArrayList<Int>()
        for (j in 0 until entries.size) {
            if (j < peerTeacherCount) {
                colors.add(Color.rgb(65, 105, 225)) // Royal Blue for Peer Teacher
            } else {
                colors.add(Color.rgb(46, 139, 87))  // Sea Green for Peer Learner
            }
        }
        dataSet.colors = colors

        // Set up the bar chart
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setLabelCount(labels.size, false)
        barChart.xAxis.labelRotationAngle = 45f

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = 5f
        barChart.axisLeft.granularity = 1f

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true

        barChart.data = BarData(dataSet)
        barChart.invalidate()

        // Generate and save PDF report
        if (pdfPath == null) {
            val pdfFile = PdfGenerator.generatePdf(this, responseMap.mapValues { it.value.toString() })
            if (pdfFile != null) {
                pdfPath = pdfFile.absolutePath
                Toast.makeText(this, "PDF report generated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun goBackToMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun sendEmail(view: View) {
        if (pdfPath == null) {
            Toast.makeText(this, "No PDF file available", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, EmailActivity::class.java)
        intent.putExtra("pdfPath", pdfPath)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            displayChart()
        }
    }
}