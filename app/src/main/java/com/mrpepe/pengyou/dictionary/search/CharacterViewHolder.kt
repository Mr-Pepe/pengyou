package com.mrpepe.pengyou.dictionary.search

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mrpepe.pengyou.*

class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var character: TextView = itemView.findViewById(R.id.proposedCharacter)

    fun bind(input: String, clickListener: (String) -> Unit) {
        character.text = input

        itemView.setOnClickListener { clickListener(input) }
    }
}
