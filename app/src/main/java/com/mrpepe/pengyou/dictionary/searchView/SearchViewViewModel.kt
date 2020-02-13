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
    private var searchPreferences = application.getSharedPreferences(MainApplication.getContext().getString(R.string.search_history), Context.MODE_PRIVATE)

    private var oldEnglishResults1 : LiveData<List<Entry>>
    private var oldEnglishResults2 : LiveData<List<Entry>>
    private var oldEnglishResults3 : LiveData<List<Entry>>
    val englishSearchResults = MediatorLiveData<List<Entry>>()
    private var oldChineseResults : LiveData<List<Entry>>
    val chineseSearchResults = MediatorLiveData<List<Entry>>()

    var requestedLanguage = MutableLiveData<SearchLanguage>()

    var searchHistory = MutableLiveData<List<Entry>>()
    var searchHistoryIDs = mutableListOf<String>()

    init {
        val entryDao = CEDict.getDatabase(application).entryDao()
        repository = SearchViewRepository(entryDao)
        oldEnglishResults1 = repository.englishSearchResults1
        oldEnglishResults2 = repository.englishSearchResults1
        oldEnglishResults3 = repository.englishSearchResults1

        oldChineseResults = repository.chineseSearchResults
        englishSearchResults.addSource(repository.englishSearchResults1) { }
        chineseSearchResults.addSource(repository.chineseSearchResults) { }
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

    fun searchForChinese(query: String) = viewModelScope.launch {
        repository.searchForChinese(query)

        chineseSearchResults.removeSource(oldChineseResults)
        oldChineseResults = repository.chineseSearchResults
        chineseSearchResults.addSource(repository.chineseSearchResults) { value -> chineseSearchResults.postValue(value)}
    }

    fun searchForEnglish(query: String) = viewModelScope.launch {
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


