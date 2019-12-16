package com.mrpepe.pengyou

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

import kotlinx.android.synthetic.main.activity_dictionary.*
import kotlinx.android.synthetic.main.activity_dictionary.view.*
import kotlinx.android.synthetic.main.content_dictionary.*

class DictionaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)
        setSupportActionBar(toolbar)

        val db = Room.databaseBuilder(applicationContext, CEDict::class.java, "cedict")
            .allowMainThreadQueries()
            .createFromAsset("cedict.db")
            .build()

        val entryDao = db.entryDao()

        val viewManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        dictionaryResultList.apply{
            layoutManager = viewManager
            setHasFixedSize(true)
        }

        toolbar.dictionary_search_view.setOnQueryTextListener(object  : android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {

                if (newText!!.isNotBlank()) {
                    val query = newText!!
                    val searchResults = entryDao.findWords(query, query+'z')
                    dictionaryResultList.adapter = SearchResultAdapter(searchResults)
                } else {
                    dictionaryResultList.adapter = SearchResultAdapter(listOf<Entry>())
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                dictionary_search_view.clearFocus()
                return true
            }
        })
    }

}

class SearchResultAdapter(private val searchResults: List<Entry>) :
    RecyclerView.Adapter<ResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        return ResultViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.dictionary_search_result, parent, false) as CardView
        )
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.headword.text = searchResults[position].simplified
        holder.pinyin.text = searchResults[position].pinyin

        var definitions = searchResults[position].definitions.split('/')

        var text : String = ""
        var iDefinition = 1

        definitions.forEach {
            if (iDefinition == 1)
                text += "1. " + it
            else
                text += "\n$iDefinition. " + it

            iDefinition++
        }

        holder.definitions.text = text

    }

    override fun getItemCount(): Int {
        return searchResults.size
    }
}

class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    internal var headword: TextView = itemView.findViewById(R.id.headword)
    internal var pinyin: TextView = itemView.findViewById(R.id.pinyin)
    internal var definitions: TextView = itemView.findViewById(R.id.definitions)
}
