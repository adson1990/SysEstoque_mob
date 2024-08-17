package com.example.sysestoque.ui.login

import android.app.Activity
import android.os.Build
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.sysestoque.databinding.ActivityLoginBinding

import com.example.sysestoque.R

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

   // @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.edtUsername
        val password = binding.edtPassword
        val login = binding.btnLogin
        val loading = binding.loading
        val register: TextView = findViewById(R.id.tvRegistro)
        val remember: CheckBox = findViewById(R.id.chkLembrarUser)
        val forgotPass: TextView = findViewById(R.id.tvEsquecerSenha)
        val scaleAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_animation) // animação de clique

        // animação ao clicar no TV de esquecer a senha
        forgotPass.setOnClickListener{
            forgotPass.startAnimation(scaleAnimation)
        }

        register.setOnClickListener{
            register.startAnimation(scaleAnimation)
        }

       remember.setOnCheckedChangeListener { _, isChecked ->
           val colorResId = if (isChecked) R.color.green_500 else R.color.blue_600
           val colorStateList = ContextCompat.getColorStateList(this, colorResId)
           if (isChecked) {
               remember.buttonTintList = ContextCompat.getColorStateList(this, R.color.green_500)
               remember.setTextColor(colorStateList)
           } else {
               remember.buttonTintList = ContextCompat.getColorStateList(this, R.color.blue_600)
               remember.setTextColor(colorStateList)
           }
       }


       //Cria uma instância do ViewModel, que gerencia a lógica de negócios e os dados da UI.
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        //Observa mudanças no estado do formulário de login, como validação de campos.
        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // botão de login só ficará habilitado se os dados do formulário de login forem considerados válidos.
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        //Observa o resultado da tentativa de login (sucesso ou erro).
        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        //Verifica se o texto do campo de usuário foi alterado e atualiza o estado do formulário para usuário e senha.
        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            //istener para ações específicas do teclado virtual (IME)
            setOnEditorActionListener { _, actionId, _ -> //param => 1- é o TextView associado, mas foi ignorado, 2- indica a ação do user, 3- Este parâmetro é um KeyEvent, representando o evento de tecla específico
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login( // chamar o endpoint de login para validar entrada no sistema
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            //Define o que acontece quando o botão de login é clicado. Chama o método login do ViewModel com os valores inseridos pelo usuário.
            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }

    //Atualiza a interface do usuário após um login bem-sucedido, mostrando uma mensagem de boas-vindas.
    private fun updateUiWithUser(model: LoggedInUserView) {
        val sexo = "M"; //retorno da API
        val saudacao = if (sexo == "M") getString(R.string.welcome_male) else getString(R.string.welcome_female)

        val user = binding.edtUsername.text.toString()
        val nome = user.substringBefore("@").uppercase()
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$saudacao $nome",
            Toast.LENGTH_LONG
        ).show()
    }

    //Exibe uma mensagem de erro se o login falhar.
    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
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