package com.example.sysestoque

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.sysestoque.backend.AuthRepository
import com.example.sysestoque.backend.ComprasResponse
import com.example.sysestoque.backend.TokenResponse
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

private lateinit var drawerLayout: DrawerLayout
private lateinit var navView: NavigationView

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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

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
                    // Abrir tela de Home
                }
                R.id.nav_profile -> {
                    // Abrir tela de Perfil
                }
                R.id.nav_settings -> {
                    // Abrir tela de Configurações
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        //referencias das views

        val tvProductName1 = findViewById<TextView>(R.id.tvNomeProduto1)
        val tvProductDate1 = findViewById<TextView>(R.id.tvDataCompra1)
        val tvProductPrice1 = findViewById<TextView>(R.id.tvValorTotal1)

        val authRepository = AuthRepository()

        fun fetchCompras(idCliente: Long, token: String) {
            authRepository.getComprasPorIdCliente(idCliente, token, object : Callback<ComprasResponse> {
                override fun onResponse(call: Call<ComprasResponse>, response: Response<ComprasResponse>) {
                    if (response.isSuccessful) {
                        val comprasList = response.body()?.content ?: emptyList()

                        if (comprasList.isNotEmpty()) {
                            val compra = comprasList[0]

                            // Atualiza os EditTexts com os dados recebidos
                            tvProductName1.setText(compra.name)
                            tvProductPrice1.setText(compra.valor.toString())
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val formattedDate = formatter.format(compra.dataVenda)
                            tvProductDate1?.text = formattedDate

                        }
                    }
                }

                override fun onFailure(call: Call<ComprasResponse>, t: Throwable) {
                    // Trate o erro (ex.: exibir um Toast com mensagem de erro)
                    Toast.makeText(this@DashboardActivity, "Erro ao buscar compras: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // Função para buscar o token e depois as compras
        fun fetchTokenAndCompras(idCliente: Long, username: String) {
            authRepository.getToken(username, object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.isSuccessful) {
                        val token = response.body()?.accessToken ?: ""
                        // Agora que temos o token, podemos buscar as compras
                        fetchCompras(idCliente, token)
                    } else {
                        Toast.makeText(this@DashboardActivity, "Erro ao obter token", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(this@DashboardActivity, "Erro ao buscar token: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        val idCliente = 12L
        val username = "ADMIN"

        fetchTokenAndCompras(idCliente, username)

    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navView)) {
            drawerLayout.closeDrawer(navView)
        } else {
            super.onBackPressed()
        }
    }
}