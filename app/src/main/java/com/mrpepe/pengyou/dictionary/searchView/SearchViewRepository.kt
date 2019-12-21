package com.mrpepe.pengyou.dictionary.searchView

import androidx.lifecycle.LiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO

class SearchViewRepository(private val entryDao: EntryDAO) {
    var searchResults : LiveData<List<Entry>>

    init {
        searchResults = entryDao.findWords("123456", "123456z")
    }

    fun searchFor(query: String) {
        searchResults = entryDao.findWords(query, query + 'z')
    }
}