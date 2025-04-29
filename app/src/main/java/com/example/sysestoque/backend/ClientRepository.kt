package com.example.sysestoque.backend

import android.content.Context
import android.util.Log
import com.example.sysestoque.backend.retrofit.ApiClient
import com.example.sysestoque.backend.retrofit.ApiCompra
import com.example.sysestoque.backend.retrofit.ApiEmail
import com.example.sysestoque.backend.retrofit.PassRequest
import com.example.sysestoque.backend.retrofit.PassResponse
import com.example.sysestoque.backend.retrofit.TokenResponse
import com.example.sysestoque.backend.retrofit.UltimaCompra
import com.example.sysestoque.data.database.DbHelperConfig
import com.example.sysestoque.data.utilitarios.Funcoes
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.Callback
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

class ClientRepository(private val context: Context) {

    private val dbHelperCongif: DbHelperConfig = DbHelperConfig(context)
    private val funcoes: Funcoes = Funcoes()
    private val apiCompra: ApiCompra
    private val apiClient: ApiClient
    private val authRepository: AuthRepository = AuthRepository()

    val authInterceptor = Interceptor { chain ->
        val tokenData = funcoes.getToken(context)
        val accessToken = tokenData.token

        val original = chain.request()
        val requestBuilder = original.newBuilder()

        if (!accessToken.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        val request = requestBuilder.build()
        chain.proceed(request)
    }

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.3.2:8080/") // estou usando genymotion, url base do virtualizador é diferente de uma máquina do próprio android studio
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // conversor automático de dados Kotlin ou Java em Json
            .build()

        apiClient = retrofit.create(ApiClient::class.java) //O Retrofit gera automaticamente o código necessário para fazer requisições HTTP baseadas nos métodos definidos na interface
        apiCompra = retrofit.create(ApiCompra::class.java)
    }

    suspend fun getComprasPorIdCliente(id: Long): List<Compras>? {
        val response = apiCompra.getComprasPorCliente(id)
        if (response.isSuccessful) {
            return response.body()
        } else {
            Log.e("API_ERROR", "Erro ao buscar compras: ${response.code()} - ${response.errorBody()?.string()}")
            return null
        }
    }

    fun getUltimasCompras(id: Long, token: String, callback: Callback<List<UltimaCompra>>) {
        val authHeader = "Bearer $token"

        val compras = dbHelperCongif.getConfiguracoes(id)
        val ordemCompra = compras?.ordemCompras

        if (ordemCompra.equals("Data")) {apiCompra.getUltimasCompras(id, authHeader).enqueue(callback)}
        else {apiCompra.getUltimasComprasPorValor(id, authHeader).enqueue(callback)}
    }

    fun registerClient(cliente: Client, callback: Callback<Client>) {
        val call = apiClient.registerClient(cliente)
        call.enqueue(callback)
    }

    fun validaEmail(email: String, token: String, callback: Callback<Long>) {
        // retrofit personalizado para esta requisição, já que o token precisa ir junto
        val retrofitWithToken = authRepository.requestToken(token) // Cria a requisição retrofit2 com o Token no cabeçalho

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

    fun updateClient(email: String, id: Long, client: Client, token: String, callback: Callback<Client>) {
        if (token != "") {
            apiClient.updateClientWithToken(id, client, "Bearer $token")
                .enqueue(callback)
        } else {
        // 1. Buscar o token com o e-mail fornecido
        authRepository.getTokenByEmail(email, object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.accessToken ?: ""
                    val expiresIn = response.body()?.expiresIn ?: -1
                    val refreshToken = response.body()?.refreshToken ?: ""
                    funcoes.saveToken(context, token, expiresIn, refreshToken)
                    if (token != null) {
                        // 2. Chamar o metodo PATCH com o token no cabeçalho
                        apiClient.updateClientWithToken(id, client, "Bearer $token")
                            .enqueue(callback)
                    } else {
                        callback.onFailure(
                            null,
                            Throwable("Token é nulo")
                        )
                    }
                } else {
                    callback.onFailure(null, Throwable("Falha na obtenção do token"))
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                callback.onFailure(null, t)
            }
        })
      }
    }

    fun setNewPassword(password: String, id: Long, token: String, callback: Callback<PassResponse>){

        val apiEmailWithToken = authRepository.requestToken(token).create(ApiEmail::class.java)

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

}