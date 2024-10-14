package com.example.sysestoque.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sysestoque.data.LoginRepository
import com.example.sysestoque.data.Result

import com.example.sysestoque.R
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.LoginRequest
import com.example.sysestoque.backend.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _loginFormState = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginFormState

    fun login(username: String, password: String) {

        val loginRequest = LoginRequest(username, password)
        authRepository.authApi.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        _loginResult.postValue(LoginResult(success = LoggedInUserView(loginResponse.token)))
                    }
                } else {
                    _loginResult.postValue(LoginResult(error = R.string.login_failed))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _loginResult.postValue(LoginResult(error = R.string.login_failed))
            }
        })
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginFormState.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginFormState.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
     /*   return if (username.contains('@') && username.contains(".com")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }*/
        return android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
                            //^começo da string
                           //?= positive lookahead  verifica se uma determinada sequência de caracteres é encontrada à frente da posição atual
                           //mas não consome esses caracteres na correspondência
                           //$ indica o final da string
        val passwordPattern = """^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&+-.;></)(])[A-Za-z\d@$!%*?&+-.;></)(]{6,}$"""
        val passwordMatcher = Regex(passwordPattern)

        return passwordMatcher.matches(password)

    }
}