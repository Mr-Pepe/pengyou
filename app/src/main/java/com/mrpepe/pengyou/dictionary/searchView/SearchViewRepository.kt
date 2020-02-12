package com.mrpepe.pengyou.dictionary.searchView

import androidx.lifecycle.LiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO

class SearchViewRepository(private val entryDao: EntryDAO) {
    var englishSearchResults1 : LiveData<List<Entry>>
    var englishSearchResults2 : LiveData<List<Entry>>
    var englishSearchResults3 : LiveData<List<Entry>>
    var englishSearchResults4 : LiveData<List<Entry>>
    var englishSearchResults5 : LiveData<List<Entry>>
    var chineseSearchResults : LiveData<List<Entry>>
    var searchHistory = mutableListOf<Entry>()

    init {
        englishSearchResults1 = entryDao.searchInDictByChinese("123456", "123456z")
        englishSearchResults2 = entryDao.searchInDictByChinese("123456", "123456z")
        englishSearchResults3 = entryDao.searchInDictByChinese("123456", "123456z")
        englishSearchResults4 = entryDao.searchInDictByChinese("123456", "123456z")
        englishSearchResults5 = entryDao.searchInDictByChinese("123456", "123456z")
        chineseSearchResults = entryDao.searchInDictByChinese("123456", "123456z")
    }

    fun searchFor(query: String) {
        englishSearchResults1 = entryDao.searchInDictByEnglish("%/${query}/%")
        englishSearchResults2 = entryDao.searchInDictByEnglish("%${query} %")
        englishSearchResults3 = entryDao.searchInDictByEnglish("% ${query}%")
        englishSearchResults4 = entryDao.searchInDictByEnglish("% ${query} %")
        englishSearchResults5 = entryDao.searchInDictByEnglish("%${query}%")
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
