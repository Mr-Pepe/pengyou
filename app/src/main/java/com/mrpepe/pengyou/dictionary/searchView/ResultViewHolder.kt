package com.mrpepe.pengyou.dictionary.searchView

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.extractDefinitions

class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var headword: TextView = itemView.findViewById(R.id.headword)
    private var pinyin: TextView = itemView.findViewById(R.id.pinyin)
    private var definitions: TextView = itemView.findViewById(R.id.definitions)

    fun bind(entry: Entry, clickListener: (Entry) -> Unit) {
        headword.text = entry.simplified
        pinyin.text = entry.pinyin
        definitions.text = extractDefinitions(entry.definitions, false)

        itemView.setOnClickListener { clickListener(entry) }
    }
}