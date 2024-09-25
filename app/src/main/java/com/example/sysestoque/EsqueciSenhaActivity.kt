package com.example.sysestoque

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.PassResponse
import com.example.sysestoque.backend.TokenResponse
import com.example.sysestoque.data.database.DatabaseHelper
import com.example.sysestoque.databinding.EsqueciSenhaLayoutBinding
import retrofit2.Callback as RetrofitCallback
import retrofit2.Response as RetrofitResponse
import retrofit2.Call as RetrofitCall
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Call as OkHttpCall
import okhttp3.Callback as OkHttpCallback
import okhttp3.Response as OkHttpResponse


class EsqueciSenhaActivity : AppCompatActivity() {

    private lateinit var binding: EsqueciSenhaLayoutBinding
    private val username = "admin"

    private lateinit var tvCodRec: TextView
    private lateinit var edtCodRec: EditText
    private lateinit var btn2: Button
    private lateinit var edtNovaSenha: EditText
    private lateinit var tvNovaSenha: TextView
    private lateinit var btn3: Button
    private var idCliente: Long = 0
    private var token: String = ""

    @SuppressLint("ClickableViewAccessibility")
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
        edtCodRec = findViewById<EditText>(R.id.edtCodRecuperacao)
        edtNovaSenha = findViewById<EditText>(R.id.edtNewPassword)

        tvCodRec = findViewById<TextView>(R.id.textView3)
        tvNovaSenha = findViewById<TextView>(R.id.textView4)

        val btn1 = findViewById<Button>(R.id.btn_submit)
            btn1.isEnabled = false
        btn2 = findViewById<Button>(R.id.btn_submit2)
        btn3 = findViewById<Button>(R.id.btn_submit3)

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

        edtNovaSenha.validPassword()
        edtNovaSenha.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= edtNovaSenha.right - edtNovaSenha.compoundDrawables[2].bounds.width()) {
                    if (edtNovaSenha.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        edtNovaSenha.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        edtNovaSenha.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_eye,
                            0
                        )
                    } else {
                        edtNovaSenha.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        edtNovaSenha.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_eye,
                            0
                        )
                    }
                    edtNovaSenha.setSelection(edtNovaSenha.text.length)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

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

        //verificar código de recuperação
        btn2.setOnClickListener{
            val codResgate = edtCodRec.text.toString()
            val dbHelper = DatabaseHelper(this)

            val codResInt = dbHelper.getCodigoRecuperacao()
            val codResString = codResInt.toString()

            if(codResString.equals(codResgate)){
                tvNovaSenha.visibility = View.VISIBLE
                edtNovaSenha.visibility = View.VISIBLE
                btn3.visibility = View.VISIBLE
                btn3.isEnabled = false
            }
        }

        // alteração da senha no DB
        btn3.setOnClickListener {
           // Toast.makeText(this@EsqueciSenhaActivity, "Botão acionado. Cliente $idCliente", Toast.LENGTH_SHORT).show()
            val newPass = edtNovaSenha.text.toString()
            mudarSenha(newPass, idCliente, token)
        }

        // Fim OnCreate
    }

    private fun getToken(username: String, callback: (Pair<String, Long>) -> Unit) {
        val authRepository = AuthRepository()

        authRepository.getToken(username, object : RetrofitCallback<TokenResponse> {
            override fun onResponse(call: RetrofitCall<TokenResponse>, response: RetrofitResponse<TokenResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    token = tokenResponse?.accessToken ?: "invalid_token"
                    val expiresIn: Long = tokenResponse?.expiresInSeconds ?: 0L
                    callback(Pair(token, expiresIn))

                    Log.i(
                        "Token_recuperado",
                        "O token foi recuperado com exito: $token e expira em: $expiresIn segundos"
                    )

                  /*  Toast.makeText(
                        this@EsqueciSenhaActivity,
                        "Token recuperado",
                        Toast.LENGTH_SHORT
                    ).show() */
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

            override fun onFailure(call: RetrofitCall<TokenResponse>, t: Throwable) {
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

        authRepository.validaEmail(email, token, object : RetrofitCallback<Long> {
            override fun onResponse(call: RetrofitCall<Long>, response: RetrofitResponse<Long>){
                if(response.isSuccessful && response.body() != null){
                    idCliente = response.body()!!
                    Log.i("Sucesso_busca", "E-mail encontrado no DB")
                    Toast.makeText(this@EsqueciSenhaActivity, "Código de recuperação enviado para o e-mail", Toast.LENGTH_LONG).show()

                    tvCodRec.visibility = View.VISIBLE
                    edtCodRec.visibility = View.VISIBLE
                    btn2.visibility = View.VISIBLE

                    salvarCodRec()
                } else {
                    Log.e("Erro_busca_email","E-mail não encontrado.")
                    Toast.makeText(this@EsqueciSenhaActivity, "E-mail não encontrado.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: RetrofitCall<Long>, t: Throwable){
                Log.wtf("Falha_comunicação","Erro: ${t.message}")
                Toast.makeText(this@EsqueciSenhaActivity, "Falha na comunicação.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun gerarCodRec(): Int{
        val codRandom = kotlin.random.Random

        return codRandom.nextInt(1000, 9999)
    }

    private fun salvarCodRec(){
        val dbHelper = DatabaseHelper(this)
        val cod: Int = gerarCodRec()
        val success = dbHelper.salvarCodigo(cod)
        if (success) {
            Log.i("Codigo_gerado", "O código gerado foi: $cod")
            //Toast.makeText(this, "Código salvo com sucesso!", Toast.LENGTH_SHORT).show()
            enviarEmail(cod.toString(), "adson-luks@hotmail.com".toString())
        } else {
            Log.e("salvar_codigo", "Falha ao tentar salvar o código $cod no DB")
            Toast.makeText(this, "Falha ao salvar o código.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarEmail(codigo: String, email: String,) {
        val client = OkHttpClient.Builder().build()

        val json = JSONObject().apply {
            put("Messages", JSONArray().put(JSONObject().apply {
                put("From", JSONObject().apply {
                    put("Email", "adsonalsf@gmail.com")
                    put("Name", "SysEstoque")
                })
                put("To", JSONArray().put(JSONObject().apply {
                    put("Email", email)
                    put("Name", "Cliente")
                }))
                put("Subject", "Recuperação de Senha")
                put("TextPart", "Seu código de recuperação é: $codigo")
                put("HTMLPart", "<h3>Seu código de recuperação é: <strong>$codigo</strong></h3>")
            }))
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://api.mailjet.com/v3.1/send")
            .post(requestBody)                                  //Minha API_KEY                                 minha API_SECRET
            .addHeader("Authorization", Credentials.basic("ae995a93d65c80d1723ed1bf602ae1b4", "93bb9067df4de1161e25cd30ff01d3f9"))
            .addHeader("Content-Type", "application/json")
            .build()

        // Envie a requisição
        client.newCall(request).enqueue(object : OkHttpCallback {
            override fun onFailure(call: OkHttpCall, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: OkHttpCall, response: OkHttpResponse) {
                if (response.isSuccessful) {
                    // E-mail enviado com sucesso
                    Log.i("EmailStatus", "E-mail enviado com sucesso.")
                } else {
                    // Captura o código de resposta e a mensagem
                    Log.e("EmailStatus", "Erro ao enviar e-mail: Código: ${response.code}, Mensagem: ${response.message}")

                    // Tente capturar o corpo da resposta para mais detalhes
                    val errorBody = response.body?.string()
                    Log.e("EmailStatus", "Corpo da resposta: $errorBody")
                    response.body?.let {
                        Log.e("EmailStatus", "Corpo da resposta: ${it.string()}") }
                }
            }
        })
    }

    private fun EditText.validPassword() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                btn3.isEnabled = password.length >= 6
                        &&
                        password.matches(Regex("""^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&+-.;></)(])[A-Za-z\d@$!%*?&+-.;></)(]{6,}$"""))
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun mudarSenha(newPassword: String, id: Long, token: String) {
        val authRepository = AuthRepository()

        authRepository.setNewPassword(newPassword, id, token, object : RetrofitCallback<PassResponse> {

            override fun onResponse(call: RetrofitCall<PassResponse>, response: RetrofitResponse<PassResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EsqueciSenhaActivity, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                    Log.i("SenhaAlterada","A senha do cliente id: $id foi alterada com sucesso!")
                    finish()
                } else {
                    Log.e("ErroAlteracaoSenha","Erro ao tentar alterar a senha do cliente id = $id")
                    Toast.makeText(this@EsqueciSenhaActivity, "Erro ao alterar a senha. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: RetrofitCall<PassResponse>, t: Throwable) {
                Toast.makeText(this@EsqueciSenhaActivity, "Falha na comunicação com o servidor.", Toast.LENGTH_SHORT).show()
            }
        })
    }

}