package com.example.sysestoque

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.util.Base64
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.RequestListener
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.Client
import com.example.sysestoque.backend.ClientRepository
import com.example.sysestoque.backend.retrofit.TokenResponse
import com.example.sysestoque.backend.retrofit.TokenRefreshResponse
import com.example.sysestoque.data.database.ColorDatabaseHelper
import com.example.sysestoque.data.database.DbHelperConfig
import com.example.sysestoque.data.database.DbHelperLogin
import com.example.sysestoque.data.database.LoginInfo
import com.example.sysestoque.data.utilitarios.ActivityManager
import com.example.sysestoque.data.utilitarios.Funcoes
import com.example.sysestoque.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.io.encoding.ExperimentalEncodingApi

private lateinit var drawerLayout: DrawerLayout
private lateinit var navView: NavigationView
private lateinit var dbHelperLogin: DbHelperLogin
private lateinit var dbHelper: ColorDatabaseHelper
private lateinit var dbConfig: DbHelperConfig
@SuppressLint("StaticFieldLeak")
private lateinit var linearLayoutCompras: LinearLayout
@SuppressLint("StaticFieldLeak")
private lateinit var tvProductDate1: TextView
@SuppressLint("StaticFieldLeak")
private lateinit var tvProductName1: TextView
@SuppressLint("StaticFieldLeak")
private lateinit var tvProductPrice1: TextView
@SuppressLint("StaticFieldLeak")
private lateinit var tvProductDate2: TextView
@SuppressLint("StaticFieldLeak")
private lateinit var tvProductName2: TextView
@SuppressLint("StaticFieldLeak")
private lateinit var tvProductPrice2: TextView
@SuppressLint("StaticFieldLeak")
private lateinit var progressBar: ProgressBar
@SuppressLint("StaticFieldLeak")
private lateinit var imageView: ImageView
@SuppressLint("StaticFieldLeak")
private lateinit var tvNomeUsuario: TextView
private lateinit var funcoes: Funcoes
@SuppressLint("StaticFieldLeak")
private lateinit var imgCompras : ImageView


private var loginInfo: LoginInfo? = null
private val authRepository = AuthRepository()

private var idClient: Long = -1L
private var mailUser: String = ""
private var isDataLoaded = false


class DashboardActivity : AppCompatActivity() {
    private val clientRepository by lazy { ClientRepository(this@DashboardActivity) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ActivityManager.addActivity(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        // Referências das views
        tvNomeUsuario = findViewById<TextView>(R.id.tvNomeUsuario)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        imageView = findViewById<ImageView>(R.id.ftUsuario)
        tvProductDate1 = findViewById<TextView>(R.id.tvDataCompra1)
        tvProductName1 = findViewById<TextView>(R.id.tvNomeProduto1)
        tvProductPrice1 = findViewById<TextView>(R.id.tvValorTotal1)
        tvProductName2 = findViewById<TextView>(R.id.tvNomeProduto2)
        tvProductDate2 = findViewById<TextView>(R.id.tvDataCompra2)
        tvProductPrice2 = findViewById<TextView>(R.id.tvValorTotal2)
        linearLayoutCompras = findViewById<LinearLayout>(R.id.linearLayoutCompras)
        imgCompras = findViewById<ImageView>(R.id.imageView1)

        // Layout do menu lateral
        drawerLayout = findViewById(R.id.drawer_layout_dashboard)
        navView = findViewById(R.id.nav_view)

        // persistência de dados
        dbHelper = ColorDatabaseHelper(this)
        dbHelperLogin = DbHelperLogin(this)
        dbConfig = DbHelperConfig(this)

        // Funções de apoio
        funcoes = Funcoes()

        // Configura o ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // obtendo dados do cliente
        val (id, email) = obterDadosUsuario()
        idClient = id
        mailUser = email

        imgCompras.setOnClickListener {
            val idCliente = idClient
            abrirComprasActivity(idCliente)
        }

        // Lidando com cliques no menu
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_profile -> {
                    abrirProfileActivity(idClient)
                    drawerLayout.closeDrawers()
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_settings -> {
                    abrirSettingsActivity(idClient)
                    drawerLayout.closeDrawers()
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_exit -> {
                    funcoes.logout(this@DashboardActivity)
                    drawerLayout.closeDrawers()
                    return@setNavigationItemSelectedListener true
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        salvarIdCliente(idClient)
        loadDataUser(mailUser, idClient)
        isDataLoaded = true

        // fim do onCreate
    }

    private fun loadDataUser(nomeUsuario: String, id: Long){
        val name = nomeUsuario.substringBefore("@").uppercase()
        tvNomeUsuario.text = name

        val config = dbConfig.getConfiguracoes(id)

        if (config != null && !config.ultimasCompras) {
            linearLayoutCompras.visibility = View.GONE
        } else{
            linearLayoutCompras.visibility = View.VISIBLE
        }

       // fetchTokenAndCompras(id)
        mostrarDadosDB()
        carregaFotoUser(id)

        val colors = dbHelper.getColors(id)
        if(colors != null) {
            colors.let { (red, green, blue) ->
                val hsv = FloatArray(3)
                Color.RGBToHSV(red, green, blue, hsv)
                hsv[1] = 1.0f
                hsv[2] = 0.8f

                val vibrantColor = Color.HSVToColor(hsv)
                if(red == 0 && green == 0 && blue == 0){
                    tvNomeUsuario.setTextColor(Color.BLACK)
                } else {
                    tvNomeUsuario.setTextColor(vibrantColor)
                }
            }
        } else {
            tvNomeUsuario.setTextColor(Color.BLACK)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun carregaFotoUser(id: Long){
        //imageView.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
        progressBar.visibility = View.VISIBLE

        val imagePath = dbHelperLogin.getFotoUsuario(id)

        if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                // Carregar a imagem diretamente do armazenamento interno
                Glide.with(this)
                    .load(file)
                    .placeholder(R.mipmap.user_icon)
                    .error(R.mipmap.user_icon)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }
                    })
                    .into(imageView)

                progressBar.visibility = View.GONE
                return
            }
        }

        // Caso não tenha imagem no banco local, buscar no repositório online
        val imgBase64 = getAccessToken(id, mailUser)

        if (!imgBase64.isNullOrEmpty()) {
            try {
                val imageDecode = Base64.decode(imgBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageDecode, 0, imageDecode.size)

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.mipmap.user_icon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(R.mipmap.user_icon)
            }
        } else {
            imageView.setImageResource(R.mipmap.user_icon)
        }

        progressBar.visibility = View.GONE
    }

    private fun getAccessToken(idCliente: Long, email: String): String{
        var foto : String = ""
        var tokenData = funcoes.getToken(this@DashboardActivity)
        val accessToken = tokenData.token
        val refresh = tokenData.refreshToken
        val expiresIn = tokenData.expiresIn
        val timeStamp = tokenData.tokenTimestamp

        if(funcoes.isTokenValid(expiresIn, timeStamp)){
            foto = getFotoCliente(idCliente, accessToken!!)
        }else if (refresh != null) {
            authRepository.getRefreshToken(refresh!!, object : Callback<TokenRefreshResponse> {
                override fun onResponse(
                    call: Call<TokenRefreshResponse>,
                    response: Response<TokenRefreshResponse>
                ) {
                    if (response.isSuccessful) {
                        val token = response.body()?.accessToken ?: ""
                        val expiresIn = response.body()?.expiresIn ?: -1
                        val refreshToken = response.body()?.refreshToken ?: ""
                        if (!token.isNullOrEmpty()) {
                            funcoes.saveToken(this@DashboardActivity, token, expiresIn, refreshToken)
                            foto = getFotoCliente(idCliente, token)
                        } else {
                            funcoes.exibirToast(
                                this@DashboardActivity,
                                R.string.erro_buscar_token,
                                "",
                                1
                            )
                        }
                    } else {
                        funcoes.exibirToast(
                            this@DashboardActivity,
                            R.string.erro_buscar_cliente,
                            "",
                            1
                        )
                    }
                }

                override fun onFailure(call: Call<TokenRefreshResponse>, t: Throwable) {
                    funcoes.exibirToast(
                        this@DashboardActivity,
                        R.string.erro_conexao_db,
                        t.message.toString(),
                        1
                    )
                    Log.e("Erro_busca_cliente", "Erro ao tentar recuperar token: ${t.message}")
                }

            })
        }

        return foto
    }

    // Função para buscar o token e depois as compras
  /*  private fun fetchTokenAndCompras(idCliente: Long) {
        val (token, refreshToken, expiredIn, tokenTimestamp) = funcoes.getToken(this@DashboardActivity)
        val valido = funcoes.isTokenValid(expiredIn, tokenTimestamp)

        if (token != null && valido) { // Se token ainda for válido
            fetchCompras(idCliente, token)
        } else if (token != null) { // se n for
            authRepository.getRefreshToken(refreshToken!!, object : Callback<TokenRefreshResponse> {
                override fun onResponse(
                    call: Call<TokenRefreshResponse>,
                    response: Response<TokenRefreshResponse>
                ) {
                    if (response.isSuccessful) {
                        val token = response.body()?.accessToken ?: ""
                        val expiresIn = response.body()?.expiresIn ?: -1
                        val refreshToken = response.body()?.refreshToken ?: ""
                        funcoes.saveToken(this@DashboardActivity, token, expiresIn, refreshToken)
                        // Agora com o token, realizar busca pelas compras
                        fetchCompras(idCliente, token)
                    } else {
                        funcoes.exibirToast(
                            this@DashboardActivity,
                            R.string.erro_buscar_token,
                            response.message(),
                            0
                        )
                    }
                }

                override fun onFailure(call: Call<TokenRefreshResponse>, t: Throwable) {
                    funcoes.exibirToast(
                        this@DashboardActivity,
                        R.string.erro_buscar_token,
                        t.message.toString(),
                        0
                    )
                }
            })
        } else { // se não tiver token salvo no sharedPreferences solicitar novo token
            val clientLoged = dbHelperLogin.getUsuarioLogado()
            val email = clientLoged?.email ?: ""
            authRepository.getTokenByEmail(email, object : Callback<TokenResponse> {
                override fun onResponse(
                    call: Call<TokenResponse>,
                    response: Response<TokenResponse>
                ) {
                    if (response.isSuccessful) {
                        val token = response.body()?.accessToken ?: ""
                        val expiresIn = response.body()?.expiresIn ?: -1
                        val refreshToken = response.body()?.refreshToken ?: ""
                        funcoes.saveToken(this@DashboardActivity, token, expiresIn, refreshToken)
                        // Agora com o token, realizar busca pelas compras
                        fetchCompras(idCliente, token)
                    } else {
                        funcoes.exibirToast(
                            this@DashboardActivity,
                            R.string.erro_buscar_token,
                            response.message(),
                            0
                        )
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    funcoes.exibirToast(
                        this@DashboardActivity,
                        R.string.erro_buscar_token,
                        t.message.toString(),
                        0
                    )
                }

            })
        }
    }

    fun fetchCompras(idCliente: Long, token: String) {
        clientRepository.getComprasPorIdCliente(idCliente, token, object : Callback<ComprasResponse> {
            override fun onResponse(call: Call<ComprasResponse>, response: Response<ComprasResponse>) {
                if (response.isSuccessful) {
                    val comprasList = response.body()?.content ?: emptyList()

                    if (comprasList.isNotEmpty()) {
                        if (linearLayoutCompras.visibility == View.GONE) {
                            linearLayoutCompras.visibility = View.VISIBLE
                        }

                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        // Preencher a primeira compra
                        if (comprasList.isNotEmpty()) {
                            val compra1 = comprasList[0]
                            tvProductName1.text = compra1.name
                            tvProductPrice1.text = compra1.valor.toString()
                            tvProductDate1.text = formatter.format(compra1.dataVenda)
                        }

                        if (comprasList.size > 1) {
                            val compra2 = comprasList[1]
                            tvProductName2.text = compra2.name
                            tvProductPrice2.text = compra2.valor.toString()
                            tvProductDate2.text = formatter.format(compra2.dataVenda)
                        }
                    }
                    else {
                        linearLayoutCompras.visibility = View.GONE
                    }
                } else {
                    funcoes.exibirToast(this@DashboardActivity, R.string.erro_conexao_db, response.message(), 1)
                }
            }

            override fun onFailure(call: Call<ComprasResponse>, t: Throwable) {
                funcoes.exibirToast(this@DashboardActivity, R.string.erro_conexao_db, t.message.toString(), 1)
            }
        })
    } */

    fun getFotoCliente(idCliente: Long, token: String): String {
        var clientePhoto : String = ""

        clientRepository.getClientById(idCliente, token, object : Callback<Client> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                if (response.isSuccessful) {
                    val cliente = response.body()
                    if (cliente != null && cliente.foto != "") {
                        clientePhoto = cliente?.foto?.takeIf { it.isNotEmpty() }!!
                    }
                } else {
                    funcoes.exibirToast(this@DashboardActivity, R.string.erro_buscar_cliente,"",1)
                }
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
                funcoes.exibirToast(this@DashboardActivity, R.string.erro_conexao_db, t.message.toString(), 1)
                Log.e(
                    "Erro_busca_cliente",
                    "Erro ao tentar recuperar dados do cliente. ${t.message}"
                )
            }
        })
        return clientePhoto
    }

    private fun obterDadosUsuario(): Pair<Long, String> {
        var idUsuario = intent.getLongExtra("ID_CLIENTE", -1L)
        var emailUsuario = intent.getStringExtra("EMAIL_CLIENTE")

        if (idUsuario == -1L || emailUsuario.isNullOrEmpty()) {
            // Se não conseguiu obter da Intent, tenta buscar do banco de dados
            val (idBanco, emailBanco) = buscarDadosDoSQLite()

            if (idBanco > 0) {
                idUsuario = idBanco
                emailUsuario = emailBanco
            } else {
                // Se não conseguiu obter o usuário logado das 2 maneiras anteriores redireciona para tela de login
                funcoes.exibirToast(this@DashboardActivity, R.string.login_failed, "", 1)
                abrirLoginActivity()
                finish()
                return Pair(-1L, "")
            }
        }

        return Pair(idUsuario, emailUsuario ?: "")
    }

    private fun buscarDadosDoSQLite(): Pair<Long, String> {
        loginInfo = dbHelperLogin.getUsuarioLogado()
        val id = loginInfo?.idClient ?: -1L
        val emailUsuario = loginInfo?.email ?: ""

        return Pair(id, emailUsuario)
    }

    private fun salvarIdCliente(idCliente: Long) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong("ID_CLIENTE", idCliente)
        editor.apply()
    }

    private fun obterIdCliente(): Long {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return prefs.getLong("ID_CLIENTE", -1L)
    }

    private fun abrirProfileActivity(id: Long) {
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra("ID_CLIENTE", id)
        }
        startActivity(intent)
    }

    private fun abrirLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun abrirSettingsActivity(id: Long) {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            putExtra("ID_CLIENTE", id)
        }
        startActivity(intent)
    }

    private fun abrirComprasActivity(id : Long){
        val intent = Intent(this, ComprasActivity::class.java).apply {
            putExtra("ID_CLIENTE", id)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing && !isChangingConfigurations) {
            // Limpa o SharedPreferences
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
        }

        ActivityManager.removeActivity(this)
    }

    override fun onResume() {
        super.onResume()

        if (!isDataLoaded) {
            val idUsuario = obterIdCliente()
            if (idUsuario != null) {
                loginInfo = dbHelperLogin.getUsuarioLogado()
                val id = loginInfo?.idClient ?: -1L
                val emailUsuario = loginInfo?.email ?: ""

                loadDataUser(emailUsuario, id)
            } else {
                funcoes.exibirToast(
                    this@DashboardActivity,
                    R.string.login_failed,
                    ". Favor refaça o login no sistema",
                    1
                )
                abrirLoginActivity()
                finish()
            }
        }
        isDataLoaded = false
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawer(navView)
        } else {
            super.onBackPressed()
        }
    }

    private fun mostrarDadosDB(){
        val dbHelperLogin = DbHelperLogin(this)
        dbHelperLogin.logarConteudoTabela()
    }
}