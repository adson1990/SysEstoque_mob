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
        private const val TABLE2_NAME = "tb_usuario_logado"
        private const val COLUMN_ID = "id"
        private const val COLUMN_REMEMBER = "remember"
        private const val COLUMN_ID_CLIENT = "id_client"
        private const val COLUMN_USER_LOGGED = "user_logged"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = ("CREATE TABLE ${DbHelperLogin.TABLE_NAME} (" +
                "${DbHelperLogin.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_REMEMBER BOOLEAN)," +
                "$COLUMN_ID_CLIENT NUMBER"
                )
        val createTableStatement2 = ("CREATE TABLE ${DbHelperLogin.TABLE2_NAME} (" +
                "${DbHelperLogin.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_USER_LOGGED TEXT)")
        db.execSQL(createTableStatement)
        db.execSQL(createTableStatement2)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${com.example.sysestoque.data.database.DbHelperLogin.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${com.example.sysestoque.data.database.DbHelperLogin.TABLE2_NAME}")
        onCreate(db)
    }

    fun lembrarCliente(remember: Boolean, id: Long): Boolean{
        val db = this.writableDatabase

        db.execSQL("DELETE FROM ${DbHelperLogin.TABLE_NAME}")

        val contentValues = ContentValues().apply {
            put(COLUMN_REMEMBER, remember)
            put(COLUMN_ID_CLIENT, id)
        }

        val result = db.insert(DbHelperLogin.TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L
    }

    fun checarLoginAutomatico(): LoginInfo? {
        val db = this.readableDatabase
        var loginInfo: LoginInfo? = null

        val cursor = db.rawQuery("SELECT $COLUMN_REMEMBER, $COLUMN_ID_CLIENT FROM ${DbHelperLogin.TABLE_NAME}", null)

        if (cursor.moveToFirst()) {
            val remember = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMEMBER)) == 1
            val idClient = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID_CLIENT))
            loginInfo = LoginInfo(remember, idClient)
        }
        cursor.close()
        return loginInfo
    }

    fun gravarUsuarioLogin(user: String, id: Long): Boolean{
        val db = this.writableDatabase

        db.execSQL("DELETE FROM tb_usuario_logado")

        val contentValues = ContentValues().apply {
            put(COLUMN_USER_LOGGED, user)
            put(COLUMN_ID_CLIENT, id)
        }

        val result = db.insert(DbHelperLogin.TABLE2_NAME, null, contentValues)
        db.close()

        return result != -1L
    }
    fun getUsuarioLogado(): LoginInfo2? {
        val db = this.readableDatabase
        var loginInfo2: LoginInfo2? = null

        val cursor = db.rawQuery("SELECT $COLUMN_USER_LOGGED, $COLUMN_ID_CLIENT FROM ${DbHelperLogin.TABLE2_NAME}", null)

        if (cursor.moveToFirst()) {
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_LOGGED))
            val idClient = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID_CLIENT))
            loginInfo2 = LoginInfo2(email, idClient)
        }
        cursor.close()
        return loginInfo2
    }
}