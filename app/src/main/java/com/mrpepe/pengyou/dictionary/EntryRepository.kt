package com.mrpepe.pengyou.dictionary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class EntryRepository(private val entryDao: EntryDAO) {
    var searchResults : LiveData<List<Entry>>

    init {
        searchResults = entryDao.findWords("123456", "123456z")
    }

    fun searchFor(query: String) {
        searchResults = entryDao.findWords(query, query + 'z')
    }
}