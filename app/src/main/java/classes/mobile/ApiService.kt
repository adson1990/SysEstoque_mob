package classes.mobile

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/api/client")
    fun cadastrarCliente(@Body client: Client): Call<Client>
}