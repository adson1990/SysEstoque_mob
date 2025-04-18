package com.example.sysestoque.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DbHelperLogin(context: Context) : SQLiteOpenHelper(context, RELEMBRAR_USUARIO, null, DATABASE_VERSION) {

    companion object {
        private const val RELEMBRAR_USUARIO = "remember_user.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "tb_grava_login"
        private const val COLUMN_ID = "id"
        private const val COLUMN_REMEMBER = "remember"
        private const val COLUMN_ID_CLIENT = "id_client"
        private const val COLUMN_USER_LOGGED = "user_logged"
        private const val COLUMN_PHOTO = "photo_user"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = ("CREATE TABLE ${DbHelperLogin.TABLE_NAME} (" +
                "${DbHelperLogin.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_REMEMBER BOOLEAN," +
                "$COLUMN_ID_CLIENT NUMBER," +
                "$COLUMN_USER_LOGGED TEXT," +
                "$COLUMN_PHOTO TEXT);"
                )
        db.execSQL(createTableStatement)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${com.example.sysestoque.data.database.DbHelperLogin.TABLE_NAME}")
        onCreate(db)
    }

    fun popularTabelasInicio(): Boolean{
        val db = this.writableDatabase
        var contentValues: ContentValues? = null

        // verificar se já existe registro na tabela
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DbHelperLogin.TABLE_NAME}", null)
        cursor.moveToFirst()
        val recordCount = cursor.getInt(0)
        cursor.close()

        if (recordCount == 0) {
            contentValues = ContentValues().apply {
                put(COLUMN_REMEMBER, false)
                put(COLUMN_ID_CLIENT, 0L)
                put(COLUMN_USER_LOGGED, "")
                put(COLUMN_PHOTO, "")
            }
        }
        val result = contentValues?.let {
            db.insert(DbHelperLogin.TABLE_NAME, null, it)
        } ?: -1L

        db.close()
        return result != -1L
    }

    fun lembrarCliente(remember: Boolean): Boolean{
        val db = this.writableDatabase

        val contentValues = ContentValues().apply {
            put(COLUMN_REMEMBER, if (remember) 1 else 0)
        }

        val result = db.update(
            TABLE_NAME,
            contentValues,
           null,
           null
            /*"$COLUMN_USER_LOGGED = ?",
            arrayOf(emailUser) */
        )
        db.close()
        return result > 0
    }

    fun checarLoginAutomatico(): Pair<Long, String> {
        val db = this.readableDatabase
        val idClient: Long
        val mailUser: String

        val cursor = db.rawQuery(
            "SELECT $COLUMN_ID_CLIENT, $COLUMN_USER_LOGGED FROM ${DbHelperLogin.TABLE_NAME} WHERE $COLUMN_REMEMBER = 1",
            null
        )

        if (cursor.moveToFirst()) {
            idClient = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID_CLIENT))
            mailUser = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_LOGGED))
        } else {
            idClient = 0L
            mailUser = "unknown"
        }
        cursor.close()
        return Pair(idClient, mailUser)
    }

    fun atualizaUsuarioLogado(email : String){
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT $COLUMN_USER_LOGGED FROM tb_grava_login",  null)

        if (cursor != null && cursor.moveToFirst()) {

            val contentValues = ContentValues().apply {
                put(COLUMN_USER_LOGGED, email)
            }

            db.update(TABLE_NAME, contentValues, null, null)
        }
        cursor.close()
        db.close()
    }

    fun salvarCaminhoFoto(photoPath : String){
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT $COLUMN_PHOTO FROM tb_grava_login",  null)

        if (cursor != null && cursor.moveToFirst()) {
            //val id = cursor.getLong(1)

            val contentValues = ContentValues().apply {
                put(COLUMN_PHOTO, photoPath)
            }

            //val rowsAffected = db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))

            val rowsAffected = db.update(TABLE_NAME, contentValues, null, null)
            Log.d("Database", "Caminho da foto salvo: $photoPath. Linhas afetadas: $rowsAffected")

        }

        cursor.close()
        db.close()
    }

    fun gravarUsuarioLogin(remember: Boolean, id: Long, email: String, foto: String): Boolean {
        val db = this.writableDatabase

        db.delete(DbHelperLogin.TABLE_NAME, "1", null) //Deleta todos os registros da tabela

        val contentValues = ContentValues().apply {
            put(COLUMN_REMEMBER, if (remember) 1 else 0)
            put(COLUMN_ID_CLIENT, id)
            put(COLUMN_USER_LOGGED, email)
            put(COLUMN_PHOTO, foto)
        }

        val result = db.insert(DbHelperLogin.TABLE_NAME, null, contentValues)

        db.close()
        return result != -1L
    }


    fun getFotoUsuario(id: Long): String{
        val db = this.readableDatabase
        var foto: String? = null

        val cursor = db.query(TABLE_NAME,
                    arrayOf(COLUMN_PHOTO),
                    "$COLUMN_ID_CLIENT = ?", arrayOf(id.toString()),
                    null,null,null)

        if (cursor.moveToFirst()) {
          foto = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO))
        }
        cursor.close()

        return foto!!
    }

    fun getUsuarioLogado(): LoginInfo? {
        val db = this.readableDatabase
        var loginInfo: LoginInfo? = null
        var idClient: Long
        var email = ""
        var rememberMe = false
        var foto = ""

        val cursor = db.rawQuery(
            "SELECT $COLUMN_USER_LOGGED, $COLUMN_PHOTO, $COLUMN_REMEMBER, $COLUMN_ID_CLIENT " +
                    "FROM ${DbHelperLogin.TABLE_NAME} WHERE $COLUMN_ID_CLIENT > 0",
            null
        )


        if (cursor.moveToFirst()) {
            idClient = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID_CLIENT))
            email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_LOGGED))
            rememberMe = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMEMBER)) == 1
            foto = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO))

            loginInfo = LoginInfo(rememberMe, idClient, email, foto)
        }
        cursor.close()
        return loginInfo
    }

    fun logarConteudoTabela() {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tb_grava_login", null)

        var recordCount = 0

        if (cursor.moveToFirst()) {
            // Captura as informações da primeira linha
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val remember = cursor.getInt(cursor.getColumnIndexOrThrow("remember")) > 0
            val idClient = cursor.getLong(cursor.getColumnIndexOrThrow("id_client"))
            val userLogged = cursor.getString(cursor.getColumnIndexOrThrow("user_logged"))
            val photoUser = cursor.getString(cursor.getColumnIndexOrThrow("photo_user"))

            // Itera pelo cursor e conta as linhas
            do {
                recordCount++ // Incrementa a contagem de registros
            } while (cursor.moveToNext())
            Log.d("DB_LOG", "ID: $id, Remember: $remember, ID_Client: $idClient, User: $userLogged, Photo: $photoUser, Total de registros: $recordCount")
        } else {
            Log.d("DB_LOG", "Nenhum registro encontrado.")
        }
        cursor.close()
        db.close()
    }
}