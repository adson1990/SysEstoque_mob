package com.example.sysestoque

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sysestoque.adapter.CompraAdapter
import com.example.sysestoque.backend.Compra
import com.example.sysestoque.databinding.ActivityComprasBinding

class ComprasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComprasBinding
    private lateinit var adapter: CompraAdapter
    private lateinit var comprasList: MutableList<Compra>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityComprasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Configurar RecyclerView
        comprasList = mutableListOf()
        adapter = CompraAdapter(comprasList)

        binding.recyclerViewCompras.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCompras.adapter = adapter

    }

}