package com.mrpepe.pengyou.dictionary.searchView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry

class SearchResultAdapter(private val clickListener: (Entry) -> Unit) :
    RecyclerView.Adapter<ResultViewHolder>() {

    private var searchResults = emptyList<Entry>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        return ResultViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.dictionary_search_result,
                    parent,
                    false
                ) as CardView
        )
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
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

