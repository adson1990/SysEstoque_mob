package com.example.sysestoque.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.sysestoque.DashboardActivity
import com.example.sysestoque.EsqueciSenhaActivity
import com.example.sysestoque.databinding.ActivityLoginBinding
import com.example.sysestoque.R
import com.example.sysestoque.RegistroActivity
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.LoginRequest
import com.example.sysestoque.backend.LoginResponse
import com.example.sysestoque.data.database.DbHelperLogin
import com.example.sysestoque.data.database.LoginInfo
import com.example.sysestoque.data.utilitarios.Funcoes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var loading: ProgressBar
    private lateinit var remember: CheckBox
    private lateinit var dbHelperLogin: DbHelperLogin
    private lateinit var funcoes: Funcoes

   // @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

       criarTabelas()

       val (id, nome) = loginAutomatico()

       if( id > 0){
          abrirDashboard(id, nome)
       }

       authRepository = AuthRepository()
       funcoes = Funcoes()

        val username = binding.edtUsername
        val password = binding.edtPassword
        val login = binding.btnLogin
        loading = binding.loading
        val register: TextView = findViewById(R.id.tvRegistro)
        remember = findViewById(R.id.chkLembrarUser)
        val forgotPass: TextView = findViewById(R.id.tvEsquecerSenha)
        val edtPassword: EditText = findViewById(R.id.edt_password)
        val scaleAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_animation) // animação de clique

        edtPassword.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_UP) {
                if(event.rawX >= edtPassword.right - edtPassword.compoundDrawables[2].bounds.width()){
                    edtPassword.transformationMethod = if (edtPassword.transformationMethod is PasswordTransformationMethod) {
                        null // Torna visível
                    } else {
                        PasswordTransformationMethod.getInstance()
                    }
                    edtPassword.setSelection(edtPassword.length())
                    return@setOnTouchListener true
                }
            }
            false
        }
        edtPassword.addTextChangedListener(object : TextWatcher {
           override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

           override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               login.isEnabled = !s.isNullOrEmpty()
           }

           override fun afterTextChanged(s: Editable?) {}
       })

        // animação ao clicar no TV de esquecer a senha
        forgotPass.setOnClickListener{
            forgotPass.startAnimation(scaleAnimation)
            abrirResetPassword()
        }

        register.setOnClickListener{
            register.startAnimation(scaleAnimation)
            abrirRegistroDeCliente()
        }

       login.setOnClickListener {
           val usernameInput = username.text.toString().trim()
           val passwordInput = password.text.toString().trim()

           if (usernameInput.isNotEmpty() && passwordInput.isNotEmpty()) {
               login.isEnabled = true
               loading.visibility = View.VISIBLE
               login(usernameInput, passwordInput)
           } else {
               funcoes.exibirToast(this@LoginActivity, R.string.alerta_preencher_campos,"",0)
           }
       }

       remember.setOnCheckedChangeListener { _, isChecked ->

           val colorResId = if (isChecked) R.color.pink_200 else R.color.gray_50
           val colorStateList = ContextCompat.getColorStateList(this, colorResId)
           if (isChecked) {
               remember.buttonTintList = ContextCompat.getColorStateList(this, R.color.pink_200)
               remember.setTextColor(colorStateList)
           } else {
               remember.buttonTintList = ContextCompat.getColorStateList(this, R.color.gray_50)
               remember.setTextColor(colorStateList)
           }
       }
    // fim onCreate
    }

    private fun criarTabelas() {
        dbHelperLogin = DbHelperLogin(this)

        dbHelperLogin.popularTabelasInicio()
    }

    private fun login(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)

        Log.d("LoginActivity", "LoginRequest: $loginRequest")

        authRepository.authApi.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.accessToken ?: ""
                    val sexo = response.body()?.sexo ?: ' '
                    val id = response.body()?.id ?: 0
                    val foto = response.body()?.foto ?: ""
                    val saudacao = if (sexo == 'M') R.string.welcome_male else R.string.welcome_female

                    val rememberMe = remember.isChecked

                    val user = binding.edtUsername.text.toString()
                    val nome = user.substringBefore("@").uppercase()

                    funcoes.exibirToast(this@LoginActivity, saudacao, nome, 0)

                    salvarUsuario(rememberMe, id, username, foto)
                    saveToken(token)
                    abrirDashboard(id, user)
                } else {
                    loading.visibility = View.GONE
                    val errorMessage = try {
                        response.errorBody()?.string() ?: "Erro desconhecido"
                    } catch (e: Exception) {
                        Log.e("Erro_response", "Erro ao tentar processar resposta do endpoint ", e)
                        "Erro ao processar a resposta"
                    }
                    funcoes.exibirToast(this@LoginActivity, R.string.login_failed, errorMessage,1)
                    loading.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                funcoes.exibirToast(this@LoginActivity,R.string.error, t.message.toString(), 1)
            }
        })
    }

    fun salvarUsuario(remenberMe: Boolean, id: Long, email: String, foto: String){
        dbHelperLogin = DbHelperLogin(this)

        dbHelperLogin.gravarUsuarioLogin(remenberMe, id, email, foto)
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.apply()
    }

    private fun abrirDashboard(id: Long, nome: String) {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            putExtra("NOME_CLIENTE", nome)
            putExtra("ID_CLIENTE", id)
        }
        startActivity(intent)
        finish()
    }

    private fun abrirRegistroDeCliente(){
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
    }

    private fun abrirResetPassword(){
        val intent = Intent(this, EsqueciSenhaActivity::class.java)
        startActivity(intent)
    }

    private fun loginAutomatico(): Pair<Long, String> {
        dbHelperLogin = DbHelperLogin(this)

        val (idUsuario, nomeUsuario) = dbHelperLogin.checarLoginAutomatico()


    return Pair(idUsuario, nomeUsuario)
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
    }
}