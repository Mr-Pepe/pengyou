package com.mrpepe.pengyou.dictionary.wordView

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mrpepe.pengyou.R
import kotlinx.android.synthetic.main.fragment_decomposition.view.*
import java.lang.Exception

class DecompositionFragment : Fragment() {
    private lateinit var model: WordViewFragmentViewModel
    private lateinit var decompositions : TextView

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
        val root = inflater.inflate(R.layout.fragment_decomposition, container, false)

        decompositions = root.decompositions

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.decompositions.observe(this, Observer {decomp ->
            var components = decomp[0].components
            var text = SpannableStringBuilder()
            var iComponent = 1

            components.forEach {
                when (iComponent) {
                    1 -> text.bold{ append("1") }.append(" ${it.character} ")

                    else -> {
                        text.append("\n")

                        text.bold { append("$iComponent") }.append(" ${it.character} ")
                    }
                }

                iComponent++
            }

            decompositions.text = text
        })

    }
}
