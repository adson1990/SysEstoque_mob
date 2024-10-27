package com.example.sysestoque

import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.sysestoque.data.database.DbHelperConfig
import com.example.sysestoque.data.database.DbHelperLogin
import com.example.sysestoque.data.utilitarios.ActivityManager
import com.example.sysestoque.data.utilitarios.Funcoes
import com.example.sysestoque.databinding.ActivitySettingsBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var dbHelperLogin: DbHelperLogin
    private lateinit var dbHelperCongif: DbHelperConfig
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rgOrderCompras: RadioGroup
    private lateinit var rgOrderItens: RadioGroup
    private lateinit var switchCompras: SwitchMaterial
    private lateinit var switchLogin: SwitchMaterial
    private lateinit var funcoes: Funcoes

    private var idCliente: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityManager.addActivity(this)

        val toolbar = binding.toolbarConfig
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout_config)
        navigationView = findViewById(R.id.navigation_view_config)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        switchLogin = binding.switchLogin
        switchCompras = binding.switchCompras
        val rbNome = binding.rbOrdemNome
        val rbPreco = binding.rbOrdemPreco
        val rbData = binding.rbOrdemData
        val rbNameSearch = binding.rbOrderNameSearch
        val rbRating = binding.rbOrderRating
        val rbPrecoSearch = binding.rbOrderPrecoSearch
        val btSalvar = binding.btSalvar
        rgOrderCompras = binding.rgOrdemCompras
        rgOrderItens = binding.rgOrdemItens

        //persistência de dados
        dbHelperLogin = DbHelperLogin(this)
        dbHelperCongif = DbHelperConfig(this)

        funcoes = Funcoes()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    funcoes.exibirToast(this@SettingsActivity, R.string.descartar, "", 0)
                    finish()
                }

                R.id.nav_profile -> {
                    abrirProfileActivity()
                    finish()
                }

                R.id.nav_settings -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }

                R.id.nav_exit -> {
                    funcoes.logout(this@SettingsActivity)
                    drawerLayout.closeDrawers() // Fecha o drawer após a ação
                    return@setNavigationItemSelectedListener true
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        val clientLogger = dbHelperLogin.checarLoginAutomatico()
        val remember = clientLogger?.remember ?: false
        switchLogin.isChecked = remember
        /*switchLogin.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dbHelperLogin.lembrarCliente(true)
            } else {
                dbHelperLogin.lembrarCliente(false)
            }
        }*/

        idCliente = clientLogger?.idClient!!
        val configData = dbHelperCongif.getConfiguracoes(idCliente)
        val compras = configData?.ultimasCompras ?: true
        switchCompras.isChecked = compras

        val ordemCompras: String = configData?.ordemCompras ?: "Data"
        when (ordemCompras) {
            "Nome" -> rbNome.isChecked = true
            "Data" -> rbData.isChecked = true
            "Preco" -> rbPreco.isChecked = true
            else -> rbData.isChecked = true
        }

        val ordemPesquisa: String = configData?.ordemPesquisa ?: "Nome"
        when (ordemPesquisa) {
            "Nome" -> rbNameSearch.isChecked = true
            "Rating" -> rbRating.isChecked = true
            "Preco" -> rbPrecoSearch.isChecked = true
            else -> rbNameSearch.isChecked = true
        }

        btSalvar.setOnClickListener{
            salvarConfigs()
        }

        // fim do onCreate
    }

    fun salvarConfigs(){
        val lastPurchase = switchCompras.isChecked
        val orderPurchase = when (rgOrderCompras.checkedRadioButtonId) {
            R.id.rbOrdemNome -> "Nome"
            R.id.rbOrdemPreco -> "Preco"
            R.id.rbOrdemData -> "Data"
            else -> "Data"
        }
        val orderSearch = when (rgOrderItens.checkedRadioButtonId) {
            R.id.rbOrderNameSearch -> "Nome"
            R.id.rbOrderRating -> "Rating"
            R.id.rbOrderPrecoSearch -> "Preco"
            else -> "Nome"
        }

        val remember = switchLogin.isChecked

            dbHelperCongif.salvarConfiguracoes(idCliente, lastPurchase, orderPurchase, orderSearch)
            dbHelperLogin.lembrarCliente(remember)
        funcoes.exibirToast(this@SettingsActivity,R.string.salvar_dados_ok, "", 0)
        finish()
    }

    private fun abrirProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_settings)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}