package com.mrpepe.pengyou.dictionary.wordView

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mrpepe.pengyou.*
import kotlinx.android.synthetic.main.fragment_word_view.*
import kotlinx.android.synthetic.main.search_result.*
import kotlinx.android.synthetic.main.search_result.view.searchResultDefinitions

class WordDefinitionFragment : Fragment() {
    private lateinit var model: WordViewViewModel
    private lateinit var definitions : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
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

            searchResultDefinitions.movementMethod = LinkMovementMethod.getInstance()

            val formattedDefinitions = DefinitionFormatter().formatDefinitions(entry, true, activity, view, MainApplication.chineseMode, MainApplication.pinyinMode)

            if (formattedDefinitions.isEmpty())
                searchResultDefinitions.text = MainApplication.getContext().getString(R.string.no_definition_found)
            else {
                val text = SpannableStringBuilder()

                formattedDefinitions.forEachIndexed { iDefinition, definition ->

                    text.bold { append("${iDefinition + 1}") }
                    when (iDefinition < 9) {
                        true -> text.append("    \t")
                        false -> text.append("  \t")
                    }
                    text.append(definition)
                    text.append("\n")
                }

                searchResultDefinitions.text = text
            }
        })
    }
}
