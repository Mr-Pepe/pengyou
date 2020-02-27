package com.mrpepe.pengyou.dictionary.wordView

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.text.bold
import androidx.core.text.italic
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mrpepe.pengyou.*
import kotlinx.android.synthetic.main.definition_table_row.view.*
import kotlinx.android.synthetic.main.fragment_word_definition.*
import kotlinx.android.synthetic.main.fragment_word_view.*
import kotlinx.android.synthetic.main.search_result.*
import kotlinx.android.synthetic.main.search_result.searchResultDefinitions
import kotlinx.android.synthetic.main.search_result.view.searchResultDefinitions

class WordDefinitionFragment : Fragment() {
    private lateinit var model: WordViewViewModel
    private lateinit var definitions : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragment?.let {
            model = ViewModelProvider(it).get(WordViewViewModel::class.java)
        } ?: throw Exception("Invalid Activity")


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_word_definition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.entry.observe(viewLifecycleOwner, Observer { entry ->

            val formattedDefinitions = DefinitionFormatter().formatDefinitions(entry, true, activity, view, MainApplication.chineseMode, MainApplication.pinyinMode)

            if (formattedDefinitions.isEmpty())
                definitionNotFounTextView.text = SpannableStringBuilder().italic { append(MainApplication.getContext().getString(R.string.no_definition_found)) }
            else {

                definitionTable.removeAllViews()

                formattedDefinitions.forEachIndexed { iFormattedDefinition, formattedDefinition ->

                    val row = layoutInflater.inflate(R.layout.definition_table_row, null)

                    row.definitionIndex.text = SpannableStringBuilder().bold { append("${iFormattedDefinition + 1}  ") }

                    row.definitionText.text = formattedDefinition
                    row.definitionText.movementMethod = LinkMovementMethod.getInstance()

                    definitionTable.addView(row)
                }

            }
        })
    }
}
