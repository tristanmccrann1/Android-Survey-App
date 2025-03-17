package edu.csuohio.androidsurveryapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.database.Cursor
import android.content.ContentValues

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        try {
            val createTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                    $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_QUESTION TEXT NOT NULL,
                    $COL_RESPONSE INTEGER NOT NULL
                )
            """.trimIndent()

            db.execSQL(createTable)
            Log.d("DatabaseHelper", "Table created successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DatabaseError", "Error creating table: ${e.message}")
        }
    }

    fun getSurveyResults(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun insertSurveyResponse(question: String, response: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("question", question)
            put("response", response)
        }
        db.insert("survey_responses", null, values)
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
            Log.d("DatabaseHelper", "Database upgraded successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DatabaseError", "Error upgrading database: ${e.message}")
        }
    }

    companion object {
        private const val DATABASE_NAME = "Survey.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "survey_responses"
        private const val COL_ID = "id"
        private const val COL_QUESTION = "question"
        private const val COL_RESPONSE = "response"
    }
}
