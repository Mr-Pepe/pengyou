package com.mrpepe.pengyou.dictionary.searchView

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.*
import com.mrpepe.pengyou.dictionary.Entry

class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var headword: TextView = itemView.findViewById(R.id.headword)
    private var pinyin: TextView = itemView.findViewById(R.id.pinyin)
    private var definitions: TextView = itemView.findViewById(R.id.definitions)
    private var hsk: TextView = itemView.findViewById(R.id.hsk)

    fun bind(entry: Entry, clickListener: (Entry) -> Unit) {
        headword.text = HeadwordFormatter().format(entry, ChineseMode.SIMPLIFIED)
        pinyin.text = PinyinConverter().getFormattedPinyin(entry.pinyin, PinyinMode.MARKS)

        // TODO: Handle empty definition list
        val formattedDefinitions = DefinitionFormatter().formatDefinitions(entry, false, null, ChineseMode.SIMPLIFIED)

        if (formattedDefinitions.isEmpty())
            definitions.text = MainApplication.getContext().getString(R.string.no_definition_found)
        else {
            val text = SpannableStringBuilder()

            formattedDefinitions.forEachIndexed { iDefinition, definition ->
                text.bold { append("${iDefinition + 1} ") }
                text.append("$definition ")
            }

            definitions.text = text
        }

        hsk.text = when (entry.hsk) {
            7 ->    "         "
            else -> "HSK " + entry.hsk.toString()
        }

        itemView.setOnClickListener { clickListener(entry) }
    }
}
