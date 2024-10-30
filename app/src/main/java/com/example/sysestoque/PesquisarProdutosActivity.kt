package com.example.sysestoque

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sysestoque.databinding.ActivityPesquisarProdutosBinding

class PesquisarProdutosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPesquisarProdutosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPesquisarProdutosBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

}