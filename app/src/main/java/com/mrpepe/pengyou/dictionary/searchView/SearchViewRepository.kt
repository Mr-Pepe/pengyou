package com.mrpepe.pengyou.dictionary.searchView

import androidx.lifecycle.LiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO

class SearchViewRepository(private val entryDao: EntryDAO) {
    var searchResults : LiveData<List<Entry>>

    init {
        searchResults = entryDao.searchInDictByChinese("123456", "123456z")
    }

    fun searchFor(query: String, mode: SearchMode) {
        searchResults = when (mode) {
            SearchMode.CHINESEDICT -> entryDao.searchInDictByChinese(query, query + 'z')
            SearchMode.ENGLISHDICT -> entryDao.searchInDictByEnglish("%${query}%")
        }
    }

    enum class SearchMode {
        CHINESEDICT,
        ENGLISHDICT
    }
}
