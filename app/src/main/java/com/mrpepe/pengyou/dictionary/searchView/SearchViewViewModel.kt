package com.mrpepe.pengyou.dictionary.searchView

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.CEDict
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.coroutines.launch

class SearchViewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : SearchViewRepository
    val searchResults = MediatorLiveData<List<Entry>>()
    private var oldResults : LiveData<List<Entry>>
    var searchLanguage = MutableLiveData<SearchLanguage>()
    var searchMode = MutableLiveData<SearchMode>()
    var searchPreferences = application.getSharedPreferences(MainApplication.getContext().getString(R.string.search_history), Context.MODE_PRIVATE)
    var searchHistory = MutableLiveData<List<Entry>>()
    var searchHistoryIDs = mutableListOf<String>()

    init {
        val entryDao = CEDict.getDatabase(application).entryDao()
        repository = SearchViewRepository(entryDao)
        oldResults = repository.searchResults
        searchResults.addSource(repository.searchResults) {value -> searchResults.value = value}
        searchMode.value = SearchMode.DICT
        searchLanguage.value = SearchLanguage.CHINESE

        searchHistoryIDs = searchPreferences.getString("search_history", "")!!.split(',').toMutableList()
        viewModelScope.launch {
            repository.getSearchHistory(searchHistoryIDs.reversed())
            searchHistory.postValue(repository.searchHistory)
        }
    }

    fun addToSearchHistory(id: String) {
        val maxLengthHistory = 10000

        if (searchHistoryIDs.size >= maxLengthHistory) {
            searchHistoryIDs = searchHistoryIDs.slice(1 until maxLengthHistory).toMutableList()
        }

        searchHistoryIDs.add(id)

        viewModelScope.launch {
            with(searchPreferences.edit()) {
                putString("search_history", searchHistoryIDs.joinToString(","))
                commit()
            }
        }

        viewModelScope.launch {
            repository.getSearchHistory(searchHistoryIDs.reversed())
        }
    }

    fun searchFor(query: String) = viewModelScope.launch {
        repository.searchFor(query, getSearchMode(searchLanguage.value!!, searchMode.value!!))
        searchResults.removeSource(oldResults)
        oldResults = repository.searchResults
        searchResults.addSource(repository.searchResults) {value -> searchResults.postValue(value)}
    }

    fun toggleLanguage(){
        when (searchLanguage.value) {
            SearchLanguage.CHINESE -> searchLanguage.value = SearchLanguage.ENGLISH
            SearchLanguage.ENGLISH -> searchLanguage.value = SearchLanguage.CHINESE
        }
    }

    enum class SearchLanguage {
        ENGLISH,
        CHINESE
    }

    // A search for example sentences can be added by adding another mode like "SENTENCES"
    enum class SearchMode {
        DICT
    }

    private fun getSearchMode(lang: SearchLanguage, mode: SearchMode): SearchViewRepository.SearchMode {
        return when (lang) {
            SearchLanguage.ENGLISH -> {
                when (mode) {
                    SearchMode.DICT -> SearchViewRepository.SearchMode.ENGLISHDICT
                }
            }
            SearchLanguage.CHINESE -> {
                when (mode) {
                    SearchMode.DICT -> SearchViewRepository.SearchMode.CHINESEDICT
                }
            }
        }
    }
}

