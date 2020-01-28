package com.mrpepe.pengyou.dictionary.wordView

import android.app.Application
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.bold
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.mrpepe.pengyou.ChineseMode
import com.mrpepe.pengyou.DefinitionFormatter
import com.mrpepe.pengyou.MainApplication

import com.mrpepe.pengyou.R
import kotlinx.android.synthetic.main.activity_word_view.*
import kotlinx.android.synthetic.main.search_result.view.*
import kotlinx.android.synthetic.main.fragment_word_definition.*
import java.lang.Exception

class WordDefinitionFragment : Fragment() {
    private lateinit var model: WordViewFragmentViewModel
    private lateinit var definitions : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.let {
            model = ViewModelProviders.of(it).get(WordViewFragmentViewModel::class.java)
        } ?: throw Exception("Invalid Activity")


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_word_definition, container, false)

        definitions = root.definitions

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.entry.observe(this, Observer { entry ->
            definitions.movementMethod = LinkMovementMethod.getInstance()

            val formattedDefinitions = DefinitionFormatter().formatDefinitions(entry, true, activity, ChineseMode.BOTH)

            if (formattedDefinitions.isEmpty())
                definitions.text = MainApplication.getContext().getString(R.string.no_definition_found)
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

                definitions.text = text
            }
        })
    }
}
