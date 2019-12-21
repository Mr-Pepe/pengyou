package com.mrpepe.pengyou.dictionary

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : EntryRepository
    val searchResults = MediatorLiveData<List<Entry>>()
    var oldResults : LiveData<List<Entry>>


    init {
        val entryDao = CEDict.getDatabase(application).entryDao()
        repository = EntryRepository(entryDao)
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

