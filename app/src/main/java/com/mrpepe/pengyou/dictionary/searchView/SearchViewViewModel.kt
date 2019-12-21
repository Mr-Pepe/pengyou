package com.mrpepe.pengyou.dictionary.searchView

import android.app.Application
import androidx.lifecycle.*
import com.mrpepe.pengyou.dictionary.CEDict
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.coroutines.launch

class SearchViewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : SearchViewRepository
    val searchResults = MediatorLiveData<List<Entry>>()
    var oldResults : LiveData<List<Entry>>


    init {
        val entryDao = CEDict.getDatabase(application).entryDao()
        repository = SearchViewRepository(entryDao)
        oldResults = repository.searchResults
        searchResults.addSource(repository.searchResults) {value -> searchResults.value = value}
    }

    fun searchFor(query: String) = viewModelScope.launch {
        repository.searchFor(query)
        searchResults.removeSource(oldResults)
        oldResults = repository.searchResults
        searchResults.addSource(repository.searchResults) {value -> searchResults.postValue(value)}
    }
}

