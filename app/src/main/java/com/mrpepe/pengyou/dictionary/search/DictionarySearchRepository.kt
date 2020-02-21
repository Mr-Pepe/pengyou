package com.mrpepe.pengyou.dictionary.search

import androidx.lifecycle.LiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO

class DictionarySearchRepository(private val entryDao: EntryDAO) {
    var englishSearchResults1 : LiveData<List<Entry>>
    var englishSearchResults2 : LiveData<List<Entry>>
    var englishSearchResults3 : LiveData<List<Entry>>
    var chineseSearchResults : LiveData<List<Entry>>
    var searchHistory = mutableListOf<Entry>()

    init {
        englishSearchResults1 = entryDao.searchInDictByChinese("123456", "123456z")
        englishSearchResults2 = entryDao.searchInDictByChinese("123456", "123456z")
        englishSearchResults3 = entryDao.searchInDictByChinese("123456", "123456z")
        chineseSearchResults = entryDao.searchInDictByChinese("123456", "123456z")
    }

    fun searchForChinese(query: String) {
        chineseSearchResults = entryDao.searchInDictByChinese(query, query + 'z')
    }

    fun searchForEnglish(query: String) {
        englishSearchResults1 = entryDao.searchInDictByEnglish1(query)
        englishSearchResults2 = entryDao.searchInDictByEnglish3("_${query}_", "_${query}", "${query}_")
        englishSearchResults3 = entryDao.searchInDictByEnglish1("%${query}%")
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