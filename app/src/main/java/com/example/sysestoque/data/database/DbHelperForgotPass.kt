package com.example.sysestoque.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelperForgotPass(context: Context) : SQLiteOpenHelper(context, RECUPERACAO_SENHA, null, DATABASE_VERSION) {

    companion object {
        private const val RECUPERACAO_SENHA = "request_new_password.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "tb_codigo_recuperacao"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CODIGO = "codigo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_CODIGO INTEGER NOT NULL)")
        db.execSQL(createTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun salvarCodigo(codigo: Int): Boolean {
        val db = this.writableDatabase

        db.execSQL("DELETE FROM tb_codigo_recuperacao")

        val contentValues = ContentValues().apply {
            put(COLUMN_CODIGO, codigo)
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L // Retorna true se a inserção foi bem-sucedida
    }

    fun getCodigoRecuperacao(): Int? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT codigo FROM tb_codigo_recuperacao", null)

        var codigoRecuperacao: Int? = null
        if (cursor.moveToFirst()) {
            codigoRecuperacao = cursor.getInt(cursor.getColumnIndexOrThrow("codigo"))
        }
        cursor.close()
        return codigoRecuperacao
    }

}