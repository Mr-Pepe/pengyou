package com.mrpepe.pengyou.dictionary.search

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.italic
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.*
import com.mrpepe.pengyou.dictionary.Entry

class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var headword: TextView = itemView.findViewById(R.id.searchResultHeadword)
    private var pinyin: TextView = itemView.findViewById(R.id.searchResultPinyin)
    private var definitions: TextView = itemView.findViewById(R.id.searchResultDefinitions)
    private var hsk: TextView = itemView.findViewById(R.id.searchResultHsk)

    fun bind(entry: Entry, clickListener: (Entry) -> Unit) {

        setValues(entry)

        itemView.setOnClickListener { clickListener(entry) }
    }

    private fun setValues(entry: Entry) {
        headword.text = HeadwordFormatter().format(entry, MainApplication.chineseMode)
        pinyin.text = PinyinConverter().getFormattedPinyin(entry.pinyin, MainApplication.pinyinMode)

        val formattedDefinitions = DefinitionFormatter().formatDefinitions(entry, false, null, itemView, MainApplication.chineseMode, MainApplication.pinyinMode)

        if (formattedDefinitions.isEmpty())
            definitions.text = SpannableStringBuilder().italic { append(MainApplication.getContext().getString(R.string.no_definition_found)) }
        else {
            val text = SpannableStringBuilder()

            formattedDefinitions.forEachIndexed { iDefinition, definition ->
                text.bold { append("${iDefinition + 1} ") }
                text.append("$definition ")
            }

            definitions.text = text
        }

        hsk.text = when (entry.hsk) {
            7 -> {
                hsk.setBackgroundResource(0)
                "         "
            }
            else -> {
                hsk.setBackgroundResource(R.drawable.background_hsk_tag)
                "HSK " + entry.hsk.toString()
            }
        }
    }
}
