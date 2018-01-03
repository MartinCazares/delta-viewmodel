package com.doepiccoding.viewmodel.examples.collection

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.doepiccoding.viewmodel.R

class WordsAdapter: RecyclerView.Adapter<WordsAdapter.ItemViewHolder>() {

    private val words = mutableListOf<String>()

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(words[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item, parent, false))
    }

    override fun getItemCount(): Int {
        return words.size
    }

    fun addAllWords(words: List<String>) {
        if (words.isEmpty())return

        val startIndex = this.words.size
        this.words.addAll(words)
        notifyItemRangeChanged(startIndex, words.size)
    }

    fun addWord(word: String, position: Int) {
        val putLast = position >= words.size
        if (putLast) {
            words.add(word)
        } else {
            words.add(position, word)
        }
        notifyItemInserted(if (putLast) words.size - 1 else position)
    }

    fun removeWord(position: Int) {
        words.removeAt(position)
        notifyItemRemoved(position)
    }

    class ItemViewHolder(itemView: View) : ViewHolder(itemView) {

        fun bind(word: String) {
            (itemView as TextView).text = word
        }

    }
}