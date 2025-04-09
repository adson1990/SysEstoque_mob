package com.example.sysestoque.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sysestoque.R
import com.example.sysestoque.backend.Compra

class CompraAdapter(private val compras: List<Compra>) :
    RecyclerView.Adapter<CompraAdapter.CompraViewHolder>() {

    class CompraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCodPrd: TextView = itemView.findViewById(R.id.txtCodPrd)
        val txtNomePrd: TextView = itemView.findViewById(R.id.txtNomePrd)
        val txtCodCli: TextView = itemView.findViewById(R.id.txtCodCli)
        val txtQtd: TextView = itemView.findViewById(R.id.txtQtd)
        val txtDataVenda: TextView = itemView.findViewById(R.id.txtDataCompra)
        val txtTotal: TextView = itemView.findViewById(R.id.txtTotal)
        val txtPrcUnitario: TextView = itemView.findViewById(R.id.txtPrcUnitario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compra, parent, false)
        return CompraViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        val compra = compras[position]
        holder.txtCodPrd.text = compra.codPrd.toString()
        holder.txtNomePrd.text = compra.nomePrd
        holder.txtCodCli.text = compra.codCli.toString()
        holder.txtQtd.text = compra.qtd.toString()
        holder.txtDataVenda.text = compra.dataVenda
        holder.txtTotal.text = holder.itemView.context.getString(R.string.sifrao) + "${compra.total}"
        holder.txtPrcUnitario.text = holder.itemView.context.getString(R.string.sifrao) + "${compra.prcUnitario}"
    }

    override fun getItemCount(): Int = compras.size
}