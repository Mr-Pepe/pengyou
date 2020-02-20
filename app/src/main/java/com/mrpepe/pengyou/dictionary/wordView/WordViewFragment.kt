package com.mrpepe.pengyou.dictionary.wordView

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.android.synthetic.main.fragment_word_view.*

private const val ARG_ENTRY = "entry"

class WordViewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var entry: Entry

    private var listener: WordViewFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            entry = it.get(ARG_ENTRY) as Entry
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_word_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wordViewHeadword.text = entry.simplified
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WordViewFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }




    // TODO: Rename method, update argument and hook method into UI event
//    fun onButtonPressed(uri: Uri) {
//        listener?.onFragmentInteraction(uri)
//    }

    interface WordViewFragmentInteractionListener {
        // TODO: Update argument type and name
//        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(entry: Entry) =
            WordViewFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ENTRY, entry)
                }
            }
    }
}
