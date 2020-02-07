package com.mrpepe.pengyou.dictionary.searchView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.Entry

class ProposedCharacterAdapter(private val clickListener: (String) -> Unit) :
    RecyclerView.Adapter<CharacterViewHolder>() {

    private var proposedCharacters = emptyList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        return CharacterViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.proposed_character,
                    parent,
                    false
                ) as CardView
        )
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(this.proposedCharacters[position], clickListener)
    }

    override fun getItemCount(): Int {
        return this.proposedCharacters.size
    }

    internal fun setProposedCharacters(proposedCharacters: List<String>) {
        this.proposedCharacters = proposedCharacters
        notifyDataSetChanged()
    }
}

