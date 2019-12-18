package com.mrpepe.pengyou.dictionary.ui.wordView


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders

import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.extractDefinitions
import kotlinx.android.synthetic.main.fragment_word_definition.*
import java.lang.Exception

class WordDefinitionFragment : Fragment() {

    private lateinit var model: WordViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = activity?.run {
            ViewModelProviders.of(this)[WordViewViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_word_definition, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        definitions.text = extractDefinitions(model.entry.definitions, true)
    }


}
