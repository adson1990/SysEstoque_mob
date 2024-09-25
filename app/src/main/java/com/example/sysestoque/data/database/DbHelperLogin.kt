package com.example.sysestoque.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelperLogin(context: Context) : SQLiteOpenHelper(context, RELEMBRAR_USUARIO, null, DATABASE_VERSION) {

    companion object {
        private const val RELEMBRAR_USUARIO = "remember_user.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "tb_grava_login"
        private const val COLUMN_ID = "id"
        private const val COLUMN_REMEMBER = "remember"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = ("CREATE TABLE ${DbHelperLogin.TABLE_NAME} (" +
                "${DbHelperLogin.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_REMEMBER BOOLEAN)")
        db.execSQL(createTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${com.example.sysestoque.data.database.DbHelperLogin.TABLE_NAME}")
        onCreate(db)
    }

    fun lembrarCliente(remember: Boolean): Boolean{
        val db = this.writableDatabase

        db.execSQL("DELETE FROM tb_grava_login")

        val contentValues = ContentValues().apply {
            put(COLUMN_REMEMBER, remember)
        }

        val result = db.insert(DbHelperLogin.TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L
    }

    fun checarLoginAutomatico(): Boolean{
        val db = this.readableDatabase
        var dadoRecuperado = false

        val cursor = db.rawQuery("SELECT remember FROM tb_grava_login", null)

        if (cursor.moveToFirst()) {
            dadoRecuperado = cursor.getInt(cursor.getColumnIndexOrThrow("remember")) == 1
        }
        cursor.close()
        return dadoRecuperado
    }
}