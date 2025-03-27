package com.example.sysestoque

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sysestoque.databinding.ActivityComprasBinding

class ComprasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComprasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityComprasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

    }

}