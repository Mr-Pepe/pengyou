package com.mrpepe.pengyou.dictionary.wordView

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrpepe.pengyou.dictionary.CEDict
import com.mrpepe.pengyou.dictionary.Entry
import com.mrpepe.pengyou.dictionary.EntryDAO

class WordViewFragmentViewModel : ViewModel() {
    var wordsContaining = MutableLiveData<List<Entry>>()
    var entry = MutableLiveData<Entry>()
    var decompositions = MutableLiveData<List<Decomposition>>()

}
