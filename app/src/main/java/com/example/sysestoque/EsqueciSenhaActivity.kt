package com.example.sysestoque

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import retrofit2.Callback
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.Client
import com.example.sysestoque.backend.TokenResponse
import com.example.sysestoque.databinding.EsqueciSenhaLayoutBinding
import retrofit2.Call
import retrofit2.Response

class EsqueciSenhaActivity : AppCompatActivity() {

    private lateinit var binding: EsqueciSenhaLayoutBinding
    private val username = "admin"

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

        val tvCodRec = findViewById<TextView>(R.id.textView3)
        val tvNovaSenha = findViewById<TextView>(R.id.textView4)

        val btn1 = findViewById<Button>(R.id.btn_submit)
            btn1.isEnabled = false
        val btn2 = findViewById<Button>(R.id.btn_submit2)
        val btn3 = findViewById<Button>(R.id.btn_submit3)

        tvCodRec.visibility = View.GONE
        tvNovaSenha.visibility = View.GONE
        edtCodRec.visibility = View.INVISIBLE
        edtNovaSenha.visibility = View.INVISIBLE
        btn2.visibility = View.INVISIBLE
        btn3.visibility = View.INVISIBLE

        // Validação de E-mail
        //------------------------------------------------------------------------------------------------------
        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                edtEmail.removeTextChangedListener(this)
                val emailInput = s.toString()

                if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    edtEmail.setTextColor(
                        ContextCompat.getColor(this@EsqueciSenhaActivity, R.color.green_600)
                    )
                    btn1.isEnabled = true
                } else {
                    // edtEmail.setTextColor(resources.getColor(R.color.red_700, theme)) Mais moderno porém requer API(M)
                    edtEmail.setTextColor(
                        ContextCompat.getColor(this@EsqueciSenhaActivity, R.color.red_700)
                    )
                    btn1.isEnabled = false
                }

                edtEmail.setSelection(emailInput?.length ?: 0)
                edtEmail.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Realizar a consulta
        //------------------------------------------------------------------------------------------------------
        btn1.setOnClickListener {
            if (btn1.isEnabled) {
                // Chama o método getToken e lida com o resultado no callback
                getToken(username) { (token, expiresIn) ->
                    if (token != "invalid_token") {
                        validarEmail(token, edtEmail.text.toString())
                    } else {
                        Toast.makeText(this@EsqueciSenhaActivity, "Token inválido", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Fim OnCreate
    }

    private fun getToken(username: String, callback: (Pair<String, Long>) -> Unit) {
        val authRepository = AuthRepository()


        authRepository.getToken(username, object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    val token: String = tokenResponse?.accessToken ?: "invalid_token"
                    val expiresIn: Long = tokenResponse?.expiresInSeconds ?: 0L
                    callback(Pair(token, expiresIn))

                    Log.i(
                        "Token_recuperado",
                        "O token foi recuperado com exito: $token e expira em: $expiresIn segundos"
                    )

                    Toast.makeText(
                        this@EsqueciSenhaActivity,
                        "Token recuperado",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    callback(Pair("invalid_token", 0L))
                    Log.e("Erro_recuperar_token", "O token não foi recuperado com êxito.")
                    Toast.makeText(
                        this@EsqueciSenhaActivity,
                        "Erro ao obter token",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Log.wtf("Error_request","Falha na requisição: ${t.message}")
                Toast.makeText(
                    this@EsqueciSenhaActivity,
                    "Falha de rede",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun validarEmail(token: String, email: String){
        val authRepository = AuthRepository()

        authRepository.validaEmail(email, token, object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>){
                if(response.isSuccessful && response.body() != null){
                    Log.i("Sucesso_busca", "E-mail encontrado no DB")
                    Toast.makeText(this@EsqueciSenhaActivity, "E-mail encontrado", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("Erro_busca_email","E-mail não encontrado.")
                    Toast.makeText(this@EsqueciSenhaActivity, "E-mail não encontrado.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable){
                Log.wtf("Falha_comunicação","Erro: ${t.message}")
                Toast.makeText(this@EsqueciSenhaActivity, "Falha na comunicação.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}