package com.mrpepe.pengyou.dictionary.wordView

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.mrpepe.pengyou.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_word_definition.view.*
import kotlinx.android.synthetic.main.search_result.view.definitions
import kotlinx.android.synthetic.main.search_result.view.hsk

class WordDefinitionFragment : Fragment() {
    private lateinit var model: WordViewViewModel
    private lateinit var definitions : TextView
    private lateinit var hsk : TextView
    private lateinit var linearLayout: LinearLayout
    private lateinit var hskLayout: LinearLayout

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
        val root = inflater.inflate(R.layout.fragment_word_definition, container, false)

        definitions = root.definitions
        hsk = root.hsk
        linearLayout = root.linearLayout
        hskLayout = root.hskLayout

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.entry.observe(viewLifecycleOwner, Observer { entry ->
            when (entry.hsk) {
                7 -> linearLayout.removeView(hskLayout)
                else -> hsk.text = "HSK " + entry.hsk.toString()
            }

            definitions.movementMethod = LinkMovementMethod.getInstance()

            val formattedDefinitions = DefinitionFormatter().formatDefinitions(entry, true, activity, view, ChineseMode.SIMPLIFIED, MainApplication.pinyinMode)

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
