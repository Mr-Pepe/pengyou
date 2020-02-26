package com.mrpepe.pengyou.dictionary.search

import android.content.SharedPreferences
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.core.text.bold
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.*
import com.mrpepe.pengyou.dictionary.Entry

class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var headword: TextView = itemView.findViewById(R.id.headword)
    private var pinyin: TextView = itemView.findViewById(R.id.pinyin)
    private var definitions: TextView = itemView.findViewById(R.id.definitions)
    private var hsk: TextView = itemView.findViewById(R.id.hsk)

    fun bind(entry: Entry, clickListener: (Entry) -> Unit) {

        setValues(entry)

        itemView.setOnClickListener { clickListener(entry) }
    }

    fun setValues(entry: Entry) {
        headword.text = HeadwordFormatter().format(entry, MainApplication.chineseMode)
        pinyin.text = PinyinConverter().getFormattedPinyin(entry.pinyin, MainApplication.pinyinMode)

        val formattedDefinitions = DefinitionFormatter().formatDefinitions(entry, false, null, itemView, MainApplication.chineseMode, MainApplication.pinyinMode)

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
    }
}
