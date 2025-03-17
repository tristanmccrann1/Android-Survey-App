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
import android.net.Uri
import android.os.Build
import android.Manifest



class SummaryActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var pdfPath: String? = null
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // Android 9 or lower
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }



        dbHelper = DatabaseHelper(this)
        barChart = findViewById(R.id.barChart)
        displayChart()

    }

    private fun displayChart() {
        val cursor: Cursor = dbHelper.getSurveyResults()  // ✅ Now it correctly calls the function
        val entries = ArrayList<BarEntry>()
        var i = 0

        while (cursor.moveToNext()) {
            val score = cursor.getInt(cursor.getColumnIndexOrThrow("response")) // ✅ Ensure correct column name
            entries.add(BarEntry(i++.toFloat(), score.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Survey Results")
        barChart.data = BarData(dataSet)
        barChart.invalidate()
    }

    fun goBackToMain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Closes the current activity
    }

    fun sendEmail(view: View) {
        if (pdfPath == null) {
            return
        }

        val file = File(pdfPath!!)
        val uri = Uri.fromFile(file)

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Survey Responses PDF")
            putExtra(Intent.EXTRA_TEXT, "Here is your survey response summary.")
            putExtra(Intent.EXTRA_STREAM, uri)
        }

        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }
}
