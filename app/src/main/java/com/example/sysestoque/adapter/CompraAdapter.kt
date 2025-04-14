package com.example.sysestoque.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sysestoque.R
import com.example.sysestoque.backend.Compras

class CompraAdapter(private val compras: List<Compras>) :
    RecyclerView.Adapter<CompraAdapter.CompraViewHolder>() {

    class CompraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomeCliente: TextView = itemView.findViewById(R.id.tvNomeCli)
        val tvDatCompra: TextView = itemView.findViewById(R.id.tvDatCompra)
        val tvValorTotal: TextView = itemView.findViewById(R.id.tvValorTotal)
        val rvItensCompra: RecyclerView = itemView.findViewById(R.id.rvItensCompra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.compra, parent, false)
        return CompraViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        val compra = compras[position]

        holder.tvNomeCliente.text = compra.nomeCliente
        holder.tvDatCompra.text = compra.dataCompra
        holder.tvValorTotal.text = "Total: R$ %.2f".format(compra.precoTotal)

        // Configurando o RecyclerView interno
        if (holder.rvItensCompra.layoutManager == null) {
            holder.rvItensCompra.layoutManager = LinearLayoutManager(holder.itemView.context)
        }
        holder.rvItensCompra.adapter = ItemCompraAdapter(compra.itens)
    }


    override fun getItemCount(): Int = compras.size
}