package com.example.sysestoque.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelperConfig (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_NAME = "appConfigDatabase.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "configuracoes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_ID = "userId"
        private const val COLUMN_ULTIMAS_COMPRAS = "ultimasCompras"
        private const val COLUMN_ORDEM_COMPRAS = "ordemCompras"
        private const val COLUMN_ORDEM_PESQUISA = "ordemPesquisa"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER NOT NULL,
                $COLUMN_ULTIMAS_COMPRAS INTEGER NOT NULL,
                $COLUMN_ORDEM_COMPRAS TEXT NOT NULL,
                $COLUMN_ORDEM_PESQUISA TEXT NOT NULL,
                UNIQUE($COLUMN_USER_ID)
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun salvarConfiguracoes(
        userId: Long,
        ultimasCompras: Boolean,
        ordemCompras: String,
        ordemPesquisa: String
    ) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_ULTIMAS_COMPRAS, if (ultimasCompras) 1 else 0)
            put(COLUMN_ORDEM_COMPRAS, ordemCompras)
            put(COLUMN_ORDEM_PESQUISA, ordemPesquisa)
        }

        // Atualiza ou Insere: se já existe, atualiza; senão, insere
        val rowsUpdated = db.update(TABLE_NAME, contentValues, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
        if (rowsUpdated == 0) {
            db.insert(TABLE_NAME, null, contentValues)
        }
        db.close()
    }

    fun getConfiguracoes(userId: Long): Configuracoes? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val ultimasCompras = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ULTIMAS_COMPRAS)) == 1
            val ordemCompras = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDEM_COMPRAS))
            val ordemPesquisa = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDEM_PESQUISA))
            cursor.close()
            Configuracoes(userId, ultimasCompras, ordemCompras, ordemPesquisa)
        } else {
            cursor.close()
            null
        }
    }
}

data class Configuracoes(
    val userId: Long,
    val ultimasCompras: Boolean,
    val ordemCompras: String,
    val ordemPesquisa: String
)