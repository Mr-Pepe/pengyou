package com.mrpepe.pengyou.dictionary.wordView

import android.app.Application
import androidx.lifecycle.*
import com.mrpepe.pengyou.MainApplication
import com.mrpepe.pengyou.dictionary.CEDict
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO
import com.mrpepe.pengyou.dictionary.StrokeOrder
import kotlinx.coroutines.launch

class WordViewViewModel() : ViewModel() {
    var isInitialized = false
    private lateinit var repository : WordViewRepository
    private var entryDao : EntryDAO = CEDict.getDatabase(MainApplication.getContext()).entryDao()

    var decompositions : MutableLiveData<List<Decomposition>> = MutableLiveData()
    var strokeOrders : MutableLiveData<List<StrokeOrder>> = MutableLiveData()
    val entry = MediatorLiveData<Entry>()
    val wordsContaining = MediatorLiveData<List<Entry>>()

    fun init(initEntry: Entry) {
        repository = WordViewRepository(entryDao, initEntry)
        entry.addSource(repository.entry) {value -> entry.value = value}
        wordsContaining.addSource(repository.wordsContaining) {value -> wordsContaining.value = value}

        viewModelScope.launch {
            repository.decompose(initEntry.simplified)
            decompositions.postValue(repository.decompositions)
        }
        viewModelScope.launch {
            repository.getStrokeOrder(initEntry.simplified)
            strokeOrders.postValue(repository.strokeOrders)
        }

        isInitialized = true
    }
}
