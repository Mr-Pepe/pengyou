package com.mrpepe.pengyou.dictionary.wordView

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrpepe.pengyou.dictionary.Entry

class WordViewFragmentViewModel : ViewModel() {
    var wordsContaining = MutableLiveData<List<Entry>>()
    var entry = MutableLiveData<Entry>()
    var decompositions = MutableLiveData<List<Decomposition>>()
    var strokeOrders = MutableLiveData<List<String>>()

}
