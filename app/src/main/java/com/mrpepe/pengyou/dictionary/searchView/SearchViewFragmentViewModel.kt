package com.mrpepe.pengyou.dictionary.searchView

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrpepe.pengyou.UpdateSearchResultsMode
import com.mrpepe.pengyou.dictionary.Entry

class SearchViewFragmentViewModel : ViewModel() {
    var englishSearchResults = MutableLiveData<List<Entry>>()
    var chineseSearchResults = MutableLiveData<List<Entry>>()
    var searchHistory = MutableLiveData<List<Entry>>()
    var displayedLanguage = MutableLiveData<SearchViewViewModel.SearchLanguage>()
    var requestedLanguage = SearchViewViewModel.SearchLanguage.CHINESE

    var searchQuery = ""

    var updateSearchResults = MutableLiveData<UpdateSearchResultsMode>()
    var addCharacterToQuery = MutableLiveData<String>()
    var deleteLastCharacterOfQuery = MutableLiveData<Boolean>()
    var submitFromDrawBoard = MutableLiveData<Boolean>()

    init {
        englishSearchResults.value = listOf()
        searchHistory.value = listOf()
        displayedLanguage.value = SearchViewViewModel.SearchLanguage.CHINESE
    }
}
