package com.mrpepe.pengyou.dictionary.searchView

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SearchViewRepository(private val entryDao: EntryDAO) {
    var searchResults : LiveData<List<Entry>>
    var searchHistory = mutableListOf<Entry>()

    init {
        searchResults = entryDao.searchInDictByChinese("123456", "123456z")
    }

    fun searchFor(query: String, mode: SearchMode) {
        searchResults = when (mode) {
            SearchMode.CHINESEDICT -> entryDao.searchInDictByChinese(query, query + 'z')
            SearchMode.ENGLISHDICT -> entryDao.searchInDictByEnglish("%${query}%")
        }
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

    enum class SearchMode {
        CHINESEDICT,
        ENGLISHDICT
    }
}
