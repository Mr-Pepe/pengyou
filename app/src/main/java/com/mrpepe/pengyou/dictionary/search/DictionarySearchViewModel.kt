package com.mrpepe.pengyou.dictionary.search

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.SearchHistory
import com.mrpepe.pengyou.dictionary.AppDatabase
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.coroutines.launch

class DictionarySearchViewModel : ViewModel() {

    private val repository : DictionarySearchRepository
    var searchHistoryIDs = listOf<String>()
    var searchHistoryEntries = MutableLiveData<List<Entry>>()

    private val mergedEnglishResults = MediatorLiveData<List<Entry>>()
    val englishResults : LiveData<List<Entry>> = mergedEnglishResults
    private val preciseEnglishResults : LiveData<List<Entry>>
    private var generalEnglishResults : LiveData<List<Entry>>

    private val _chineseSearchResults = MediatorLiveData<List<Entry>>()
    val chineseSearchResults : LiveData<List<Entry>> = _chineseSearchResults

    // Used for the SearchResultFragment to notify the DictionarySearchFragment to update the
    // result counts
    val updateResultCounts = MutableLiveData<Boolean>()

    var requestedLanguage = MutableLiveData<SearchLanguage>()
    var displayedLanguage = MutableLiveData<SearchLanguage>()

    var searchQuery = ""
    var newSearchLive = MutableLiveData<Boolean>()
    var newSearch = false

    private var listener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "search_history") {
                searchHistoryIDs = SearchHistory.getHistoryIds()
                reloadSearchHistory()
            }
        }

    init {
        val entryDao = AppDatabase.getDatabase(MainApplication.getContext()).entryDao()
        repository = DictionarySearchRepository(entryDao)

        preciseEnglishResults = repository.preciseEnglishSearchResults
        generalEnglishResults = repository.generalEnglishResults
        mergedEnglishResults.addSource(preciseEnglishResults) { entries ->
            mergedEnglishResults.value = (entries ?: listOf()).union(mergedEnglishResults.value ?: listOf())?.toList()

            mergedEnglishResults.postValue(mergedEnglishResults.value)
        }

        _chineseSearchResults.addSource(repository.chineseSearchResults) {entries ->
            _chineseSearchResults.value =
                (entries ?: listOf())
                    .sortedWith(
                        compareBy({
                            it.wordLength},
                            {it.hsk},
                            {it.pinyinLength},
                            {it.priority}
                        )
                    )

            _chineseSearchResults.postValue(_chineseSearchResults.value)
        }

        requestedLanguage.value = SearchLanguage.CHINESE
        displayedLanguage.value = SearchLanguage.CHINESE

        SearchHistory.searchPreferences.registerOnSharedPreferenceChangeListener(listener)

        searchHistoryIDs = SearchHistory.getHistoryIds()

        viewModelScope.launch {
            repository.getSearchHistory(searchHistoryIDs.reversed())
            searchHistoryEntries.postValue(repository.searchHistory)
        }
    }

    fun search() {
        newSearch = true
        newSearchLive.value = true

        if (searchQuery.isNotBlank()) {
            viewModelScope.launch { repository.searchForChinese(searchQuery) }
            viewModelScope.launch {

                repository.searchForEnglish(searchQuery)

                mergedEnglishResults.removeSource(generalEnglishResults)
                generalEnglishResults = repository.generalEnglishResults

                mergedEnglishResults.addSource(generalEnglishResults) { entries ->
                    mergedEnglishResults.value = (preciseEnglishResults.value ?: listOf())
                        .union(entries.sortedWith( compareBy({ it.hsk }, { it.wordLength })))
                        .toList()

                    mergedEnglishResults.postValue(mergedEnglishResults.value)
                }
            }

        } else {
            repository.clearEnglishResults()
            repository.clearChineseResults()
        }
    }

    private fun reloadSearchHistory() {
        viewModelScope.launch {
            repository.getSearchHistory(searchHistoryIDs.reversed())
            searchHistoryEntries.postValue(repository.searchHistory)
        }
    }

    fun toggleDisplayedLanguage(){
        when (requestedLanguage.value) {
            SearchLanguage.CHINESE -> requestedLanguage.value = SearchLanguage.ENGLISH
            SearchLanguage.ENGLISH -> requestedLanguage.value = SearchLanguage.CHINESE
            else -> {}
        }
    }

    enum class SearchLanguage {
        ENGLISH,
        CHINESE,
        NONE
    }
}
