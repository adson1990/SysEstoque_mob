package com.example.sysestoque.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sysestoque.R
import com.example.sysestoque.backend.ItensCompra

class ItemCompraAdapter(private val itens: List<ItensCompra>) :
    RecyclerView.Adapter<ItemCompraAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomePrd: TextView = itemView.findViewById(R.id.tvNomePrd)
        val tvDescPrd: TextView = itemView.findViewById(R.id.tvDescPrd)
        val tvValorProduto: TextView = itemView.findViewById(R.id.tvValorProduto)
        val tvQuantidade: TextView = itemView.findViewById(R.id.tvQuantidade)
        val tvCategoriaPrd: TextView = itemView.findViewById(R.id.tvCategoriaPrd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compra, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itens[position]
        holder.tvNomePrd.text = item.nomeProduto
        holder.tvDescPrd.text = item.descricao
        holder.tvValorProduto.text = "R$ %.2f".format(item.prcUnitario)
        holder.tvQuantidade.text = "Qtd: ${item.quantidade}"
        holder.tvCategoriaPrd.text = "Categoria: ${item.categorias}"
    }

    override fun getItemCount(): Int = itens.size
}
