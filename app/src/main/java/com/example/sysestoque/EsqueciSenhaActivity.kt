package com.example.sysestoque

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import retrofit2.Callback
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.Client
import com.example.sysestoque.backend.TokenRequest
import com.example.sysestoque.backend.TokenResponse
import com.example.sysestoque.databinding.EsqueciSenhaLayoutBinding
import retrofit2.Call
import retrofit2.Response

class EsqueciSenhaActivity : AppCompatActivity() {

    private lateinit var binding: EsqueciSenhaLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.esqueci_senha_layout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_esquecer_senha)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = EsqueciSenhaLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val edtEmail = findViewById<EditText>(R.id.edtEmailAddress)
        val edtCodRec = findViewById<EditText>(R.id.edtCodRecuperacao)
        val edtNovaSenha = findViewById<EditText>(R.id.edtNewPassword)

        val tvCodRec =findViewById<TextView>(R.id.textView3)
        val tvNovaSenha =findViewById<TextView>(R.id.textView4)

        val btn1 = findViewById<Button>(R.id.btn_submit)
        val btn2 = findViewById<Button>(R.id.btn_submit2)
        val btn3 = findViewById<Button>(R.id.btn_submit3)

        tvCodRec.visibility = View.GONE
        tvNovaSenha.visibility = View.GONE
        edtCodRec.visibility = View.INVISIBLE
        edtNovaSenha.visibility = View.INVISIBLE
        btn2.visibility = View.INVISIBLE
        btn3.visibility = View.INVISIBLE

        btn1.setOnClickListener {
            val authRepository = AuthRepository()
            val username = "ADMIN"

            authRepository.getToken(username, object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.accessToken
                        val expiresIn = response.body()?.expiresIn

                        Log.i("Token_recuperado","O token foi recuperado com exito: $token e expira em: $expiresIn segundos")

                        Toast.makeText(this@EsqueciSenhaActivity, "Token: $token, Expira em: $expiresIn", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("Erro_recuperar_token","O token não foi recuperado com êxito.")
                        Toast.makeText(this@EsqueciSenhaActivity, "Erro ao obter token", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(this@EsqueciSenhaActivity, "Falha na requisição: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

}