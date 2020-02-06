package com.mrpepe.pengyou.dictionary.searchView

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrpepe.pengyou.UpdateSearchResultsMode
import com.mrpepe.pengyou.dictionary.Entry

class SearchViewFragmentViewModel : ViewModel() {
    var searchResults = MutableLiveData<List<Entry>>()
    var searchHistory = MutableLiveData<List<Entry>>()
    var newHistoryEntry = MutableLiveData<String>()

    var searchQuery = ""

    var updateSearchResults = MutableLiveData<UpdateSearchResultsMode>()

    init {
        searchResults.value = listOf()
        searchHistory.value = listOf()
    }
}
