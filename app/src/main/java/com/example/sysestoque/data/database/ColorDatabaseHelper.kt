package com.example.sysestoque.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ColorDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, "colors.db", null, 1
) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE IF NOT EXISTS ClientNameColors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                red INTEGER,
                green INTEGER,
                blue INTEGER
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ClientNameColors")
        onCreate(db)
    }

    fun saveColors(red: Int, green: Int, blue: Int) {
        val db = writableDatabase
        db.execSQL("DELETE FROM ClientNameColors")

        val values = ContentValues().apply {
            put("red", red)
            put("green", green)
            put("blue", blue)
        }
        db.insert("ClientNameColors", null, values)
        db.close()
    }

    fun getColors(): Triple<Int, Int, Int>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ClientNameColors ORDER BY id DESC LIMIT 1", null)
        return if (cursor.moveToFirst()) {
            val red = cursor.getInt(cursor.getColumnIndexOrThrow("red"))
            val green = cursor.getInt(cursor.getColumnIndexOrThrow("green"))
            val blue = cursor.getInt(cursor.getColumnIndexOrThrow("blue"))
            cursor.close()
            Triple(red, green, blue)
        } else {
            cursor.close()
            null
        }
    }
}
