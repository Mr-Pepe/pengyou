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

    val englishSearchResults : LiveData<List<Entry>>
    var chineseSearchResults : LiveData<List<Entry>>

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

        englishSearchResults = repository.englishSearchResults
        chineseSearchResults = repository.chineseSearchResults
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
            viewModelScope.launch { repository.searchForEnglish(searchQuery) }

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
