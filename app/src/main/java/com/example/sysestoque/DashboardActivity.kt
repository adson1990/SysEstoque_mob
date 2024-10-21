package com.example.sysestoque

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
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
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.RequestListener
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.ClientRepository
import com.example.sysestoque.backend.ComprasResponse
import com.example.sysestoque.backend.TokenResponse
import com.example.sysestoque.data.database.ColorDatabaseHelper
import com.example.sysestoque.data.database.DbHelperLogin
import com.example.sysestoque.data.database.LoginInfo
import com.example.sysestoque.data.utilitarios.ActivityManager
import com.example.sysestoque.data.utilitarios.Funcoes
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
lateinit var funcoes: Funcoes

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

        drawerLayout = findViewById(R.id.drawer_layout_dashboard)
        navView = findViewById(R.id.nav_view)

        dbHelper = ColorDatabaseHelper(this)
        dbHelperLogin = DbHelperLogin(this)
        funcoes = Funcoes()

        // Configura o ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Lidando com cliques no menu
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_profile -> {
                    abrirProfileActivity()
                    drawerLayout.closeDrawers()
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_settings -> {
                    // Abrir tela de Configurações
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

        var loginInfo: LoginInfo? = null
        loginInfo = dbHelperLogin.getUsuarioLogado()

        val emailUsuario = loginInfo?.email ?: ""
        val nomeUsuario = emailUsuario.substringBefore("@").uppercase()
        val tvNomeUsuario = findViewById<TextView>(R.id.tvNomeUsuario)
        tvNomeUsuario.text = nomeUsuario

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val imageUrl = loginInfo?.foto ?: ""
        val imageView = findViewById<ImageView>(R.id.ftUsuario)

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.mipmap.user_icon)
            .error(R.mipmap.user_icon)
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

        val tvProductName1 = findViewById<TextView>(R.id.tvNomeProduto1)
        val tvProductDate1 = findViewById<TextView>(R.id.tvDataCompra1)
        val tvProductPrice1 = findViewById<TextView>(R.id.tvValorTotal1)

        val tvProductName2 = findViewById<TextView>(R.id.tvNomeProduto2)
        val tvProductDate2 = findViewById<TextView>(R.id.tvDataCompra2)
        val tvProductPrice2 = findViewById<TextView>(R.id.tvValorTotal2)

        val authRepository = AuthRepository()
        val clientRepository = ClientRepository()

        fun fetchCompras(idCliente: Long, token: String) {
            clientRepository.getComprasPorIdCliente(idCliente, token, object : Callback<ComprasResponse> {
                override fun onResponse(call: Call<ComprasResponse>, response: Response<ComprasResponse>) {
                    if (response.isSuccessful) {
                        val comprasList = response.body()?.content ?: emptyList()

                        if (comprasList.isNotEmpty()) {
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                            // Preencher a primeira compra
                            if (comprasList.size > 0) {
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
        fun fetchTokenAndCompras(idCliente: Long, username: String) {
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

        val idCliente = loginInfo?.idClient ?: 0
        val username = loginInfo?.email ?: ""

        fetchTokenAndCompras(idCliente, username)

        mostrarDadosDB()

        val colors = dbHelper.getColors(idCliente)
        if(colors != null) {
            colors?.let { (red, green, blue) ->
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


        // fim do onCreate
    }

    private fun abrirProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawer(navView)
        } else {
            super.onBackPressed()
        }
    }

    fun mostrarDadosDB(){
        val dbHelperLogin = DbHelperLogin(this)
        dbHelperLogin.logarConteudoTabela()
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityManager.removeActivity(this)
    }
}