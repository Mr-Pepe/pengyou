package com.mrpepe.pengyou.dictionary.searchView

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.HeadwordFormatter
import com.mrpepe.pengyou.PinyinConverter
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.extractDefinitions

class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var headword: TextView = itemView.findViewById(R.id.headword)
    private var pinyin: TextView = itemView.findViewById(R.id.pinyin)
    private var definitions: TextView = itemView.findViewById(R.id.definitions)
    private var hsk: TextView = itemView.findViewById(R.id.hsk)

    fun bind(entry: Entry, clickListener: (Entry) -> Unit) {
        headword.text = HeadwordFormatter().format(entry, HeadwordFormatter.FormatMode.BOTH)
        pinyin.text = PinyinConverter().getFormattedPinyin(entry.pinyin, PinyinConverter.PinyinMode.MARKS)
        definitions.text = extractDefinitions(entry, asList = false, linkWords = false, context = null)

        hsk.text = when (entry.hsk) {
            7 -> ""
            else -> "HSK " + entry.hsk.toString()
        }

        itemView.setOnClickListener { clickListener(entry) }
    }
}
