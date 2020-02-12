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
    private var oldEnglishResults4 : LiveData<List<Entry>>
    private var oldEnglishResults5 : LiveData<List<Entry>>
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
        oldEnglishResults4 = repository.englishSearchResults1
        oldEnglishResults5 = repository.englishSearchResults1

        oldChineseResults = repository.chineseSearchResults
        englishSearchResults.addSource(repository.englishSearchResults1) { }
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

        oldEnglishResults1 = repository.englishSearchResults1
        oldEnglishResults2 = repository.englishSearchResults2
        oldEnglishResults3 = repository.englishSearchResults3
        oldEnglishResults4 = repository.englishSearchResults4
        oldEnglishResults5 = repository.englishSearchResults5
        englishSearchResults.removeSource(oldEnglishResults1)
        englishSearchResults.removeSource(oldEnglishResults2)
        englishSearchResults.removeSource(oldEnglishResults3)
        englishSearchResults.removeSource(oldEnglishResults4)
        englishSearchResults.removeSource(oldEnglishResults5)
        englishSearchResults.addSource(repository.englishSearchResults1) { value -> englishSearchResults.value = mergeEnglishResults() }
        englishSearchResults.addSource(repository.englishSearchResults2) { value -> englishSearchResults.value = mergeEnglishResults() }
        englishSearchResults.addSource(repository.englishSearchResults3) { value -> englishSearchResults.value = mergeEnglishResults() }
        englishSearchResults.addSource(repository.englishSearchResults4) { value -> englishSearchResults.value = mergeEnglishResults() }
        englishSearchResults.addSource(repository.englishSearchResults5) { value -> englishSearchResults.value = mergeEnglishResults() }

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

    private fun mergeEnglishResults(): List<Entry> {
        var output = listOf<Entry>()

        repository.englishSearchResults1.value?.let { output += repository.englishSearchResults1.value!! }
        repository.englishSearchResults2.value?.let { output += repository.englishSearchResults2.value!! }
        repository.englishSearchResults3.value?.let { output += repository.englishSearchResults3.value!! }
        repository.englishSearchResults4.value?.let { output += repository.englishSearchResults4.value!! }
        repository.englishSearchResults5.value?.let { output += repository.englishSearchResults5.value!! }

        return output
    }
}


