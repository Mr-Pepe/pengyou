package com.mrpepe.pengyou.dictionary.wordView

import androidx.lifecycle.*
import com.mrpepe.pengyou.ChineseMode
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.dictionary.AppDatabase
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO
import com.mrpepe.pengyou.dictionary.StrokeOrder
import kotlinx.coroutines.launch

class WordViewViewModel : ViewModel() {
    private var isInitialized = false
    private lateinit var repository : WordViewRepository
    private var entryDao : EntryDAO = AppDatabase.getDatabase(MainApplication.getContext()).entryDao()

    var decompositions : MutableLiveData<List<Decomposition>> = MutableLiveData()
    var strokeOrders : MutableLiveData<List<StrokeOrder>> = MutableLiveData()
    val entry = MediatorLiveData<Entry>()
    val wordsContaining = MediatorLiveData<List<Entry>>()

    fun init(initEntry: Entry) {
        repository = WordViewRepository(entryDao, initEntry)
        entry.addSource(repository.entry) {value -> entry.value = value}
        wordsContaining.addSource(repository.wordsContaining) {value -> wordsContaining.value = value}

        val word = when (MainApplication.chineseMode) {
            ChineseMode.simplified -> initEntry.simplified
            ChineseMode.simplifiedTraditional -> initEntry.simplified
            ChineseMode.traditional -> initEntry.traditional
            ChineseMode.traditionalSimplified -> initEntry.traditional
            else -> initEntry.simplified
        }

        viewModelScope.launch {
            repository.decompose(word)
            decompositions.postValue(repository.decompositions)
        }
        viewModelScope.launch {
            repository.getStrokeOrder(word)
            strokeOrders.postValue(repository.strokeOrders)
        }

        isInitialized = true
    }
}
