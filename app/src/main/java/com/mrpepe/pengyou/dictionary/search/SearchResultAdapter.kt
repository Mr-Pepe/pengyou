package com.mrpepe.pengyou.dictionary.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry

class SearchResultAdapter(private val clickListener: (Entry) -> Unit) :
    RecyclerView.Adapter<SearchResultViewHolder>() {

    private var searchResults = emptyList<Entry>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.search_result,
                    parent,
                    false
                ) as CardView
        )
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(this.searchResults[position], clickListener)
    }

    override fun getItemCount(): Int {
        return this.searchResults.size
    }

    internal fun setEntries(searchResults: List<Entry>) {
        this.searchResults = searchResults
        notifyDataSetChanged()
    }
}

