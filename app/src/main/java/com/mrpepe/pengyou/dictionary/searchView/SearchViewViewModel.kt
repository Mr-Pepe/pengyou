package com.mrpepe.pengyou.dictionary.searchView

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.R
import com.mrpepe.pengyou.dictionary.CEDict
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.coroutines.launch

class SearchViewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : SearchViewRepository
    val englishSearchResults = MediatorLiveData<List<Entry>>()
    val chineseSearchResults = MediatorLiveData<List<Entry>>()
    private var oldEnglishResults : LiveData<List<Entry>>
    private var oldChineseResults : LiveData<List<Entry>>
    var requestedLanguage = MutableLiveData<SearchLanguage>()
    var searchPreferences = application.getSharedPreferences(MainApplication.getContext().getString(R.string.search_history), Context.MODE_PRIVATE)
    var searchHistory = MutableLiveData<List<Entry>>()
    var searchHistoryIDs = mutableListOf<String>()

    init {
        val entryDao = CEDict.getDatabase(application).entryDao()
        repository = SearchViewRepository(entryDao)
        oldEnglishResults = repository.englishSearchResults
        oldChineseResults = repository.chineseSearchResults
        englishSearchResults.addSource(repository.englishSearchResults) { value -> englishSearchResults.value = value}
        chineseSearchResults.addSource(repository.chineseSearchResults) { value -> chineseSearchResults.value = value}
        requestedLanguage.value = SearchLanguage.CHINESE

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
        repository.searchFor(query)

        englishSearchResults.removeSource(oldEnglishResults)
        oldEnglishResults = repository.englishSearchResults
        englishSearchResults.addSource(repository.englishSearchResults) { value -> englishSearchResults.postValue(value)}

        chineseSearchResults.removeSource(oldChineseResults)
        oldChineseResults = repository.chineseSearchResults
        chineseSearchResults.addSource(repository.chineseSearchResults) { value -> chineseSearchResults.postValue(value)}
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
}

