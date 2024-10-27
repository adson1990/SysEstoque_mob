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
                blue INTEGER,
                idCliente INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
        db.execSQL("CREATE INDEX idx_client_id ON ClientNameColors (idCliente);")

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ClientNameColors")
        onCreate(db)
    }

    fun saveColors(red: Int, green: Int, blue: Int, idCliente: Long) {
        val db = writableDatabase
        //db.execSQL("DELETE FROM ClientNameColors")

        val cursor = db.rawQuery(
            "SELECT id FROM ClientNameColors WHERE idCliente = ?",
            arrayOf(idCliente.toString())
        )

        val exists = cursor.moveToFirst()
        cursor.close()

        val values = ContentValues().apply {
            put("red", red)
            put("green", green)
            put("blue", blue)
            put("idCliente", idCliente)
        }

        if (exists) {
            // Se o cliente já tem um registro, faz um update.
            db.update(
                "ClientNameColors",
                values,
                "idCliente = ?",
                arrayOf(idCliente.toString())
            )
        } else {
            // Se não existe, insere um novo registro.
            db.insert("ClientNameColors", null, values)
        }

        db.close()
    }

    fun getColors(idCliente: Long): Triple<Int,Int,Int>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT red, green, blue FROM ClientNameColors WHERE idCliente = ?", arrayOf(idCliente.toString()))
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

data class ClientColor(
    val red: Int,
    val green: Int,
    val blue: Int,
    val idCliente: Long
)
