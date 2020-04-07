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
    private var headword: LayoutedTextView = itemView.findViewById(R.id.searchResultHeadword)
    private var pinyin: TextView = itemView.findViewById(R.id.searchResultPinyin)
    private var definitions: TextView = itemView.findViewById(R.id.searchResultDefinitions)
    private var hsk: TextView = itemView.findViewById(R.id.searchResultHsk)

    fun bind(entry: Entry, clickListener: (Entry) -> Unit, longClickListener: (Entry) -> Boolean) {

        setValues(entry)

        itemView.setOnClickListener { clickListener(entry) }
        itemView.setOnLongClickListener { longClickListener(entry) }
    }

    private fun setValues(entry: Entry) {
        headword.text = HeadwordFormatter().format(entry, MainApplication.chineseMode, false)

        // Put alternative headword on new line so it's not broken up in the middle
        // The custom class and listener are needed here because the layout is sometimes not
        // loaded yet, in which case lineCount returns 0
        headword.setOnLayoutListener(object: LayoutedTextView.OnLayoutListener {
            override fun onLayouted(view : TextView) {
                if (headword.lineCount > 1 &&
                    (MainApplication.chineseMode == ChineseMode.traditionalSimplified ||
                     MainApplication.chineseMode == ChineseMode.simplifiedTraditional)) {

                    headword.text = HeadwordFormatter().format(entry, MainApplication.chineseMode, true)
                }
            }
        })


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
