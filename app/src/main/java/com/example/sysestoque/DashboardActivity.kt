package com.example.sysestoque

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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
import com.example.sysestoque.backend.ClientRepository
import com.example.sysestoque.backend.ComprasResponse
import com.example.sysestoque.backend.TokenResponse
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
import java.text.SimpleDateFormat
import java.util.Locale

private lateinit var drawerLayout: DrawerLayout
private lateinit var navView: NavigationView
private lateinit var dbHelperLogin: DbHelperLogin
private lateinit var dbHelper: ColorDatabaseHelper
private lateinit var dbConfig: DbHelperConfig
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

private var loginInfo: LoginInfo? = null
private val clientRepository = ClientRepository()
private val authRepository = AuthRepository()

private var linearLayoutCompras: LinearLayout = TODO()
private var idClient: Long = -1L
private var nameUser: String = ""


class DashboardActivity : AppCompatActivity() {
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
        val (id, nome) = obterDadosUsuario()
        idClient = id
        nameUser = nome

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
        loadDataUser(nameUser, idClient)

        // fim do onCreate
    }

    private fun loadDataUser(nomeUsuario: String, id: Long){
        tvNomeUsuario.text = nomeUsuario
        carregaFotoUser(id)

        val config = dbConfig.getConfiguracoes(id)

            if(config != null && config.ultimasCompras){
                fetchTokenAndCompras(id, nomeUsuario)
            }else if(!config!!.ultimasCompras) {
                linearLayoutCompras.visibility = View.GONE
            }


        mostrarDadosDB()

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

    private fun carregaFotoUser(id: Long){
       // val urlComTimestamp = "$imageUrl?timestamp=${System.currentTimeMillis()}"

        val imageUrl = dbHelperLogin.getFotoUsuario(id)

        Glide.with(this)
            .load(imageUrl)
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
    }

    fun fetchCompras(idCliente: Long, token: String) {
        clientRepository.getComprasPorIdCliente(idCliente, token, object : Callback<ComprasResponse> {
            override fun onResponse(call: Call<ComprasResponse>, response: Response<ComprasResponse>) {
                if (response.isSuccessful) {
                    val comprasList = response.body()?.content ?: emptyList()

                    if (comprasList.isNotEmpty()) {
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
                } else {
                    funcoes.exibirToast(this@DashboardActivity, R.string.erro_conexao_db, response.message(), 1)
                }
            }

            override fun onFailure(call: Call<ComprasResponse>, t: Throwable) {
                funcoes.exibirToast(this@DashboardActivity, R.string.erro_conexao_db, t.message.toString(), 1)
            }
        })
    }

    // Função para buscar o token e depois as compras
    private fun fetchTokenAndCompras(idCliente: Long, username: String) {
        authRepository.getTokenByEmail(username, object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.accessToken ?: ""
                    // Agora com o token, realizar busca pelas compras
                    fetchCompras(idCliente, token)
                } else {
                    funcoes.exibirToast(this@DashboardActivity, R.string.erro_buscar_token, response.message(),0)
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                funcoes.exibirToast(this@DashboardActivity, R.string.erro_buscar_token, t.message.toString(),0)
            }
        })
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

    private fun obterDadosUsuario(): Pair<Long, String> {
        val idUsuario = intent.getLongExtra("ID_CLIENTE", -1L)
        val nomeUsuario = intent.getStringExtra("NOME_CLIENTE")

        return if (idUsuario != -1L && nomeUsuario != null) {
            Pair(idUsuario, nomeUsuario)
        } else (if(idUsuario != -1L) {
            buscarDadosDoSQLite(idUsuario)
        } else {
            funcoes.exibirToast(this@DashboardActivity, R.string.login_failed,"",1)
            abrirLoginActivity()
            finish()
            null
        })!!
    }

    private fun buscarDadosDoSQLite(idUsuario: Long): Pair<Long, String> {
        loginInfo = dbHelperLogin.getUsuarioLogado(idUsuario)
        val id = loginInfo?.idClient ?: -1L
        val emailUsuario = loginInfo?.email ?: ""
        val nomeUsuario = emailUsuario.substringBefore("@").uppercase()

        return Pair(id, nomeUsuario)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityManager.removeActivity(this)
    }

    override fun onResume() {
        super.onResume()

        val idUsuario = obterIdCliente()

        if (idUsuario != null) {
            loginInfo = dbHelperLogin.getUsuarioLogado(idUsuario)
            val id = loginInfo?.idClient ?: -1L
            val emailUsuario = loginInfo?.email ?: ""
            val nomeUsuario = emailUsuario.substringBefore("@").uppercase()

            loadDataUser(nomeUsuario, id)
        }else {
            funcoes.exibirToast(this@DashboardActivity,R.string.login_failed,". Favor refaça o login no sistema", 1)
            abrirLoginActivity()
            finish()
        }
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
}