package com.mrpepe.pengyou.dictionary.search

import android.content.SharedPreferences
import android.os.Handler
import androidx.lifecycle.*
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.SearchHistory
import com.mrpepe.pengyou.dictionary.AppDatabase
import com.mrpepe.pengyou.dictionary.Entry
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DictionarySearchViewModel : ViewModel() {

    private val repository : DictionarySearchRepository
    var searchHistoryIDs = listOf<String>()
    var searchHistoryEntries = MutableLiveData<List<Entry>>()

    private val _englishSearchResults = MediatorLiveData<List<Entry>>()
    val englishResults : LiveData<List<Entry>> = _englishSearchResults

    private val _chineseSearchResults = MediatorLiveData<List<Entry>>()
    val chineseSearchResults : LiveData<List<Entry>> = _chineseSearchResults

    var requestedLanguage = SearchLanguage.CHINESE
    var displayedLanguage = MutableLiveData<SearchLanguage>()

    var searchQuery = MutableLiveData("")
    private var blankQuery = true
    var newSearchLive = MutableLiveData<Boolean>()
    var newSearch = false

    private val delayedHandler = Handler()
    private val switchDelay : Long = 300

    private var chineseSearchJob : Job? = null
    private var englishSearchJob : Job? = null

    private var listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "search_history") {
                    searchHistoryIDs = SearchHistory.getHistoryIds()
                    reloadSearchHistory()
                }
            }

    init {
        val entryDao = AppDatabase.getDatabase(MainApplication.getContext()).entryDao()
        repository = DictionarySearchRepository(entryDao)

        _englishSearchResults.addSource(repository.englishSearchResults) { entries ->
            _englishSearchResults.value =
                    (entries ?: listOf())
                        .sortedWith(compareBy({ it.hsk }, { it.priority }))

            if (requestedLanguage == SearchLanguage.ENGLISH) {
                if (entries?.size ?: 0 > 0 || blankQuery) {
                    displayedLanguage.postValue(SearchLanguage.ENGLISH)
                }
                else {
                    delayedHandler.removeCallbacksAndMessages(null)
                    delayedHandler.postDelayed(
                        {
                            if (englishResults.value?.size ?: 0 == 0) {
                                if (chineseSearchResults.value?.size ?: 0 > 0) {
                                    displayedLanguage.postValue(SearchLanguage.CHINESE)
                                }
                                else {
                                    displayedLanguage.postValue(displayedLanguage.value)
                                }
                            }
                            else {
                                displayedLanguage.postValue(SearchLanguage.ENGLISH)
                            }
                        }, switchDelay)
                }
            }

        }

        _chineseSearchResults.addSource(repository.chineseSearchResults) { entries ->
            _chineseSearchResults.value =
                    (entries ?: listOf())
                        .sortedWith(
                            compareBy(
                                { it.wordLength },
                                { it.hsk },
                                { it.pinyinLength },
                                { it.priority }
                            )
                        )

            if (requestedLanguage == SearchLanguage.CHINESE) {
                if (entries?.size ?: 0 > 0 || blankQuery) {
                    displayedLanguage.postValue(SearchLanguage.CHINESE)
                }
                else {
                    delayedHandler.removeCallbacksAndMessages(null)
                    delayedHandler.postDelayed(
                        {
                            if (chineseSearchResults.value?.size ?: 0 == 0) {
                                if (englishResults.value?.size ?: 0 > 0) {
                                    displayedLanguage.postValue(SearchLanguage.ENGLISH)
                                }
                                else {
                                    displayedLanguage.postValue(displayedLanguage.value)
                                }
                            }
                            else {
                                displayedLanguage.postValue(SearchLanguage.CHINESE)
                            }
                        }, switchDelay)
                }
            }
        }

        requestedLanguage = SearchLanguage.CHINESE
        displayedLanguage.value = SearchLanguage.CHINESE

        SearchHistory.searchPreferences.registerOnSharedPreferenceChangeListener(listener)

        searchHistoryIDs = SearchHistory.getHistoryIds()

        viewModelScope.launch {
            repository.getSearchHistory(searchHistoryIDs.reversed())
            searchHistoryEntries.postValue(repository.searchHistory)
        }
    }

    fun search(query : String) {
        chineseSearchJob?.cancel()
        englishSearchJob?.cancel()
        searchQuery.postValue(query)
        blankQuery = query.isBlank()
        newSearch = true
        newSearchLive.value = true

        if (query.isNotBlank()) {
            chineseSearchJob = viewModelScope.launch { repository.searchForChinese(query) }
            englishSearchJob = viewModelScope.launch { repository.searchForEnglish(query) }
        }
        else {
            repository.clearEnglishResults()
            repository.clearChineseResults()
            displayedLanguage.postValue(requestedLanguage)
        }
    }

    private fun reloadSearchHistory() {
        viewModelScope.launch {
            repository.getSearchHistory(searchHistoryIDs.reversed())
            searchHistoryEntries.postValue(repository.searchHistory)
        }
    }

    fun requestLanguage(language : SearchLanguage) {
        delayedHandler.removeCallbacksAndMessages(null)
        requestedLanguage = language
        displayedLanguage.postValue(language)
    }

    enum class SearchLanguage {
        ENGLISH,
        CHINESE
    }
}
