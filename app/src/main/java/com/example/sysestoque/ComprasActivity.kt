package com.example.sysestoque

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sysestoque.adapter.CompraAdapter
import com.example.sysestoque.backend.ClientRepository
import com.example.sysestoque.backend.Compras
import com.example.sysestoque.databinding.ActivityComprasBinding
import kotlinx.coroutines.launch
import com.example.sysestoque.data.utilitarios.Funcoes

class ComprasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComprasBinding
    private lateinit var adapter: CompraAdapter
    private lateinit var comprasList: MutableList<Compras>
    private lateinit var funcoes: Funcoes

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityComprasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // omitir o título do projeto na toolbar da activity

        // Configurar RecyclerView
        comprasList = mutableListOf()
        adapter = CompraAdapter(comprasList)

        binding.recyclerViewCompras.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCompras.adapter = adapter

        funcoes = Funcoes()
        val repository = ClientRepository(this)

        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val clientId = sharedPrefs.getLong("ID_CLIENTE", -1L)

        if (clientId != -1L) {
            lifecycleScope.launch {
                val compras = repository.getComprasPorIdCliente(clientId)
                if (compras != null) {
                    comprasList.clear()
                    comprasList.addAll(compras)
                    adapter.notifyDataSetChanged()
                } else {
                    funcoes.exibirToast(this@ComprasActivity, R.string.erro_compras, "", 0)
                }
            }
        } else {
            funcoes.exibirToast(this@ComprasActivity, R.string.id_invalido, "", 0)
        }


        // fim onCreate
    }

}