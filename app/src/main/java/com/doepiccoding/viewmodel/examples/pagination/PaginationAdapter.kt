package com.doepiccoding.viewmodel.examples.pagination

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.doepiccoding.viewmodel.R

class PaginationAdapter : RecyclerView.Adapter<PaginationAdapter.ItemViewHolder>() {

    private val itemsInPage = mutableListOf<String>()

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemsInPage[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item, parent, false))
    }

    override fun getItemCount(): Int {
        return itemsInPage.size
    }

    fun removeAllItems() {
        val size = itemsInPage.size
        itemsInPage.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun addAllItems(words: List<String>) {
        if (words.isEmpty())return

        val startIndex = this.itemsInPage.size
        this.itemsInPage.addAll(words)
        notifyItemRangeChanged(startIndex, words.size)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(word: String) {
            (itemView as TextView).text = word
        }

    }
}