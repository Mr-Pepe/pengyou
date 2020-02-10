package com.mrpepe.pengyou.dictionary.searchView

import androidx.lifecycle.LiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO

class SearchViewRepository(private val entryDao: EntryDAO) {
    var englishSearchResults : LiveData<List<Entry>>
    var chineseSearchResults : LiveData<List<Entry>>
    var searchHistory = mutableListOf<Entry>()

    init {
        englishSearchResults = entryDao.searchInDictByChinese("123456", "123456z")
        chineseSearchResults = entryDao.searchInDictByChinese("123456", "123456z")
    }

    fun searchFor(query: String) {
        englishSearchResults = entryDao.searchInDictByEnglish("%${query}%")
        chineseSearchResults = entryDao.searchInDictByChinese(query, query + 'z')
    }

    suspend fun getSearchHistory(ids: List<String>) {
        searchHistory.clear()

        if (ids.isNotEmpty()) {
            ids.forEach { id ->
                if (id != "")
                    searchHistory.add(entryDao.getEntryByIdAsync(id.toInt()))
            }
        }
        else {
            searchHistory.clear()
        }
    }
}
