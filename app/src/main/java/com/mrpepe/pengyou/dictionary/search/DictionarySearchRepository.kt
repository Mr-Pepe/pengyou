package com.mrpepe.pengyou.dictionary.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO

class DictionarySearchRepository(private val entryDao: EntryDAO) {
    var englishSearchResults1 : LiveData<List<Entry>>
    var englishSearchResults2 : LiveData<List<Entry>>
    var englishSearchResults3 : LiveData<List<Entry>>
    private val _chineseSearchResults = MediatorLiveData<List<Entry>>()
    var chineseSearchResults: LiveData<List<Entry>> = _chineseSearchResults
    private val chineseSearchResultSources = mutableListOf<LiveData<List<Entry>>>()
    val chineseQueryCompleted = MutableLiveData<Boolean>()
    var searchHistory = mutableListOf<Entry>()

    init {
        englishSearchResults1 = entryDao.searchInDictByChinese("123456", "123456z")
        englishSearchResults2 = entryDao.searchInDictByChinese("123456", "123456z")
        englishSearchResults3 = entryDao.searchInDictByChinese("123456", "123456z")
        val chineseSearchResult = entryDao.searchInDictByChinese("123456", "123456z")
        chineseSearchResultSources.add(chineseSearchResult)
        _chineseSearchResults.addSource(chineseSearchResult) { _chineseSearchResults.postValue(chineseSearchResults.value?.plus(it))}
    }

    suspend fun searchForChinese(rawQuery: String) {
        var queries = mutableListOf("")

        var iCharacter = 0
        var character = ""

        // Replace character with simplified versions if available
        while (iCharacter < rawQuery.length) {
            if (rawQuery.hasSurrogatePairAt(iCharacter)) {
                character += rawQuery[iCharacter]
                character += rawQuery[iCharacter+1]
                iCharacter += 2
            }
            else {
                character = rawQuery[iCharacter].toString()
                iCharacter += 1
            }

            val conversionsList = entryDao.getTraditionalToSimplifiedCharacters(character)
            var conversions = listOf<String>()

            if (conversionsList.isNotEmpty()) {
                conversions = conversionsList[0].split(',')
            }

            if (conversions.isNotEmpty()) {
                val tmpQueries = mutableListOf<String>()

                for (i in conversions.indices) {
                    queries.forEach { it ->
                        tmpQueries.add(it + conversions[i])
                    }
                }

                queries = tmpQueries
            }
            else {
                val iterator = queries.listIterator()

                while (iterator.hasNext()) {
                    val prevValue = iterator.next()
                    iterator.set(prevValue + character)
                }
            }
        }

        // Check if query is part of traditional phrase and replace with simplified
        val conversionsList = entryDao.getTraditionalToSimplifiedPhrases("$rawQuery")

        if (conversionsList.isNotEmpty()) {
            queries.add(conversionsList[0])
        }

        clearChineseResults()

        queries.forEach {query ->
            val chineseSearchResult = entryDao.searchInDictByChinese(query, query + 'z')
            chineseSearchResultSources.add(chineseSearchResult)
                _chineseSearchResults.addSource(chineseSearchResult) {entries ->
                    _chineseSearchResults.value = chineseSearchResults.value?.union(entries)?.toList()
                    _chineseSearchResults.postValue(_chineseSearchResults.value)
            }
        }

        val chineseSearchResult = entryDao.searchInDictByTraditional(rawQuery, rawQuery + 'z')
            chineseSearchResultSources.add(chineseSearchResult)
                _chineseSearchResults.addSource(chineseSearchResult) {entries ->
                    _chineseSearchResults.value = chineseSearchResults.value?.union(entries)?.toList()
                    _chineseSearchResults.postValue(_chineseSearchResults.value)
            }

    }

    fun clearChineseResults() {
        _chineseSearchResults.value = listOf()
        chineseSearchResultSources.forEach { _chineseSearchResults.removeSource(it) }
        chineseSearchResultSources.clear()
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
