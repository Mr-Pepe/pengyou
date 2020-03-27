package com.mrpepe.pengyou.dictionary.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO
import java.util.*

class DictionarySearchRepository(private val entryDao: EntryDAO) {

    private val _chineseSearchResults = MediatorLiveData<List<Entry>>()
    var chineseSearchResults: LiveData<List<Entry>> = _chineseSearchResults
    private val chineseSearchResultSources = mutableListOf<LiveData<List<Entry>>>()

    private val _englishSearchResults = MediatorLiveData<List<Entry>>()
    var englishSearchResults: LiveData<List<Entry>> = _englishSearchResults
    private val englishSearchResultSources = mutableListOf<LiveData<List<Entry>>>()

    var searchHistory = mutableListOf<Entry>()

    init {
        val chineseSearchResult = entryDao.searchInDictByChinese("123456", "123456z")
        chineseSearchResultSources.add(chineseSearchResult)
        _chineseSearchResults.addSource(chineseSearchResult) { _chineseSearchResults.postValue(chineseSearchResults.value?.plus(it))}

        val englishSearchResult = entryDao.searchInDictByEnglish1("123456")
        englishSearchResultSources.add(chineseSearchResult)
        _englishSearchResults.addSource(englishSearchResult) { _englishSearchResults.postValue(englishSearchResults.value?.plus(it))}
    }

    suspend fun searchForChinese(rawQuery: String) {

        val cleanedQuery = rawQuery
            .replace("ü", "u:")
            .replace("v", "u:")
            .replace("\\s".toRegex(), "")
            .trimEnd().toLowerCase(Locale.ROOT)

        val queries = getSimplifiedQueries(cleanedQuery)

        clearChineseResults()

        // Search for the queries converted to simplified characters
        queries.forEach {query ->
            addResultToChineseResults(entryDao.searchInDictByChinese(query, query + 'z'))
        }

        // Just in case also search for a match in the traditional headword
        addResultToChineseResults(entryDao.searchInDictByTraditional(cleanedQuery, cleanedQuery + 'z'))
    }

    private fun addResultToChineseResults(result: LiveData<List<Entry>>) {
        chineseSearchResultSources.add(result)

        _chineseSearchResults.addSource(result) {entries ->
            _chineseSearchResults.value = chineseSearchResults.value?.union(entries)?.toList()
            _chineseSearchResults.postValue(_chineseSearchResults.value)
        }
    }

    private fun addResultToEnglishResults(result: LiveData<List<Entry>>) {
        englishSearchResultSources.add(result)

        _englishSearchResults.addSource(result) {entries ->
            _englishSearchResults.value = englishSearchResults.value?.union(entries)?.toList()
            _englishSearchResults.postValue(_englishSearchResults.value)
        }
    }

    fun clearChineseResults() {
        _chineseSearchResults.value = listOf()
        chineseSearchResultSources.forEach { _chineseSearchResults.removeSource(it) }
        chineseSearchResultSources.clear()
    }

    fun clearEnglishResults() {
        _englishSearchResults.value = listOf()
        englishSearchResultSources.forEach { _englishSearchResults.removeSource(it) }
        englishSearchResultSources.clear()
    }

    fun searchForEnglish(rawQuery: String) {
        val cleanedQuery = rawQuery.trimEnd().toLowerCase(Locale.ROOT)

        clearEnglishResults()

        addResultToEnglishResults(entryDao.searchInDictByEnglish1(cleanedQuery))
        addResultToEnglishResults(entryDao.searchInDictByEnglish3("_${cleanedQuery}_", "_${cleanedQuery}", "${cleanedQuery}_"))
        addResultToEnglishResults(entryDao.searchInDictByEnglish1("%${cleanedQuery}%"))
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

    private suspend fun getSimplifiedQueries(rawQuery: String): MutableList<String> {
        // Check if characters in the query can be converted to simplified characters
        val queries = getTraditionalToSimplifiedCharacters(rawQuery)

        // Check if query is part of traditional phrase and replace with simplified
        val conversionsList = getTraditionalToSimplifiedPhrases(rawQuery)

        if (conversionsList.isNotEmpty()) {
            queries.add(conversionsList[0])
        }

        return queries
    }

    private suspend fun getTraditionalToSimplifiedCharacters(rawQuery: String): MutableList<String> {
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
                    queries.forEach {
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

        return queries
    }

    private suspend fun getTraditionalToSimplifiedPhrases(rawQuery: String): List<String> {
        return entryDao.getTraditionalToSimplifiedPhrases(rawQuery)
    }
}
