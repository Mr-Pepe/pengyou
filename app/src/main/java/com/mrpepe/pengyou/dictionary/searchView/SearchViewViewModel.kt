package com.mrpepe.pengyou.dictionary.searchView

import android.app.Application
import androidx.lifecycle.*
import com.mrpepe.pengyou.dictionary.CEDict
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.coroutines.launch

class SearchViewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository : SearchViewRepository
    val searchResults = MediatorLiveData<List<Entry>>()
    private var oldResults : LiveData<List<Entry>>
    var searchLanguage = MutableLiveData<SearchLanguage>()
    var searchMode = MutableLiveData<SearchMode>()


    init {
        val entryDao = CEDict.getDatabase(application).entryDao()
        repository = SearchViewRepository(entryDao)
        oldResults = repository.searchResults
        searchResults.addSource(repository.searchResults) {value -> searchResults.value = value}
        searchMode.value = SearchMode.DICT
        searchLanguage.value = SearchLanguage.CHINESE
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

