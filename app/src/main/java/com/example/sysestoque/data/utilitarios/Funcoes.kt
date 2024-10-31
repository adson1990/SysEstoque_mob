package com.example.sysestoque.data.utilitarios

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.sysestoque.R
import com.example.sysestoque.data.database.DbHelperLogin
import com.example.sysestoque.ui.login.LoginActivity

class Funcoes {

    fun exibirToast(context: Context, msgResId: Int, optionalMsg: String="", duracao: Int){
        val mensagem = if (optionalMsg.isNotEmpty()) {
            "${context.getString(msgResId)}: $optionalMsg"
        } else {
            context.getString(msgResId)
        }
        Toast.makeText(context, mensagem, duracao).show() // 1 longo 0- curto
    }

    fun exibirDialogo(contexto: Context, msgTitle: Int, msgAlert: Int,
                      positive: Boolean, negative: Boolean, neutral : Boolean){
        val mensagemTitulo = contexto.getString(msgTitle)
        val mensagemAlert = contexto.getString(msgAlert)

        val builder = AlertDialog.Builder(contexto)
        builder.setTitle(mensagemTitulo)
        builder.setMessage(mensagemAlert)
        if (positive) {
            builder.setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()

            }
        }
        if(negative){
            builder.setNegativeButton("No"){ dialog, _ ->
                dialog.dismiss()
            }
        }
        if(neutral) {
            builder.setNeutralButton("Return"){ dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun logout(context: Context){
        val dbHelperLogin = DbHelperLogin(context)
        dbHelperLogin.lembrarCliente(false)
        exibirToast(context, R.string.msg_logout,"",0)

        ActivityManager.finishAll()

        chamarLoginActivity(context)
    }

    private fun chamarLoginActivity(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
       // context.startActivity(intent)

        if (context is Activity) {
            context.startActivity(intent)
        } else {
            context.applicationContext.startActivity(intent)
        }
    }

    fun saveToken(context: Context, token: String, expiresIn: Long) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.putLong("expiresIn", expiresIn)
        editor.putLong("tokenTimestamp", System.currentTimeMillis())
        editor.apply()
    }

    fun getToken(context: Context): Triple<String?, Long, Long> {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val expiresIn = sharedPreferences.getLong("expiresIn", 0L)
        val tokenTimestamp =  sharedPreferences.getLong("tokenTimestamp", 0L)
        return Triple(token, expiresIn, tokenTimestamp)
    }

    fun isTokenValid(expiresIn: Long, tokenTimestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeElapsed = currentTime - tokenTimestamp

        return timeElapsed < expiresIn * 1000
    }
}