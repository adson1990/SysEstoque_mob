package com.example.sysestoque.backend

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

class ClientRepository() {

    private val apiClient: ApiClient
    private val authRepository: AuthRepository = AuthRepository()

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(logging) // Adiciona o interceptador
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.3.2:8080/") // estou usando genymotion, url base do virtualizador é diferente de uma máquina do próprio android studio
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // conversor automático de dados Kotlin ou Java em Json
            .build()

        apiClient = retrofit.create(ApiClient::class.java) //O Retrofit gera automaticamente o código necessário para fazer requisições HTTP baseadas nos métodos definidos na interface
    }

    fun registerClient(cliente: Client, callback: Callback<Client>) {
        val call = apiClient.registerClient(cliente)
        call.enqueue(callback)
    }

    fun getComprasPorIdCliente(id: Long, token: String, callback: Callback<ComprasResponse>){
        val retrofitWithToken = authRepository.requestToken(token)
        val apiComprasWithToken = retrofitWithToken.create(ApiCompras::class.java)

        val comprasCall = apiComprasWithToken.getComprasPorId(id)
        comprasCall.enqueue(object : Callback<ComprasResponse> {
            override fun onResponse(call: Call<ComprasResponse>, response: Response<ComprasResponse>) {
                if (response.isSuccessful) {
                    callback.onResponse(call, response)
                } else {
                    callback.onFailure(call, Throwable("Erro ao buscar as compras"))
                }
            }

            override fun onFailure(call: Call<ComprasResponse>, t: Throwable) {
                callback.onFailure(call, t)
            }
        })
    }

    fun validaEmail(email: String, token: String, callback: Callback<Long>) {
        //  val request = EmailRequest(email)

        // retrofit personalizado para esta requisição, já que o token precisa ir junto
        val retrofitWithToken = authRepository.requestToken(token)

        val apiEmailWithToken = retrofitWithToken.create(ApiEmail::class.java)

        // Agora, faz a chamada com o Retrofit que inclui o token no header
        val emailCall = apiEmailWithToken.searchEmail(email)
        emailCall.enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                callback.onResponse(call, response)
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                callback.onFailure(call, t)
            }
        })
    }

    fun setNewPassword(password: String, id: Long, token: String, callback: Callback<PassResponse>){

        val retrofitWithToken = Retrofit.Builder()
            .baseUrl("http://10.0.3.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .method(original.method, original.body)
                val requestWithToken = requestBuilder.build()
                chain.proceed(requestWithToken)
            }.build())
            .build()

        val apiEmailWithToken = retrofitWithToken.create(ApiEmail::class.java)

        val passRequest = PassRequest(password, id)

        val passCall = apiEmailWithToken.newPassword(id, passRequest)
        passCall.enqueue(object : Callback<PassResponse> {
            override fun onResponse(call: Call<PassResponse>, response: Response<PassResponse>) {
                if (response.isSuccessful) {
                    callback.onResponse(call, response)
                } else {
                    Log.e("FailureConnection","A senha do cliente não será alterada! Falha na comunicação")
                    callback.onFailure(call, Throwable("Erro ao alterar a senha"))
                }
            }

            override fun onFailure(call: Call<PassResponse>, t: Throwable) {
                callback.onFailure(call, t)
            }
        })
    }

    fun getClientById(id: Long, token: String, callback: Callback<Client>) {
        Log.d("Token_Debug", "Token: Bearer $token")
        val accessToken = apiClient.getClientById(id, "Bearer $token")
        accessToken.enqueue(object : Callback<Client> {
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                if(response.isSuccessful){
                    callback.onResponse(call, response)
                } else {
                    callback.onFailure(call, Throwable("Erro ao buscar cliente por ID."))
                }
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
                callback.onFailure(call, t)
            }
        })
    }

    fun getTokenByEmail(email: String, callback: Callback<TokenResponse>){
        val request = TokenRequest(email)
        val call = apiClient.getTokenByEmail(request)
        call.enqueue(callback)
    }
}