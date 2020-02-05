package com.mrpepe.pengyou.dictionary.searchView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrpepe.pengyou.UpdateSearchResultsMode
import com.mrpepe.pengyou.dictionary.CEDict
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO

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
