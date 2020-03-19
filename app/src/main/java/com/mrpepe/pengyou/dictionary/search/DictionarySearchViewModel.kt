package com.mrpepe.pengyou.dictionary.search

import android.content.SharedPreferences
import androidx.lifecycle.*
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.SearchHistory
import com.mrpepe.pengyou.dictionary.AppDatabase
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.coroutines.launch

class DictionarySearchViewModel() : ViewModel() {

    private val repository : DictionarySearchRepository
    var searchHistoryIDs = listOf<String>()
    var searchHistoryEntries = MutableLiveData<List<Entry>>()

    private var oldEnglishResults1 : LiveData<List<Entry>>
    private var oldEnglishResults2 : LiveData<List<Entry>>
    private var oldEnglishResults3 : LiveData<List<Entry>>
    val englishSearchResults = MediatorLiveData<List<Entry>>()
    val chineseSearchResults = MediatorLiveData<List<Entry>>()

    var requestedLanguage = MutableLiveData<SearchLanguage>()
    var displayedLanguage = MutableLiveData<SearchLanguage>()

    var searchQuery = ""
    var newSearchLive = MutableLiveData<Boolean>()
    var newSearch = false

    private var listener = object : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            if (key == "search_history") {
                searchHistoryIDs = SearchHistory.getHistoryIds()
                reloadSearchHistory()
            }
        }
    }

    init {
        val entryDao = AppDatabase.getDatabase(MainApplication.getContext()).entryDao()
        repository = DictionarySearchRepository(entryDao)
        oldEnglishResults1 = repository.englishSearchResults1
        oldEnglishResults2 = repository.englishSearchResults1
        oldEnglishResults3 = repository.englishSearchResults1

        englishSearchResults.addSource(repository.englishSearchResults1) { }
        chineseSearchResults.addSource(repository.chineseSearchResults) { value -> chineseSearchResults.postValue(value)}
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
            viewModelScope.launch {
                searchForChinese(searchQuery
                            .replace("ü", "u:")
                            .replace("v", "u:")
                            .trimEnd().toLowerCase())
            }
            viewModelScope.launch {
                searchForEnglish(searchQuery.trimEnd().toLowerCase())
            }
        } else {
            englishSearchResults.value = listOf()
            repository.clearChineseResults()
        }
    }

    fun reloadSearchHistory() {
        viewModelScope.launch {
            repository.getSearchHistory(searchHistoryIDs.reversed())
            searchHistoryEntries.postValue(repository.searchHistory)
        }
    }

    private fun searchForChinese(query: String) = viewModelScope.launch {
        repository.searchForChinese(query)
    }

    private fun searchForEnglish(query: String) = viewModelScope.launch {
        repository.searchForEnglish(query)

        oldEnglishResults1 = repository.englishSearchResults1
        oldEnglishResults2 = repository.englishSearchResults2
        oldEnglishResults3 = repository.englishSearchResults3
        englishSearchResults.removeSource(oldEnglishResults1)
        englishSearchResults.removeSource(oldEnglishResults2)
        englishSearchResults.removeSource(oldEnglishResults3)
        englishSearchResults.addSource(repository.englishSearchResults1) {  }
        englishSearchResults.addSource(repository.englishSearchResults2) {  }
        englishSearchResults.addSource(repository.englishSearchResults3) { _ -> englishSearchResults.value = mergeEnglishResults() }
    }

    fun toggleDisplayedLanguage(){
        when (requestedLanguage.value) {
            SearchLanguage.CHINESE -> requestedLanguage.value = SearchLanguage.ENGLISH
            SearchLanguage.ENGLISH -> requestedLanguage.value = SearchLanguage.CHINESE
        }
    }

    enum class SearchLanguage {
        ENGLISH,
        CHINESE
    }

    private fun mergeEnglishResults(): List<Entry> {
        var output = setOf<Entry>()

        repository.englishSearchResults1.value?.let { output = output.union(repository.englishSearchResults1.value!!) }
        repository.englishSearchResults2.value?.let { output = output.union(repository.englishSearchResults2.value!!) }
        repository.englishSearchResults3.value?.let { output = output.union(repository.englishSearchResults3.value!!) }

        return output.toList()
    }
}


