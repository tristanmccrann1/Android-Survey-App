package edu.csuohio.androidsurveryapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startSurvey(view: View) {
        startActivity(Intent(this, SurveyActivity::class.java))
    }

    fun viewSummary(view: View) {
        startActivity(Intent(this, SummaryActivity::class.java))
    }

    fun emailReport(view: View) {
        startActivity(Intent(this, EmailActivity::class.java))
    }
}
